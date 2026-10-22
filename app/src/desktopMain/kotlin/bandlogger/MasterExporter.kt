package bandlogger

import java.nio.file.*
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object MasterExporter {
    private const val MAX_PDF = 64 * 1024 * 1024
    private const val MAX_MEMBER = 512L * 1024 * 1024
    private fun safeLabel(text: String) = text.map { if (it in "<>:\"/\\|?*" || it.code < 32) '_' else it }
        .joinToString("").trim().trimEnd('.').take(32).ifBlank { "Untitled" }
    private fun hash(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    private fun fileHash(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    private fun sourcePath(root: Path, record: ChartRecord): Path {
        val relative = Path.of(record.relativePath)
        require(!relative.isAbsolute && relative.none { it.toString() == ".." } && relative.toString().isNotBlank()) { "Unsafe source path. Refresh the library." }
        val path = root.resolve(relative).normalize()
        require(path.startsWith(root)) { "Source path escaped the library." }
        var current = root
        relative.forEach { current = current.resolve(it); require(!Files.isSymbolicLink(current)) { "Symbolic links cannot be exported." } }
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && path.toRealPath().startsWith(root)) { "Source unavailable: ${record.relativePath}. Refresh the library." }
        return path
    }
    private fun verify(root: Path, record: ChartRecord): Path {
        val path = sourcePath(root, record)
        val current = LocalCharts.inspect(path, record.relativePath)
        require(current.sha256 == record.sha256 && current.size == record.size && current.error == null) {
            "Source changed: ${record.relativePath}. Refresh and review before exporting."
        }
        require(current.size <= MAX_PDF) { "${record.relativePath} exceeds the 64 MiB per-PDF limit." }
        return path
    }
    private fun bytes(root: Path, record: ChartRecord): ByteArray {
        val path = verify(root, record)
        val bytes = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS).use { it.readNBytes(MAX_PDF + 1) }
        require(bytes.size <= MAX_PDF && hash(bytes) == record.sha256) { "Source changed during export: ${record.relativePath}." }
        return bytes
    }

    // Complete files in a staging folder before publication. The caller then persists the
    // returned manifest; recover() handles failure between publication and metadata save.
    fun generate(project: Project, preview: MasterPreview, id: String = UUID.randomUUID().toString(),
                 now: Instant = Instant.now()): LibraryMaster {
        require(preview == MasterPlanner.preview(project)) { "The preview changed. Review it again." }
        return generateSelected(project, preview, id, now)
    }
    internal fun generateSelected(project: Project, preview: MasterPreview, id: String, now: Instant,
                                  targetOverride: Path? = null, label: String = "Library Master",
                                  packageNames: Map<String, String>? = null,
                                  companionManifest: ((LibraryMaster) -> String)? = null): LibraryMaster {
        val full = MasterPlanner.preview(project)
        require(preview.projectId == project.id && preview.members.size == full.members.size)
        preview.members.forEach { m ->
            val actual = full.members.single { it.member.id == m.member.id }
            require(m.member == actual.member && m.choices.all { choice -> actual.choices.contains(choice) }) { "Selections changed. Review again." }
        }
        require(Regex("[A-Za-z0-9-]{1,64}").matches(id))
        require(preview.members.any { it.selectedCount > 0 }) { "No accepted charts are selected. Review the library first." }
        val destination = Path.of(LocalCharts.folder(Path.of(requireNotNull(project.exportDestination) { "Choose an export destination." })))
        val source = Path.of(LocalCharts.folder(Path.of(requireNotNull(project.catalog.source))))
        val selected = preview.members.flatMap { it.choices.mapNotNull { c -> c.chart } }.distinctBy { it.id }
        selected.forEach { require(it.source == project.catalog.source); verify(source, it) }
        preview.members.forEach { member ->
            val charts = member.choices.mapNotNull { it.chart }
            require(charts.sumOf { it.size } <= MAX_MEMBER) { "${member.member.name}'s package exceeds the 512 MiB source-data limit." }
            val names = charts.map { Path.of(it.relativePath).fileName.toString().lowercase() }
            require(names.distinct().size == names.size) { "${member.member.name} has colliding PDF filenames. Resolve them before export." }
        }
        val date = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss").withZone(ZoneOffset.UTC).format(now)
        val masterRoot = if (targetOverride == null) ExportFolders.child(ExportFolders.library(project), "Master") else destination
        if (targetOverride == null) {
            require(project.masters.none { it.id == id }) { "This master identity is already recorded." }
            val candidates = listOf(masterRoot) + Files.newDirectoryStream(masterRoot).use { children -> children.filter { Files.isDirectory(it, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(it) }.toList() }
            candidates.forEach { folder ->
                val manifest = folder.resolve("manifest.json")
                if (Files.isRegularFile(manifest, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(manifest)) {
                    val existing = MasterCodec.decode(Files.readString(manifest))
                    require(existing.id != id) { "This master identity already exists. Recover it instead." }
                }
            }
        }
        var masterTarget = masterRoot
        if (targetOverride == null && Files.newDirectoryStream(masterRoot).use { it.iterator().hasNext() }) {
            var revision = 2
            while (Files.exists(masterRoot.resolve("Revision $revision"))) revision++
            masterTarget = masterRoot.resolve("Revision $revision")
        }
        val target = targetOverride ?: masterTarget
        // First master publishes directly in Library/Master; reserve it only at publication.
        if (targetOverride == null && target == masterRoot) Files.delete(masterRoot)
        require(!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) { "This master folder already exists. Nothing was overwritten." }
        val stage = Files.createDirectory(target.parent.resolve(".bandlogger-pending-$id"))
        var published = false
        try {
            Files.writeString(stage.resolve("INCOMPLETE.txt"), "Incomplete export. Do not distribute this folder. Retry from band.logger; no master has been recorded.")
            val members = preview.members.mapIndexed { index, member ->
                val choices = member.choices.filter { it.chart != null }
                val warnings = member.warnings + member.choices.filter { it.state != SelectionState.PRIMARY }
                    .map { "${it.tune.title}: ${it.explanation}" }
                if (choices.isEmpty()) MasterMember(member.member.id, member.member.name, member.preferences,
                    warnings = warnings + "No accepted charts selected; no package generated.")
                else {
                    val title = packageNames?.get(member.member.id) ?: "${project.prefix.ifBlank { project.name }} $label - ${member.member.name} - $date"
                    val base = packageNames?.get(member.member.id) ?: "${(index + 1).toString().padStart(2, '0')} - ${safeLabel(project.name)} $label - ${safeLabel(member.member.name)} - $date"
                    val zipName = "$base.zip"
                    ZipOutputStream(Files.newOutputStream(stage.resolve(zipName), StandardOpenOption.CREATE_NEW)).use { zip ->
                        zip.putNextEntry(ZipEntry("$base.4ss"))
                        zip.write(ForScore.header(title).toByteArray(Charsets.UTF_8))
                        choices.forEach { choice ->
                            val chart = choice.chart!!
                            val pdf = bytes(source, chart)
                            zip.write(ForScore.scoreElement(ProofScore(choice.tune.title, Path.of(chart.relativePath).fileName.toString(), pdf)).toByteArray(Charsets.UTF_8))
                        }
                        zip.write(ForScore.footer.toByteArray(Charsets.UTF_8))
                        zip.closeEntry()
                        zip.putNextEntry(ZipEntry("READ-ME.txt"))
                        zip.write(("Save this ZIP locally on your iPad. Extract in Apple Files, then share the .4ss to forScore (14.3 or newer). Open Setlists and find: $title\n" +
                            "This embeds PDFs. Existing matching filenames may create additional copies; annotations are not automatically transferred.\n" +
                            "Selections: ${choices.size}. Review notes:\n" + warnings.joinToString("\n").ifEmpty { "None." }).toByteArray(Charsets.UTF_8))
                        zip.closeEntry()
                    }
                    if (companionManifest != null) {
                        val expected = setOf("$base.4ss", "READ-ME.txt")
                        val seen = mutableSetOf<String>()
                        java.util.zip.ZipInputStream(Files.newInputStream(stage.resolve(zipName))).use { zip ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                val entry = zip.nextEntry ?: break
                                require(!entry.isDirectory && entry.name in expected && seen.add(entry.name)) { "Invalid supplement ZIP entries." }
                                while (zip.read(buffer) >= 0) { /* sequential CRC-checked read, bounded memory */ }
                                zip.closeEntry()
                            }
                        }
                        require(seen == expected) { "Incomplete supplement ZIP." }
                    }
                    MasterMember(member.member.id, member.member.name, member.preferences, zipName, fileHash(stage.resolve(zipName)),
                        choices.map { choice -> val c = choice.chart!!
                            MasterItem(choice.tune.id, choice.tune.title, c.id, c.metadata!!.part, c.metadata.revision, c.source,
                                c.relativePath, Path.of(c.relativePath).fileName.toString(), c.sha256)
                        }, warnings)
                }
            }
            selected.forEach { verify(source, it) }
            val master = LibraryMaster(id, project.id, project.name, now.toString(), target.toString(), members,
                preview.notices + preview.issues.map { "${it.chart.relativePath}: ${it.reason}" })
            Files.writeString(stage.resolve("manifest.json"), MasterCodec.encode(master), StandardOpenOption.CREATE_NEW)
            companionManifest?.let { Files.writeString(stage.resolve("supplement.json"), it(master), StandardOpenOption.CREATE_NEW) }
            Files.delete(stage.resolve("INCOMPLETE.txt"))
            Files.move(stage, target)
            published = true
            return master
        } finally {
            if (!published) {
                // Only files created directly by this operation; never traverse source data.
                runCatching {
                    Files.newDirectoryStream(stage).use { children -> children.forEach { Files.deleteIfExists(it) } }
                    Files.deleteIfExists(stage)
                }
            }
        }
    }

    fun recover(project: Project, folder: String, allowSupplement: Boolean = false): LibraryMaster {
        val root = Path.of(LocalCharts.folder(Path.of(folder)))
        require(allowSupplement || !Files.exists(root.resolve("supplement.json"))) { "Use Recover supplement for this folder; it is not a Library Master." }
        require(!Files.exists(root.resolve("INCOMPLETE.txt"))) { "This folder contains an incomplete export." }
        val path = root.resolve("manifest.json")
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && Files.size(path) <= 16L * 1024 * 1024) { "No valid manifest in this folder." }
        val master = MasterCodec.decode(Files.readString(path))
        require(master.projectId == project.id) { "This master belongs to another project." }
        // Windows TEMP may use an 8.3 alias (RUNNER~1) while folder() returns its long path.
        // The recorded original must still exist and resolve to this exact directory.
        require(Path.of(master.folder).toRealPath() == root) { "Recover from the original export folder." }
        master.members.forEach { member ->
            member.zipName?.let { name ->
                require(name.none { it in "<>:\"/\\|?*" || it.code < 32 } && name.endsWith(".zip", true) && name != "..") { "Unsafe package name." }
                val zip = root.resolve(name)
                require(Files.isRegularFile(zip, LinkOption.NOFOLLOW_LINKS) && fileHash(zip) == member.zipSha256) { "Package missing or changed: $name" }
            }
            member.charts.forEach { c ->
                require(project.catalog.charts.any { it.id == c.chartId && it.tuneId == c.tuneId && it.sha256 == c.sha256 &&
                    it.metadata?.part == c.part && it.metadata.revision == c.revision }) { "The manifest does not match this project's catalog." }
            }
        }
        return master
    }
}