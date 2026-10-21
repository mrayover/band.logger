package bandlogger

/** Read-only filesystem snapshot. Never reconcile or save review state from this screen. */
data class PendingSourceCheck(val issues: Map<String, String>, val notices: List<String>)
object PendingSources {
    fun check(project: Project): PendingSourceCheck {
        val source = project.catalog.source ?: return PendingSourceCheck(emptyMap(), listOf("Connect a source folder in Library."))
        val records = project.catalog.charts.filter { it.source == source && it.current && !it.ignored }
        return try {
            val files = LocalCharts.scan(source).associateBy { it.relativePath }
            val issues = records.mapNotNull { record ->
                val file = files[record.relativePath]
                val reason = when {
                    file == null -> "Source missing: ${record.relativePath}. Refresh Library."
                    file.error != null -> "Unreadable source: ${record.relativePath}. ${file.error}"
                    file.sha256 != record.sha256 || file.size != record.size -> "Source content changed: ${record.relativePath}. Refresh and review in Library."
                    else -> null
                }
                reason?.let { record.id to it }
            }.toMap()
            val unknown = files.keys.count { path -> project.catalog.charts.none { it.source == source && it.current && it.relativePath == path } }
            PendingSourceCheck(issues, if (unknown > 0) listOf("$unknown unindexed PDF(s). Refresh Library to discover and review them; they are not ready for distribution.") else emptyList())
        } catch (e: Exception) {
            PendingSourceCheck(records.associate { it.id to "Source verification failed: ${e.message}. Check the folder and refresh Library." },
                listOf("Source verification failed. No saved selection is treated as ready. ${e.message}"))
        }
    }
}
