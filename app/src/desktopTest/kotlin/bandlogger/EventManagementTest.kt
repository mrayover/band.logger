package bandlogger

import java.nio.file.*
import kotlin.test.*

class EventManagementTest {
    private data class Fixture(val root: Path, val store: FileWorkspaceStore, var project: Project) { fun save(value: Project) { store.save(Workspace().saveProject(value)); project = value } }
    private fun fixture(): Fixture {
        val root = Files.createTempDirectory("event-management-synthetic-")
        val tune = Tune("t", "Synthetic tune", "synthetic")
        val event = BandEvent("event", "project", "Test venue", EventType.GIG, "2026-10-22", EventSet("set", listOf(EventTuneEntry("entry", "t"))))
        val project = Project("project", "Test band", members = listOf(Member("member", "Musician", emptyList(), listOf("C"))), catalog = Catalog(tunes = listOf(tune)), events = listOf(event), exportDestination = root.toString())
        val store = FileWorkspaceStore(root.resolve("workspace.json")); store.save(Workspace().saveProject(project))
        return Fixture(root, store, project)
    }

    private fun Fixture.publish(conflict: EventConflict = EventConflict.NEW): EventExportResult = EventExports.publish(project, EventExportPlanner.preview(project, "event"), root.toString(), true, true, conflict, ::save)
    @Test fun trackedPackagesPersistAcrossReopenAndEventEdits() {
        val f = fixture(); val output = f.publish()
        assertEquals("2026-10-22 Test venue", output.folder.fileName.toString())
        val reopened = FileWorkspaceStore(f.root.resolve("workspace.json")).load().projects.single()
        val record = reopened.eventExports.single(); assertEquals(EventExportState.ACTIVE, record.state)
        assertEquals(output.folder.toString(), record.folder); assertEquals("member", record.packages.single().memberId)
        f.save(reopened.saveEvent(reopened.events.single().copy(title = "New title", date = "2026-11-01")))
        assertEquals(record, f.store.load().projects.single().eventExports.single())
        EventExports.deleteEvent(f.project, "event", f::save)
        assertFalse(Files.exists(output.folder)); assertTrue(f.project.events.isEmpty()); assertEquals(EventExportState.DELETED, f.project.eventExports.single().state)
    }
    @Test fun repeatedExportsRequireExplicitChoiceAndRevisionNamesAreReadable() {
        val f = fixture(); val first = f.publish(); val hash = EventExports.hash(first.folder.resolve(first.zipNames.single()))
        assertFails { f.publish() }
        val second = f.publish(EventConflict.REVISION)
        assertEquals("2026-10-22 Test venue - Revision 2", second.folder.fileName.toString())
        assertEquals(hash, EventExports.hash(first.folder.resolve(first.zipNames.single())))
        assertEquals(2, f.project.eventExports.count { it.state == EventExportState.ACTIVE })
    }
    @Test fun replacementPublishesNewBeforeCleaningAllOldTrackedRevisions() {
        val f = fixture(); val first = f.publish(); val second = f.publish(EventConflict.REVISION)
        val newest = f.publish(EventConflict.REPLACE)
        assertFalse(Files.exists(first.folder)); assertFalse(Files.exists(second.folder)); assertTrue(Files.exists(newest.folder))
        assertEquals(1, f.project.eventExports.count { it.state == EventExportState.ACTIVE })
    }
    @Test fun deletionPreservesChartsMastersSupplementsAndForeignFiles() {
        val f = fixture(); val output = f.publish()
        val source = Files.createDirectories(f.root.resolve("Charts")); Files.writeString(source.resolve("keep.pdf"), "keep")
        val master = Files.createDirectories(f.root.resolve("Library/Master")); Files.writeString(master.resolve("keep.zip"), "keep")
        Files.writeString(output.folder.resolve("unrelated.txt"), "keep")
        EventExports.deleteEvent(f.project, "event", f::save)
        assertTrue(f.project.events.isEmpty()); assertEquals(EventExportState.CLEANUP, f.project.eventExports.single().state)
        assertTrue(Files.exists(output.folder.resolve(output.zipNames.single())))
        assertEquals("keep", Files.readString(source.resolve("keep.pdf"))); assertEquals("keep", Files.readString(master.resolve("keep.zip")))
        assertEquals("keep", Files.readString(output.folder.resolve("unrelated.txt")))
    }
    @Test fun unavailableDestinationKeepsDurableRetryableCleanup() {
        val f = fixture(); val output = f.publish()
        val moved = f.root.resolve("offline-Gigs"); Files.move(output.folder.parent, moved)
        EventExports.deleteEvent(f.project, "event", f::save)
        assertEquals(EventExportState.CLEANUP, f.store.load().projects.single().eventExports.single().state)
        assertNotNull(f.project.eventExports.single().error)
        Files.move(moved, output.folder.parent)
        EventExports.cleanup(f.store.load().projects.single(), f::save)
        assertFalse(Files.exists(output.folder)); assertEquals(EventExportState.DELETED, f.project.eventExports.single().state)
    }
    @Test fun partialDeleteFailureSurvivesRestartAndRetry() {
        val f = fixture(); val first = f.publish(); f.publish(EventConflict.REVISION)
        val pending = f.project.copy(events = emptyList(), eventExports = f.project.eventExports.map { it.copy(state = EventExportState.CLEANUP) })
        f.save(pending); var calls = 0
        EventExports.cleanup(f.project, f::save) { path -> if (++calls == 2) throw java.io.IOException("Synthetic Drive disconnect"); Files.delete(path); Unit }
        assertEquals(EventExportState.CLEANUP, f.project.eventExports.first().state)
        assertFalse(Files.exists(first.folder.resolve(first.zipNames.single())))
        EventExports.cleanup(FileWorkspaceStore(f.root.resolve("workspace.json")).load().projects.single(), f::save)
        assertTrue(f.project.eventExports.all { it.state == EventExportState.DELETED }); assertFalse(Files.exists(first.folder))
    }
    @Test fun failedIntentSavePublishesNothingAndPreservesEvent() {
        val f = fixture()
        assertFails { EventExports.publish(f.project, EventExportPlanner.preview(f.project, "event"), f.root.toString(), true, true, EventConflict.NEW) { throw java.io.IOException("Disk full") } }
        assertFalse(Files.exists(f.root.resolve("Gigs/2026-10-22 Test venue"))); assertEquals(1, f.store.load().projects.single().events.size)
    }
    @Test fun failedActivationSaveRecoversManifestAfterRestart() {
        val f = fixture(); var saves = 0
        assertFails { EventExports.publish(f.project, EventExportPlanner.preview(f.project, "event"), f.root.toString(), true, true, EventConflict.NEW) {
            if (++saves == 2) throw java.io.IOException("Synthetic save failure"); f.save(it)
        } }
        assertEquals(EventExportState.PENDING, f.store.load().projects.single().eventExports.single().state)
        EventExports.recoverPending(f.store.load().projects.single(), f::save)
        assertEquals(EventExportState.ACTIVE, f.project.eventExports.single().state)
        EventExports.deleteEvent(f.project, "event", f::save)
        assertEquals(EventExportState.DELETED, f.project.eventExports.single().state)
    }
    @Test fun failedEventRemovalSaveCannotDeleteAnyPackage() {
        val f = fixture(); val result = f.publish()
        assertFails { EventExports.deleteEvent(f.project, "event") { throw java.io.IOException("No save") } }
        assertTrue(Files.exists(result.folder.resolve(result.zipNames.single()))); assertEquals(1, f.store.load().projects.single().events.size)
    }
    @Test fun tamperedPackagesAndForeignManifestBlockDeletionAndReplacement() {
        val f = fixture(); val output = f.publish()
        Files.writeString(output.folder.resolve(output.zipNames.single()), "tampered")
        assertFails { f.publish(EventConflict.REPLACE) }
        EventExports.deleteEvent(f.project, "event", f::save)
        assertEquals(EventExportState.CLEANUP, f.project.eventExports.single().state)
        assertEquals("tampered", Files.readString(output.folder.resolve(output.zipNames.single())))
    }
    @Test fun legacyFoldersWithoutIdsAreReviewOnlyVerifiedManifestCanBeRecovered() {
        val f = fixture(); val output = f.publish()
        val noHistory = f.project.copy(eventExports = emptyList())
        val legacy = Files.createDirectory(output.folder.parent.resolve("20261021 Old gig")); Files.writeString(legacy.resolve("old.zip"), "keep")
        val discovery = EventExports.discover(noHistory)
        assertEquals(1, discovery.verified.size); assertTrue(discovery.review.any { it.contains("Old gig") })
        assertEquals(output.record.id,discovery.verified.single().id)
        assertEquals("event",discovery.verified.single().event.id)
        assertEquals(output.folder.toRealPath(),Path.of(discovery.verified.single().folder).toRealPath())
        assertEquals(output.record.identity(),EventRecordCodec.decode(Files.readString(output.folder.resolve("event-export.json"))).identity())
        val recordedBytes = Files.readAllBytes(f.root.resolve("workspace.json"))
        val tracked = EventExports.discover(f.project)
        assertTrue(tracked.verified.isEmpty(), "Existing tracked output must not be discovered again through an alias")
        assertContentEquals(recordedBytes,Files.readAllBytes(f.root.resolve("workspace.json")))
        assertEquals("keep", Files.readString(legacy.resolve("old.zip")))
        assertTrue(EventExports.discover(noHistory.copy(id = "another", events = emptyList())).verified.isEmpty())
    }
    @Test fun schemaSevenMigrationPreservesOriginalAndHistoryAcrossRepeatedSaves() {
        val f = fixture(); f.publish()
        val text = WorkspaceCodec.encode(Workspace().saveProject(f.project)).replace("\"schemaVersion\": 11", "\"schemaVersion\": 7")
        val path = f.root.resolve("legacy.json"); Files.writeString(path, text)
        val store = FileWorkspaceStore(path); assertEquals(11, store.load().schemaVersion); store.save(store.load()); store.save(store.load())
        assertEquals(text, Files.readString(f.root.resolve("workspace.pre-event-exports.backup.json")))
        assertEquals(f.project.eventExports, store.load().projects.single().eventExports)
    }
    @Test fun forgedPathCannotDeleteLibraryMaster() {
        val f = fixture(); val result = f.publish(); val record = f.project.eventExports.single()
        val forged = record.copy(folder = f.root.resolve("Library/Master").toString(), state = EventExportState.CLEANUP)
        val current = f.project.copy(eventExports = listOf(forged)); f.save(current)
        EventExports.cleanup(current, f::save)
        assertEquals(EventExportState.CLEANUP, f.project.eventExports.single().state); assertTrue(Files.exists(result.folder))
    }

    @Test fun driveIconMetadataIsBackedUpBeforeVerifiedRemoval() {
        val f = fixture(); val output = f.publish()
        val text = "[.ShellClassInfo]\r\nConfirmFileOp=0\r\nIconResource=C:\\Program Files\\Google\\Drive File Stream\\132.0.0.0\\GoogleDriveFS.exe,27\r\n[ViewState]\r\nMode=\r\nVid=\r\nFolderType=Generic\r\nLogo=\r\n"
        val bytes = text.toByteArray(Charsets.UTF_16LE)
        val ini = Files.write(output.folder.resolve("desktop.ini"), bytes)
        Files.setAttribute(ini, "dos:hidden", true)
        EventExports.deleteEvent(f.project, "event", f::save)
        assertEquals(EventExportState.DELETED, f.project.eventExports.single().state)
        assertFalse(Files.exists(output.folder))
        val stored = f.store.load().projects.single().eventExports.single()
        assertContentEquals(bytes, java.util.Base64.getDecoder().decode(stored.providerMetadata.getValue("desktop.ini")))
    }
    @Test fun unknownDesktopIniRemainsUnrelatedAndBlocksCleanup() {
        val f = fixture(); val output = f.publish()
        val ini = Files.writeString(output.folder.resolve("desktop.ini"), "User customizations must stay")
        Files.setAttribute(ini, "dos:hidden", true)
        EventExports.deleteEvent(f.project, "event", f::save)
        assertEquals(EventExportState.CLEANUP, f.project.eventExports.single().state)
        assertEquals("User customizations must stay", Files.readString(ini))
        assertTrue(Files.exists(output.folder.resolve(output.zipNames.single())))
    }

    @Test fun discoveryRejectsCopiedManifestAtAnUnrelatedFolder() {
        val f = fixture(); val output = f.publish()
        val copied = Files.createDirectory(output.folder.parent.resolve("2026-10-22 Copied manifest"))
        Files.newDirectoryStream(output.folder).use { files -> files.forEach { Files.copy(it,copied.resolve(it.fileName)) } }
        val manifestBefore = Files.readAllBytes(copied.resolve("event-export.json"))
        val packageBefore = Files.readAllBytes(copied.resolve(output.zipNames.single()))
        val result = EventExports.discover(f.project.copy(eventExports = emptyList()))
        assertEquals(listOf(output.record.id),result.verified.map { it.id })
        assertTrue(result.review.any { it.contains("Copied manifest") && it.contains("verification failed") })
        assertContentEquals(manifestBefore,Files.readAllBytes(copied.resolve("event-export.json")))
        assertContentEquals(packageBefore,Files.readAllBytes(copied.resolve(output.zipNames.single())))
    }
}
