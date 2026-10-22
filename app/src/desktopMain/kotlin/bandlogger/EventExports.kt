package bandlogger

import java.nio.file.*
import java.security.MessageDigest

enum class EventConflict { NEW, REVISION, REPLACE }
data class ExportDiscovery(val verified: List<EventExportRecord>, val review: List<String>)

/** Deletes only manifest-proven packages. No recursive filesystem deletion is used. */
object EventExports {
    fun hash(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(65536)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    private fun normal(path: Path, directory: Boolean) {
        require(!Files.isSymbolicLink(path) && if (directory) Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) else Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) { "Unavailable or unsafe path: $path" }
    }
    private fun folder(record: EventExportRecord): Path {
        val root = Path.of(record.root).toAbsolutePath().normalize()
        normal(root, true)
        val path = Path.of(record.folder).toAbsolutePath().normalize()
        require(path.parent.parent == root && path.parent.fileName.toString() == FileLocations.eventTypeFolder(record.event.type)) { "Export path is outside the recorded event destination." }
        normal(path.parent, true)
        require(path.parent.toRealPath().parent == root.toRealPath())
        return path
    }
    private fun readRecord(path: Path): EventExportRecord {
        normal(path, false)
        val bytes = Files.newInputStream(path).use { it.readNBytes(4 * 1024 * 1024 + 1) }
        require(bytes.size <= 4 * 1024 * 1024) { "Event manifest is too large." }
        return EventRecordCodec.decode(bytes.decodeToString())
    }
    // Drive for desktop injects this exact hidden icon file after publication.
    // Unknown desktop.ini contents remain unrelated content and block cleanup.
    internal fun driveMetadata(path: Path): ByteArray? = runCatching {
        if (path.fileName.toString() != "desktop.ini") return null
        normal(path, false)
        val attrs = Files.readAttributes(path, java.nio.file.attribute.DosFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        if (!attrs.isHidden) return null
        val bytes = Files.newInputStream(path).use { it.readNBytes(4097) }
        if (bytes.size > 4096) return null
        val text = bytes.toString(Charsets.UTF_16LE).removePrefix("\uFEFF").replace("\r\n", "\n")
        val lines = text.trimEnd('\n').split('\n')
        if (lines.size != 8 || lines[0] != "[.ShellClassInfo]" || lines[1] != "ConfirmFileOp=0" ||
            !Regex("IconResource=[A-Za-z]:\\\\Program Files\\\\Google\\\\Drive File Stream\\\\[0-9.]+\\\\GoogleDriveFS\\.exe,27").matches(lines[2]) ||
            lines.drop(3) != listOf("[ViewState]", "Mode=", "Vid=", "FolderType=Generic", "Logo=")) return null
        bytes
    }.getOrNull()
    internal fun verified(record: EventExportRecord, allowMissing: Boolean = false): Path {
        record.validate()
        val path = folder(record); normal(path, true)
        require(path.toRealPath().parent == path.parent.toRealPath()) { "Export folder redirects outside its recorded parent." }
        require(readRecord(path.resolve("event-export.json")).identity() == record.identity()) { "Event manifest does not match its tracked identity." }
        val allowed = record.packages.map { it.filename }.toSet() + "event-export.json"
        Files.newDirectoryStream(path).use { children -> children.forEach { require(it.fileName.toString() in allowed || driveMetadata(it) != null) { "Unrelated file preserved: $it" }; normal(it, false) } }
        record.packages.forEach { pkg ->
            val file = path.resolve(pkg.filename)
            if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                normal(file, false)
                require(hash(file) == pkg.sha256) { "Changed package preserved: $file" }
            } else require(allowMissing) { "Missing package: $file" }
        }
        return path
    }
    @Synchronized
    fun publish(project: Project, plan: EventExportPlan, destination: String, missing: Boolean, undated: Boolean,
                conflict: EventConflict, save: (Project) -> Unit): EventExportResult {
        require(project.eventExports.none { it.event.id == plan.event.id && it.state == EventExportState.PENDING }) { "Recover the pending event export from Events before exporting again." }
        val previous = project.eventExports.filter { it.event.id == plan.event.id && it.state == EventExportState.ACTIVE }
        require(previous.isEmpty() || conflict != EventConflict.NEW) { "Choose Create revision or Replace tracked exports." }
        if (conflict == EventConflict.REPLACE) previous.forEach { verified(it) }
        var current = if (project.projectRoot != null) project.withExportRoot(destination) else project.copy(exportDestination = destination)
        val result = EventExporter.generate(project, plan, destination, missing, undated, revision = conflict != EventConflict.NEW) { pending ->
            current = current.recordEventExport(pending)
            save(current) // durable intent exists before the no-replacement publication move
        }
        current = current.recordEventExport(result.record.copy(state = EventExportState.ACTIVE))
        if (conflict == EventConflict.REPLACE) previous.forEach { current = current.recordEventExport(it.copy(state = EventExportState.CLEANUP)) }
        save(current) // if this fails PENDING record + manifest recover on restart
        if (conflict == EventConflict.REPLACE) current = cleanup(current, save)
        val unfinished = current.eventExports.filter { it.state == EventExportState.CLEANUP }
        return result.copy(warnings = result.warnings + unfinished.map { "Previous export cleanup remains pending: ${it.folder}. Retry from Events. ${it.error ?: ""}" })
    }
    fun recoverPending(project: Project, save: (Project) -> Unit): Project {
        var current = project
        project.eventExports.filter { it.state == EventExportState.PENDING }.forEach { record ->
            val path = folder(record)
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                verified(record)
                current = current.recordEventExport(record.copy(state = EventExportState.ACTIVE, error = null))
            } else {
                val stage = Path.of(record.stage).toAbsolutePath().normalize()
                require(stage.parent == path.parent && stage.fileName.toString() == ".bandlogger-event-${record.id}")
                require(!Files.exists(stage, LinkOption.NOFOLLOW_LINKS)) { "Incomplete staging folder retained for review: $stage" }
                current = current.recordEventExport(record.copy(state = EventExportState.DELETED, error = "Publication did not complete; no output exists."))
            }
            save(current)
        }
        return current
    }
    fun deleteEvent(project: Project, eventId: String, save: (Project) -> Unit): Project {
        require(project.events.any { it.id == eventId }) { "Event no longer exists." }
        require(project.eventExports.none { it.event.id == eventId && it.state == EventExportState.PENDING }) { "Recover pending exports before deleting this event." }
        var current = project.copy(events = project.events.filterNot { it.id == eventId }, eventExports = project.eventExports.map {
            if (it.event.id == eventId && it.state == EventExportState.ACTIVE) it.copy(state = EventExportState.CLEANUP) else it
        })
        save(current) // tombstone with original event + exact paths survives all cleanup failures
        current = cleanup(current, save)
        return current
    }
    fun cleanup(project: Project, save: (Project) -> Unit, remove: (Path) -> Unit = { Files.delete(it); Unit }): Project {
        var current = project
        project.eventExports.filter { it.state == EventExportState.CLEANUP }.forEach { original ->
            var record = original
            try {
                val path = folder(record) // unavailable root is a failure, never successful absence
                if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                    if (Files.exists(path.resolve("event-export.json"), LinkOption.NOFOLLOW_LINKS)) {
                        verified(record, allowMissing = true) // preflight every remaining file before any removal
                        val metadataPath = path.resolve("desktop.ini")
                        if (Files.exists(metadataPath, LinkOption.NOFOLLOW_LINKS)) {
                            val bytes = requireNotNull(driveMetadata(metadataPath)) { "Unknown desktop.ini preserved." }
                            val encoded = java.util.Base64.getEncoder().encodeToString(bytes)
                            require(record.providerMetadata["desktop.ini"] == null || record.providerMetadata["desktop.ini"] == encoded) { "Provider metadata changed; preserved for review." }
                            record = record.copy(providerMetadata = record.providerMetadata + ("desktop.ini" to encoded))
                            current = current.recordEventExport(record); save(current) // immutable bytes retained before removing provider decoration
                        }
                        record.packages.forEach { pkg ->
                            val file = path.resolve(pkg.filename)
                            if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                                normal(file, false); require(hash(file) == pkg.sha256)
                                remove(file)
                            }
                            record = record.copy(removedFiles = (record.removedFiles + pkg.filename).distinct(), error = null)
                            current = current.recordEventExport(record); save(current)
                        }
                        // Persist full file progress before deleting the manifest.
                        if (Files.exists(metadataPath, LinkOption.NOFOLLOW_LINKS)) {
                            val metadata = requireNotNull(driveMetadata(metadataPath))
                            require(java.util.Base64.getEncoder().encodeToString(metadata) == record.providerMetadata["desktop.ini"])
                            remove(metadataPath)
                        }
                        remove(path.resolve("event-export.json"))
                    } else require(record.removedFiles.toSet() == record.packages.map { it.filename }.toSet()) { "Missing identity manifest; folder preserved for review." }
                    normal(path, true)
                    require(Files.newDirectoryStream(path).use { !it.iterator().hasNext() }) { "Unrelated content preserved in $path" }
                    remove(path)
                }
                record = record.copy(state = EventExportState.DELETED, error = null)
            } catch (e: Exception) { record = record.copy(error = "Cleanup incomplete: ${e.message}") }
            current = current.recordEventExport(record); save(current)
        }
        return current
    }
    /** Read-only bounded scan of configured roots; names alone never authorize association. */
    fun discover(project: Project): ExportDiscovery {
        val found = mutableListOf<EventExportRecord>(); val review = mutableListOf<String>()
        val roots = (listOfNotNull(project.exportDestination, project.eventsExportRoot) + project.projectRoot?.let { listOf(Path.of(it).resolve("02 - Repertoire Distributions").toString(), Path.of(it).resolve("03 - Events").toString(), Path.of(it).resolve("03 - Events/Gigs/Events").toString(), Path.of(it).resolve("06 - Testing").toString()) }.orEmpty()).distinct()
        roots.forEach { configured ->
            val configuredRoot = Path.of(configured).toAbsolutePath().normalize()
            if (!Files.isDirectory(configuredRoot)) { review += "Destination unavailable: $configuredRoot"; return@forEach }
            // Compare the same filesystem spelling used by publishers, including Windows 8.3 aliases.
            val root = configuredRoot.toRealPath()
            listOf(root, root.resolve("Events"), root.resolve("Gigs"), root.resolve("Rehearsals")).filter { Files.isDirectory(it, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(it) }.forEach { parent ->
                Files.newDirectoryStream(parent).use { children -> children.take(1000).filter { Files.isDirectory(it, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(it) }.forEach { path ->
                    if (project.eventExports.none { runCatching { Path.of(it.folder).toRealPath() == path.toRealPath() }.getOrDefault(false) }) {
                        val manifest = path.resolve("event-export.json")
                        if (Files.exists(manifest, LinkOption.NOFOLLOW_LINKS)) {
                            runCatching {
                                val record = readRecord(manifest)
                                require(record.projectId == project.id && project.events.any { it.id == record.event.id })
                                require(Path.of(record.folder).toRealPath() == path.toRealPath())
                                verified(record)
                                found += record.copy(state = EventExportState.ACTIVE)
                            }.onFailure { review += "$path — identity/package verification failed: ${it.message}" }
                        } else if (parent != root || path.fileName.toString().startsWith("20") || path.fileName.toString().startsWith("Undated")) {
                            if (path.fileName.toString() !in listOf("Library", "Supplements", "Gigs", "Rehearsals", "Events")) review += "$path — no verifiable event ID; review manually, left untouched"
                        }
                    }
                } }
            }
        }
        return ExportDiscovery(found.distinctBy { it.id }, review.distinct())
    }
}
