package bandlogger

import kotlin.test.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile

class LocalChartsTest {
    @Test fun scanAndImportPreserveAllSourceBytes() {
        val root = Files.createTempDirectory("charts")
        val incoming = Files.createTempDirectory("incoming").resolve("external.pdf")
        val pdf = syntheticPdf()
        Files.write(incoming, pdf)
        val existing = root.resolve("Q Existing -r00- C.pdf")
        Files.write(existing, pdf)
        val imported = LocalCharts.importPdf(incoming, root.toString(), "Q New -r00- Bb.pdf")
        assertEquals(2, LocalCharts.scan(root.toString()).size)
        assertContentEquals(pdf, Files.readAllBytes(incoming))
        assertContentEquals(pdf, Files.readAllBytes(existing))
        assertContentEquals(pdf, Files.readAllBytes(imported))
        assertFails { LocalCharts.importPdf(incoming, root.toString(), "Q Existing -r00- C.pdf") }
        assertContentEquals(pdf, Files.readAllBytes(existing))
        Files.list(root).use { assertEquals(2L, it.count()) }
    }
    @Test fun nestedFilesAndExternallyAddedFilesAreDiscovered() {
        val root = Files.createTempDirectory("nested-charts")
        val sub = Files.createDirectory(root.resolve("parts"))
        Files.write(sub.resolve("Q Tune -r00- C.PDF"), syntheticPdf())
        Files.writeString(root.resolve("notes.txt"), "Ignored")
        val first = LocalCharts.scan(root.toString())
        assertEquals("parts/Q Tune -r00- C.PDF", first.single().relativePath)
        Files.write(root.resolve("Q Added -r00- Bb.pdf"), syntheticPdf())
        assertEquals(2, LocalCharts.scan(root.toString()).size)
    }
    @Test fun unavailableFolderCannotEraseSavedCatalog() {
        val root = Files.createTempDirectory("unavailable-charts")
        Files.write(root.resolve("Q Tune -r00- C.pdf"), syntheticPdf())
        val c = Catalog(source = root.toString()).reconcile(LocalCharts.scan(root.toString()), "Q", 1, ::newId)
        val store = FileWorkspaceStore(Files.createTempDirectory("catalog-store").resolve("workspace.json"))
        val state = Workspace().saveProject(Project("q", "Q", catalog = c))
        store.save(state)
        assertFails { LocalCharts.scan(root.resolve("offline").toString()) }
        assertEquals(state, store.load())
    }
    @Test fun unsafeImportsAndNonPdfsAreRejected() {
        val root = Files.createTempDirectory("safe-import")
        val src = Files.createTempFile("not-pdf", ".pdf")
        Files.writeString(src, "Not a PDF")
        listOf("../outside.pdf", "CON.pdf", "C:\\outside.pdf", "normal.pdf").forEach {
            assertFails { LocalCharts.importPdf(src, root.toString(), it) }
        }
        assertTrue(LocalCharts.scan(root.toString()).isEmpty())
    }
    @Test fun invalidPdfIsVisibleForReviewInsteadOfDropped() {
        val root = Files.createTempDirectory("invalid-chart")
        Files.writeString(root.resolve("Q Invalid -r00- C.pdf"), "oops")
        assertNotNull(LocalCharts.scan(root.toString()).single().error)
    }
    @Test fun changedFilesGetDifferentChecksums() {
        val root = Files.createTempDirectory("changed-chart")
        val file = root.resolve("Q Tune -r00- C.pdf")
        Files.write(file, syntheticPdf())
        val first = LocalCharts.scan(root.toString()).single()
        Files.write(file, syntheticPdf() + "\n% revised".toByteArray())
        val second = LocalCharts.scan(root.toString()).single()
        assertNotEquals(first.sha256, second.sha256)
    }
    @Test fun proofZipContainsExactXmlAndCannotOverwrite() {
        val path = Files.createTempDirectory("zip-proof").resolve("proof.zip")
        writeProofZip(path)
        val before = Files.readAllBytes(path)
        ZipFile(path.toFile()).use { zip ->
            val text = zip.getInputStream(zip.getEntry("band.logger-synthetic-proof.4ss")).bufferedReader().use { it.readText() }
            assertEquals(proofXml(), text)
            assertNotNull(zip.getEntry("READ-ME.txt"))
        }
        assertFails { writeProofZip(path) }
        assertContentEquals(before, Files.readAllBytes(path))
    }
    @Test fun migrationCreatesPermanentChunkOneBackup() {
        val root = Files.createTempDirectory("migration")
        val path = root.resolve("workspace.json")
        val original = """{"schemaVersion":1,"projects":[],"selectedId":null}"""
        Files.writeString(path, original)
        val store = FileWorkspaceStore(path)
        val migrated = store.load()
        store.save(migrated)
        store.save(migrated.saveProject(Project("q", "Quaternity")))
        assertEquals(original, Files.readString(root.resolve("workspace.chunk1.backup.json")))
        assertEquals(8, store.load().schemaVersion)
    }
}