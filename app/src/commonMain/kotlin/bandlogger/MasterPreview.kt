package bandlogger

enum class SelectionState { PRIMARY, FALLBACK, MISSING, BLOCKED }
data class MasterChoice(val tune: Tune, val chart: ChartRecord?, val state: SelectionState, val explanation: String)
data class MemberPreview(val member: Member, val preferences: List<String>, val warnings: List<String>, val choices: List<MasterChoice>) {
    val selectedCount get() = choices.count { it.chart != null }
    val fallbackCount get() = choices.count { it.state == SelectionState.FALLBACK }
    val blockedCount get() = choices.count { it.state == SelectionState.BLOCKED }
    val missingCount get() = choices.count { it.state == SelectionState.MISSING }
}
data class PreviewIssue(val chart: ChartRecord, val reason: String)
data class MasterPreview(val projectId: String, val members: List<MemberPreview>, val issues: List<PreviewIssue>, val notices: List<String>)

// Pure snapshot planning. File access and generation belong to the platform export service.
object MasterPlanner {
    fun preview(project: Project): MasterPreview {
        val catalog = project.catalog
        val records = if (catalog.source == null) emptyList() else catalog.charts
            .filter { it.source == catalog.source && it.current }
        val tunes = catalog.tunes.filter { tune -> records.any { it.tuneId == tune.id } }
            .sortedWith(compareBy({ it.key }, { it.title }, { it.id }))
        val issues = records.mapNotNull { chart ->
            val status = catalog.status(chart)
            val reason = if (chart.tuneId == null && status == "Ready") "Unclassified tune" else status
            if (reason == "Ready") null else PreviewIssue(chart, reason)
        }.sortedWith(compareBy({ it.chart.relativePath }, { it.chart.id }))
        val notices = buildList {
            if (catalog.source == null) add("Connect a chart folder under Library.")
            if (catalog.lastScan == null) add("Refresh the library before reviewing allocations.")
            if (project.members.isEmpty()) add("Add members and chart preferences under Members.")
            if (tunes.isEmpty()) add("No classified tunes in the connected folder.")
        }
        val members = project.members.map { member ->
            val normalized = member.preferences.map { raw ->
                chartParts.find { it.equals(raw.trim(), ignoreCase = true) }
            }
            val unsupported = member.preferences.filterIndexed { index, _ -> normalized[index] == null }
            val preferences = normalized.filterNotNull().distinct()
            val warnings = buildList {
                if (member.preferences.isEmpty()) add("Set ordered chart preferences under Members.")
                if (unsupported.isNotEmpty()) add("Unsupported preferences: ${unsupported.joinToString()}. Edit this member before selecting charts.")
            }
            val choices = tunes.map { tune ->
                if (warnings.isNotEmpty()) MasterChoice(tune, null, SelectionState.BLOCKED, warnings.joinToString(" "))
                else select(catalog, records.filter { it.tuneId == tune.id }, tune, preferences)
            }
            MemberPreview(member, preferences, warnings, choices)
        }
        return MasterPreview(project.id, members, issues, notices)
    }

    private fun select(catalog: Catalog, records: List<ChartRecord>, tune: Tune, preferences: List<String>): MasterChoice {
        for ((index, part) in preferences.withIndex()) {
            // Explicit exclusions may be skipped. Missing, unreviewed or conflicting latest records
            // block the tune, so we never silently downgrade or bypass an unresolved preferred part.
            val candidates = records.filter { !it.ignored && it.metadata?.part == part }
            if (candidates.isEmpty()) continue
            val revision = candidates.maxOf { it.metadata!!.revision }
            val latest = candidates.filter { it.metadata!!.revision == revision }
            val reasons = latest.map { catalog.status(it) }.filter { it != "Ready" }.distinct().sorted()
            if (reasons.isNotEmpty()) return MasterChoice(tune, null, SelectionState.BLOCKED,
                "$part r${revision.toString().padStart(2, '0')} needs attention: ${reasons.joinToString()}. Review in Library; no older revision or fallback selected.")
            // Identical-content duplicate paths represent one chart; pick a stable path.
            val chosen = latest.sortedWith(compareBy({ it.relativePath }, { it.id })).first()
            return MasterChoice(tune, chosen, if (index == 0) SelectionState.PRIMARY else SelectionState.FALLBACK,
                if (index == 0) "First preference: $part."
                else "Fallback: $part. No included chart for ${preferences.take(index).joinToString(" or ")}.")
        }
        return MasterChoice(tune, null, SelectionState.MISSING,
            "No included chart matches ${preferences.joinToString(" → ")}. Review available parts or member preferences.")
    }
}