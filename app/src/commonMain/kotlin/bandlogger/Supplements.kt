package bandlogger

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable data class LibrarySupplement(
    val number: Int, val publication: LibraryMaster, val baselineMasterIds: Map<String, String>,
    val previousId: String? = null, val selectionHash: String
) {
    val id get() = publication.id
    fun validate() {
        require(number > 0 && Regex("[a-fA-F0-9]{64}").matches(selectionHash))
        publication.validate()
        require(publication.members.filter { it.charts.isNotEmpty() }.all { it.memberId in baselineMasterIds })
    }
}
object SupplementCodec {
    private val json = Json { prettyPrint = true; encodeDefaults = true }
    fun encode(value: LibrarySupplement): String { value.validate(); return json.encodeToString(value) }
    fun decode(text: String) = json.decodeFromString<LibrarySupplement>(text).also { it.validate() }
}
fun Project.recordSupplement(value: LibrarySupplement): Project {
    value.validate()
    require(value.publication.projectId == id) { "Supplement belongs to another project." }
    val prior = supplements.find { it.id == value.id }
    if (prior != null) { require(prior == value) { "Conflicting supplement identity." }; return this }
    require(value.number == supplements.size + 1 && value.previousId == supplements.lastOrNull()?.id) { "Supplement history changed. Recover the next publication in sequence." }
    value.baselineMasterIds.forEach { (member, master) ->
        require(masters.any { it.id == master && it.members.any { m -> m.memberId == member && m.charts.isNotEmpty() } }) { "Supplement baseline does not belong to this project/member." }
    }
    return copy(supplements = supplements + value)
}
data class SupplementPreview(val projectId: String, val number: Int, val previousId: String?, val baselines: Map<String, String>, val pending: PendingDistribution, val packages: MasterPreview) {
    val selectedCount get() = packages.members.sumOf { it.selectedCount }
    val warnings get() = pending.notices + pending.fileIssues.map { "${it.chart.relativePath}: ${it.reason}" } +
        pending.entries.filter { it.kind == PendingKind.INITIAL || it.kind == PendingKind.ATTENTION }.map { "${it.member.name} · ${it.tune.title}: ${it.explanation}" } +
        pending.baselines.filter { it.master == null }.map { "${it.member.name}: initial Library Master required; excluded from supplement." }
}
object SupplementPlanner {
    fun preview(project: Project, sourceIssues: Map<String, String> = emptyMap()): SupplementPreview {
        val pending = PendingPlanner.calculate(project, sourceIssues)
        val full = MasterPlanner.preview(project)
        val members = full.members.map { member ->
            val eligible = pending.entries.filter { it.member.id == member.member.id && it.kind in listOf(PendingKind.NEW_TUNE, PendingKind.NEW_PART, PendingKind.REVISED) && it.chart != null }
            member.copy(choices = member.choices.filter { choice -> eligible.any { it.tune.id == choice.tune.id && it.chart?.id == choice.chart?.id } })
        }
        return SupplementPreview(project.id, project.supplements.size + 1, project.supplements.lastOrNull()?.id,
            pending.baselines.mapNotNull { b -> b.master?.let { b.member.id to it.id } }.toMap(), pending,
            full.copy(members = members, notices = full.notices +
                pending.baselines.filter { it.master == null }.map { "${it.member.name}: initial Library Master required; excluded." } +
                pending.entries.filter { it.kind == PendingKind.ATTENTION }.map { "${it.member.name} · ${it.tune.title}: ${it.explanation}" }))
    }
}
