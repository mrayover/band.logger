package bandlogger

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable enum class EventExportState { PENDING, ACTIVE, CLEANUP, DELETED }
@Serializable data class EventPackage(val memberId: String, val memberName: String, val filename: String, val sha256: String)
@Serializable data class EventCoverRecord(val setId: String, val setNumber: Int, val filename: String, val sha256: String, val logoSha256: String? = null)
@Serializable data class EventExportRecord(
    val id: String, val projectId: String, val event: BandEvent,
    val root: String, val folder: String, val stage: String, val createdAt: String,
    val packages: List<EventPackage>, val state: EventExportState = EventExportState.PENDING,
    val removedFiles: List<String> = emptyList(), val error: String? = null,
    val providerMetadata: Map<String, String> = emptyMap(), val covers: List<EventCoverRecord> = emptyList()
) {
    fun validate() {
        require(id.isNotBlank() && projectId == event.projectId && root.isNotBlank() && folder.isNotBlank())
        event.validate()
        require(packages.isNotEmpty() && packages.map { it.filename.lowercase() }.distinct().size == packages.size)
        packages.forEach { require(ForScore.validReferenceFilename(it.filename.removeSuffix(".zip") + ".pdf") && it.filename.endsWith(".zip") && Regex("[a-f0-9]{64}").matches(it.sha256)) }
        require(providerMetadata.keys.all { it == "desktop.ini" } && providerMetadata.values.all { it.length <= 8192 })
        require(covers.map { it.setId }.distinct().size == covers.size && covers.map { it.filename.lowercase() }.distinct().size == covers.size)
        require(covers.isEmpty() || covers.map { it.setId }.toSet() == event.sets.filter { it.entries.isNotEmpty() }.map { it.id }.toSet())
        covers.forEach { cover ->
            require(event.sets.getOrNull(cover.setNumber - 1)?.let { it.id == cover.setId && it.entries.isNotEmpty() } == true)
            require(ForScore.validReferenceFilename(cover.filename) && Regex("[a-f0-9]{64}").matches(cover.sha256))
            require(cover.logoSha256 == null || Regex("[a-f0-9]{64}").matches(cover.logoSha256))
        }
        require(removedFiles.all { file -> packages.any { it.filename == file } })
    }
    fun identity() = copy(event = event.forExport(), state = EventExportState.PENDING, removedFiles = emptyList(), error = null, providerMetadata = emptyMap())
}
object EventRecordCodec {
    private val json = Json { prettyPrint = true; encodeDefaults = true }
    fun encode(record: EventExportRecord): String {
        val element = json.encodeToJsonElement(record.also { it.validate() }.identity()).jsonObject
        val event = element.getValue("event").jsonObject.filterKeys { it !in setOf("notes", "status", "isTest", "performanceHistory") }
        return json.encodeToString(JsonObject(element + ("event" to JsonObject(event))))
    }
    fun decode(text: String) = json.decodeFromString<EventExportRecord>(text).also {
        require(it.event == it.event.forExport()) { "Export manifest contains internal event data." }
        it.validate()
    }
}
fun Project.recordEventExport(record: EventExportRecord) = copy(eventExports =
    if (eventExports.any { it.id == record.id }) eventExports.map { if (it.id == record.id) record else it } else eventExports + record)
