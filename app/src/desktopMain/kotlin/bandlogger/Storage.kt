package bandlogger

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.*
import java.nio.file.AtomicMoveNotSupportedException

class FileWorkspaceStore(private val path: Path) : WorkspaceStore {
    override fun load(): Workspace = if (Files.exists(path)) WorkspaceCodec.decode(Files.readString(path)) else Workspace()
    override fun save(value: Workspace) {
        val text = WorkspaceCodec.encode(value)
        Files.createDirectories(path.parent)
        val temp = Files.createTempFile(path.parent, "workspace-", ".tmp")
        try {
            Files.writeString(temp, text)
            if (Files.exists(path)) {
                val oldText = Files.readString(path)
                if (Regex("\"schemaVersion\"\\s*:\\s*1\\b").containsMatchIn(oldText)) {
                    val original = path.resolveSibling("workspace.chunk1.backup.json")
                    if (!Files.exists(original)) Files.copy(path, original)
                }
                Files.copy(path, path.resolveSibling("workspace.backup.json"), REPLACE_EXISTING)
            }
            try { Files.move(temp, path, ATOMIC_MOVE, REPLACE_EXISTING) }
            catch (_: AtomicMoveNotSupportedException) { Files.move(temp, path, REPLACE_EXISTING) }
        } finally { Files.deleteIfExists(temp) }
    }
}
fun dataDirectory(): Path = Path.of(System.getProperty("bandlogger.dataDir")
    ?: (System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home")) + "/band.logger")