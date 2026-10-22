package bandlogger

import java.nio.file.*
import java.nio.channels.FileChannel
import java.nio.file.StandardCopyOption.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable data class LayoutMove(
    val kind: String, val identity: String, val source: String, val target: String,
    val originalHashes: Map<String,String>, val targetHashes: Map<String,String>, val rewritten: Map<String,String>,
    val sourceDirectories: List<String> = emptyList(), val targetDirectories: List<String> = emptyList()
)
@Serializable data class LayoutJournal(
    val projectId: String, val workspace: String, val root: String, val beforeHash: String,
    val afterWorkspace: String, val moves: List<LayoutMove>, val phase: String = "PREPARED"
)

/** Copy/verify -> persist references -> verified direct-file cleanup. Journals/backups are local. */
object DriveLayoutMigration {
    private val json = Json { prettyPrint = true; encodeDefaults = true }
    private fun hash(path: Path): String {
        val digest=java.security.MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path,LinkOption.NOFOLLOW_LINKS).use { input ->
            val buffer=ByteArray(65536)
            while(true) { val count=input.read(buffer);if(count<0)break;digest.update(buffer,0,count) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    private fun normalDirectory(path: Path): Path {
        require(Files.isDirectory(path,LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)) { "Unavailable or redirected migration folder: $path" }
        return path.toRealPath()
    }
    private fun writeJournal(path: Path, value: LayoutJournal) {
        val temp = Files.createTempFile(path.parent,"layout-",".tmp")
        try {
            Files.writeString(temp,json.encodeToString(value))
            try { Files.move(temp,path,ATOMIC_MOVE,REPLACE_EXISTING) }
            catch (_: AtomicMoveNotSupportedException) { Files.move(temp,path,REPLACE_EXISTING) }
        } finally { Files.deleteIfExists(temp) }
    }
    private fun inventory(folder: Path, allowed: Set<String>, directories: List<String> = emptyList()): Map<String,String> {
        normalDirectory(folder)
        return Files.newDirectoryStream(folder).use { children -> children.mapNotNull { file ->
            val name=file.fileName.toString()
            if (Files.isDirectory(file,LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(file) && name in directories) return@mapNotNull null
            require(Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(file)) { "Non-file preserved: $file" }
            require(name in allowed || EventExports.driveMetadata(file) != null) { "Unrelated file preserved: $file" }
            name to hash(file)
        }.toMap() }
    }
    private fun check(folder: Path, expected: Map<String,String>, missingAllowed: Boolean = false, directories: List<String> = emptyList()) {
        val actual=inventory(folder,expected.keys,directories)
        actual.forEach { (name,value) ->
            if(name in expected) require(value==expected[name]) { "Changed migration file preserved: ${folder.resolve(name)}" }
        }
        // Drive can lag directory enumeration after a write. Verify expected bytes by
        // opening each named file sequentially; listings only identify unrelated content.
        expected.forEach { (name,sha) ->
            val file=folder.resolve(name)
            if(!missingAllowed || Files.exists(file,LinkOption.NOFOLLOW_LINKS)) require(hash(file)==sha) { "Migration file bytes changed: $file" }
        }
    }
    private fun <T> locked(workspace: Path, action: ()->T): T =
        FileChannel.open(workspace.resolveSibling("workspace.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE).use { channel ->
            requireNotNull(channel.tryLock()) { "Close band.logger before migrating its workspace." }.use { action() }
        }
    fun prepare(workspace: Path, desired: Workspace, projectId: String, rootPath: Path, journalDirectory: Path): Path =
        locked(workspace) { prepareUnlocked(workspace,desired,projectId,rootPath,journalDirectory) }
    private fun prepareUnlocked(workspace: Path, desired: Workspace, projectId: String, rootPath: Path, journalDirectory: Path): Path {
        val root=normalDirectory(rootPath)
        require(FileLocations.numberedRoot(root.toString())==root)
        require(!Files.exists(journalDirectory)) { "Resume the existing journal instead of preparing another migration." }
        val beforeBytes=Files.readAllBytes(workspace)
        val beforeHash=CoverGenerator.sha256(beforeBytes)
        val before=WorkspaceCodec.decode(beforeBytes.toString(Charsets.UTF_8))
        require(before.projects.any { it.id==projectId })
        val p=desired.projects.single { it.id==projectId }
        require(desired.projects.filterNot { it.id==projectId }==before.projects.filterNot { it.id==projectId }) { "Other projects must remain unchanged." }
        require(p.catalog.source==before.projects.single { it.id==projectId }.catalog.source) { "Migration cannot change the authoritative chart source." }
        require(p.eventExports.none { it.state in listOf(EventExportState.PENDING,EventExportState.CLEANUP) }) { "Recover pending exports/cleanup before migration." }
        // Source/catalog identities are verified before preparing any directory changes.
        val library=normalDirectory(Path.of(requireNotNull(p.catalog.source)))
        p.catalog.charts.filter { it.present && it.current && it.source==p.catalog.source }.forEach { chart ->
            val relative=Path.of(chart.relativePath)
            require(!relative.isAbsolute && relative.none { it.toString()==".." })
            val file=library.resolve(relative)
            require(Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(file) && file.toRealPath().startsWith(library) && hash(file)==chart.sha256) { "Chart source changed; migration stopped: $file" }
        }
        val sourceFolders=p.masters.map { Path.of(it.folder).toRealPath() } + p.supplements.map { Path.of(it.publication.folder).toRealPath() } + p.eventExports.filter { it.state==EventExportState.ACTIVE }.map { Path.of(it.folder).toRealPath() }
        val dist=root.resolve("02 - Repertoire Distributions")
        val targetFolders=p.masters.mapIndexed { index,_ -> if(index==0) dist.resolve("Master") else dist.resolve("Master/Revision ${index+1}") } +
            p.supplements.map { dist.resolve("Supplements/Supplement ${it.number.toString().padStart(3,'0')}") } +
            p.eventExports.filter { it.state==EventExportState.ACTIVE }.map { record -> root.resolve(if(p.events.single { it.id==record.event.id }.isTest) "06 - Testing" else "03 - Events").resolve(FileLocations.eventTypeFolder(record.event.type)).resolve(Path.of(record.folder).fileName) }
        val moves=mutableListOf<LayoutMove>()
        fun relocate(kind: String,id: String,sourceText: String,targetPath: Path,allowed: Set<String>,rewritten: Map<String,String>): String {
            val source=normalDirectory(Path.of(sourceText));val target=targetPath.toAbsolutePath().normalize()
            require(source.startsWith(root) && target.startsWith(root) && source!=root && target!=root)
            if(source==target) return target.toString()
            require(!target.startsWith(source) && !source.startsWith(target)) { "Overlapping migration paths." }
            if(Files.exists(target,LinkOption.NOFOLLOW_LINKS)) {
                normalDirectory(target)
                require(Files.newDirectoryStream(target).use { !it.iterator().hasNext() }) { "Existing target preserved: $target" }
            }
            val sourceDirs=sourceFolders.filter { it.parent==source }.map { it.fileName.toString() }
            val targetDirs=targetFolders.filter { it.parent==target }.map { it.fileName.toString() }
            val original=inventory(source,allowed,sourceDirs)
            require(allowed.all { it in original }) { "Missing owned export files in $source" }
            val targetHashes=original.filterKeys { it!="desktop.ini" }.toMutableMap()
            rewritten.forEach { (name,text) -> targetHashes[name]=CoverGenerator.sha256(text.toByteArray(Charsets.UTF_8)) }
            moves+=LayoutMove(kind,id,source.toString(),target.toString(),original,targetHashes,rewritten,sourceDirs,targetDirs)
            return target.toString()
        }
        val masters=p.masters.mapIndexed { index,master ->
            require(MasterExporter.recover(p,master.folder)==master)
            val target=if(index==0) dist.resolve("Master") else dist.resolve("Master/Revision ${index+1}")
            val updated=master.copy(folder=target.toString())
            updated.copy(folder=relocate("MASTER",master.id,master.folder,target,master.members.mapNotNull { it.zipName }.toSet()+"manifest.json",mapOf("manifest.json" to MasterCodec.encode(updated))))
        }
        val supplements=p.supplements.map { supplement ->
            require(SupplementExporter.recover(p,supplement.publication.folder)==supplement)
            val target=dist.resolve("Supplements/Supplement ${supplement.number.toString().padStart(3,'0')}")
            val updated=supplement.copy(publication=supplement.publication.copy(folder=target.toString()))
            val path=relocate("SUPPLEMENT",supplement.id,supplement.publication.folder,target,
                supplement.publication.members.mapNotNull { it.zipName }.toSet()+setOf("manifest.json","supplement.json"),
                mapOf("manifest.json" to MasterCodec.encode(updated.publication),"supplement.json" to SupplementCodec.encode(updated)))
            updated.copy(publication=updated.publication.copy(folder=path))
        }
        val events=p.eventExports.map { record ->
            if(record.state==EventExportState.DELETED) record else {
                EventExports.verified(record)
                val test=p.events.single { it.id==record.event.id }.isTest
                val parent=root.resolve(if(test) "06 - Testing" else "03 - Events")
                val target=parent.resolve(FileLocations.eventTypeFolder(record.event.type)).resolve(Path.of(record.folder).fileName)
                val updated=record.copy(root=parent.toString(),folder=target.toString(),stage=target.parent.resolve(".bandlogger-event-${record.id}").toString())
                updated.copy(folder=relocate("EVENT",record.id,record.folder,target,record.packages.map { it.filename }.toSet()+"event-export.json",mapOf("event-export.json" to EventRecordCodec.encode(updated))))
            }
        }
        val updated=p.withExportRoot(root.toString()).copy(masters=masters,supplements=supplements,eventExports=events)
        val after=desired.copy(projects=desired.projects.map { if(it.id==projectId) updated else it })
        after.validate()
        require(moves.map { it.target.lowercase() }.distinct().size==moves.size)
        Files.createDirectories(journalDirectory)
        Files.write(journalDirectory.resolve("workspace.before.json"),beforeBytes,StandardOpenOption.CREATE_NEW)
        moves.forEachIndexed { index,move ->
            val backup=Files.createDirectories(journalDirectory.resolve("exports/$index"))
            move.originalHashes.forEach { (name,sha) ->
                val original=Path.of(move.source).resolve(name)
                Files.copy(original,backup.resolve(name))
                require(hash(original)==sha && hash(backup.resolve(name))==sha) { "Backup verification failed." }
            }
        }
        require(hash(workspace)==beforeHash) { "Workspace changed during preparation; no exports were moved." }
        val journal=LayoutJournal(projectId,workspace.toAbsolutePath().toString(),root.toString(),beforeHash,WorkspaceCodec.encode(after),moves)
        val path=journalDirectory.resolve("journal.json");writeJournal(path,journal)
        return path
    }
    private fun verifyDestinations(journal: LayoutJournal, expected: Workspace) {
        journal.moves.forEach { check(Path.of(it.target),it.targetHashes,directories=it.targetDirectories) }
        val p=expected.projects.single { it.id==journal.projectId }
        p.masters.forEach { require(MasterExporter.recover(p,it.folder)==it) }
        p.supplements.forEach { require(SupplementExporter.recover(p,it.publication.folder)==it) }
        p.eventExports.filter { it.state==EventExportState.ACTIVE }.forEach { EventExports.verified(it) }
    }
    fun resume(journalPath: Path, checkpoint: (String)->Unit = {}): Workspace {
        val journal=json.decodeFromString<LayoutJournal>(Files.readString(journalPath))
        return locked(Path.of(journal.workspace)) { resumeUnlocked(journalPath,checkpoint) }
    }
    private fun resumeUnlocked(journalPath: Path, checkpoint: (String)->Unit): Workspace {
        var journal=json.decodeFromString<LayoutJournal>(Files.readString(journalPath))
        // Journals prepared before the Drive decoration adjustment remain resumable.
        // The source/backup hash stays strict; a provider icon file is not package payload.
        if(journal.moves.any { "desktop.ini" in it.targetHashes }) {
            journal=journal.copy(moves=journal.moves.map { it.copy(targetHashes=it.targetHashes-"desktop.ini") })
            writeJournal(journalPath,journal)
        }
        val workspace=Path.of(journal.workspace);val store=FileWorkspaceStore(workspace)
        val expected=WorkspaceCodec.decode(journal.afterWorkspace)
        val root=normalDirectory(Path.of(journal.root))
        if(journal.phase=="DONE") {
            val current=store.load()
            require(current.projects.single { it.id==journal.projectId }.projectRoot==root.toString())
            ExportFolders.library(current.projects.single { it.id==journal.projectId })
            verifyDestinations(journal,current);return current
        }
        if(journal.phase in listOf("PREPARED","COPIED")) {
            require(hash(workspace)==journal.beforeHash || Files.readString(workspace)==journal.afterWorkspace) { "Workspace changed; preserve journal and review before retrying." }
            FileLocations.numberedFolders.forEach { ExportFolders.child(root,it) }
            ExportFolders.library(expected.projects.single { it.id==journal.projectId })
            journal.moves.forEachIndexed { index,move ->
                val source=Path.of(move.source);val target=Path.of(move.target)
                require(source.toAbsolutePath().normalize().startsWith(root) && target.toAbsolutePath().normalize().startsWith(root))
                check(source,move.originalHashes,directories=move.sourceDirectories)
                if(Files.exists(target) && Files.newDirectoryStream(target).use { it.iterator().hasNext() }) check(target,move.targetHashes,directories=move.targetDirectories)
                else {
                    // Only descendants of the checked root; never follow a redirected parent.
                    var parent=root
                    root.relativize(target.parent).forEach { parent=ExportFolders.child(parent,it.toString()) }
                    val stage=target.parent.resolve(".bandlogger-layout-${journalPath.parent.fileName}-$index")
                    if(!Files.exists(stage)) Files.createDirectory(stage)
                    inventory(stage,move.targetHashes.keys)
                    move.targetHashes.forEach { (name,_) ->
                        val file=stage.resolve(name)
                        if(!Files.exists(file)) {
                            val rewritten=move.rewritten[name]
                            if(rewritten!=null) Files.writeString(file,rewritten,StandardOpenOption.CREATE_NEW)
                            else Files.copy(source.resolve(name),file)
                        }
                        require(hash(file)==move.targetHashes[name]) { "Staging bytes changed: $file" }
                    }
                    check(stage,move.targetHashes)
                    if(Files.exists(target)) { normalDirectory(target); require(Files.newDirectoryStream(target).use { !it.iterator().hasNext() });Files.delete(target) }
                    Files.move(stage,target)
                    check(target,move.targetHashes,directories=move.targetDirectories)
                }
                checkpoint("copied:$index")
            }
            verifyDestinations(journal,expected)
            journal=journal.copy(phase="COPIED");writeJournal(journalPath,journal)
            checkpoint("before-save")
            if(Files.readString(workspace)!=journal.afterWorkspace) store.save(expected)
            require(store.load()==expected)
            journal=journal.copy(phase="RECORDED");writeJournal(journalPath,journal)
            checkpoint("after-save")
        }
        require(journal.phase=="RECORDED")
        require(store.load()==expected) { "Workspace changed after migration; retain originals for review." }
        verifyDestinations(journal,expected)
        journal.moves.withIndex().sortedByDescending { Path.of(it.value.source).nameCount }.forEach { (index,move) ->
            val source=Path.of(move.source)
            if(Files.exists(source,LinkOption.NOFOLLOW_LINKS)) {
                check(source,move.originalHashes,missingAllowed=true,directories=move.sourceDirectories)
                move.originalHashes.forEach { (name,sha) ->
                    val file=source.resolve(name)
                    if(Files.exists(file,LinkOption.NOFOLLOW_LINKS)) { require(hash(file)==sha);Files.delete(file) }
                }
                // A provider may add a known decoration after the original inventory.
                Files.newDirectoryStream(source).use { children -> children.forEach { file ->
                    val bytes=requireNotNull(EventExports.driveMetadata(file)) { "Unrelated source content preserved: $file" }
                    val backup=journalPath.parent.resolve("provider-$index-${file.fileName}")
                    if(!Files.exists(backup)) Files.write(backup,bytes,StandardOpenOption.CREATE_NEW)
                    require(Files.readAllBytes(backup).contentEquals(bytes));Files.delete(file)
                } }
                require(Files.newDirectoryStream(source).use { !it.iterator().hasNext() });Files.delete(source)
            }
            checkpoint("cleaned:$index")
        }
        // Remove only now-empty legacy containers, never a numbered root or unrelated files.
        journal.moves.forEach { move ->
            var parent=Path.of(move.source).parent
            while(parent!=null && parent.startsWith(root) && parent!=root && parent.fileName.toString() !in FileLocations.numberedFolders) {
                if(!Files.isDirectory(parent,LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(parent)) break
                val children=Files.newDirectoryStream(parent).use { it.toList() }
                val ownedMetadata=children.all { file -> EventExports.driveMetadata(file)!=null ||
                    (file.fileName.toString()==".bandlogger-project-id" && Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(file) && Files.size(file)<1024 && Files.readString(file)==journal.projectId) }
                if(!ownedMetadata) break
                val backup=Files.createDirectories(journalPath.parent.resolve("containers/"+CoverGenerator.sha256(parent.toString().toByteArray()).take(20)))
                children.forEach { file ->
                    val copy=backup.resolve(file.fileName)
                    if(!Files.exists(copy)) Files.copy(file,copy)
                    require(hash(copy)==hash(file));Files.delete(file)
                }
                require(Files.newDirectoryStream(parent).use { !it.iterator().hasNext() })
                Files.delete(parent);parent=parent.parent
            }
        }
        verifyDestinations(journal,store.load())
        journal=journal.copy(phase="DONE");writeJournal(journalPath,journal)
        return store.load()
    }
}
