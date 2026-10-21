package bandlogger

import kotlinx.serialization.Serializable

val chartParts = listOf("C", "Bb", "Eb", "Bass", "Drums", "Guitar", "Piano")
fun titleKey(title: String) = title.trim().replace(Regex("\\s+"), " ").lowercase()
@Serializable data class ChartMetadata(val title: String, val part: String, val revision: Int) {
    fun validate() {
        require(title.isNotBlank()) { "A tune title is required." }
        require(part in chartParts) { "Choose C, Bb, Eb, Bass, Drums, Guitar or Piano." }
        require(revision in 0..9999) { "Revision must be between 0 and 9999." }
    }
}
object ChartNames {
    fun parse(filename: String, prefix: String): ChartMetadata? {
        val match = Regex("^(.+?)\\s+-r([0-9]{2,4})-\\s+([A-Za-z]+)\\.pdf$", RegexOption.IGNORE_CASE).matchEntire(filename) ?: return null
        var title = match.groupValues[1].trim()
        if (prefix.isNotBlank()) {
            val start = Regex("^" + Regex.escape(prefix.trim()) + "\\s+", RegexOption.IGNORE_CASE).find(title) ?: return null
            title = title.substring(start.value.length).trim()
        }
        val part = chartParts.find { it.equals(match.groupValues[3], true) } ?: return null
        return ChartMetadata(title, part, match.groupValues[2].toInt()).takeIf { title.isNotBlank() }
    }
    fun filename(prefix: String, metadata: ChartMetadata): String {
        metadata.validate()
        return (if (prefix.isBlank()) "" else prefix.trim() + " ") + metadata.title.trim() +
            " -r${metadata.revision.toString().padStart(2, '0')}- ${metadata.part}.pdf"
    }
}
@Serializable data class Tune(val id: String, val title: String, val key: String)
@Serializable data class ChartRecord(
    val id: String, val source: String, val relativePath: String, val sha256: String,
    val size: Long, val modified: Long, val discovered: Long,
    val tuneId: String? = null, val metadata: ChartMetadata? = null,
    val current: Boolean = true, val present: Boolean = true, val accepted: Boolean = false,
    val ignored: Boolean = false, val changed: Boolean = false,
    val fileError: String? = null, val note: String = ""
)
data class ScannedPdf(val relativePath: String, val sha256: String, val size: Long, val modified: Long, val error: String? = null)
@Serializable data class Catalog(
    val source: String? = null, val tunes: List<Tune> = emptyList(),
    val charts: List<ChartRecord> = emptyList(), val lastScan: Long? = null
) {
    fun validate() {
        require(tunes.map { it.id }.distinct().size == tunes.size)
        require(charts.map { it.id }.distinct().size == charts.size)
        require(tunes.map { it.key }.distinct().size == tunes.size)
        charts.forEach { c ->
            require(c.tuneId == null || tunes.any { it.id == c.tuneId })
            c.metadata?.validate()
        }
    }
    fun active() = charts.filter { it.source == source && it.current && it.present && !it.ignored }
    fun conflicts(record: ChartRecord): Boolean = record.metadata != null && charts.any {
        it.id != record.id && !it.ignored && it.source == record.source && it.tuneId == record.tuneId &&
            it.metadata?.part == record.metadata.part && it.metadata?.revision == record.metadata.revision && it.sha256 != record.sha256
    }
    fun status(record: ChartRecord): String = when {
        record.source != source -> "Previous folder"
        !record.current -> "Previous content"
        !record.present -> "Missing file"
        record.ignored -> "Excluded"
        record.fileError != null -> "Unreadable PDF"
        record.metadata == null -> "Check filename"
        conflicts(record) -> "Revision conflict"
        record.changed && !record.accepted -> "Changed content"
        !record.accepted -> "New chart"
        else -> "Ready"
    }
    fun needsReview() = active().filter { status(it) != "Ready" }
    fun reconcile(files: List<ScannedPdf>, prefix: String, now: Long, newId: () -> String): Catalog {
        val root = requireNotNull(source) { "Choose a chart folder first." }
        require(files.map { it.relativePath }.distinct().size == files.size)
        val tuneList = tunes.toMutableList()
        val records = charts.map { if (it.source == root) it.copy(present = false) else it }.toMutableList()
        files.sortedBy { it.relativePath }.forEach { file ->
            val same = records.indexOfFirst { it.source == root && it.relativePath == file.relativePath && it.sha256 == file.sha256 && it.current }
            if (same >= 0) {
                records[same] = records[same].copy(present = true, size = file.size, modified = file.modified, fileError = file.error)
            } else {
                val prior = records.indexOfFirst { it.source == root && it.relativePath == file.relativePath && it.current }
                if (prior >= 0) records[prior] = records[prior].copy(current = false, present = false)
                val meta = ChartNames.parse(file.relativePath.substringAfterLast('/'), prefix)
                val tune = meta?.let { m -> tuneList.find { it.key == titleKey(m.title) } ?: Tune(newId(), m.title, titleKey(m.title)).also { tuneList.add(it) } }
                records.add(ChartRecord(newId(), root, file.relativePath, file.sha256, file.size, file.modified, now,
                    tune?.id, meta, changed = prior >= 0, fileError = file.error))
            }
        }
        return copy(tunes = tuneList, charts = records, lastScan = now).also { it.validate() }
    }
    fun review(id: String, metadata: ChartMetadata, note: String, newId: () -> String): Catalog {
        metadata.validate()
        val original = charts.single { it.id == id }
        require(original.current && original.present && original.source == source) { "Refresh and select an available file." }
        require(original.fileError == null) { "This file could not be recognized as a PDF. Repair it outside band.logger, then refresh." }
        val tune = tunes.find { it.key == titleKey(metadata.title) } ?: Tune(newId(), metadata.title.trim(), titleKey(metadata.title))
        val revised = original.copy(metadata = metadata.copy(title = metadata.title.trim()), tuneId = tune.id, accepted = true, ignored = false, note = note.trim())
        require(!conflicts(revised)) { "Different PDF content already uses this tune, part and revision. Assign the correct new revision or exclude the unwanted record first." }
        return copy(tunes = if (tunes.any { it.id == tune.id }) tunes else tunes + tune, charts = charts.map { if (it.id == id) revised else it })
    }
    fun exclude(id: String, excluded: Boolean): Catalog = copy(charts = charts.map { if (it.id == id) it.copy(ignored = excluded, accepted = if (excluded) false else it.accepted) else it })
    fun acceptNew(): Catalog = copy(charts = charts.map {
        if (it.source == source && it.current && it.present && !it.ignored && !it.changed && it.metadata != null && it.fileError == null && !conflicts(it)) it.copy(accepted = true) else it
    })
}