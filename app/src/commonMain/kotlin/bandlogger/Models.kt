package bandlogger

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable data class Theme(
    val background: String = "#171B24", val foreground: String = "#F4F2EA",
    val accent: String = "#E7C66A", val secondary: String = "#7698AD",
    val surface: String = "#242B36"
) {
    fun validate() = require(listOf(background, foreground, accent, secondary, surface)
        .all { Regex("#[0-9a-fA-F]{6}").matches(it) }) { "Use six-digit colors such as #171B24." }
}
@Serializable data class Member(val id: String, val name: String, val roles: List<String>, val preferences: List<String>)
@Serializable data class Project(
    val id: String, val name: String, val prefix: String = "", val theme: Theme = Theme(),
    val logoReference: String? = null, val members: List<Member> = emptyList(), val catalog: Catalog = Catalog(),
    val exportDestination: String? = null
)
@Serializable data class Workspace(val schemaVersion: Int = 2, val projects: List<Project> = emptyList(), val selectedId: String? = null) {
    fun validate() {
        require(schemaVersion == 2) { "This data was created by an unsupported application version." }
        require(projects.map { it.id }.distinct().size == projects.size) { "Duplicate project identity." }
        require(selectedId == null || projects.any { it.id == selectedId }) { "Selected project is missing." }
        projects.forEach { p ->
            require(p.id.isNotBlank() && p.name.isNotBlank()) { "Project name is required." }
            p.theme.validate()
            p.catalog.validate()
            require(p.members.map { it.id }.distinct().size == p.members.size)
            p.members.forEach { require(it.id.isNotBlank() && it.name.isNotBlank()) { "Member name is required." } }
        }
    }
    fun saveProject(project: Project): Workspace = copy(
        projects = if (projects.any { it.id == project.id }) projects.map { if (it.id == project.id) project else it } else projects + project,
        selectedId = project.id
    ).also { it.validate() }
    fun select(id: String): Workspace = copy(selectedId = id).also { it.validate() }
}
object WorkspaceCodec {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    fun encode(value: Workspace): String { value.validate(); return json.encodeToString(value) }
    fun decode(value: String): Workspace {
        val stored = json.decodeFromString<Workspace>(value)
        val migrated = if (stored.schemaVersion == 1) stored.copy(schemaVersion = 2) else stored
        return migrated.also { it.validate() }
    }
}
interface WorkspaceStore { fun load(): Workspace; fun save(value: Workspace) }
// Android implements these boundaries using app-private storage and SAF/Drive URIs.
interface ChartSource { suspend fun readBytes(reference: String): ByteArray }
interface ExportDestination { suspend fun writeNew(filename: String, bytes: ByteArray): String }
fun quaternity(id: String, newId: () -> String): Project = Project(
    id, "Quaternity", "Q", members = listOf(
        Member(newId(), "Marcus", listOf("Guitar"), listOf("Guitar", "C")),
        Member(newId(), "Jordan", listOf("Tenor saxophone", "EWI"), listOf("Bb", "C")),
        Member(newId(), "Xavier", listOf("Bass"), listOf("Bass", "C")),
        Member(newId(), "Ethan", listOf("Drums"), listOf("Drums", "C"))
    )
)