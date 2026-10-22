package bandlogger

import kotlinx.serialization.Serializable

@Serializable enum class EventStatus { PLANNED, COMPLETED, CANCELED }
@Serializable data class PerformedEntry(val id: String, val tuneId: String, val title: String)
@Serializable data class PerformedSet(val id: String, val entries: List<PerformedEntry>)
/** Append-only revisions. Tune/occurrence IDs and labels survive later plan/catalog edits. */
@Serializable data class PerformanceSnapshot(
    val revision: Int, val recordedAt: String, val title: String, val type: EventType,
    val date: String?, val isTest: Boolean, val sets: List<PerformedSet>
) {
    val entries: List<PerformedEntry> get() = sets.flatMap { it.entries }
    fun validate() {
        require(revision > 0 && recordedAt.isNotBlank() && title.isNotBlank())
        require(date == null || validEventDate(date))
        require(sets.isNotEmpty() && sets.all { it.id.isNotBlank() } && sets.map { it.id }.distinct().size == sets.size)
        require(entries.all { it.id.isNotBlank() && it.tuneId.isNotBlank() && it.title.isNotBlank() })
        require(entries.map { it.id }.distinct().size == entries.size)
    }
}
fun BandEvent.recordPerformance(project: Project, recordedAt: String, correction: Boolean = false): BandEvent {
    require(project.id == projectId) { "Event belongs to another project." }
    require(if (correction) status == EventStatus.COMPLETED && performance != null else status != EventStatus.COMPLETED) {
        "Use the explicit correction action for an existing completed record."
    }
    val titles = project.catalog.tunes.associate { it.id to it.title }
    val snapshot = PerformanceSnapshot(performanceHistory.size + 1, recordedAt, title.trim(), type, date, isTest,
        sets.map { set -> PerformedSet(set.id, set.entries.map { entry ->
            PerformedEntry(entry.id, entry.tuneId, requireNotNull(titles[entry.tuneId]) { "Selected tune is missing from this catalog." })
        }) })
    return copy(status = EventStatus.COMPLETED, performanceHistory = performanceHistory + snapshot).also { it.validate() }
}
/** Only distribution metadata crosses the export boundary; internal observations/facts stay local. */
fun BandEvent.forExport() = copy(notes = "", status = EventStatus.PLANNED, isTest = false, performanceHistory = emptyList())

enum class EventView { UPCOMING, HISTORY }
fun BandEvent.historyTitle() = if (status == EventStatus.COMPLETED) performance?.title ?: title else title
fun BandEvent.historyType() = if (status == EventStatus.COMPLETED) performance?.type ?: type else type
fun BandEvent.historyDate() = if (status == EventStatus.COMPLETED) performance?.date else date
fun Project.eventsFor(view: EventView, type: EventType? = null): List<BandEvent> {
    val selected = events.filter { (it.status == EventStatus.PLANNED) == (view == EventView.UPCOMING) && (type == null || it.historyType() == type) }
    return if (view == EventView.UPCOMING) selected.sortedWith(compareBy<BandEvent> { it.date == null }.thenBy { it.date }.thenBy { it.title.lowercase() }.thenBy { it.id })
    else selected.sortedWith(compareBy<BandEvent> { it.historyDate() == null }.thenByDescending { it.historyDate() }.thenBy { it.historyTitle().lowercase() }.thenBy { it.id })
}
