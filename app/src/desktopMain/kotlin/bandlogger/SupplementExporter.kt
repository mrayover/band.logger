package bandlogger

import java.nio.file.*
import java.security.MessageDigest
import java.time.Instant

class SupplementRecordFailure(val supplement: LibrarySupplement, cause: Exception) : Exception("Packages completed but history was not saved. Recover ${supplement.publication.folder} before publishing again. ${cause.message}", cause)
object SupplementExporter {
    private fun hash(text: String) = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    fun folder(project: Project, number: Int): Path {
        val destination = Path.of(LocalCharts.folder(Path.of(requireNotNull(project.exportDestination) { "Choose an export destination in Library master first." })))
        var current = destination
        listOf("Supplements", "Project-${hash(project.id).take(12)}").forEach { label ->
            current = current.resolve(label)
            try { Files.createDirectory(current) } catch (_: FileAlreadyExistsException) { }
            require(Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(current) && current.toRealPath().startsWith(destination)) { "Unsafe Supplements folder." }
        }
        return current.resolve("Supplement ${number.toString().padStart(3, '0')}")
    }
    private fun fingerprint(members: List<MasterMember>): String = hash(members.filter { it.charts.isNotEmpty() }.sortedBy { it.memberId }.joinToString("\n") { m ->
        m.memberId + ":" + m.charts.sortedBy { it.tuneId }.joinToString(";") { "${it.tuneId}:${it.chartId}:${it.part}:${it.revision}:${it.sha256}" }
    })
    private fun journal(project: Project, directory: Path) = directory.resolve("supplement-pending-${hash(project.id).take(20)}.txt")
    fun clearRecordedIntent(project: Project, directory: Path = dataDirectory()) {
        val path = journal(project, directory)
        if (Files.exists(path)) {
            val id = Files.readAllLines(path).firstOrNull()
            if (project.supplements.any { it.id == id }) Files.delete(path)
        }
    }
    @Synchronized
    fun generate(project: Project, preview: SupplementPreview, id: String = newId(), now: Instant = Instant.now(), journalDirectory: Path = dataDirectory()): LibrarySupplement {
        Files.createDirectories(journalDirectory)
        clearRecordedIntent(project, journalDirectory)
        val intent = journal(project, journalDirectory)
        if (Files.exists(intent)) {
            val pending = Files.readAllLines(intent)
            val location = pending.drop(1).joinToString("\n")
            require(location.isNotBlank()) { "Unreadable pending publication journal; preserve it before recovery." }
            require(!Files.exists(Path.of(location))) { "A completed supplement awaits recording. Recover $location before publishing again." }
            Files.delete(intent) // no completed output: retry without advancing history; old stage retained
        }
        val checked = PendingSources.check(project)
        require(preview == SupplementPlanner.preview(project, checked.issues)) { "Pending selections or source files changed. Review a new preview." }
        require(preview.selectedCount > 0) { "No eligible pending selections. Nothing was published." }
        val target = folder(project, preview.number)
        require(!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) { "Supplement ${preview.number} already exists. Recover its completed folder before retrying; nothing was overwritten." }
        var record: LibrarySupplement? = null
        Files.writeString(intent, "$id\n$target", StandardOpenOption.CREATE_NEW)
        try {
            MasterExporter.generateSelected(project, preview.packages, id, now, target, "Library Supplement ${preview.number.toString().padStart(3, '0')}") { manifest ->
                LibrarySupplement(preview.number, manifest, preview.baselines, preview.previousId, fingerprint(manifest.members)).also { record = it }.let(SupplementCodec::encode)
            }
        } catch (e: Exception) {
            if (!Files.exists(target)) Files.deleteIfExists(intent)
            throw e
        }
        return requireNotNull(record)
    }
    fun publish(project: Project, preview: SupplementPreview, journalDirectory: Path = dataDirectory(), save: (Project) -> Unit): LibrarySupplement {
        val result = generate(project, preview, journalDirectory = journalDirectory)
        try { save(project.recordSupplement(result)) }
        catch (e: Exception) { throw SupplementRecordFailure(result, e) }
        runCatching { clearRecordedIntent(project.recordSupplement(result), journalDirectory) }
        return result
    }
    fun recover(project: Project, location: String): LibrarySupplement {
        val root = Path.of(LocalCharts.folder(Path.of(location)))
        val text = Files.newInputStream(root.resolve("supplement.json")).use { it.readNBytes(16 * 1024 * 1024 + 1) }
        require(text.size <= 16 * 1024 * 1024) { "Supplement manifest is too large." }
        val result = SupplementCodec.decode(text.toString(Charsets.UTF_8))
        val manifest = MasterExporter.recover(project, location, allowSupplement = true)
        require(manifest == result.publication && fingerprint(manifest.members) == result.selectionHash) { "Supplement metadata/package selections changed." }
        project.recordSupplement(result) // verifies identity, numbering, project and chain; idempotent
        return result
    }
}
