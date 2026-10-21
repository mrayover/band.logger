package bandlogger

import kotlin.test.*

class MultipleSetsTest {
    private var next = 0
    private fun id() = "n-${++next}"
    private val project = Project("p", "Band", catalog = Catalog(tunes = listOf(Tune("a", "Alpha", "alpha"), Tune("b", "Beta", "beta"))))
    private fun event() = newEvent(project, EventType.GIG, ::id).copy(title = "Show")

    @Test fun addingSetsPreservesOriginalAndHasNoSmallLimit() {
        val original = event()
        var edited = original
        repeat(50) { edited = edited.addSet(::id) }
        assertEquals(51, edited.sets.size)
        assertEquals(original.initialSet, edited.initialSet)
        assertEquals(51, edited.sets.map { it.id }.distinct().size)
        edited.validate()
    }
    @Test fun destinationSelectionAndAddingToSeparateSets() {
        val value = event().addSet(::id)
        val selection = EventSetSelection(value.initialSet.id).select(value, value.sets.last().id)
        val edited = value.addTune(project, "a", ::id, selection.resolve(value).id).addTune(project, "b", ::id, value.initialSet.id)
        assertEquals(listOf("b"), edited.initialSet.entries.map { it.tuneId })
        assertEquals(listOf("a"), edited.sets.last().entries.map { it.tuneId })
        assertFalse(EventDraft(value, value).isDirty)
        assertFailsWith<IllegalArgumentException> { selection.select(value, "missing") }
        assertFailsWith<IllegalArgumentException> { value.addTune(project, "a", ::id, "missing") }
    }
    @Test fun repeatedTunesHaveDistinctStableIdentitiesAcrossSets() {
        var value = event().addSet(::id)
        value = value.addTune(project, "a", ::id).addTune(project, "a", ::id).addTune(project, "a", ::id, value.sets.last().id)
        value.validate()
        assertEquals(listOf("a", "a", "a"), value.entries.map { it.tuneId })
        assertEquals(3, value.entries.map { it.id }.distinct().size)
        assertEquals(project.catalog, project.saveEvent(value).catalog)
    }
    @Test fun upAndDownUpdateOrderWithoutTouchingOtherSets() {
        var value = event().addSet(::id)
        value = value.addTune(project, "a", ::id).addTune(project, "b", ::id).addTune(project, "a", ::id, value.sets.last().id)
        val first = value.initialSet.entries.first()
        val last = value.initialSet.entries.last()
        val moved = value.moveEntry(value.initialSet.id, last.id, -1)
        assertEquals(listOf(last, first), moved.initialSet.entries)
        assertEquals(value.additionalSets, moved.additionalSets)
        assertEquals(value, moved.moveEntry(moved.initialSet.id, last.id, 1))
        assertTrue(EventDraft(moved, value).isDirty)
    }
    @Test fun boundaryMovesAreNoOpsAndBadMovesAreRejected() {
        val value = event().addTune(project, "a", ::id)
        val entry = value.entries.single()
        assertEquals(value, value.moveEntry(value.initialSet.id, entry.id, -1))
        assertEquals(value, value.moveEntry(value.initialSet.id, entry.id, 1))
        assertFailsWith<IllegalArgumentException> { value.moveEntry(value.initialSet.id, "missing", 1) }
        assertFailsWith<IllegalArgumentException> { value.moveEntry(value.initialSet.id, entry.id, 2) }
    }
    @Test fun removingOneRepeatedOccurrencePreservesOthers() {
        val value = event().addTune(project, "a", ::id).addTune(project, "a", ::id)
        val edited = value.removeEntry(value.entries.first().id)
        assertEquals(listOf(value.entries.last()), edited.entries)
        assertEquals(value.initialSet.id, edited.initialSet.id)
    }
    @Test fun removingEmptySetDoesNotNeedConfirmation() {
        val value = event().addSet(::id)
        assertEquals(listOf(value.initialSet), value.removeSet(value.sets.last().id).sets)
    }
    @Test fun removingNonEmptySetRequiresExplicitConfirmation() {
        var value = event().addSet(::id)
        val target = value.sets.last().id
        value = value.addTune(project, "a", ::id, target)
        assertFailsWith<IllegalArgumentException> { value.removeSet(target) }
        assertEquals(listOf(value.initialSet), value.removeSet(target, confirmed = true).sets)
        assertEquals(1, value.sets.last().entries.size)
    }
    @Test fun cannotRemoveLastSetEvenWhenConfirmed() {
        val value = event()
        assertFailsWith<IllegalArgumentException> { value.removeSet(value.initialSet.id, confirmed = true) }
    }
    @Test fun removalRenumbersByOrderAndRetainsIdentitiesAndContents() {
        var value = event().addSet(::id).addSet(::id)
        value = value.addTune(project, "b", ::id, value.sets.last().id)
        val retained = value.sets.last()
        val edited = value.removeSet(value.sets[1].id)
        assertEquals(listOf(value.initialSet, retained), edited.sets)
        assertEquals(retained, edited.sets[1]) // displayed as Set 2, original ID retained
        assertEquals(listOf(retained), edited.removeSet(edited.initialSet.id).sets)
    }
    @Test fun activeSetRemovalSelectsRemainingSetAndUnrelatedRemovalKeepsSelection() {
        val value = event().addSet(::id).addSet(::id)
        val selection = EventSetSelection(value.sets[1].id)
        val edited = value.removeSet(selection.setId)
        assertEquals(edited.initialSet.id, selection.reconcile(edited).setId)
        assertEquals(selection, selection.reconcile(value.removeSet(value.sets.last().id)))
    }
    @Test fun validationCoversAdditionalSetsAndForeignTuneReferences() {
        val value = event().addSet(::id)
        assertFailsWith<IllegalArgumentException> { value.copy(additionalSets = listOf(value.initialSet)).validate() }
        val bad = value.copy(additionalSets = listOf(EventSet("foreign", listOf(EventTuneEntry("entry", "other-project-tune")))))
        assertFailsWith<IllegalArgumentException> { Workspace().saveProject(project.copy(events = listOf(bad))) }
        val repeat = value.addTune(project, "a", ::id)
        val duplicatedId = repeat.copy(additionalSets = listOf(EventSet("second", repeat.initialSet.entries)))
        assertFailsWith<IllegalArgumentException> { duplicatedId.validate() }
    }
}
