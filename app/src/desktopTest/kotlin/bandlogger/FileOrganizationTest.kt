package bandlogger

import java.nio.file.*
import java.security.MessageDigest
import java.time.Instant
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.*

class FileOrganizationTest {
    private fun hash(text: String) = MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
    private data class Fixture(val root: Path, val source: Path, val project: Project)
    private fun fixture(name: String = "Quaternity", members: List<Member> = listOf(Member("m","Marcus",emptyList(),listOf("C")),Member("j","Jordan",emptyList(),listOf("C")))): Fixture {
        val root=Files.createTempDirectory("organization-test"); val source=Files.createDirectory(root.resolve("charts"))
        val out=Files.createDirectory(root.resolve("distributions"))
        Files.write(source.resolve("Q First -r00- C.pdf"),syntheticPdf())
        val catalog=Catalog(source=source.toString()).reconcile(LocalCharts.scan(source.toString()),"Q",1,::newId).acceptNew()
        var p=Project("p",name,"Q",members=members,catalog=catalog,exportDestination=out.toString())
        p=p.recordMaster(MasterExporter.generate(p,MasterPlanner.preview(p)))
        Files.write(source.resolve("Q Second -r00- C.pdf"),syntheticPdf())
        p=p.copy(catalog=p.catalog.reconcile(LocalCharts.scan(source.toString()),"Q",2,::newId).acceptNew())
        return Fixture(root,source,p)
    }
    private fun publish(f: Fixture,p: Project=f.project) = SupplementExporter.generate(p,SupplementPlanner.preview(p),journalDirectory=f.root)
    private fun withEvent(p: Project,type: EventType,date: String?="2026-10-22"): Project {
        val event=newEvent(p,type,::newId).copy(title="Band night",date=date).addTune(p,p.catalog.tunes.first().id,::newId)
        return p.saveEvent(event)
    }
    // Create a genuine old-format package with the unchanged shared master writer in isolated data.
    private fun legacy(f: Fixture): LibrarySupplement {
        val p=f.project; val plan=SupplementPlanner.preview(p)
        val target=Path.of(p.exportDestination!!).resolve("Supplements/Project-${hash(p.id).take(12)}/Supplement 001")
        Files.createDirectories(target.parent)
        var record: LibrarySupplement?=null
        MasterExporter.generateSelected(p,plan.packages,"legacy",Instant.parse("2026-10-21T00:00:00Z"),target,"Library Supplement 001",companionManifest={ manifest ->
            val fingerprint=hash(manifest.members.filter { it.charts.isNotEmpty() }.sortedBy { it.memberId }.joinToString("\n") { m ->
                m.memberId+":"+m.charts.sortedBy { it.tuneId }.joinToString(";") { "${it.tuneId}:${it.chartId}:${it.part}:${it.revision}:${it.sha256}" }
            })
            LibrarySupplement(1,manifest,plan.baselines,selectionHash=fingerprint).also { record=it }.let(SupplementCodec::encode)
        })
        return record!!
    }
    @Test fun pdfChooserStartsAtStagingAndMissingFolderFallsBack() {
        val root=Files.createTempDirectory("staging-test")
        javax.swing.SwingUtilities.invokeAndWait {
            assertEquals(root.toRealPath().toFile(),pdfChooser(root).currentDirectory.canonicalFile)
            val usual=javax.swing.JFileChooser().currentDirectory.canonicalFile
            assertEquals(usual,pdfChooser(root.resolve("unavailable")).currentDirectory.canonicalFile)
        }
        assertNull(FileLocations.pdfInitialDirectory(root.resolve("missing")))
        val file=Files.writeString(root.resolve("not-directory"),"keep")
        assertNull(FileLocations.pdfInitialDirectory(file)); assertEquals("keep",Files.readString(file))
    }
    @Test fun importCopiesToAuthoritativeLibraryAndLeavesStagingUntouched() {
        val root=Files.createTempDirectory("import-location"); val stage=Files.createDirectory(root.resolve("stage")); val source=stage.resolve("incoming.pdf")
        val library=Files.createDirectory(root.resolve("library")); Files.write(source,syntheticPdf())
        val accepted=LocalCharts.importPdf(source,library.toString(),"Q Tune -r00- C.pdf")
        assertEquals(library.toRealPath(),accepted.parent)
        assertEquals("Q Tune -r00- C.pdf",accepted.fileName.toString())
        assertContentEquals(syntheticPdf(),Files.readAllBytes(accepted))
        assertContentEquals(syntheticPdf(),Files.readAllBytes(source))
        assertFailsWith<IllegalArgumentException> { LocalCharts.importPdf(source,library.toString(),"Q Tune -r00- C.pdf") }
    }
    @Test fun readableSupplementNamesEmbedIdenticalPdfBytesAndOnlyAffectedMembers() {
        val f=fixture(); val p=f.project.copy(members=f.project.members.map { if(it.id=="j") it.copy(preferences=listOf("Bb")) else it })
        val record=publish(f,p)
        assertEquals(Path.of(p.exportDestination!!).toRealPath().resolve("Library/Supplements/Supplement 001"),Path.of(record.publication.folder))
        val allocation=record.publication.members.single { it.zipName!=null }
        assertEquals("Quaternity - Supplement 001 - Marcus.zip",allocation.zipName)
        assertEquals(1,record.publication.members.count { it.zipName!=null })
        ZipFile(Path.of(record.publication.folder).resolve(allocation.zipName!!).toFile()).use { zip ->
            val entry=zip.getEntry("Quaternity - Supplement 001 - Marcus.4ss"); assertNotNull(entry)
            val doc=zip.getInputStream(entry).use { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it) }
            assertEquals("Quaternity - Supplement 001 - Marcus",doc.documentElement.getAttribute("title"))
            val score=doc.getElementsByTagName("score").item(0) as org.w3c.dom.Element
            assertContentEquals(syntheticPdf(),java.util.Base64.getDecoder().decode(score.getAttribute("data")))
            assertEquals("Q Second -r00- C.pdf",score.getAttribute("path"))
        }
    }
    @Test fun identicalAndSanitizedProjectNamesRemainIsolatedAcrossSessions() {
        val root=Files.createTempDirectory("same-project-name")
        val one=Project("one","Same: Band",exportDestination=root.toString())
        val two=one.copy(id="two",name="Same? Band")
        val first=SupplementExporter.folder(one,1)
        assertEquals("Supplements",first.parent.fileName.toString())
        assertFailsWith<IllegalArgumentException> { SupplementExporter.folder(two,1) }
        val separate=Files.createDirectory(root.resolve("Other band"))
        val second=SupplementExporter.folder(two.copy(exportDestination=separate.toString()),1)
        assertNotEquals(first,second)
        assertEquals(first,SupplementExporter.folder(one,2).parent.resolve("Supplement 001"))
        assertEquals("one",Files.readString(first.parent.parent.resolve(".bandlogger-project-id")))
    }
    @Test fun duplicateMemberNamesGainSuffixOnlyWhenNeeded() {
        val f=fixture(members=listOf(Member("a","Same",emptyList(),listOf("C")),Member("b","same",emptyList(),listOf("C"))))
        val names=publish(f).publication.members.mapNotNull { it.zipName }
        assertEquals(listOf("Quaternity - Supplement 001 - Same.zip","Quaternity - Supplement 001 - same - 2.zip"),names)
    }
    @Test fun unsafeReservedNamesAreSanitizedAndUnownedFoldersArePreserved() {
        val root=Files.createTempDirectory("folder-name")
        val p=Project("p","CON",exportDestination=root.toString())
        assertEquals(root.toRealPath().resolve("Library/Supplements/Supplement 001"),SupplementExporter.folder(p,1))
        val occupied=Files.createDirectories(root.resolve("Supplements/Busy")); Files.writeString(occupied.resolve("owner.txt"),"preserve")
        val other=SupplementExporter.folder(p.copy(name="Busy"),1)
        assertNotEquals(occupied,other.parent); assertEquals("preserve",Files.readString(occupied.resolve("owner.txt")))
        assertFalse(Files.exists(occupied.resolve(".bandlogger-project-id")))
    }
    @Test fun legacyReceiptRecoveryAndNextNumberUseNewFolderWithoutMovingHistory() {
        val f=fixture(); val old=legacy(f); val oldPath=Path.of(old.publication.folder)
        val original=Files.readAllBytes(oldPath.resolve("supplement.json"))
        val zipName=old.publication.members.first().zipName!!; val zip=Files.readAllBytes(oldPath.resolve(zipName))
        var p=f.project.recordSupplement(old)
        assertEquals(old,SupplementExporter.recover(p,old.publication.folder))
        Files.write(f.source.resolve("Q Third -r00- C.pdf"),syntheticPdf())
        p=p.copy(catalog=p.catalog.reconcile(LocalCharts.scan(f.source.toString()),"Q",3,::newId).acceptNew())
        val second=publish(f,p)
        assertEquals(2,second.number); assertEquals(old.id,second.previousId)
        assertTrue(second.publication.folder.endsWith("Library\\Supplements\\Supplement 002"))
        assertTrue(second.publication.members.all { it.zipName!!.contains(" - Supplement 002 - ") })
        val state=WorkspaceCodec.decode(WorkspaceCodec.encode(Workspace().saveProject(p.recordSupplement(second))))
        assertEquals(listOf(old,second),state.projects.single().supplements)
        assertContentEquals(original,Files.readAllBytes(oldPath.resolve("supplement.json")))
        assertContentEquals(zip,Files.readAllBytes(oldPath.resolve(zipName)))
        assertEquals(old,SupplementExporter.recover(p,old.publication.folder))
        assertFailsWith<IllegalArgumentException> { publish(f,p) }
    }
    @Test fun unrecordedLegacyOutputBlocksDuplicateUnderReadableConvention() {
        val f=fixture(); val old=legacy(f)
        val failure=assertFailsWith<IllegalArgumentException> { publish(f) }
        assertTrue(failure.message!!.contains("Recover")); assertTrue(Files.exists(Path.of(old.publication.folder)))
        assertTrue(f.project.supplements.isEmpty())
    }
    @Test fun renamedProjectRetainsOriginalReceiptsAndPendingJournalProtection() {
        val f=fixture(); val old=publish(f)
        val p=f.project.copy(name="Changed name")
        assertFailsWith<IllegalArgumentException> { publish(f,p) }
        assertEquals(old,SupplementExporter.recover(p,old.publication.folder))
        assertTrue(Files.exists(Path.of(old.publication.folder)))
    }
    @Test fun gigAndRehearsalRoutesUseSavedTypeWithoutLibraryDestination() {
        val f=fixture(); val root=Files.createDirectory(f.root.resolve("events"))
        EventType.entries.forEach { type ->
            val p=withEvent(f.project,type).copy(eventsExportRoot=root.toString())
            val plan=EventExportPlanner.preview(p,p.events.single().id)
            val output=EventExporter.generate(p,plan,p.eventsExportRoot!!)
            assertEquals(root.toRealPath().resolve(FileLocations.eventTypeFolder(type)),output.folder.parent)
            assertEquals("2026-10-22 Band night",output.folder.fileName.toString())
            output.zipNames.forEach { name -> ZipFile(output.folder.resolve(name).toFile()).use { zip ->
                assertEquals(2,zip.size()); val entry=zip.entries().asSequence().single { it.name.endsWith(".4ss") }
                val xml=zip.getInputStream(entry).use { it.readBytes().decodeToString() }
                EventExporter.validateXml(xml); assertFalse(xml.contains("data=")); assertFalse(zip.entries().asSequence().any { it.name.endsWith(".pdf") })
            } }
        }
        assertFalse(Files.exists(Path.of(f.project.exportDestination!!).resolve("Gigs")))
    }
    @Test fun unavailableConfiguredEventsRootNeverFallsBackToOtherDestinations() {
        val f=fixture(); val missing=f.root.resolve("unavailable")
        val p=withEvent(f.project,EventType.GIG).copy(exportDestination=missing.toString(),eventsExportRoot=missing.toString())
        assertEquals(missing.toString(),FileLocations.eventsInitialRoot(p,f.root))
        assertFails { EventExporter.generate(p,EventExportPlanner.preview(p,p.events.single().id),missing.toString()) }
        assertFailsWith<IllegalArgumentException> { EventExporter.eventsRoot("") }
        assertFalse(Files.exists(missing)); assertFalse(Files.exists(Path.of(p.exportDestination!!).resolve("Gigs")))
        assertNull(FileLocations.eventsInitialRoot(p.copy(exportDestination=null,eventsExportRoot=null),missing))
        assertNull(FileLocations.eventsInitialRoot(p.copy(exportDestination=null,eventsExportRoot=null,brandId=null),f.root))
        assertEquals(f.root.toRealPath().toString(),FileLocations.eventsInitialRoot(p.copy(exportDestination=null,eventsExportRoot=null,brandId=Branding.QUATERNITY),f.root))
    }
    @Test fun eventsRootSettingPersistsAndSchemaSixBackupRetainsAllHistory() {
        val f=fixture(); val record=publish(f)
        val p=withEvent(f.project.recordSupplement(record),EventType.REHEARSAL)
        val old=WorkspaceCodec.encode(Workspace().saveProject(p)).replace("\"schemaVersion\": 11","\"schemaVersion\": 6").replace("\"eventsExportRoot\": null","\"oldField\": null")
        val path=f.root.resolve("workspace.json"); Files.writeString(path,old)
        Files.writeString(f.root.resolve("workspace.pre-supplements.backup.json"),"previous backup")
        val store=FileWorkspaceStore(path); val loaded=store.load(); assertEquals(11,loaded.schemaVersion)
        assertEquals(p.copy(brandId="quaternity"),loaded.projects.single()); assertEquals(old,Files.readString(path))
        val configured=loaded.projects.single().copy(eventsExportRoot=f.root.toString()); store.save(loaded.saveProject(configured)); store.save(store.load())
        assertEquals(configured,store.load().projects.single())
        assertEquals(old,Files.readString(f.root.resolve("workspace.pre-event-destinations.backup.json")))
        assertEquals("previous backup",Files.readString(f.root.resolve("workspace.pre-supplements.backup.json")))
    }
    @Test fun undatedRepeatExportsKeepMemberNamingAndPreviousBytes() {
        val f=fixture(); val p=withEvent(f.project,EventType.REHEARSAL,null)
        val plan=EventExportPlanner.preview(p,p.events.single().id)
        assertFailsWith<IllegalArgumentException> { EventExporter.generate(p,plan,f.root.toString()) }
        val first=EventExporter.generate(p,plan,f.root.toString(),false,true,id="first")
        val before=Files.readAllBytes(first.folder.resolve(first.zipNames.first()))
        val second=EventExporter.generate(p,plan,f.root.toString(),false,true,id="second",revision=true)
        assertEquals("Rehearsals",second.folder.parent.fileName.toString())
        assertTrue(second.folder.fileName.toString().startsWith("Undated Band night - Revision "))
        assertTrue(second.zipNames.first().startsWith("Undated Band night (Marcus - C)"))
        assertContentEquals(before,Files.readAllBytes(first.folder.resolve(first.zipNames.first())))
    }
}
