package bandlogger

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable data class MasterItem(
    val tuneId: String, val title: String, val chartId: String, val part: String, val revision: Int,
    val source: String, val relativePath: String, val filename: String, val sha256: String
)
@Serializable data class MasterMember(
    val memberId: String, val memberName: String, val preferences: List<String>,
    val zipName: String? = null, val zipSha256: String? = null,
    val charts: List<MasterItem> = emptyList(), val warnings: List<String> = emptyList()
)
@Serializable data class LibraryMaster(
    val id: String, val projectId: String, val projectName: String, val createdAt: String,
    val folder: String, val members: List<MasterMember>, val warnings: List<String> = emptyList(),
    val formatVersion: Int = 1
) {
    fun validate() {
        require(formatVersion == 1 && id.isNotBlank() && projectId.isNotBlank() && folder.isNotBlank())
        require(members.map { it.memberId }.distinct().size == members.size)
        require(members.any { it.charts.isNotEmpty() }) { "A master must include at least one chart." }
        members.forEach {
            require((it.charts.isEmpty() && it.zipName == null && it.zipSha256 == null) ||
                (it.charts.isNotEmpty() && !it.zipName.isNullOrBlank() && !it.zipSha256.isNullOrBlank()))
            require(it.charts.map { c -> c.tuneId }.distinct().size == it.charts.size)
            it.charts.forEach { c -> require(c.chartId.isNotBlank() && c.part in chartParts && c.revision in 0..9999) }
        }
    }
}
object MasterCodec {
    private val json = Json { prettyPrint = true; encodeDefaults = true }
    fun encode(master: LibraryMaster): String { master.validate(); return json.encodeToString(master) }
    fun decode(text: String): LibraryMaster = json.decodeFromString<LibraryMaster>(text).also { it.validate() }
}
fun Project.recordMaster(master: LibraryMaster): Project {
    master.validate()
    require(master.projectId == id) { "This master belongs to a different project." }
    val prior = masters.find { it.id == master.id }
    require(prior == null || prior == master) { "A different master already uses this identity." }
    return if (prior != null) this else copy(masters = masters + master)
}