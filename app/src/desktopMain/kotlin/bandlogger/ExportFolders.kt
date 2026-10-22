package bandlogger
import java.nio.file.*

object ExportFolders {
    fun child(parent: Path, name: String): Path {
        val target = parent.resolve(name)
        try { Files.createDirectory(target) } catch (_: FileAlreadyExistsException) { }
        require(Files.isDirectory(target, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(target) && target.toRealPath().parent == parent.toRealPath()) { "Unsafe export folder: $target" }
        return target
    }
    fun library(project: Project): Path {
        val configured = requireNotNull(project.projectRoot ?: project.exportDestination)
        val root = if (project.projectRoot != null) FileLocations.numberedRoot(configured) else Path.of(LocalCharts.folder(Path.of(configured)))
        val library = child(root, if (project.projectRoot != null) "02 - Repertoire Distributions" else "Library")
        val marker = library.resolve(".bandlogger-project-id")
        if (!Files.exists(marker, LinkOption.NOFOLLOW_LINKS)) Files.writeString(marker, project.id, StandardOpenOption.CREATE_NEW)
        require(Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(marker) && Files.readString(marker) == project.id) { "This Library folder belongs to another project. Choose a separate export root." }
        return library
    }
}
