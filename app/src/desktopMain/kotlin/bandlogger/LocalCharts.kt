package bandlogger

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object LocalCharts {
    fun folder(path: Path): String {
        require(Files.isDirectory(path) && Files.isReadable(path)) { "The chart folder is unavailable. Check Google Drive and try again." }
        return path.toRealPath().toString()
    }
    fun inspect(path: Path, relative: String): ScannedPdf {
        val before = Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        require(before.isRegularFile && !before.isSymbolicLink) { "Only regular PDF files are supported." }
        val digest = MessageDigest.getInstance("SHA-256")
        var header = byteArrayOf()
        Files.newInputStream(path).use { stream ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                if (Thread.currentThread().isInterrupted) throw InterruptedException("Scan canceled")
                val length = stream.read(buffer)
                if (length < 0) break
                if (header.isEmpty()) header = buffer.copyOf(minOf(length, 1024))
                digest.update(buffer, 0, length)
            }
        }
        val after = Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        require(before.size() == after.size() && before.lastModifiedTime() == after.lastModifiedTime()) { "A PDF changed during scanning. Wait for synchronization, then refresh." }
        val error = if (header.toString(Charsets.ISO_8859_1).contains("%PDF-")) null else "The file has no PDF header."
        return ScannedPdf(relative, digest.digest().joinToString("") { "%02x".format(it) }, after.size(), after.lastModifiedTime().toMillis(), error)
    }
    // Fail the whole scan on IO errors so a partial listing can never mark unseen files missing.
    fun scan(source: String): List<ScannedPdf> {
        val root = Path.of(folder(Path.of(source)))
        val files = mutableListOf<ScannedPdf>()
        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
            override fun visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (attrs.isRegularFile && !attrs.isSymbolicLink && path.fileName.toString().endsWith(".pdf", true))
                    files.add(inspect(path, root.relativize(path).toString().replace('\\', '/')))
                return FileVisitResult.CONTINUE
            }
            override fun visitFileFailed(file: Path, exc: java.io.IOException): FileVisitResult { throw exc }
        })
        return files.sortedBy { it.relativePath }
    }
    fun importPdf(source: Path, folder: String, filename: String): Path {
        require(filename.isNotBlank() && filename.endsWith(".pdf", true) && filename.length <= 220 &&
            filename.none { it in "<>:\"/\\|?*" || it.code < 32 } && filename.trim() == filename && !filename.endsWith('.')) { "Use a valid PDF filename without slashes or Windows reserved characters." }
        require(!Regex("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\.|$)", RegexOption.IGNORE_CASE).containsMatchIn(filename)) { "That filename is reserved by Windows." }
        val root = Path.of(this.folder(Path.of(folder)))
        val target = root.resolve(filename)
        require(!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) { "A file with this name already exists. Choose a different revision or filename." }
        val original = inspect(source, source.fileName.toString())
        require(original.error == null) { "Select a PDF file." }
        val temporary = Files.createTempFile(root, ".bandlogger-import-", ".tmp")
        try {
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING)
            val copied = inspect(temporary, filename)
            require(copied.sha256 == original.sha256) { "The source changed during import. Nothing has been published; try again." }
            Files.move(temporary, target) // deliberately no REPLACE_EXISTING
            return target
        } finally { Files.deleteIfExists(temporary) }
    }
}
fun writeProofZip(target: Path) {
    Files.newOutputStream(target, StandardOpenOption.CREATE_NEW).use { output ->
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("band.logger-synthetic-proof.4ss"))
            zip.write(proofXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("READ-ME.txt"))
            zip.write("Save this ZIP locally on your iPad. Extract it in Apple Files, then share the .4ss to forScore. Open Setlists and find band.logger & forScore - technical proof (the displayed title uses a long dash). This contains synthetic test pages only.".toByteArray())
            zip.closeEntry()
        }
    }
}