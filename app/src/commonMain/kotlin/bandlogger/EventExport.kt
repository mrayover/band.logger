package bandlogger

/** Reference planning reads recorded master metadata only, never source PDFs or current allocations. */
data class EventReference(val entryId: String, val tuneId: String, val title: String, val filename: String?, val reason: String? = null)
data class EventExportSet(val number: Int, val entries: List<EventReference>)
data class EventMemberExport(val member: Member, val preferredPart: String?, val baselineId: String?, val baseName: String, val sets: List<EventExportSet>) {
    val entries get() = sets.flatMap { it.entries }
    val matchedCount get() = entries.count { it.filename != null }
    val missingCount get() = entries.count { it.filename == null }
}
data class EventExportPlan(val projectId: String, val event: BandEvent, val folderName: String, val members: List<EventMemberExport>, val skippedSets: List<Int>) {
    val needsMissingAcknowledgement get() = members.any { it.missingCount > 0 || it.baselineId == null }
    val needsUndatedAcknowledgement get() = event.date == null
    val warnings get() = skippedSets.map { "Empty Set $it will be skipped." } +
        if (event.date == null) listOf("No saved event date: filenames use Undated.") else emptyList()
    fun requireAcknowledgements(missing: Boolean, undated: Boolean) {
        require(!needsMissingAcknowledgement || missing) { "Acknowledge missing references and placeholder-only members before exporting." }
        require(!needsUndatedAcknowledgement || undated) { "Supply a saved event date or acknowledge Undated filenames." }
    }
}
object EventExportPlanner {
    fun safeLabel(text: String, limit: Int = 50): String {
        var label = text.map { if (it in "<>:\"/\\|?*" || it.code < 32) '_' else it }.joinToString("").trim().trimEnd('.').take(limit).trimEnd(' ', '.')
        if (label.lastOrNull()?.isHighSurrogate() == true) label = label.dropLast(1)
        if (label.isBlank()) label = "Untitled"
        if (Regex("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?").matches(label)) label = "_$label"
        return label
    }
    fun preview(project: Project, eventId: String): EventExportPlan {
        val event = requireNotNull(project.events.find { it.id == eventId }) { "Save this event in the current project before exporting." }
        event.validate()
        require(event.projectId == project.id) { "Event belongs to another project." }
        require(event.entries.isNotEmpty()) { "This event contains no songs." }
        require(project.members.isNotEmpty()) { "Add project members before exporting." }
        val date = event.date?.replace("-", "") ?: "Undated"
        val label = "$date ${safeLabel(event.title)}"
        val titles = project.catalog.tunes.associate { it.id to it.title }
        val used = mutableSetOf<String>()
        val members = PendingPlanner.baselines(project).map { baseline ->
            val member = baseline.member
            val part = member.preferences.firstNotNullOfOrNull { raw -> chartParts.find { it.equals(raw.trim(), true) } }
            val memberLabel = safeLabel(member.name, 32) + (part?.let { " - $it" } ?: "")
            val proposed = "$label ($memberLabel)"
            var name = proposed
            var index = 2
            while (!used.add(name.lowercase())) { name = "$proposed - ${index++}" }
            val charts = baseline.master?.members?.single { it.memberId == member.id }?.charts.orEmpty().associateBy { it.tuneId }
            val sets = event.sets.mapIndexedNotNull { setIndex, set ->
                if (set.entries.isEmpty()) null else EventExportSet(setIndex + 1, set.entries.map { entry ->
                    val chart = charts[entry.tuneId]
                    val title = titles[entry.tuneId] ?: chart?.title ?: "Unavailable tune"
                    val filename = chart?.filename?.takeIf { ForScore.validReferenceFilename(it) }
                    val reason = when {
                        baseline.master == null -> "No recorded non-empty Library Master for this member."
                        chart == null -> "Tune is absent from this member's latest baseline."
                        filename == null -> "Recorded filename is unsafe or invalid; it was not guessed or changed."
                        else -> null
                    }
                    EventReference(entry.id, entry.tuneId, title, filename, reason)
                })
            }
            EventMemberExport(member, part, baseline.master?.id, name, sets)
        }
        return EventExportPlan(project.id, event, label, members, event.sets.mapIndexedNotNull { i, set -> if (set.entries.isEmpty()) i + 1 else null })
    }
    fun xml(member: EventMemberExport): String = buildString {
        append(ForScore.header(member.baseName))
        member.sets.forEach { set ->
            append(ForScore.placeholderElement("Set ${set.number}"))
            set.entries.forEach { entry ->
                append(if (entry.filename == null) ForScore.placeholderElement("Missing chart — ${entry.title}")
                    else ForScore.referenceElement(entry.title, entry.filename))
            }
        }
        append(ForScore.footer)
    }
}
