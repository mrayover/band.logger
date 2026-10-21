package bandlogger

import java.nio.file.Files
import kotlin.test.*

class MultipleSetsPersistenceTest {
    private var next = 0
    private fun id() = "entry-${++next}"
    private val project = Project("p", "Band", catalog = Catalog(tunes = listOf(Tune("a", "Alpha", "alpha"), Tune("b", "Beta", "beta"))))
    private fun event() = newEvent(project, EventType.REHEARSAL, ::id).copy(title = "Practice", date = "2026-10-21")

    @Test fun saveRestartEditReorderAndRemovePreserveAllOtherData() {
        val path = Files.createTempDirectory("multiple-sets").resolve("workspace.json")
        val workspace = Workspace().saveProject(project).saveProject(Project("other", "Other")).select(project.id)
        var value = event().addSet(::id).addSet(::id)
        value = value.addTune(project, "a", ::id).addTune(project, "b", ::id).addTune(project, "a", ::id, value.sets[1].id).addTune(project, "a", ::id, value.sets[1].id)
        EventDraft(value).saveTo(workspace, FileWorkspaceStore(path))
        val reopened = FileWorkspaceStore(path).load()
        assertEquals(value, reopened.projects.first().events.single())
        val moved = value.moveEntry(value.initialSet.id, value.initialSet.entries.last().id, -1).removeSet(value.sets.last().id)
        EventDraft(moved, value).saveTo(reopened, FileWorkspaceStore(path))
        val again = FileWorkspaceStore(path).load()
        assertEquals(moved, again.projects.first().events.single())
        assertEquals(value.sets.map { it.id }.take(2), moved.sets.map { it.id })
        assertEquals(value.entries.map { it.id }.toSet(), moved.entries.map { it.id }.toSet())
        assertEquals(reopened.projects.last(), again.projects.last())
        assertEquals(project.catalog, again.projects.first().catalog)
        assertEquals(reopened, WorkspaceCodec.decode(Files.readString(path.resolveSibling("workspace.backup.json"))))
    }
    @Test fun realSchemaFourSingleSetLoadsIntactAndBackupsAreImmutable() {
        val path = Files.createTempDirectory("single-set-migrate").resolve("workspace.json")
        val original = """{"schemaVersion":4,"projects":[{"id":"p","name":"Band","catalog":{"tunes":[{"id":"a","title":"Alpha","key":"alpha"}]},"events":[{"id":"event-original","projectId":"p","title":"Practice","type":"GIG","date":"2026-11-01","initialSet":{"id":"set-original","entries":[{"id":"entry-original","tuneId":"a"}]}}]}],"selectedId":"p"}"""
        Files.writeString(path, original)
        val oldBackup = path.resolveSibling("workspace.pre-events.backup.json")
        Files.writeString(oldBackup, "prior immutable events backup")
        val store = FileWorkspaceStore(path)
        val loaded = store.load()
        val value = loaded.projects.single().events.single()
        assertEquals(5, loaded.schemaVersion)
        assertEquals(1, value.sets.size)
        assertEquals("event-original", value.id)
        assertEquals("set-original", value.initialSet.id)
        assertEquals(EventTuneEntry("entry-original", "a"), value.entries.single())
        assertEquals(original, Files.readString(path))
        EventDraft(value.addSet(::id), value).saveTo(loaded, store)
        store.save(store.load())
        assertEquals(original, Files.readString(path.resolveSibling("workspace.pre-multisets.backup.json")))
        assertEquals("prior immutable events backup", Files.readString(oldBackup))
        assertEquals(value.initialSet, store.load().projects.single().events.single().initialSet)
    }
    @Test fun failedMultiSetSaveKeepsDirtyDraftAndDiskData() {
        val initial = Workspace().saveProject(project)
        val draft = EventDraft(event().addSet(::id))
        val failing = object : WorkspaceStore {
            override fun load() = initial
            override fun save(value: Workspace) { error("Unavailable") }
        }
        assertFails { draft.saveTo(initial, failing) }
        assertTrue(draft.isDirty)
        assertEquals(initial, failing.load())
    }
}
