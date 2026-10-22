package bandlogger

/** A recorded delivery, not evidence that a musician installed it. */
data class DistributedChart(val chart: MasterItem, val distributionId: String, val supplementNumber: Int? = null) {
    val sourceLabel get() = supplementNumber?.let { "Supplement ${it.toString().padStart(3, '0')}" } ?: "Library Master"
}
data class MemberRepertoire(
    val member: Member, val master: LibraryMaster?,
    val byPart: Map<Pair<String, String>, DistributedChart>, val byTune: Map<String, DistributedChart>
)

/** Pure metadata resolution. No source/catalog allocation, package IO or workspace writes. */
object DistributionResolver {
    fun resolve(project: Project): List<MemberRepertoire> {
        // Reuse publication/chain/anchor validation before resolving any uncertain reference.
        var history = project.copy(supplements = emptyList())
        project.supplements.forEach { supplement ->
            require(runCatching {
                require(history.supplements.none { it.id == supplement.id })
                require(project.masters.none { it.id == supplement.id })
                require(PendingPlanner.valid(supplement.publication, project.id))
                supplement.baselineMasterIds.forEach { (member, id) ->
                    require(project.masters.count { it.id == id && PendingPlanner.valid(it, project.id) &&
                        it.members.any { m -> m.memberId == member && m.charts.isNotEmpty() } } == 1)
                }
                history = history.recordSupplement(supplement)
            }.isSuccess) { "Recorded Supplement ${supplement.number.toString().padStart(3, '0')} is inconsistent. Inspect distribution history before exporting; no filenames were guessed." }
        }
        return PendingPlanner.baselines(project).map { baseline ->
            val byPart = linkedMapOf<Pair<String, String>, DistributedChart>()
            val byTune = linkedMapOf<String, DistributedChart>()
            fun apply(charts: List<MasterItem>, id: String, number: Int? = null) {
                charts.forEach { chart ->
                    val delivered = DistributedChart(chart, id, number)
                    byPart[chart.tuneId to chart.part] = delivered
                    byTune[chart.tuneId] = delivered
                }
            }
            baseline.master?.let { master ->
                require(project.masters.count { it.id == master.id } == 1) { "Conflicting Library Master identity. Inspect distribution history before exporting." }
                apply(master.members.single { it.memberId == baseline.member.id }.charts, master.id)
                // Validated project history is already consecutive publication order, never timestamp order.
                project.supplements.filter { it.baselineMasterIds[baseline.member.id] == master.id }.forEach { supplement ->
                    apply(supplement.publication.members.singleOrNull { it.memberId == baseline.member.id }?.charts.orEmpty(), supplement.id, supplement.number)
                }
            }
            MemberRepertoire(baseline.member, baseline.master, byPart.toMap(), byTune.toMap())
        }
    }
}
