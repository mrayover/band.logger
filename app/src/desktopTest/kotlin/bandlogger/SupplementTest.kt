package bandlogger

import java.nio.file.*
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import java.util.Base64
import kotlin.test.*

class SupplementTest {
    private data class Fixture(val root: Path, val source: Path, val project: Project)
    private fun fixture(): Fixture {
        val root=Files.createTempDirectory("supplements-test")
        val source=Files.createDirectory(root.resolve("charts")); val destination=Files.createDirectory(root.resolve("exports"))
        Files.write(source.resolve("Q Alpha -r00- C.pdf"), syntheticPdf())
        val catalog=Catalog(source=source.toString()).reconcile(LocalCharts.scan(source.toString()),"Q",1,::newId).acceptNew()
        var p=Project("p","Band","Q",members=listOf(Member("c","Concert",emptyList(),listOf("C")),Member("bb","Sax",emptyList(),listOf("Bb","C"))),catalog=catalog,exportDestination=destination.toString())
        p=p.recordMaster(MasterExporter.generate(p,MasterPlanner.preview(p)))
        return Fixture(root,source,p)
    }
    private fun scan(f: Fixture, p: Project=f.project) = p.copy(catalog=p.catalog.reconcile(LocalCharts.scan(f.source.toString()),"Q",2,::newId).acceptNew())
    private fun preview(p: Project)=SupplementPlanner.preview(p,PendingSources.check(p).issues)
    private fun newTune(f: Fixture): Project { Files.write(f.source.resolve("Q Beta -r00- C.pdf"),syntheticPdf()+"\n% Beta".toByteArray()); return scan(f) }

    @Test fun newTunesPublishPersistAndBecomeUnchanged() {
        val f=fixture(); val p=newTune(f); val before=LocalCharts.scan(f.source.toString())
        val plan=preview(p); assertEquals(2,plan.selectedCount)
        val record=SupplementExporter.generate(p,plan,journalDirectory=f.root)
        assertEquals(1,record.number); assertEquals(2,record.publication.members.count { it.zipName!=null })
        val path=f.root.resolve("workspace.json"); val updated=p.recordSupplement(record)
        FileWorkspaceStore(path).save(Workspace().saveProject(updated))
        val reopened=FileWorkspaceStore(path).load().projects.single()
        assertEquals(updated,reopened); assertEquals(0,preview(reopened).selectedCount)
        assertEquals(p.masters,reopened.masters); assertEquals(before,LocalCharts.scan(f.source.toString()))
        assertEquals(record,SupplementExporter.recover(reopened,record.publication.folder))
        assertEquals(reopened,reopened.recordSupplement(record))
    }
    @Test fun newPartsAndRevisionsAreIndependentPerMemberAndPart() {
        val f=fixture()
        Files.write(f.source.resolve("Q Alpha -r02- Bb.pdf"),syntheticPdf()+"\n% Bb".toByteArray())
        var p=scan(f); var plan=preview(p)
        assertEquals(1,plan.selectedCount); assertEquals("bb",plan.packages.members.single { it.selectedCount==1 }.member.id)
        p=p.recordSupplement(SupplementExporter.generate(p,plan,journalDirectory=f.root))
        assertEquals(0,preview(p).selectedCount)
        Files.write(f.source.resolve("Q Alpha -r01- C.pdf"),syntheticPdf()+"\n% C revision".toByteArray())
        p=scan(f,p); plan=preview(p)
        assertEquals(1,plan.selectedCount); assertEquals("c",plan.packages.members.single { it.selectedCount==1 }.member.id)
        p=p.recordSupplement(SupplementExporter.generate(p,plan,journalDirectory=f.root))
        assertEquals(listOf(1,2),p.supplements.map { it.number }); assertEquals(0,preview(p).selectedCount)
    }
    @Test fun changingBackToAnAlreadyDeliveredPartDoesNotReexportIt() {
        val f=fixture(); Files.write(f.source.resolve("Q Alpha -r01- Bb.pdf"),syntheticPdf()+"\n% Bb".toByteArray())
        var p=scan(f); p=p.recordSupplement(SupplementExporter.generate(p,preview(p),journalDirectory=f.root))
        p=p.copy(members=p.members.map { if(it.id=="bb") it.copy(preferences=listOf("C")) else it })
        assertEquals(0,preview(p).selectedCount)
    }
    @Test fun unchangedAndNoInitialMasterAreNeverPublished() {
        val f=fixture(); assertEquals(0,preview(f.project).selectedCount)
        assertFailsWith<IllegalArgumentException> { SupplementExporter.generate(f.project,preview(f.project),journalDirectory=f.root) }
        val p=newTune(f).copy(masters=emptyList())
        assertEquals(0,preview(p).selectedCount); assertTrue(preview(p).warnings.any { it.contains("initial Library Master") })
    }
    @Test fun unresolvedAndMissingFilesAreBlockedWithoutDowngrade() {
        val f=fixture(); Files.write(f.source.resolve("Q Alpha -r01- C.pdf"),syntheticPdf()+"\n% revision".toByteArray())
        var p=f.project.copy(catalog=f.project.catalog.reconcile(LocalCharts.scan(f.source.toString()),"Q",2,::newId))
        assertEquals(0,preview(p).selectedCount)
        p=p.copy(catalog=p.catalog.acceptNew()); val stale=preview(p)
        Files.delete(f.source.resolve("Q Alpha -r01- C.pdf"))
        assertEquals(0,preview(p).selectedCount)
        assertFailsWith<IllegalArgumentException> { SupplementExporter.generate(p,stale,journalDirectory=f.root) }
        assertTrue(p.supplements.isEmpty())
    }
    @Test fun duplicatePublicationAndUnrecordedOutputCannotAdvanceNumber() {
        val f=fixture(); val p=newTune(f); val plan=preview(p); val record=SupplementExporter.generate(p,plan,journalDirectory=f.root)
        val bytes=Files.readAllBytes(Path.of(record.publication.folder).resolve("supplement.json"))
        assertFailsWith<IllegalArgumentException> { SupplementExporter.generate(p,plan,journalDirectory=f.root) }
        assertEquals(1,preview(p).number); assertTrue(p.supplements.isEmpty())
        assertContentEquals(bytes,Files.readAllBytes(Path.of(record.publication.folder).resolve("supplement.json")))
        assertEquals(0,preview(p.recordSupplement(SupplementExporter.recover(p,record.publication.folder))).selectedCount)
    }
    @Test fun failedWorkspaceSaveLeavesRecoverablePackagesAndUnadvancedHistory() {
        val f=fixture(); val p=newTune(f)
        val error=assertFailsWith<SupplementRecordFailure> { SupplementExporter.publish(p,preview(p),f.root) { error("Disk unavailable") } }
        assertTrue(p.supplements.isEmpty()); assertEquals(1,preview(p).number)
        val recovered=SupplementExporter.recover(p,error.supplement.publication.folder)
        assertEquals(1,p.recordSupplement(recovered).supplements.size)
    }
    @Test fun recoveryRejectsChangedPackageAndOtherProject() {
        val f=fixture(); val p=newTune(f); val record=SupplementExporter.generate(p,preview(p),journalDirectory=f.root)
        assertFailsWith<IllegalArgumentException> { SupplementExporter.recover(p.copy(id="other"),record.publication.folder) }
        val member=record.publication.members.first { it.zipName!=null }
        Files.writeString(Path.of(record.publication.folder).resolve(member.zipName!!),"changed")
        assertFailsWith<IllegalArgumentException> { SupplementExporter.recover(p,record.publication.folder) }
        assertTrue(p.supplements.isEmpty())
    }
    @Test fun zipEmbedsOnlyThePendingPdfWithExactBytes() {
        val f=fixture(); val p=newTune(f); val record=SupplementExporter.generate(p,preview(p),journalDirectory=f.root)
        record.publication.members.filter { it.zipName!=null }.forEach { member ->
            ZipFile(Path.of(record.publication.folder).resolve(member.zipName!!).toFile()).use { zip ->
                val entry=zip.entries().asSequence().single { it.name.endsWith(".4ss") }
                val factory=DocumentBuilderFactory.newInstance().apply { setFeature("http://apache.org/xml/features/disallow-doctype-decl",true) }
                val doc=zip.getInputStream(entry).use { factory.newDocumentBuilder().parse(it) }
                val scores=doc.getElementsByTagName("score"); assertEquals(1,scores.length)
                val score=scores.item(0) as org.w3c.dom.Element
                assertContentEquals(Files.readAllBytes(f.source.resolve("Q Beta -r00- C.pdf")),Base64.getDecoder().decode(score.getAttribute("data")))
            }
        }
    }
    @Test fun historyAndNumberingAreProjectIsolated() {
        val f=fixture(); val p=newTune(f); val record=SupplementExporter.generate(p,preview(p),journalDirectory=f.root)
        val other=f.project.copy(id="other",masters=f.project.masters.map { it.copy(projectId="other") })
        assertFailsWith<IllegalArgumentException> { other.recordSupplement(record) }
        assertFailsWith<IllegalArgumentException> { SupplementExporter.folder(other,1) }
        val separate=Files.createDirectory(f.root.resolve("other-exports"))
        assertNotEquals(SupplementExporter.folder(p,1),SupplementExporter.folder(other.copy(exportDestination=separate.toString()),1))
        val state=Workspace().saveProject(p.recordSupplement(record)).saveProject(other)
        val loaded=WorkspaceCodec.decode(WorkspaceCodec.encode(state))
        assertTrue(loaded.projects.last().supplements.isEmpty()); assertEquals(1,loaded.projects.first().supplements.size)
    }
    @Test fun schemaFiveMigrationKeepsEventsCatalogMastersAndImmutableBackup() {
        val f=fixture(); val path=f.root.resolve("workspace.json")
        val current=WorkspaceCodec.encode(Workspace().saveProject(f.project))
        val old=current.replace("\"schemaVersion\": 8","\"schemaVersion\": 5").replace("\"supplements\": []","\"oldField\": []")
        Files.writeString(path,old); val store=FileWorkspaceStore(path); val loaded=store.load()
        assertEquals(f.project,loaded.projects.single()); assertEquals(old,Files.readString(path))
        store.save(loaded); store.save(loaded)
        assertEquals(old,Files.readString(path.resolveSibling("workspace.pre-supplements.backup.json")))
    }
    @Test fun eventExportResolvesRecordedSupplement() {
        val f=fixture(); var p=newTune(f); val tune=p.catalog.tunes.single { it.title=="Beta" }
        val event=newEvent(p,EventType.GIG,::newId).copy(title="Show",date="2026-10-21").addTune(p,tune.id,::newId)
        p=p.saveEvent(event); p=p.recordSupplement(SupplementExporter.generate(p,preview(p),journalDirectory=f.root))
        assertEquals(0,preview(p).selectedCount)
        assertTrue(EventExportPlanner.preview(p,event.id).members.all { it.matchedCount==1 && it.entries.single().distribution?.supplementNumber==1 })
    }
    @Test fun durableIntentBlocksDuplicateEvenAfterChangingDestinationOrProjectName() {
        val f=fixture(); val p=newTune(f); val record=SupplementExporter.generate(p,preview(p),journalDirectory=f.root)
        val alternate=Files.createDirectory(f.root.resolve("alternate"))
        val changed=p.copy(name="Renamed",exportDestination=alternate.toString())
        assertFailsWith<IllegalArgumentException> { SupplementExporter.generate(changed,preview(changed),journalDirectory=f.root) }
        assertEquals(record,SupplementExporter.recover(changed,record.publication.folder))
        assertFailsWith<IllegalArgumentException> { MasterExporter.recover(p,record.publication.folder) }
    }}
