package bandlogger

import kotlin.test.*

class EventHistoryTest {
    private var sequence = 0
    private fun id() = "history-${++sequence}"
    private val project = Project("p", "Band", catalog = Catalog(tunes = listOf(Tune("a", "Alpha", "alpha"), Tune("b", "Beta", "beta"))))
    private fun event() = newEvent(project, EventType.GIG, ::id).copy(title = "Venue", date = "2026-10-20")
        .addTune(project, "a", ::id).addTune(project, "a", ::id).addSet(::id).let { it.addTune(project, "b", ::id, it.sets.last().id) }
    private val now = "2026-10-22T18:00:00Z"

    @Test fun oldDatesAndNewTestPlansAreNeverProofOfPerformance() {
        val plan = event().copy(isTest = true, date = "2000-01-01")
        assertEquals(EventStatus.PLANNED, plan.status)
        assertNull(plan.countablePerformance)
        assertTrue(plan.performanceHistory.isEmpty())
        assertEquals(listOf(plan), project.copy(events = listOf(plan)).eventsFor(EventView.UPCOMING))
    }
    @Test fun completionCapturesIdsRepetitionsAndLabelsIndependentlyOfLaterPlans() {
        val plan = event()
        val complete = plan.recordPerformance(project, now)
        val facts = requireNotNull(complete.countablePerformance)
        assertEquals(listOf("a", "a", "b"), facts.entries.map { it.tuneId })
        assertEquals(plan.entries.map { it.id }, facts.entries.map { it.id })
        assertEquals(plan.sets.map { it.id }, facts.sets.map { it.id })
        assertEquals(2, facts.entries.map { it.tuneId }.distinct().size)
        assertEquals(1, setOf(complete.id).size) // occurrences do not create extra events
        val edited = complete.copy(title = "Future plan", type = EventType.REHEARSAL, date = "2027-01-01", isTest = true)
            .removeEntry(complete.entries.first().id)
        val renamedCatalog = project.copy(catalog = project.catalog.copy(tunes = project.catalog.tunes.map { it.copy(title = "Renamed ${it.title}") }))
        val saved = renamedCatalog.saveEvent(complete).saveEvent(edited).events.single()
        assertEquals(facts, saved.countablePerformance)
        assertEquals("Venue", saved.historyTitle())
        assertEquals(EventType.GIG, saved.historyType())
        assertEquals("2026-10-20", saved.historyDate())
        assertEquals(listOf("Alpha", "Alpha", "Beta"), facts.entries.map { it.title })
    }
    @Test fun correctionsAppendAndPriorRecordsCannotBeSilentlyReplacedOrDropped() {
        val complete = event().recordPerformance(project, now)
        val changed = complete.removeEntry(complete.entries.first().id)
        assertFails { changed.recordPerformance(project, now) }
        val corrected = changed.recordPerformance(project, "2026-10-23T18:00:00Z", correction = true)
        assertEquals(2, corrected.performanceHistory.size)
        assertEquals(complete.performance, corrected.performanceHistory.first())
        assertEquals(2, corrected.performance?.revision)
        assertEquals(2, corrected.performance?.entries?.size)
        val saved = project.saveEvent(complete).saveEvent(corrected)
        assertFails { saved.saveEvent(corrected.copy(performanceHistory = listOf(corrected.performanceHistory.first()))) }
        assertFails { saved.saveEvent(corrected.copy(performanceHistory = corrected.performanceHistory.map { it.copy(title = "Silently rewritten") })) }
    }
    @Test fun severalExplicitCorrectionsCanBeSavedTogetherWithoutRewritingSavedRevisions() {
        val complete = event().recordPerformance(project, now)
        val first = complete.copy(notes = "First observation").recordPerformance(project, now, correction = true)
        val second = first.removeEntry(first.entries.first().id).recordPerformance(project, now, correction = true)
        val saved = project.saveEvent(complete).saveEvent(second).events.single()
        assertEquals(3, saved.performanceHistory.size)
        assertEquals(complete.performance, saved.performanceHistory.first())
        assertEquals(2, saved.performance?.entries?.size)
    }
    @Test fun canceledAndTestEventsNeverQualifyAndStatusChangesPreserveRecords() {
        val complete = event().recordPerformance(project, now)
        val canceled = complete.copy(status = EventStatus.CANCELED)
        assertNull(canceled.countablePerformance)
        assertEquals(complete.performance, canceled.performance)
        assertNull(canceled.copy(status = EventStatus.PLANNED).countablePerformance)
        val test = event().copy(isTest = true).recordPerformance(project, now)
        assertNull(test.countablePerformance)
        assertNull(test.copy(isTest = false).countablePerformance) // changing a future plan cannot reclassify old facts
        assertNotNull(test.copy(isTest = false).recordPerformance(project, now, correction = true).countablePerformance)
        assertFails { event().copy(status = EventStatus.COMPLETED).validate() }
    }
    @Test fun historyFiltersSortRecordedDatesAndKeepUndatedAndCanceledEventsReachable() {
        val old = event().recordPerformance(project, now)
        val recent = event().copy(date = "2026-10-21", type = EventType.REHEARSAL).recordPerformance(project, now)
        val canceled = event().copy(date = null, status = EventStatus.CANCELED)
        val planned = event().copy(date = "1990-01-01")
        val p = project.copy(events = listOf(canceled, planned, old, recent))
        assertEquals(listOf(recent, old, canceled), p.eventsFor(EventView.HISTORY))
        assertEquals(listOf(old, canceled), p.eventsFor(EventView.HISTORY, EventType.GIG))
        assertEquals(listOf(recent), p.eventsFor(EventView.HISTORY, EventType.REHEARSAL))
        assertEquals(listOf(planned), p.eventsFor(EventView.UPCOMING))
    }
    @Test fun notesArePrivateAndSnapshotDoesNotCaptureObservations() {
        val original = event()
        val complete = original.copy(notes = "Private feedback\nSecond paragraph", isTest = true).recordPerformance(project, now)
        assertEquals(original, complete.forExport())
        assertEquals(complete.performance, complete.copy(notes = "New observations").performance)
        assertFails { complete.recordPerformance(project.copy(id = "other"), now, correction = true) }
    }
}
