package bandlogger
import kotlin.test.*
import java.nio.file.Files
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import java.io.StringReader

class DesktopTest {
    @Test fun persistenceSurvivesRestartAndKeepsBackup() {
        val path = Files.createTempDirectory("bandlogger-test").resolve("workspace.json")
        val one = Workspace().saveProject(Project("one", "First"))
        FileWorkspaceStore(path).save(one)
        val two = one.saveProject(Project("two", "Second"))
        FileWorkspaceStore(path).save(two)
        assertEquals(two, FileWorkspaceStore(path).load())
        assertEquals(one, WorkspaceCodec.decode(Files.readString(path.resolveSibling("workspace.backup.json"))))
    }
    @Test fun corruptDataFailsWithoutResetting() {
        val path = Files.createTempDirectory("bandlogger-corrupt").resolve("workspace.json")
        Files.writeString(path, "broken")
        assertFails { FileWorkspaceStore(path).load() }
        assertEquals("broken", Files.readString(path))
    }
    @Test fun xmlRoundTripAndOrdering() {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        val doc = factory.newDocumentBuilder().parse(InputSource(StringReader(proofXml())))
        assertEquals("forScore", doc.documentElement.tagName)
        assertEquals("setlist", doc.documentElement.getAttribute("kind"))
        assertEquals("1.0", doc.documentElement.getAttribute("version"))
        assertEquals("band.logger & forScore — technical proof", doc.documentElement.getAttribute("title"))
        val scores = doc.getElementsByTagName("score")
        assertEquals(2, scores.length)
        val first = scores.item(0) as org.w3c.dom.Element
        assertEquals("Synthetic & safe <one>", first.getAttribute("title"))
        assertEquals("Synthetic & safe.pdf", first.getAttribute("path"))
        assertContentEquals(syntheticPdf(), Base64.getDecoder().decode(first.getAttribute("data")))
        assertEquals("Second proof.pdf", (scores.item(1) as org.w3c.dom.Element).getAttribute("path"))
    }
    @Test fun fontsActuallyLoad() {
        listOf("Lato-Regular.ttf", "Lato-Bold.ttf").forEach { filename ->
            val stream = javaClass.classLoader.getResourceAsStream("fonts/$filename")
            assertNotNull(stream)
            stream.use { assertEquals("Lato", java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, it).family) }
        }
    }
    @Test fun unsafeXmlRejected() {
        assertFailsWith<IllegalArgumentException> { ForScore.generate("bad\u0001", emptyList()) }
        assertFailsWith<IllegalArgumentException> { ForScore.generate("proof", listOf(ProofScore("bad", "../bad.pdf", syntheticPdf()))) }
    }
}