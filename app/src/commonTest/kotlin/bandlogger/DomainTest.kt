package bandlogger
import kotlin.test.*

class DomainTest {
    @Test fun projectsRemainIndependent() {
        val first = Project("one", "Quaternity", members = listOf(Member("m", "Marcus", listOf("Guitar"), listOf("Guitar", "C"))))
        val second = Project("two", "Another band", theme = Theme(accent = "#123456"))
        val state = Workspace().saveProject(first).saveProject(second).select("one")
        val restored = WorkspaceCodec.decode(WorkspaceCodec.encode(state))
        assertEquals(first, restored.projects[0])
        assertEquals("#123456", restored.projects[1].theme.accent)
        assertTrue(restored.projects[1].members.isEmpty())
        assertEquals("one", restored.selectedId)
    }
    @Test fun invalidDataRejected() {
        assertFailsWith<IllegalArgumentException> { Workspace().saveProject(Project("x", " ")) }
        assertFailsWith<IllegalArgumentException> { Theme(accent = "red").validate() }
        assertFailsWith<IllegalArgumentException> { Workspace().select("missing") }
        assertFailsWith<IllegalArgumentException> { Workspace(schemaVersion = 99).validate() }
    }
    @Test fun memberEditsRoundTrip() {
        val p = Project("p", "Band", members = listOf(Member("m", "New name", listOf("Sax", "EWI"), listOf("Bb", "C"))))
        val saved = WorkspaceCodec.decode(WorkspaceCodec.encode(Workspace().saveProject(p)))
        assertEquals(p.members, saved.projects.single().members)
    }
    @Test fun defaultsEditable() {
        var n = 0
        val p = quaternity("q") { (++n).toString() }
        assertEquals(4, p.members.size)
        assertEquals(listOf("Tenor saxophone", "EWI"), p.members[1].roles)
        assertEquals("New", p.copy(name = "New").name)
    }
}