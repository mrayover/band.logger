package bandlogger

import java.nio.file.Files
import kotlin.test.*

class MasterDestinationTest {
    @Test fun destinationPersistsAcrossRestartPerProjectWithoutWritingExports() {
        val root = Files.createTempDirectory("master-destination")
        val destination = Files.createDirectory(root.resolve("exports"))
        val path = root.resolve("workspace.json")
        val one = Project("one", "One", exportDestination = destination.toString())
        val two = Project("two", "Two")
        FileWorkspaceStore(path).save(Workspace().saveProject(one).saveProject(two))
        val restored = FileWorkspaceStore(path).load()
        assertEquals(destination.toString(), restored.projects[0].exportDestination)
        assertNull(restored.projects[1].exportDestination)
        assertEquals(one, restored.select("one").projects[0])
        Files.list(destination).use { assertEquals(0L, it.count()) }
    }
    @Test fun existingSchemaTwoLoadsWithNoDestination() {
        val original = """{"schemaVersion":2,"projects":[{"id":"p","name":"Band"}],"selectedId":"p"}"""
        val restored = WorkspaceCodec.decode(original)
        assertNull(restored.projects.single().exportDestination)
        val revised = restored.saveProject(restored.projects.single().copy(exportDestination = "provider-reference"))
        assertEquals("provider-reference", WorkspaceCodec.decode(WorkspaceCodec.encode(revised)).projects.single().exportDestination)
    }
}