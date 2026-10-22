package bandlogger

import java.nio.file.Files
import java.nio.file.Path

/** Chooser preferences are independent of authoritative source and distribution locations. */
object FileLocations {
    val pdfStaging: Path = Path.of("C:/Users/mrove/OneDrive/Desktop/Quaternity/band.logger/sorted")
    val defaultEvents: Path = Path.of("G:/My Drive/Quartet/band.logger/03 - Events")
    fun existingDirectory(preferred: Path): String? = runCatching {
        if (Files.isDirectory(preferred) && Files.isReadable(preferred)) preferred.toRealPath().toString() else null
    }.getOrNull()
    fun pdfInitialDirectory(preferred: Path = pdfStaging): String? = existingDirectory(preferred)
    fun eventsInitialRoot(project: Project, preferred: Path = defaultEvents.parent): String? =
        project.projectRoot ?: project.exportDestination ?: project.eventsExportRoot ?: if (project.brandId == Branding.QUATERNITY) existingDirectory(preferred) else null
    val numberedFolders = listOf("00 - Inbox", "01 - Chart Library", "02 - Repertoire Distributions", "03 - Events", "04 - Catalog & Backups", "05 - Archive", "06 - Testing")
    fun numberedRoot(value: String): Path {
        val root = Path.of(LocalCharts.folder(Path.of(value)))
        require(root.fileName.toString() !in numberedFolders + listOf("Library", "Events", "Master", "Supplements", "Gigs", "Rehearsals")) {
            "Choose the project root containing the numbered folders, not a distribution/event subfolder."
        }
        return root
    }
    fun inbox(project: Project): Path = project.projectRoot?.let { Path.of(it).resolve("00 - Inbox") } ?: pdfStaging
    fun libraryDestination(project: Project): String? = (project.projectRoot ?: project.exportDestination)?.let {
        Path.of(it).resolve(if (project.projectRoot != null) "02 - Repertoire Distributions" else "Library").toString()
    }
    fun masterDestination(project: Project): String? = libraryDestination(project)?.let { Path.of(it).resolve("Master").toString() }
    fun supplementDestination(project: Project, number: Int): String? = libraryDestination(project)?.let {
        Path.of(it).resolve("Supplements").resolve("Supplement ${number.toString().padStart(3,'0')}").toString()
    }
    fun eventDestination(project: Project, plan: EventExportPlan, destination: String): Path {
        val root = Path.of(destination)
        val parent = if (project.projectRoot == null) root else root.resolve(if (plan.testOutput) "06 - Testing" else "03 - Events")
        return parent.resolve(eventTypeFolder(plan.event.type)).resolve(plan.folderName)
    }
    fun eventTypeFolder(type: EventType) = if (type == EventType.GIG) "Gigs" else "Rehearsals"
}
