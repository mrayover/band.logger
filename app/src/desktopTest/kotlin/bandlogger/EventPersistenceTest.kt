package bandlogger

import java.nio.file.Files
import kotlin.test.*

class EventPersistenceTest {
    private var sequence = 0
    private fun id() = "id-${++sequence}"
    private val project = Project("p", "Band", catalog = Catalog(tunes = listOf(Tune("t", "Tune", "tune"))))
    private fun event() = newEvent(project, EventType.REHEARSAL, ::id).copy(title = "Practice").addTune(project, "t", ::id)

    @Test fun createRestartReopenEditRestartPreservesEveryIdentityAndOtherData() {
        val path = Files.createTempDirectory("events-restart").resolve("workspace.json")
        val initial = Workspace().saveProject(project).saveProject(Project("other", "Other band")).select("p")
        val value = event()
        val saved = EventDraft(value).saveTo(initial, FileWorkspaceStore(path))
        assertFalse(saved.draft.isDirty)
        val reopened = FileWorkspaceStore(path).load()
        assertEquals(value, reopened.projects.first().events.single())
        assertTrue(reopened.projects.last().events.isEmpty())
        val edited = value.copy(title = "Show", type = EventType.GIG, date = "2026-12-01")
        EventDraft(edited, value).saveTo(reopened, FileWorkspaceStore(path))
        val again = FileWorkspaceStore(path).load()
        assertEquals(edited, again.projects.first().events.single())
        assertEquals(value.initialSet, again.projects.first().events.single().initialSet)
        assertEquals(project.catalog, again.projects.first().catalog)
        assertEquals(reopened.projects.last(), again.projects.last())
        assertEquals(reopened, WorkspaceCodec.decode(Files.readString(path.resolveSibling("workspace.backup.json"))))
    }
    @Test fun allLegacySchemasMigrateWithoutLosingDataAndKeepImmutableOriginals() {
        for (version in 1..3) {
            val path = Files.createTempDirectory("events-migrate-$version").resolve("workspace.json")
            val current = WorkspaceCodec.encode(Workspace().saveProject(project))
            val legacy = current.replace("\"schemaVersion\": 11", "\"schemaVersion\": $version").replace("\"events\": []", "\"legacyFutureField\": []")
            Files.writeString(path, legacy)
            val existingBackup = path.resolveSibling("workspace.chunk1.backup.json")
            Files.writeString(existingBackup, "previous immutable backup")
            val store = FileWorkspaceStore(path)
            val loaded = store.load()
            assertEquals(11, loaded.schemaVersion)
            assertEquals(project, loaded.projects.single())
            assertEquals(legacy, Files.readString(path)) // load alone never rewrites
            EventDraft(event()).saveTo(loaded, store)
            assertEquals(legacy, Files.readString(path.resolveSibling("workspace.pre-events.backup.json")))
            store.save(store.load())
            assertEquals(legacy, Files.readString(path.resolveSibling("workspace.pre-events.backup.json")))
            assertEquals("previous immutable backup", Files.readString(existingBackup))
        }
    }
    @Test fun failedStorageWriteRetainsDirtyDraftAndSavedWorkspace() {
        val initial = Workspace().saveProject(project)
        val draft = EventDraft(event())
        val broken = object : WorkspaceStore {
            override fun load() = initial
            override fun save(value: Workspace) { error("Disk unavailable") }
        }
        assertFailsWith<IllegalStateException> { draft.saveTo(initial, broken) }
        assertTrue(draft.isDirty)
        assertEquals(initial, broken.load())
        assertTrue(broken.load().projects.single().events.isEmpty())
    }
    @Test fun switchingProjectsCannotSaveAStaleDraftIntoAnotherWorkspace() {
        val initial = Workspace().saveProject(project).saveProject(Project("other", "Other"))
        var wrote = false
        val store = object : WorkspaceStore {
            override fun load() = initial
            override fun save(value: Workspace) { wrote = true }
        }
        assertFailsWith<IllegalArgumentException> { EventDraft(event()).saveTo(initial, store) }
        assertFalse(wrote)
    }
    @Test fun invalidSaveDoesNotTouchExistingWorkspaceOrBackups() {
        val path = Files.createTempDirectory("events-invalid").resolve("workspace.json")
        val store = FileWorkspaceStore(path)
        val initial = Workspace().saveProject(project)
        store.save(initial)
        val bytes = Files.readAllBytes(path)
        assertFailsWith<IllegalArgumentException> { EventDraft(event().copy(date = "2026-02-30")).saveTo(initial, store) }
        assertContentEquals(bytes, Files.readAllBytes(path))
        assertFalse(Files.exists(path.resolveSibling("workspace.backup.json")))
    }
}
