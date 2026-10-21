package bandlogger

import java.nio.file.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import java.io.StringReader

// The event-export path has no PDF/file-source reader and never updates workspace metadata.
data class EventExportResult(val folder: Path, val zipNames: List<String>, val warnings: List<String>)
object EventExporter {
    fun eventsRoot(destination: String): Path {
        val root = Path.of(LocalCharts.folder(Path.of(destination)))
        require(Files.isWritable(root)) { "Choose a writable export destination." }
        val events = root.resolve("Events")
        if (Files.exists(events, LinkOption.NOFOLLOW_LINKS)) {
            require(Files.isDirectory(events, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(events) && events.toRealPath().startsWith(root)) { "Events must be a normal folder inside the export destination." }
        }
        return events
    }
    fun validateXml(xml: String) {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        val root = factory.newDocumentBuilder().parse(InputSource(StringReader(xml))).documentElement
        require(root.tagName == "forScore" && root.getAttribute("kind") == "setlist" && root.getAttribute("version") == "1.0")
        val children = root.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node is org.w3c.dom.Element) {
                require(node.tagName == "score" || node.tagName == "placeholder")
                require(!node.hasAttribute("data")) { "Event exports must remain reference-only." }
                require(node.hasAttribute("title"))
                if (node.tagName == "score") require(ForScore.validReferenceFilename(node.getAttribute("path")))
            }
        }
    }
    private fun readme(plan: EventExportPlan, member: EventMemberExport): String = buildString {
        append("Event: ${plan.event.title}\nDate: ${plan.event.date ?: "Undated (acknowledged)"}\nMember: ${member.member.name}\nSetlist: ${member.baseName}\n")
        append("Preferred part: ${member.preferredPart ?: "Not set"} (descriptive; recorded fallback parts may be used).\n")
        append("${member.entries.size} song entries: ${member.matchedCount} matched recorded references; ${member.missingCount} missing.\n")
        append("Reference-only: NO PDFs included. Filenames must already exist in this member's forScore library. A generated Library Master does not prove installation.\n")
        append("Transfer ZIP through Google Drive, save locally in iPad Files, extract, then share the .4ss to forScore. Open Setlists and choose the named setlist. Do not import READ-ME.txt as a score.\n")
        append("Set headings and actual matching/repeated-entry behavior require owner iPad verification. Missing installed filenames may appear as placeholders.\n")
        append("Baseline: ${member.baselineId ?: "No recorded Library Master: placeholder-only output (acknowledged)"}\n")
        plan.warnings.forEach { append("Warning: $it\n") }
        member.sets.forEach { set -> set.entries.filter { it.filename == null }.forEach { entry ->
            append("Set ${set.number}: Missing chart — ${entry.title} · ${entry.reason}\n")
        } }
    }
    internal fun verifyCopy(input: InputStream, expected: ByteArray) {
        require(input.readNBytes(expected.size + 1).contentEquals(expected)) {
            "The written member ZIP could not be verified. Try again or choose a local export folder."
        }
    }
    internal fun verifyPackage(bytes: ByteArray, xmlName: String, expectedXml: String, expectedReadme: String) {
        val expected = mapOf(xmlName to expectedXml, "READ-ME.txt" to expectedReadme)
        val seen = mutableSetOf<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                require(!entry.isDirectory && entry.name in expected && seen.add(entry.name)) { "Unexpected member ZIP contents." }
                val text = zip.readBytes().toString(Charsets.UTF_8)
                require(text == expected.getValue(entry.name)) { "Member ZIP contents changed." }
                if (entry.name == xmlName) validateXml(text)
                zip.closeEntry() // consumes and checks the entry CRC
            }
        }
        require(seen == expected.keys) { "Incomplete member ZIP." }
    }
    fun generate(project: Project, plan: EventExportPlan, destination: String,
                 acknowledgeMissing: Boolean = false, acknowledgeUndated: Boolean = false,
                 id: String = UUID.randomUUID().toString(), now: Instant = Instant.now()): EventExportResult {
        require(plan == EventExportPlanner.preview(project, plan.event.id)) { "Saved event or member baselines changed. Review a new preview." }
        plan.requireAcknowledgements(acknowledgeMissing, acknowledgeUndated)
        require(Regex("[A-Za-z0-9-]{1,64}").matches(id))
        // Validate all XML before creating any output. Never read chart paths or master ZIPs.
        plan.members.forEach { validateXml(EventExportPlanner.xml(it)) }
        val events = eventsRoot(destination)
        try { Files.createDirectory(events) } catch (_: FileAlreadyExistsException) { /* checked again below */ }
        val confirmedEvents = eventsRoot(destination)
        require(Files.isDirectory(confirmedEvents))
        val stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC).format(now)
        var target = events.resolve(plan.folderName)
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) target = events.resolve("${plan.folderName} - $stamp - $id")
        require(!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) { "Export folder already exists. Nothing was overwritten." }
        val stage = Files.createDirectory(events.resolve(".bandlogger-event-$id"))
        var published = false
        try {
            Files.writeString(stage.resolve("INCOMPLETE.txt"), "Incomplete event export. Do not distribute.", StandardOpenOption.CREATE_NEW)
            plan.members.forEach { member ->
                val zipPath = stage.resolve("${member.baseName}.zip")
                val xml = EventExportPlanner.xml(member)
                val instructions = readme(plan, member)
                val buffer = ByteArrayOutputStream()
                ZipOutputStream(buffer).use { zip ->
                    zip.putNextEntry(ZipEntry("${member.baseName}.4ss"))
                    zip.write(xml.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    zip.putNextEntry(ZipEntry("READ-ME.txt"))
                    zip.write(instructions.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                // Finish the central directory before writing to a virtual/cloud drive.
                val archive = buffer.toByteArray()
                verifyPackage(archive, "${member.baseName}.4ss", xml, instructions)
                Files.write(zipPath, archive, StandardOpenOption.CREATE_NEW)
                // Drive can briefly report a stale file length to random-access ZipFile.
                // Sequentially verify every byte instead of consulting that metadata.
                Files.newInputStream(zipPath).use { verifyCopy(it, archive) }
            }
            Files.delete(stage.resolve("INCOMPLETE.txt"))
            Files.move(stage, target) // no replacement: a racing existing folder fails safely
            published = true
            return EventExportResult(target, plan.members.map { "${it.baseName}.zip" }, plan.warnings +
                plan.members.filter { it.missingCount > 0 }.map { "${it.member.name}: ${it.missingCount} missing references retained as placeholders." })
        } finally {
            if (!published) runCatching {
                Files.newDirectoryStream(stage).use { files -> files.forEach { Files.deleteIfExists(it) } }
                Files.deleteIfExists(stage)
            }
        }
    }
}
