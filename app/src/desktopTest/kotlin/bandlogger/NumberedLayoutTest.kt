package bandlogger

import java.nio.file.*
import java.nio.channels.FileChannel
import kotlin.test.*

class NumberedLayoutTest {
    private data class Fixture(val root: Path,val workspace: Path,val project: Project)
    private fun fixture(): Fixture {
        val root=Files.createTempDirectory("numbered-layout").toRealPath()
        val charts=Files.createDirectories(root.resolve("01 - Chart Library"))
        val distribution=Files.createDirectories(root.resolve("02 - Repertoire Distributions"))
        Files.write(charts.resolve("Q Alpha -r00- C.pdf"),syntheticPdf())
        var p=Project("p","Quaternity","Q",members=listOf(Member("m","Player",emptyList(),listOf("C"))),
            catalog=Catalog(source=charts.toString()).reconcile(LocalCharts.scan(charts.toString()),"Q",1,::newId).acceptNew(),exportDestination=distribution.toString(),brandId=Branding.QUATERNITY)
        repeat(2) { p=p.recordMaster(MasterExporter.generate(p,MasterPlanner.preview(p))) }
        repeat(2) { index ->
            Files.write(charts.resolve("Q Added ${index+1} -r00- C.pdf"),syntheticPdf())
            p=p.copy(catalog=p.catalog.reconcile(LocalCharts.scan(charts.toString()),"Q",(index+2).toLong(),::newId).acceptNew())
            val supplement=SupplementExporter.generate(p,SupplementPlanner.preview(p),journalDirectory=root)
            p=p.recordSupplement(supplement);SupplementExporter.clearRecordedIntent(p,root)
        }
        val legacyEvents=Files.createDirectories(root.resolve("03 - Events/Gigs"))
        fun save(next: Project) { p=next }
        repeat(3) { index ->
            var event=newEvent(p,if(index==1) EventType.REHEARSAL else EventType.GIG,::newId)
                .copy(title="Event $index",date="2026-10-22",notes="Private notes $index",isTest=index==2)
                .addTune(p,p.catalog.tunes.first().id,::newId).addTune(p,p.catalog.tunes.first().id,::newId).addSet(::newId)
            event=event.addTune(p,p.catalog.tunes.last().id,::newId,event.sets.last().id)
            if(index==0) event=event.recordPerformance(p,"2026-10-22T18:00:00Z")
            p=p.saveEvent(event)
            EventExports.publish(p,EventExportPlanner.preview(p,event.id),legacyEvents.toString(),true,true,EventConflict.NEW,::save)
        }
        val workspace=root.resolve("workspace.json")
        FileWorkspaceStore(workspace).save(Workspace().saveProject(p).saveProject(Project("other","Other band")).select(p.id))
        return Fixture(root,workspace,p)
    }
    private fun plan(f: Fixture) = DriveLayoutMigration.prepare(f.workspace,FileWorkspaceStore(f.workspace).load(),f.project.id,f.root,f.root.resolve("migration"))
    private fun assertMigrated(f: Fixture) {
        val w=FileWorkspaceStore(f.workspace).load();val p=w.projects.first()
        assertEquals(f.root.toString(),p.projectRoot)
        assertEquals(f.project.catalog,p.catalog)
        assertEquals(f.project.events,p.events)
        assertEquals(Project("other","Other band"),w.projects.last())
        assertEquals(listOf("Master","Revision 2"),p.masters.map { Path.of(it.folder).fileName.toString() })
        p.masters.forEachIndexed { index,m ->
            assertEquals(f.project.masters[index].copy(folder=m.folder),m)
            assertEquals(m,MasterExporter.recover(p,m.folder))
            m.members.forEach { member -> assertEquals(member.zipSha256,EventExports.hash(Path.of(m.folder).resolve(member.zipName!!))) }
        }
        p.supplements.forEachIndexed { index,s ->
            assertEquals(f.project.supplements[index].copy(publication=f.project.supplements[index].publication.copy(folder=s.publication.folder)),s)
            assertEquals(s,SupplementExporter.recover(p,s.publication.folder))
            assertEquals(f.root.resolve("02 - Repertoire Distributions/Supplements/Supplement ${s.number.toString().padStart(3,'0')}"),Path.of(s.publication.folder))
        }
        p.eventExports.forEach { record ->
            val event=p.events.single { it.id==record.event.id }
            val parent=f.root.resolve(if(event.isTest) "06 - Testing" else "03 - Events").resolve(FileLocations.eventTypeFolder(event.type))
            assertEquals(parent,Path.of(record.folder).parent)
            EventExports.verified(record)
            assertEquals(f.project.eventExports.single { it.id==record.id }.packages,record.packages)
        }
        assertEquals(3,SupplementPlanner.preview(p).number)
        val before=DistributionResolver.resolve(f.project).map { it.byTune }
        assertEquals(before,DistributionResolver.resolve(p).map { it.byTune })
        assertFalse(Files.exists(f.root.resolve("02 - Repertoire Distributions/Library")))
    }
    @Test fun migrationMovesAllRevisionsAndPackagesWithoutChangingIdentitiesNotesOrReferences() {
        val f=fixture();val original=Files.readAllBytes(f.workspace);val journal=plan(f)
        DriveLayoutMigration.resume(journal);assertMigrated(f)
        assertContentEquals(original,Files.readAllBytes(journal.parent.resolve("workspace.before.json")))
        assertEquals(FileWorkspaceStore(f.workspace).load(),DriveLayoutMigration.resume(journal))
        assertTrue(Files.exists(journal.parent.resolve("exports/0/manifest.json")))
        val p=FileWorkspaceStore(f.workspace).load().projects.first()
        val next=MasterExporter.generate(p,MasterPlanner.preview(p))
        assertEquals("Revision 3",Path.of(next.folder).fileName.toString())
        assertEquals("Quaternity - Master - Player.zip",next.members.single().zipName)
        var current=p
        EventExports.deleteEvent(current,p.events.first().id) { current=it }
        assertEquals(2,current.events.size)
        assertTrue(current.eventExports.filter { it.event.id==p.events.first().id }.all { it.state==EventExportState.DELETED })
    }
    @Test fun migrationRecoversFromCopySaveAndCleanupInterruptions() {
        for(point in listOf("copied:0","before-save","after-save","cleaned:0")) {
            val f=fixture();val journal=plan(f)
            assertFailsWith<IllegalStateException> { DriveLayoutMigration.resume(journal) { if(it==point) error("Simulated interruption") } }
            DriveLayoutMigration.resume(journal);assertMigrated(f)
        }
    }
    @Test fun unrelatedSourceFilesAndOccupiedTargetsAreNeverMovedOrOverwritten() {
        for(target in listOf(false,true)) {
            val f=fixture();val before=Files.readAllBytes(f.workspace)
            val folder=if(target) Files.createDirectories(f.root.resolve("02 - Repertoire Distributions/Master")) else Path.of(f.project.masters.last().folder)
            val personal=Files.writeString(folder.resolve("personal.txt"),"Keep this")
            assertFailsWith<IllegalArgumentException> { plan(f) }
            assertEquals("Keep this",Files.readString(personal));assertContentEquals(before,Files.readAllBytes(f.workspace))
            f.project.masters.forEach { assertTrue(Files.exists(Path.of(it.folder).resolve("manifest.json"))) }
        }
    }
    @Test fun changedSourcesOrWorkspaceStopRetryBeforeDeletingOriginals() {
        for(changeWorkspace in listOf(false,true)) {
            val f=fixture();val journal=plan(f)
            assertFails { DriveLayoutMigration.resume(journal) { if(it=="before-save") error("Stop") } }
            if(changeWorkspace) {
                val store=FileWorkspaceStore(f.workspace);val old=store.load();store.save(old.saveProject(old.projects.first().copy(name="New user edit")))
            } else {
                val master=f.project.masters.last();Files.writeString(Path.of(master.folder).resolve(master.members.single().zipName!!),"Changed user bytes")
            }
            assertFailsWith<IllegalArgumentException> { DriveLayoutMigration.resume(journal) }
            f.project.masters.forEach { assertTrue(Files.exists(Path.of(it.folder).resolve("manifest.json"))) }
        }
    }
    @Test fun migrationRejectsChangedTargetAndLockedWorkspace() {
        val f=fixture();val journal=plan(f)
        FileChannel.open(f.workspace.resolveSibling("workspace.lock"),StandardOpenOption.WRITE).use { channel ->
            channel.lock().use { assertFails { DriveLayoutMigration.resume(journal) } }
        }
        assertFails { DriveLayoutMigration.resume(journal) { if(it=="before-save") error("Stop") } }
        Files.writeString(f.root.resolve("02 - Repertoire Distributions/Master/manifest.json"),"Changed")
        assertFails { DriveLayoutMigration.resume(journal) }
        assertTrue(Files.exists(Path.of(f.project.masters.first().folder)))
    }
    @Test fun configuredRootRoutesNewExportsAndKeepsOtherProjectsAndChartSourcesIndependent() {
        val f=fixture();val journal=plan(f);DriveLayoutMigration.resume(journal)
        var p=FileWorkspaceStore(f.workspace).load().projects.first()
        assertEquals(f.root.resolve("00 - Inbox"),FileLocations.inbox(p))
        assertEquals(f.root.resolve("02 - Repertoire Distributions/Master").toString(),FileLocations.masterDestination(p))
        assertEquals(f.project.catalog.source,p.catalog.source)
        assertNull(FileLocations.eventsInitialRoot(Project("other","Other band"),f.root))
        assertFails { FileLocations.numberedRoot(f.root.resolve("03 - Events").toString()) }
        for(type in EventType.entries) {
            val event=newEvent(p,type,::newId).copy(title="Future ${type.name}",date="2026-10-23",isTest=type==EventType.REHEARSAL)
                .addTune(p,p.catalog.tunes.first().id,::newId)
            p=p.saveEvent(event)
            val plan=EventExportPlanner.preview(p,event.id)
            val output=EventExporter.generate(p,plan,p.projectRoot!!,true,true)
            assertEquals(FileLocations.eventDestination(p,plan,p.projectRoot!!),output.folder)
        }
        assertFalse(Files.exists(f.root.resolve("Library")))
        assertFalse(Files.exists(f.root.resolve("Gigs")))
        assertFalse(Files.exists(f.root.resolve("Supplements")))
    }
    @Test fun driveIconDecorationIsBackedUpButNotTreatedAsRequiredPackagePayload() {
        val f=fixture()
        val text="[.ShellClassInfo]\r\nConfirmFileOp=0\r\nIconResource=C:\\Program Files\\Google\\Drive File Stream\\132.0.0.0\\GoogleDriveFS.exe,27\r\n[ViewState]\r\nMode=\r\nVid=\r\nFolderType=Generic\r\nLogo=\r\n"
        val bytes=("\uFEFF"+text).toByteArray(Charsets.UTF_16LE)
        val ini=Files.write(Path.of(f.project.masters.first().folder).resolve("desktop.ini"),bytes)
        Files.setAttribute(ini,"dos:hidden",true)
        val journal=plan(f)
        DriveLayoutMigration.resume(journal);assertMigrated(f)
        assertContentEquals(bytes,Files.readAllBytes(journal.parent.resolve("exports/0/desktop.ini")))
        assertFalse(Files.exists(f.root.resolve("02 - Repertoire Distributions/Master/desktop.ini")))
    }
    @Test fun schemaTenMigrationPreservesHistoryAndCreatesAnImmutableNumberedLayoutBackup() {
        val f=fixture();val text=Files.readString(f.workspace).replace("\"schemaVersion\": 11","\"schemaVersion\": 10")
        Files.writeString(f.workspace,text)
        val store=FileWorkspaceStore(f.workspace);val loaded=store.load()
        assertEquals(11,loaded.schemaVersion);assertNull(loaded.projects.first().projectRoot)
        assertEquals(f.project.events,loaded.projects.first().events)
        assertEquals(text,Files.readString(f.workspace))
        store.save(loaded);store.save(store.load())
        assertEquals(text,Files.readString(f.workspace.resolveSibling("workspace.pre-numbered-layout.backup.json")))
    }
}
