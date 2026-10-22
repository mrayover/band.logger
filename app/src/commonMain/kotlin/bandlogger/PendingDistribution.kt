package bandlogger

/** Ephemeral comparison only: nothing here updates catalogs, manifests or receipt state. */
enum class PendingKind(val label: String) {
    NEW_TUNE("New Tunes"), NEW_PART("New Parts"), REVISED("Revised Charts"),
    UNCHANGED("Unchanged"), INITIAL("Initial master required"), ATTENTION("Needs attention")
}
data class PendingEntry(val member: Member, val tune: Tune, val chart: ChartRecord?,
    val previous: MasterItem?, val kind: PendingKind, val explanation: String)
data class MemberBaseline(val member: Member, val master: LibraryMaster?)
data class PendingDistribution(val entries: List<PendingEntry>, val baselines: List<MemberBaseline>,
    val fileIssues: List<PreviewIssue>, val notices: List<String>) {
    fun count(kind: PendingKind) = entries.count { it.kind == kind }
}
object PendingPlanner {
    // Exporter writes UTC Instant strings. Normalize fractions so .1Z sorts after the same second's Z.
    private fun timeKey(value: String): String? {
        val m = Regex("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(?:\\.(\\d{1,9}))?Z").matchEntire(value) ?: return null
        return m.groupValues[1] + m.groupValues[2].padEnd(9, '0')
    }
    private fun valid(master: LibraryMaster, projectId: String): Boolean = runCatching {
        master.validate()
        require(master.projectId == projectId && timeKey(master.createdAt) != null)
        master.members.forEach { member ->
            if (member.charts.isNotEmpty()) require(Regex("[a-fA-F0-9]{64}").matches(member.zipSha256.orEmpty()))
            member.charts.forEach { require(it.tuneId.isNotBlank() && Regex("[a-fA-F0-9]{64}").matches(it.sha256)) }
        }
    }.isSuccess

    private fun validMasters(project: Project) = project.masters.filter { valid(it, project.id) }
        .sortedWith(compareBy<LibraryMaster>({ timeKey(it.createdAt) }, { it.id }))

    fun baselines(project: Project): List<MemberBaseline> {
        val masters = validMasters(project)
        return project.members.map { member -> MemberBaseline(member,
            masters.lastOrNull { master -> master.members.any { it.memberId == member.id && it.charts.isNotEmpty() } }) }
    }

    fun calculate(project: Project, sourceIssues: Map<String, String> = emptyMap()): PendingDistribution {
        // Overlay read-only source errors before selection, so a broken preferred chart cannot trigger a fallback.
        val checked = project.copy(catalog = project.catalog.copy(charts = project.catalog.charts.map {
            if (it.id in sourceIssues) it.copy(fileError = sourceIssues.getValue(it.id)) else it
        }))
        val preview = MasterPlanner.preview(checked)
        val masters = validMasters(project)
        val baselines = baselines(project)
        val entries = preview.members.flatMap { allocation ->
            val baseline = baselines.single { it.member.id == allocation.member.id }.master
            val initial = baseline?.members?.single { it.memberId == allocation.member.id }?.charts.orEmpty()
            // Track each delivered instrument part independently within this member's latest master epoch.
            val additions = project.supplements.filter { it.baselineMasterIds[allocation.member.id] == baseline?.id }
                .flatMap { it.publication.members.filter { m -> m.memberId == allocation.member.id }.flatMap { m -> m.charts } }
            val prior = (initial + additions).groupBy { it.tuneId to it.part }.values.map { it.last() }
            val history = masters.flatMap { it.members.filter { m -> m.memberId == allocation.member.id }.flatMap { m -> m.charts } }
            val choices = allocation.choices.map { choice ->
                val previous = prior.find { it.tuneId == choice.tune.id && it.part == choice.chart?.metadata?.part } ?: prior.lastOrNull { it.tuneId == choice.tune.id }
                val chart = choice.chart
                val kind = when {
                    chart == null -> PendingKind.ATTENTION
                    baseline == null -> PendingKind.INITIAL
                    previous == null -> PendingKind.NEW_TUNE
                    previous.part == chart.metadata!!.part && previous.revision == chart.metadata.revision &&
                        previous.sha256.equals(chart.sha256, true) -> PendingKind.UNCHANGED
                    previous.part != chart.metadata!!.part && (history + additions).none { it.tuneId == choice.tune.id && it.part == chart.metadata.part } -> PendingKind.NEW_PART
                    else -> PendingKind.REVISED
                }
                val explanation = if (chart == null) {
                    val errors = project.catalog.charts.filter { it.tuneId == choice.tune.id }.mapNotNull { sourceIssues[it.id] }.distinct()
                    (listOf(choice.explanation) + errors).joinToString(" ")
                } else choice.explanation + when (kind) {
                    PendingKind.NEW_PART -> " This part has not appeared in this member's recorded masters."
                    PendingKind.REVISED -> " Revision, content or previously exported part selection differs from the baseline."
                    PendingKind.INITIAL -> " Generate an initial Library Master for this member first."
                    else -> ""
                }
                PendingEntry(allocation.member, choice.tune, chart, previous, kind, explanation)
            }
            // A tune entirely absent from the active source still needs an explicit exception.
            choices + prior.filter { old -> choices.none { it.tune.id == old.tuneId } }.map { old ->
                PendingEntry(allocation.member, Tune(old.tuneId, old.title, titleKey(old.title)), null, old,
                    PendingKind.ATTENTION, "Previously exported tune has no current chart in the connected catalog. Refresh Library and inspect the source folder.")
            }
        }.sortedWith(compareBy({ it.tune.key }, { it.tune.id }, { it.member.name }, { it.member.id }))
        val issues = preview.issues.filter { !it.chart.ignored }.map { issue ->
            issue.copy(reason = sourceIssues[issue.chart.id] ?: issue.reason)
        }
        return PendingDistribution(entries, baselines, issues, preview.notices +
            preview.members.flatMap { m -> m.warnings.map { "${m.member.name}: $it" } } +
            if (masters.size != project.masters.size) listOf("Some recorded masters are invalid or belong to another project and were excluded from baseline selection.") else emptyList())
    }
}
