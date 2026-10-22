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
    fun eventsInitialRoot(project: Project, preferred: Path = defaultEvents): String? =
        project.exportDestination ?: project.eventsExportRoot ?: existingDirectory(preferred)
    fun eventTypeFolder(type: EventType) = if (type == EventType.GIG) "Gigs" else "Rehearsals"
}
