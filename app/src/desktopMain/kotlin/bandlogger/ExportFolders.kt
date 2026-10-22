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
        val root = Path.of(LocalCharts.folder(Path.of(requireNotNull(project.exportDestination))))
        val library = child(root, "Library")
        val marker = library.resolve(".bandlogger-project-id")
        if (!Files.exists(marker, LinkOption.NOFOLLOW_LINKS)) Files.writeString(marker, project.id, StandardOpenOption.CREATE_NEW)
        require(Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(marker) && Files.readString(marker) == project.id) { "This Library folder belongs to another project. Choose a separate export root." }
        return library
    }
}
