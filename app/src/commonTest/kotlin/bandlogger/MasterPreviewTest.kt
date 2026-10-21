package bandlogger

import kotlin.test.*

class MasterPreviewTest {
    private val member = Member("m", "Player", listOf("Guitar"), listOf("Guitar", "C"))
    private fun chart(id: String, part: String = "C", revision: Int = 0, tune: String = "a") =
        ChartRecord(id, "source", "$id.pdf", id, 10, 1, 1, tune, ChartMetadata(tune, part, revision), accepted = true)
    private fun project(charts: List<ChartRecord>, members: List<Member> = listOf(member)) = Project("p", "Band", members = members,
        catalog = Catalog("source", listOf(Tune("a", "Alpha", "alpha"), Tune("b", "Beta", "beta")), charts, 1))
    private fun choice(project: Project) = MasterPlanner.preview(project).members.single().choices.single()

    @Test fun preferenceOutranksOtherPartsRevision() {
        val p = project(listOf(chart("c", revision = 8), chart("g0", "Guitar"), chart("g2", "Guitar", 2)))
        val result = choice(p)
        assertEquals("g2", result.chart?.id)
        assertEquals(SelectionState.PRIMARY, result.state)
    }
    @Test fun missingPrimaryUsesVisibleFallbackAndMissingAllOmits() {
        val fallback = choice(project(listOf(chart("c"))))
        assertEquals(SelectionState.FALLBACK, fallback.state)
        assertTrue(fallback.explanation.contains("Guitar"))
        val missing = choice(project(listOf(chart("bb", "Bb"))))
        assertEquals(SelectionState.MISSING, missing.state)
        assertNull(missing.chart)
    }
    @Test fun newestPendingPreventsDowngradeAndFallback() {
        val p = project(listOf(chart("old", "Guitar"), chart("new", "Guitar", 1).copy(accepted = false), chart("fallback")))
        val result = choice(p)
        assertEquals(SelectionState.BLOCKED, result.state)
        assertNull(result.chart)
        assertTrue(result.explanation.contains("New chart"))
    }
    @Test fun missingUnreadableChangedAndConflictingPreferredChartsBlock() {
        val variants = listOf(
            chart("top", "Guitar", 1).copy(present = false),
            chart("top", "Guitar", 1).copy(fileError = "Bad header"),
            chart("top", "Guitar", 1).copy(changed = true, accepted = false)
        )
        variants.forEach {
            assertEquals(SelectionState.BLOCKED, choice(project(listOf(it, chart("old", "Guitar"), chart("c")))).state)
        }
        val conflict = project(listOf(chart("x", "Guitar"), chart("y", "Guitar"), chart("c")))
        assertEquals(SelectionState.BLOCKED, choice(conflict).state)
    }
    @Test fun historicalConflictingBytesStillBlockUntilExcluded() {
        val current = chart("current", "Guitar")
        val old = chart("old", "Guitar").copy(current = false, present = false)
        assertEquals(SelectionState.BLOCKED, choice(project(listOf(current, old))).state)
        assertEquals("current", choice(project(listOf(current, old.copy(ignored = true)))).chart?.id)
    }
    @Test fun excludedAndPreviousFolderRecordsNeverSelected() {
        val excluded = chart("excluded", "Guitar").copy(ignored = true)
        val other = chart("other", "Guitar", 5).copy(source = "other-source")
        val p = project(listOf(excluded, other, chart("c")))
        val result = choice(p)
        assertEquals("c", result.chart?.id)
        assertEquals(SelectionState.FALLBACK, result.state)
        assertEquals(listOf("Excluded"), MasterPlanner.preview(p).issues.map { it.reason })
    }
    @Test fun identicalDuplicatesAndOrderingAreDeterministic() {
        val charts = listOf(chart("z"), chart("a").copy(sha256 = "z"), chart("beta", tune = "b"))
        val first = MasterPlanner.preview(project(charts))
        val reordered = MasterPlanner.preview(project(charts.reversed()).let { it.copy(catalog = it.catalog.copy(tunes = it.catalog.tunes.reversed())) })
        assertEquals(first, reordered)
        assertEquals(listOf("a", "b"), first.members.single().choices.map { it.tune.id })
        assertEquals("a.pdf", first.members.single().choices.first().chart?.relativePath)
    }
    @Test fun normalizedPreferencesAndUnsupportedPreferencesAreExplicit() {
        val normalized = member.copy(preferences = listOf(" guitar ", "c", "C"))
        assertEquals(listOf("Guitar", "C"), MasterPlanner.preview(project(listOf(chart("c")), listOf(normalized))).members.single().preferences)
        val invalid = member.copy(preferences = listOf("Unknown", "C"))
        val preview = MasterPlanner.preview(project(listOf(chart("c")), listOf(invalid))).members.single()
        assertTrue(preview.warnings.single().contains("Unknown"))
        assertNull(preview.choices.single().chart)
        assertEquals(SelectionState.BLOCKED, preview.choices.single().state)
        assertTrue(MasterPlanner.preview(project(listOf(chart("c")), listOf(member.copy(preferences = emptyList())))).members.single().warnings.isNotEmpty())
    }
    @Test fun projectsAndMembersAreIndependent() {
        val p = project(listOf(chart("c"), chart("bb", "Bb")), listOf(member, member.copy(id = "sax", preferences = listOf("Bb"))))
        val result = MasterPlanner.preview(p)
        assertEquals(listOf("c", "bb"), result.members.map { it.choices.single().chart?.id })
        val other = Project("other", "Solo", members = listOf(member))
        assertTrue(MasterPlanner.preview(other).members.single().choices.isEmpty())
        assertEquals("other", MasterPlanner.preview(other).projectId)
    }
    @Test fun unclassifiedFilesAreVisibleWithoutFabricatingTunes() {
        val unknown = chart("unknown").copy(metadata = null, tuneId = null, accepted = false)
        val result = MasterPlanner.preview(project(listOf(unknown)))
        assertEquals("Check filename", result.issues.single().reason)
        assertTrue(result.members.single().choices.isEmpty())
    }
    @Test fun emptySourceAndMembersHaveActionableNotices() {
        val result = MasterPlanner.preview(Project("p", "Empty"))
        assertTrue(result.members.isEmpty())
        assertTrue(result.notices.any { it.contains("Connect") })
        assertTrue(result.notices.any { it.contains("Add members") })
    }
    @Test fun previewDoesNotMutateReviewDecisions() {
        val p = project(listOf(chart("new").copy(accepted = false)))
        val before = WorkspaceCodec.encode(Workspace().saveProject(p))
        MasterPlanner.preview(p)
        assertEquals(before, WorkspaceCodec.encode(Workspace().saveProject(p)))
        assertFalse(p.catalog.charts.single().accepted)
    }
}