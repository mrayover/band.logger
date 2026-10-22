package bandlogger

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable enum class EventExportState { PENDING, ACTIVE, CLEANUP, DELETED }
@Serializable data class EventPackage(val memberId: String, val memberName: String, val filename: String, val sha256: String)
@Serializable data class EventExportRecord(
    val id: String, val projectId: String, val event: BandEvent,
    val root: String, val folder: String, val stage: String, val createdAt: String,
    val packages: List<EventPackage>, val state: EventExportState = EventExportState.PENDING,
    val removedFiles: List<String> = emptyList(), val error: String? = null,
    val providerMetadata: Map<String, String> = emptyMap()
) {
    fun validate() {
        require(id.isNotBlank() && projectId == event.projectId && root.isNotBlank() && folder.isNotBlank())
        event.validate()
        require(packages.isNotEmpty() && packages.map { it.filename.lowercase() }.distinct().size == packages.size)
        packages.forEach { require(ForScore.validReferenceFilename(it.filename.removeSuffix(".zip") + ".pdf") && it.filename.endsWith(".zip") && Regex("[a-f0-9]{64}").matches(it.sha256)) }
        require(providerMetadata.keys.all { it == "desktop.ini" } && providerMetadata.values.all { it.length <= 8192 })
        require(removedFiles.all { file -> packages.any { it.filename == file } })
    }
    fun identity() = copy(state = EventExportState.PENDING, removedFiles = emptyList(), error = null, providerMetadata = emptyMap())
}
object EventRecordCodec {
    private val json = Json { prettyPrint = true; encodeDefaults = true }
    fun encode(record: EventExportRecord) = json.encodeToString(record.also { it.validate() }.identity())
    fun decode(text: String) = json.decodeFromString<EventExportRecord>(text).also { it.validate() }
}
fun Project.recordEventExport(record: EventExportRecord) = copy(eventExports =
    if (eventExports.any { it.id == record.id }) eventExports.map { if (it.id == record.id) record else it } else eventExports + record)
