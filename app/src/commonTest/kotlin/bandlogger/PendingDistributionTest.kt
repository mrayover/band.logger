package bandlogger

import kotlin.test.*

class PendingDistributionTest {
    private val m = Member("m", "Player", emptyList(), listOf("Guitar", "C"))
    private fun chart(id: String = "c", part: String = "C", revision: Int = 0, tune: String = "a", hash: String = "a".repeat(64)) =
        ChartRecord(id, "source", "$id.pdf", hash, 10, 1, 1, tune, ChartMetadata(tune, part, revision), accepted = true)
    private fun project(charts: List<ChartRecord>, members: List<Member> = listOf(m)) = Project("p", "Band", members = members,
        catalog = Catalog("source", listOf(Tune("a", "Alpha", "alpha"), Tune("b", "Beta", "beta")), charts, 1))
    private fun master(p: Project, id: String = "master", time: String = "2026-10-21T10:00:00Z"): LibraryMaster {
        val members = MasterPlanner.preview(p).members.map { mp ->
            val charts = mp.choices.mapNotNull { choice -> choice.chart?.let { c -> MasterItem(choice.tune.id, choice.tune.title, c.id, c.metadata!!.part, c.metadata.revision, c.source, c.relativePath, c.relativePath, c.sha256) } }
            MasterMember(mp.member.id, mp.member.name, mp.preferences, if(charts.isEmpty()) null else "member.zip", if(charts.isEmpty()) null else "f".repeat(64), charts)
        }
        return LibraryMaster(id, p.id, p.name, time, "export", members)
    }
    private fun withBaseline(current: Project, prior: Project) = current.copy(masters = listOf(master(prior)))
    @Test fun newTune() {
        val p=project(listOf(chart())); val result=PendingPlanner.calculate(withBaseline(project(listOf(chart(),chart("b",tune="b"))),p))
        assertEquals(1,result.count(PendingKind.NEW_TUNE)); assertEquals(1,result.count(PendingKind.UNCHANGED))
    }
    @Test fun newPreferredPartReplacesFallbackDespiteLowerRevision() {
        val p=project(listOf(chart(revision=8)))
        val result=PendingPlanner.calculate(withBaseline(project(listOf(chart(revision=8),chart("g","Guitar",1))),p))
        assertEquals(PendingKind.NEW_PART,result.entries.single().kind); assertEquals(1,result.entries.single().chart!!.metadata!!.revision)
    }
    @Test fun revisedChartAndSameRevisionChangedHash() {
        val p=project(listOf(chart()))
        listOf(chart("r",revision=1),chart("r",hash="b".repeat(64))).forEach {
            assertEquals(PendingKind.REVISED,PendingPlanner.calculate(withBaseline(project(listOf(it)),p)).entries.single().kind)
        }
    }
    @Test fun identicalContentRenameDoesNotRedistribute() {
        val p=project(listOf(chart()))
        assertEquals(PendingKind.UNCHANGED,PendingPlanner.calculate(withBaseline(project(listOf(chart("renamed"))),p)).entries.single().kind)
    }
    @Test fun memberAllocationsIndependent() {
        val members=listOf(m,m.copy(id="bass",preferences=listOf("Bass")))
        val p=project(listOf(chart(),chart("bass","Bass",9)),members)
        val result=PendingPlanner.calculate(withBaseline(project(listOf(chart("c1",revision=1),chart("bass","Bass",9)),members),p))
        assertEquals(PendingKind.REVISED,result.entries.single { it.member.id=="m" }.kind)
        assertEquals(PendingKind.UNCHANGED,result.entries.single { it.member.id=="bass" }.kind)
    }
    @Test fun previouslyExportedPartSwitchIsRevisedSelection() {
        val c=project(listOf(chart())); val guitar=project(listOf(chart("g","Guitar")))
        val current=c.copy(masters=listOf(master(c,"old","2026-10-20T10:00:00Z"),master(guitar)))
        assertEquals(PendingKind.REVISED,PendingPlanner.calculate(current).entries.single().kind)
    }
    @Test fun unapprovedConflictMissingExcludedAndChangedBlock() {
        val p=project(listOf(chart()))
        val variants=listOf(listOf(chart().copy(accepted=false)),listOf(chart(),chart("conflict",hash="b".repeat(64))),
            listOf(chart().copy(present=false)),listOf(chart().copy(ignored=true)),listOf(chart().copy(changed=true,accepted=false)))
        variants.forEach { assertEquals(PendingKind.ATTENTION,PendingPlanner.calculate(withBaseline(project(it),p)).entries.single().kind) }
    }
    @Test fun liveSourceErrorBlocksUnchangedAndDoesNotFallback() {
        val p=project(listOf(chart("g","Guitar"),chart()))
        val result=PendingPlanner.calculate(withBaseline(p,p),mapOf("g" to "Source content changed"))
        assertEquals(PendingKind.ATTENTION,result.entries.single().kind); assertTrue(result.entries.single().explanation.contains("Source content changed"))
    }
    @Test fun memberWithoutBaselineNeedsInitialMaster() {
        val p=project(listOf(chart())); val current=p.copy(members=listOf(m,m.copy(id="new")),masters=listOf(master(p)))
        val result=PendingPlanner.calculate(current)
        assertEquals(1,result.count(PendingKind.INITIAL)); assertEquals(1,result.count(PendingKind.UNCHANGED))
    }
    @Test fun latestNonemptyMasterPerMemberAndFractionOrdering() {
        val p=project(listOf(chart())); val revised=project(listOf(chart("r",revision=1)))
        val older=master(p,"z","2026-10-21T10:00:00Z"); val newer=master(revised,"a","2026-10-21T10:00:00.1Z")
        val current=revised.copy(masters=listOf(newer,older))
        assertEquals("a",PendingPlanner.calculate(current).baselines.single().master!!.id)
        assertEquals(PendingKind.UNCHANGED,PendingPlanner.calculate(current).entries.single().kind)
    }
    @Test fun timeTieUsesIdAndSkippedMemberKeepsPriorBaseline() {
        val p=project(listOf(chart())); val a=master(p,"a"); val z=master(p,"z")
        assertEquals("z",PendingPlanner.calculate(p.copy(masters=listOf(z,a))).baselines.single().master!!.id)
        val other=m.copy(id="other")
        val later=master(project(listOf(chart()),listOf(other)),"later","2026-10-22T10:00:00Z")
        assertEquals("a",PendingPlanner.calculate(p.copy(masters=listOf(a,later))).baselines.single().master!!.id)
    }
    @Test fun emptyQueueAndEmptyProject() {
        val p=project(listOf(chart())); val result=PendingPlanner.calculate(withBaseline(p,p))
        assertEquals(0,result.count(PendingKind.NEW_TUNE)+result.count(PendingKind.NEW_PART)+result.count(PendingKind.REVISED))
        assertTrue(PendingPlanner.calculate(Project("empty","Empty")).entries.isEmpty())
    }
    @Test fun projectIsolationAndInvalidBaseline() {
        val p=project(listOf(chart()))
        listOf(master(p).copy(projectId="other"),master(p).copy(createdAt="invalid"),master(p).copy(members=listOf(master(p).members.single().copy(zipSha256="bad")))).forEach {
            val result=PendingPlanner.calculate(p.copy(masters=listOf(it)))
            assertEquals(PendingKind.INITIAL,result.entries.single().kind); assertTrue(result.notices.isNotEmpty())
        }
    }
    @Test fun removedTuneWarnsAndHistoryRemainsUnchanged() {
        val p=project(listOf(chart())); val current=withBaseline(project(emptyList()),p)
        val before=WorkspaceCodec.encode(Workspace().saveProject(current))
        assertEquals(PendingKind.ATTENTION,PendingPlanner.calculate(current).entries.single().kind)
        assertEquals(before,WorkspaceCodec.encode(Workspace().saveProject(current)))
    }
}
