package bandlogger

import kotlin.test.*

class EventsTest {
    private var sequence = 0
    private fun id() = "id-${++sequence}"
    private val tune = Tune("t", "First tune", "first tune")
    private val otherTune = Tune("u", "Second tune", "second tune")
    private val project = Project("p", "Band", catalog = Catalog(tunes = listOf(tune, otherTune)))
    private fun event(type: EventType = EventType.REHEARSAL) = newEvent(project, type, ::id).copy(title = "Practice")

    @Test fun createsBothTypesAndOneStableEmptySet() {
        EventType.entries.forEach { type ->
            val value = event(type)
            value.validate()
            assertEquals(type, value.type)
            assertEquals("p", value.projectId)
            assertTrue(value.initialSet.entries.isEmpty())
            assertNotEquals(value.id, value.initialSet.id)
        }
    }
    @Test fun titleIsRequiredAndDateIsOptional() {
        event().validate()
        assertFailsWith<IllegalArgumentException> { event().copy(title = " ").validate() }
        assertFailsWith<IllegalArgumentException> { event().copy(date = "").validate() }
    }
    @Test fun datesHandleLeapYearsAndRejectInvalidValues() {
        listOf("2026-10-21", "2028-02-29", "2000-02-29", "0001-01-01").forEach { assertTrue(validEventDate(it), it) }
        listOf("2026-02-29", "1900-02-29", "2026-04-31", "2026-13-01", "2026-00-01", "2026-01-00", "0000-01-01", "2026-1-1", "tomorrow").forEach { assertFalse(validEventDate(it), it) }
    }
    @Test fun selectionsReferenceCatalogWithoutDuplicatingTunes() {
        val value = event().addTune(project, "t", ::id).addTune(project, "u", ::id)
        val saved = project.saveEvent(value)
        assertEquals(project.catalog, saved.catalog)
        assertEquals(listOf("t", "u"), saved.events.single().initialSet.entries.map { it.tuneId })
        assertEquals(3, value.addTune(project, "t", ::id).entries.size)
    }
    @Test fun foreignProjectAndUnknownTunesAreRejected() {
        val value = event()
        assertFailsWith<IllegalArgumentException> { value.addTune(project.copy(id = "other"), "t", ::id) }
        assertFailsWith<IllegalArgumentException> { value.addTune(project, "unknown", ::id) }
        assertFailsWith<IllegalArgumentException> { Project("other", "Other").saveEvent(value) }
        assertFailsWith<IllegalArgumentException> { project.saveEvent(value.copy(initialSet = value.initialSet.copy(entries = listOf(EventTuneEntry("e", "unknown"))))) }
    }
    @Test fun editsReplaceEventWithoutChangingIdentities() {
        val value = event().addTune(project, "t", ::id)
        val updated = value.copy(title = "Gig night", type = EventType.GIG, date = "2026-11-01")
        val saved = project.saveEvent(value).saveEvent(updated)
        assertEquals(listOf(updated), saved.events)
        assertEquals(value.id, updated.id)
        assertEquals(value.initialSet, updated.initialSet)
    }
    @Test fun removingOneEntryRetainsOtherEntryAndSetIdentities() {
        val value = event().addTune(project, "t", ::id).addTune(project, "u", ::id)
        val updated = value.removeEntry(value.initialSet.entries.first().id)
        assertEquals(value.initialSet.id, updated.initialSet.id)
        assertEquals(listOf(value.initialSet.entries.last()), updated.initialSet.entries)
    }
    @Test fun dirtyTrackingHandlesNewReopenedChangedAndRevertedDrafts() {
        val value = event()
        assertTrue(EventDraft(value).isDirty)
        val opened = EventDraft(value, value)
        assertFalse(opened.isDirty)
        assertTrue(opened.copy(value = value.copy(title = "Edited")).isDirty)
        assertFalse(opened.copy(value = value.copy(title = "Edited")).copy(value = value).isDirty)
    }
    @Test fun duplicateNestedIdentitiesAndWrongOwnershipAreRejected() {
        val value = event().addTune(project, "t", ::id)
        listOf(
            project.copy(events = listOf(value, value)),
            project.copy(events = listOf(value, value.copy(id = "another"))),
            project.copy(events = listOf(value.copy(projectId = "another"))),
            project.copy(events = listOf(value.copy(initialSet = value.initialSet.copy(entries = value.initialSet.entries + value.initialSet.entries.single().copy(tuneId = "u")))))
        ).forEach { invalid -> assertFailsWith<IllegalArgumentException> { Workspace().saveProject(invalid) } }
    }
    @Test fun currentSchemaIsEncodedAndFutureSchemaIsRejected() {
        val encoded = WorkspaceCodec.encode(Workspace().saveProject(project.saveEvent(event())))
        assertTrue(encoded.contains("\"schemaVersion\": 6"))
        assertFailsWith<IllegalArgumentException> { WorkspaceCodec.decode(encoded.replace("\"schemaVersion\": 6", "\"schemaVersion\": 99")) }
    }
}
