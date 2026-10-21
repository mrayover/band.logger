package bandlogger

import java.nio.file.Files
import kotlin.test.*

class PendingSourcesTest {
    @Test fun checksBytesMissingFilesAndUnindexedFilesWithoutSaving() {
        val root=Files.createTempDirectory("pending-source")
        try {
            val path=root.resolve("Q Alpha -r00- C.pdf")
            Files.write(path,syntheticPdf())
            val catalog=Catalog(source=root.toString()).reconcile(LocalCharts.scan(root.toString()),"Q",1,::newId).acceptNew()
            val project=Project("p","Band",catalog=catalog)
            val before=WorkspaceCodec.encode(Workspace().saveProject(project))
            assertTrue(PendingSources.check(project).issues.isEmpty())
            Files.writeString(path,"%PDF- changed")
            assertTrue(PendingSources.check(project).issues.values.single().contains("changed"))
            Files.delete(path)
            assertTrue(PendingSources.check(project).issues.values.single().contains("missing"))
            Files.write(root.resolve("new.pdf"),syntheticPdf())
            assertTrue(PendingSources.check(project).notices.single().contains("unindexed"))
            assertEquals(before,WorkspaceCodec.encode(Workspace().saveProject(project)))
        } finally { Files.walk(root).use { it.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) } }
    }
    @Test fun unavailableFolderBlocksSavedSelections() {
        val project=Project("p","Band",catalog=Catalog(source="Z:/not-a-real-pending-source",charts=listOf(
            ChartRecord("c","Z:/not-a-real-pending-source","c.pdf","a".repeat(64),1,1,1))))
        assertTrue(PendingSources.check(project).issues.containsKey("c"))
    }
}
