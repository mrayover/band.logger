package bandlogger

import kotlinx.serialization.Serializable

@Serializable enum class EventType { REHEARSAL, GIG }
@Serializable data class EventTuneEntry(val id: String, val tuneId: String)
@Serializable data class EventSet(val id: String, val entries: List<EventTuneEntry> = emptyList())
@Serializable data class BandEvent(
    val id: String, val projectId: String, val title: String,
    val type: EventType, val date: String? = null, val initialSet: EventSet,
    val additionalSets: List<EventSet> = emptyList(),
    val notes: String = "", val status: EventStatus = EventStatus.PLANNED,
    val isTest: Boolean = false, val performanceHistory: List<PerformanceSnapshot> = emptyList()
) {
    // Keep the original serialized initialSet so legacy event IDs and entries load intact.
    val sets: List<EventSet> get() = listOf(initialSet) + additionalSets
    val entries: List<EventTuneEntry> get() = sets.flatMap { it.entries }
    val performance: PerformanceSnapshot? get() = performanceHistory.lastOrNull()
    val countablePerformance: PerformanceSnapshot? get() = performance?.takeIf { status == EventStatus.COMPLETED && !it.isTest }
    fun validate() {
        require(id.isNotBlank() && projectId.isNotBlank() && sets.all { it.id.isNotBlank() }) { "Event and set identities are required." }
        require(status != EventStatus.COMPLETED || performance != null) { "Confirm the performed setlist when marking an event Completed." }
        performanceHistory.forEachIndexed { index, record -> record.validate(); require(record.revision == index + 1) { "Invalid performance revision." } }
        require(title.isNotBlank()) { "Event title is required." }
        require(date == null || validEventDate(date)) { "Use a real date in YYYY-MM-DD format, or leave it blank." }
        require(sets.map { it.id }.distinct().size == sets.size) { "Duplicate set identity." }
        require(entries.all { it.id.isNotBlank() && it.tuneId.isNotBlank() })
        require(entries.map { it.id }.distinct().size == entries.size) { "Duplicate tune-entry identity." }
    }
    private fun withSets(value: List<EventSet>): BandEvent {
        require(value.isNotEmpty()) { "Keep at least one set." }
        return copy(initialSet = value.first(), additionalSets = value.drop(1))
    }
    fun addSet(newId: () -> String) = withSets(sets + EventSet(newId()))
    fun removeSet(setId: String, confirmed: Boolean = false): BandEvent {
        val target = sets.single { it.id == setId }
        require(sets.size > 1) { "Keep at least one set." }
        require(target.entries.isEmpty() || confirmed) { "Confirm removal of this set and its songs." }
        return withSets(sets.filterNot { it.id == setId })
    }
    fun addTune(project: Project, tuneId: String, newId: () -> String, setId: String = initialSet.id): BandEvent {
        require(project.id == projectId) { "This event belongs to another project." }
        require(project.catalog.tunes.any { it.id == tuneId }) { "Tune is not in this project's catalog." }
        require(sets.any { it.id == setId }) { "Destination set is missing." }
        val entry = EventTuneEntry(newId(), tuneId)
        return withSets(sets.map { if (it.id == setId) it.copy(entries = it.entries + entry) else it })
    }
    fun removeEntry(id: String) = withSets(sets.map { it.copy(entries = it.entries.filterNot { entry -> entry.id == id }) })
    fun moveEntry(setId: String, entryId: String, direction: Int): BandEvent {
        require(direction == -1 || direction == 1)
        val target = sets.single { it.id == setId }
        val index = target.entries.indexOfFirst { it.id == entryId }
        require(index >= 0) { "Tune entry is missing." }
        val destination = index + direction
        if (destination !in target.entries.indices) return this
        val reordered = target.entries.toMutableList().apply { add(destination, removeAt(index)) }
        return withSets(sets.map { if (it.id == setId) it.copy(entries = reordered) else it })
    }
}
// Selection is editor state; choosing a destination alone does not dirty the event.
data class EventSetSelection(val setId: String) {
    fun resolve(event: BandEvent) = event.sets.firstOrNull { it.id == setId } ?: event.sets.first()
    fun select(event: BandEvent, id: String): EventSetSelection {
        require(event.sets.any { it.id == id }) { "Destination set is missing." }
        return EventSetSelection(id)
    }
    fun reconcile(event: BandEvent) = EventSetSelection(resolve(event).id)
}
fun validEventDate(value: String): Boolean {
    if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(value)) return false
    val year = value.substring(0, 4).toInt()
    val month = value.substring(5, 7).toInt()
    val day = value.substring(8, 10).toInt()
    if (year !in 1..9999 || month !in 1..12) return false
    val leap = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    return day in 1..listOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)[month - 1]
}
fun newEvent(project: Project, type: EventType, newId: () -> String) =
    BandEvent(newId(), project.id, "", type, initialSet = EventSet(newId()))
fun Project.saveEvent(event: BandEvent): Project {
    event.validate()
    require(event.projectId == id) { "This event belongs to another project." }
    require(event.entries.all { entry -> catalog.tunes.any { it.id == entry.tuneId } }) { "A selected tune is not in this project's catalog." }
    events.find { it.id == event.id }?.let { previous ->
        require(event.performanceHistory.take(previous.performanceHistory.size) == previous.performanceHistory) { "Keep existing performance revisions; use an explicit correction." }
    }
    return copy(events = if (events.any { it.id == event.id }) events.map { if (it.id == event.id) event else it } else events + event)
}
data class EventDraft(val value: BandEvent, val baseline: BandEvent? = null) {
    val isDirty: Boolean get() = value != baseline
    fun saved() = copy(baseline = value)
}
data class SavedEventDraft(val workspace: Workspace, val draft: EventDraft)
fun EventDraft.saveTo(workspace: Workspace, store: WorkspaceStore): SavedEventDraft {
    require(workspace.selectedId == value.projectId) { "Select this event's project before saving." }
    val project = workspace.projects.single { it.id == value.projectId }
    val normalized = value.copy(title = value.title.trim())
    val next = workspace.saveProject(project.saveEvent(normalized))
    store.save(next)
    return SavedEventDraft(next, EventDraft(normalized, normalized))
}
