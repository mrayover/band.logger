package bandlogger

import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.Base64
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import kotlin.test.*

class MasterExporterTest {
    private data class Fixture(val root: Path, val source: Path, val destination: Path, val project: Project)
    private fun fixture(): Fixture {
        val root = Files.createTempDirectory("master-test")
        val source = Files.createDirectory(root.resolve("charts"))
        val destination = Files.createDirectory(root.resolve("exports"))
        Files.write(source.resolve("Q Alpha & One -r00- C.pdf"), syntheticPdf())
        Files.write(source.resolve("Q Beta -r00- C.pdf"), syntheticPdf() + "\n% concert".toByteArray())
        Files.write(source.resolve("Q Beta -r02- Bb.pdf"), syntheticPdf() + "\n% Bb".toByteArray())
        val catalog = Catalog(source = source.toRealPath().toString()).reconcile(LocalCharts.scan(source.toString()), "Q", 1, ::newId).acceptNew()
        val project = Project("project", "Band & friends", "Q", members = listOf(
            Member("c", "Concert", emptyList(), listOf("C")),
            Member("bb", "Sax & <lead>", emptyList(), listOf("Bb", "C")),
            Member("drums", "Drums", emptyList(), listOf("Drums"))
        ), catalog = catalog, exportDestination = destination.toString())
        return Fixture(root, source, destination, project)
    }
    private fun generate(f: Fixture) = MasterExporter.generate(f.project, MasterPlanner.preview(f.project))
    private fun children(path: Path) = Files.list(path).use { it.toList() }

    @Test fun memberZipsRoundTripBytesOrderEscapingAndManifest() {
        val f = fixture()
        val before = LocalCharts.scan(f.source.toString())
        val master = generate(f)
        assertEquals(2, master.members.count { it.zipName != null })
        assertNull(master.members.last().zipName)
        assertTrue(master.members.last().warnings.any { it.contains("no package") })
        master.members.filter { it.zipName != null }.forEach { member ->
            ZipFile(Path.of(master.folder).resolve(member.zipName!!).toFile()).use { zip ->
                val entry = zip.entries().toList().single { it.name.endsWith(".4ss") }
                val xml = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                val factory = DocumentBuilderFactory.newInstance()
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                val doc = factory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
                assertTrue(doc.documentElement.getAttribute("title").contains(member.memberName))
                val scores = doc.getElementsByTagName("score")
                assertEquals(member.charts.size, scores.length)
                member.charts.forEachIndexed { index, chart ->
                    val score = scores.item(index) as org.w3c.dom.Element
                    assertEquals(chart.title, score.getAttribute("title"))
                    assertEquals(chart.filename, score.getAttribute("path"))
                    assertContentEquals(Files.readAllBytes(f.source.resolve(chart.relativePath)), Base64.getDecoder().decode(score.getAttribute("data")))
                }
                assertNotNull(zip.getEntry("READ-ME.txt"))
            }
        }
        assertEquals(listOf("C", "Bb"), master.members[1].charts.map { it.part })
        assertEquals(before, LocalCharts.scan(f.source.toString()))
        assertEquals(master, MasterCodec.decode(Files.readString(Path.of(master.folder).resolve("manifest.json"))))
        assertEquals(master, MasterExporter.recover(f.project, master.folder))
        assertEquals(1, children(f.destination).size)
    }
    @Test fun changedAndMissingSourceFailWithoutPublication() {
        val f = fixture()
        val file = f.source.resolve("Q Alpha & One -r00- C.pdf")
        Files.write(file, syntheticPdf() + "changed".toByteArray())
        assertFails { generate(f) }
        assertTrue(children(f.destination).isEmpty())
        Files.delete(file)
        assertFails { generate(f) }
        assertTrue(children(f.destination).isEmpty())
        assertTrue(f.project.masters.isEmpty())
    }
    @Test fun collisionDoesNotOverwriteAnExistingMaster() {
        val f = fixture()
        val now = Instant.parse("2026-10-21T03:00:00Z")
        val preview = MasterPlanner.preview(f.project)
        val first = MasterExporter.generate(f.project, preview, "fixed-id", now)
        val bytes = Files.readAllBytes(Path.of(first.folder).resolve(first.members.first().zipName!!))
        assertFails { MasterExporter.generate(f.project, preview, "fixed-id", now) }
        assertContentEquals(bytes, Files.readAllBytes(Path.of(first.folder).resolve(first.members.first().zipName!!)))
    }
    @Test fun failureAfterFirstMemberCleansPartialFilesAndRecordsNothing() {
        val f = fixture()
        val broken = f.project.copy(members = f.project.members.mapIndexed { i, m -> if (i == 1) m.copy(name = "bad\u0001name") else m })
        assertFails { MasterExporter.generate(broken, MasterPlanner.preview(broken)) }
        Files.walk(f.destination).use { paths -> assertFalse(paths.anyMatch { it.fileName.toString().endsWith(".zip") || it.fileName.toString() == "manifest.json" }) }
        assertTrue(broken.masters.isEmpty())
    }
    @Test fun completedFilesSurviveMetadataFailureAndRecoveryIsIdempotent() {
        val f = fixture()
        val master = generate(f)
        val blocker = f.root.resolve("not-a-directory")
        Files.writeString(blocker, "blocking fixture")
        assertFails { FileWorkspaceStore(blocker.resolve("workspace.json")).save(Workspace().saveProject(f.project.recordMaster(master))) }
        val recovered = MasterExporter.recover(f.project, master.folder)
        val recorded = f.project.recordMaster(recovered).recordMaster(recovered)
        assertEquals(1, recorded.masters.size)
        val path = f.root.resolve("workspace.json")
        FileWorkspaceStore(path).save(Workspace().saveProject(recorded))
        assertEquals(master, FileWorkspaceStore(path).load().projects.single().masters.single())
    }
    @Test fun recoveryRejectsOtherProjectsAndTamperedPackages() {
        val f = fixture()
        val master = generate(f)
        assertFails { MasterExporter.recover(f.project.copy(id = "different"), master.folder) }
        Files.writeString(Path.of(master.folder).resolve(master.members.first().zipName!!), "corrupted")
        assertFails { MasterExporter.recover(f.project, master.folder) }
    }
    @Test fun stalePreviewUnsafePathsAndNoSelectionsAreRejected() {
        val f = fixture()
        val preview = MasterPlanner.preview(f.project)
        assertFails { MasterExporter.generate(f.project.copy(members = emptyList()), preview) }
        val unsafe = f.project.copy(catalog = f.project.catalog.copy(charts = f.project.catalog.charts.map { it.copy(relativePath = "../outside.pdf") }))
        assertFails { MasterExporter.generate(unsafe, MasterPlanner.preview(unsafe)) }
        val empty = f.project.copy(members = emptyList())
        assertFails { MasterExporter.generate(empty, MasterPlanner.preview(empty)) }
        assertTrue(children(f.destination).isEmpty())
    }
    @Test fun duplicatePdfBasenamesAcrossTunesAreRejected() {
        val f = fixture()
        val first = f.project.catalog.charts.first()
        val dir = Files.createDirectory(f.source.resolve("other"))
        val filename = Path.of(first.relativePath).fileName.toString()
        Files.copy(f.source.resolve(first.relativePath), dir.resolve(filename))
        val alternateTune = f.project.catalog.tunes.first { it.id != first.tuneId }
        val duplicate = first.copy(id = "duplicate", relativePath = "other/$filename", tuneId = alternateTune.id,
            metadata = first.metadata!!.copy(title = alternateTune.title))
        val p = f.project.copy(catalog = f.project.catalog.copy(charts = listOf(first, duplicate)))
        assertFails { MasterExporter.generate(p, MasterPlanner.preview(p)) }
        assertTrue(children(f.destination).isEmpty())
    }
    @Test fun schemaTwoMigrationPreservesDestinationAndImmutableBackup() {
        val f = fixture()
        val path = f.root.resolve("workspace.json")
        val old = WorkspaceCodec.encode(Workspace().saveProject(f.project)).replace("\"schemaVersion\": 10", "\"schemaVersion\": 2")
        Files.writeString(path, old)
        val store = FileWorkspaceStore(path)
        val loaded = store.load()
        assertEquals(10, loaded.schemaVersion)
        assertEquals(f.project, loaded.projects.single())
        store.save(loaded)
        store.save(loaded.saveProject(f.project.copy(name = "Renamed")))
        assertEquals(old, Files.readString(f.root.resolve("workspace.pre-masters.backup.json")))
    }

    @Test fun canonicalRecoveryStillRejectsCopiedOrMissingOriginalFolders() {
        val f = fixture(); val master = generate(f)
        val original = Path.of(master.folder)
        val copied = Files.createDirectory(f.root.resolve("copied-master"))
        Files.newDirectoryStream(original).use { files -> files.forEach { Files.copy(it,copied.resolve(it.fileName)) } }
        val manifestBefore = Files.readAllBytes(copied.resolve("manifest.json"))
        val failure = assertFailsWith<IllegalArgumentException> { MasterExporter.recover(f.project,copied.toString()) }
        assertEquals("Recover from the original export folder.",failure.message)
        val renamed = original.resolveSibling("temporarily-unavailable-master")
        Files.move(original,renamed)
        assertFailsWith<java.nio.file.NoSuchFileException> { MasterExporter.recover(f.project,copied.toString()) }
        Files.move(renamed,original)
        assertEquals(master,MasterExporter.recover(f.project,original.toString()))
        assertContentEquals(manifestBefore,Files.readAllBytes(copied.resolve("manifest.json")))
    }
}