package bandlogger

import kotlin.test.*
import java.nio.file.*
import java.time.Instant
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import java.io.StringReader

class EventExportTest {
    private val alpha = Tune("a", "Alpha & <One> \"Q\"", "alpha")
    private val beta = Tune("b", "Betty", "betty")
    private val members = listOf(Member("g", "Marcus", emptyList(), listOf("Guitar", "C")), Member("s", "Jordan", emptyList(), listOf("Bb", "C")), Member("n", "New musician", emptyList(), emptyList()))
    private fun chart(tune: Tune, part: String, filename: String = "Q ${tune.id} -r00- $part.pdf") = MasterItem(tune.id, tune.title, "${tune.id}-$part", part, 0, "Z:/not-present", "not-opened/$filename", filename, "a".repeat(64))
    private fun master(id: String = "master", time: String = "2026-10-20T12:00:00Z", guitar: List<MasterItem> = listOf(chart(alpha, "Guitar"), chart(beta, "C")), sax: List<MasterItem> = listOf(chart(alpha, "Bb"))) =
        LibraryMaster(id, "p", "Band", time, "Z:/not-present-master", listOf(
            MasterMember("g", "Old name", listOf("Guitar", "C"), if (guitar.isEmpty()) null else "g.zip", if (guitar.isEmpty()) null else "b".repeat(64), guitar),
            MasterMember("s", "Old sax name", listOf("Bb"), if (sax.isEmpty()) null else "s.zip", if (sax.isEmpty()) null else "c".repeat(64), sax)))
    private fun event() = BandEvent("e", "p", "Eden Wine Lounge", EventType.GIG, "2026-10-21", EventSet("set1", listOf(EventTuneEntry("e1", "a"), EventTuneEntry("e2", "b"), EventTuneEntry("e3", "a"))),
        listOf(EventSet("set2", listOf(EventTuneEntry("e4", "b"))), EventSet("set3", listOf(EventTuneEntry("e5", "a")))))
    private fun project() = Project("p", "Band", members = members, catalog = Catalog(tunes = listOf(alpha, beta)), masters = listOf(master()), events = listOf(event()))
    private fun plan(project: Project = project()) = EventExportPlanner.preview(project, "e")
    private fun parse(xml: String) = DocumentBuilderFactory.newInstance().apply { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }.newDocumentBuilder().parse(InputSource(StringReader(xml)))
    private fun elements(xml: String): List<org.w3c.dom.Element> {
        val children = parse(xml).documentElement.childNodes
        return (0 until children.length).mapNotNull { children.item(it) as? org.w3c.dom.Element }
    }
    private fun root() = Files.createTempDirectory("event-export-test")
    private fun generate(p: Project = project(), destination: Path = root(), id: String = "run") = EventExporter.generate(p, plan(p), destination.toString(), true, true, id, Instant.parse("2026-10-21T01:02:03Z"))

    @Test fun referenceRootAttributesEscapingAndNoPdfPayload() {
        val xml = EventExportPlanner.xml(plan().members.first())
        val root = parse(xml).documentElement
        assertEquals("forScore", root.tagName)
        assertEquals("setlist", root.getAttribute("kind"))
        assertEquals("1.0", root.getAttribute("version"))
        assertEquals("20261021 Eden Wine Lounge (Marcus - Guitar)", root.getAttribute("title"))
        assertEquals(alpha.title, elements(xml)[1].getAttribute("title"))
        assertFalse(xml.contains("data=")); assertFalse(xml.contains("JVBER")); assertFalse(xml.contains("%PDF"))
        assertTrue(xml.contains("&amp;")); assertTrue(xml.contains("&lt;")); assertTrue(xml.contains("&quot;"))
        EventExporter.validateXml(xml)
    }
    @Test fun allSetsTuneOrderAndRepeatedOccurrencesShareOneDocument() {
        val rows = elements(EventExportPlanner.xml(plan().members.first()))
        assertEquals(listOf("Set 1", "a", "b", "a", "Set 2", "b", "Set 3", "a"), rows.map { if (it.tagName == "placeholder") it.getAttribute("title") else it.getAttribute("path").split(' ')[1] })
        assertEquals(3, rows.count { it.tagName == "placeholder" })
        assertEquals(5, rows.count { it.tagName == "score" })
    }
    @Test fun eachMemberUsesExactRecordedFilenameAndLegitimateFallback() {
        val p = project()
        val guitar = plan(p).members.first()
        assertEquals(5, guitar.matchedCount)
        assertEquals("Q b -r00- C.pdf", guitar.sets.first().entries[1].filename)
        val sax = plan(p).members[1]
        assertEquals(3, sax.matchedCount); assertEquals(2, sax.missingCount)
        assertEquals("Q a -r00- Bb.pdf", sax.entries.first().filename)
        assertNull(sax.entries[1].filename)
        assertEquals("Guitar", guitar.preferredPart)
    }
    @Test fun newerLocalChartsAndChangedPreferencesNeverReplaceRecordedSelection() {
        val p = project().copy(members = members.map { if (it.id == "g") it.copy(preferences = listOf("Piano")) else it })
        val member = plan(p).members.first()
        assertEquals("Piano", member.preferredPart)
        assertEquals("Q a -r00- Guitar.pdf", member.entries.first().filename)
        assertTrue(member.baseName.endsWith("(Marcus - Piano)"))
        assertTrue(p.catalog.charts.isEmpty()) // reference resolution does not need local PDFs
    }
    @Test fun latestNonEmptyBaselineUsesTimeThenIdAndRetainsSkippedMember() {
        val old = master("old", "2026-10-20T12:00:00Z")
        val latest = master("z", "2026-10-21T12:00:00.1Z", listOf(chart(alpha, "C", "exact-old-installed.pdf")), emptyList())
        val tied = latest.copy(id = "a", members = latest.members.map { if (it.memberId == "g") it.copy(charts = listOf(chart(alpha, "Guitar"))) else it })
        val p = project().copy(masters = listOf(latest, old, tied))
        val value = plan(p)
        assertEquals("z", value.members[0].baselineId)
        assertEquals("exact-old-installed.pdf", value.members[0].entries[0].filename)
        assertNull(value.members[0].entries[1].filename) // absent from latest, never union history
        assertEquals("old", value.members[1].baselineId)
        assertEquals(PendingPlanner.baselines(p).map { it.master?.id }, value.members.map { it.baselineId })
    }
    @Test fun invalidAndForeignBaselinesAreExcludedWithoutGuessing() {
        val broken = master().copy(members = master().members.map { it.copy(zipSha256 = "invalid") })
        val p = project().copy(masters = listOf(broken, master().copy(projectId = "other")))
        assertTrue(plan(p).members.all { it.baselineId == null && it.missingCount == 5 })
    }
    @Test fun missingEntriesBecomeExplicitPlaceholdersInExactPositions() {
        val sax = plan().members[1]
        val rows = elements(EventExportPlanner.xml(sax))
        assertEquals("placeholder", rows[2].tagName)
        assertEquals("Missing chart — Betty", rows[2].getAttribute("title"))
        assertEquals("score", rows[3].tagName)
        assertEquals(2, rows.count { it.getAttribute("title") == "Missing chart — Betty" })
    }
    @Test fun noBaselineProducesHeadingsAndPlaceholderOnlyWithMemberOnlyLabel() {
        val member = plan().members.last()
        assertNull(member.baselineId); assertEquals(0, member.matchedCount); assertEquals(5, member.missingCount)
        assertTrue(member.baseName.endsWith("(New musician)"))
        assertTrue(elements(EventExportPlanner.xml(member)).all { it.tagName == "placeholder" })
    }
    @Test fun unsafeRecordedFilenameIsMissingRatherThanSanitizedOrGuessed() {
        val p = project().copy(masters = listOf(master(guitar = listOf(chart(alpha, "Guitar", "../wrong.pdf")))))
        assertNull(plan(p).members.first().entries.first().filename)
        assertTrue(plan(p).members.first().entries.first().reason!!.contains("unsafe"))
    }
    @Test fun emptySetsAreSkippedWithOriginalNumbersAndWarning() {
        val e = event().copy(initialSet = EventSet("empty"), additionalSets = listOf(EventSet("second", listOf(EventTuneEntry("x", "a")))))
        val value = plan(project().copy(events = listOf(e)))
        assertEquals(listOf(1), value.skippedSets)
        assertEquals("Set 2", elements(EventExportPlanner.xml(value.members.first())).first().getAttribute("title"))
        assertTrue(value.warnings.single().contains("Empty Set 1"))
    }
    @Test fun emptyEventAndProjectWithoutMembersAreRejected() {
        assertFailsWith<IllegalArgumentException> { plan(project().copy(events = listOf(event().copy(initialSet = EventSet("empty"), additionalSets = emptyList())))) }
        assertFailsWith<IllegalArgumentException> { plan(project().copy(members = emptyList())) }
    }
    @Test fun unsavedOrForeignEventCannotBeExported() {
        assertFailsWith<IllegalArgumentException> { plan(project().copy(events = emptyList())) }
        assertFailsWith<IllegalArgumentException> { plan(project().copy(events = listOf(event().copy(projectId = "other")))) }
    }
    @Test fun dateIsSavedDateAndUndatedNeedsSeparateAcknowledgement() {
        assertTrue(plan().folderName.startsWith("2026-10-21 "))
        val value = plan(project().copy(events = listOf(event().copy(date = null))))
        assertTrue(value.members.all { it.baseName.startsWith("Undated ") })
        assertFailsWith<IllegalArgumentException> { value.requireAcknowledgements(true, false) }
        assertFailsWith<IllegalArgumentException> { value.requireAcknowledgements(false, true) }
        value.requireAcknowledgements(true, true)
    }
    @Test fun missingAcknowledgementIsEnforcedBeforeAnyFileCreation() {
        val dir = root()
        assertFailsWith<IllegalArgumentException> { EventExporter.generate(project(), plan(), dir.toString()) }
        assertFalse(Files.exists(dir.resolve("Gigs")))
        val complete = project().copy(members = listOf(members.first()))
        EventExportPlanner.preview(complete, "e").requireAcknowledgements(false, false)
    }
    @Test fun safeNamesUseCurrentMembersAvoidCaseInsensitiveCollisionsAndKeepSavedTitle() {
        val p = project().copy(events = listOf(event().copy(title = "Bad: title/with?* characters")), members = listOf(members[0].copy(name = "SAME"), members[1].copy(name = "same", preferences = listOf("Guitar"))))
        val value = plan(p)
        assertEquals(2, value.members.map { it.baseName.lowercase() }.distinct().size)
        assertTrue(value.members.last().baseName.endsWith(" - 2"))
        assertTrue(value.members.all { member -> member.baseName.none { it in "<>:\"/\\|?*" } })
        assertEquals("Bad: title/with?* characters", value.event.title)
        assertEquals("_CON", EventExportPlanner.safeLabel("CON"))
        assertEquals("Untitled", EventExportPlanner.safeLabel("..."))
        assertTrue(EventExportPlanner.safeLabel("x".repeat(200)).length <= 50)
    }
    @Test fun zipContainsExactlyOneCompleteReferenceXmlAndReadmePerMember() {
        val p = project(); val before = WorkspaceCodec.encode(Workspace().saveProject(p)); val result = generate(p)
        assertEquals(3, result.zipNames.size)
        result.zipNames.forEach { name -> ZipFile(result.folder.resolve(name).toFile()).use { zip ->
            val entries = zip.entries().asSequence().toList()
            assertEquals(2, entries.size)
            assertEquals(1, entries.count { it.name.endsWith(".4ss") })
            assertEquals(0, entries.count { it.name.endsWith(".pdf", true) })
            val xml = zip.getInputStream(entries.single { it.name.endsWith(".4ss") }).use { it.readBytes().decodeToString() }
            EventExporter.validateXml(xml)
            assertEquals(8, elements(xml).size)
            assertFalse(xml.contains("data="))
            val readme = zip.getInputStream(zip.getEntry("READ-ME.txt")).use { it.readBytes().decodeToString() }
            assertTrue(readme.contains("NO PDFs")); assertTrue(readme.contains("Google Drive"))
        } }
        assertEquals(before, WorkspaceCodec.encode(Workspace().saveProject(p)))
        assertTrue(result.folder.parent.fileName.toString() == "Gigs")
    }
    @Test fun repeatedExportUsesDistinctFolderAndNeverOverwrites() {
        val dir = root(); val one = generate(destination = dir, id = "first")
        val bytes = Files.readAllBytes(one.folder.resolve(one.zipNames.first()))
        assertFailsWith<IllegalArgumentException> { generate(destination = dir, id = "second") }
        val p = project()
        val two = EventExporter.generate(p, plan(p), dir.toString(), true, true, id = "second", revision = true)
        assertNotEquals(one.folder, two.folder)
        assertContentEquals(bytes, Files.readAllBytes(one.folder.resolve(one.zipNames.first())))
        assertFailsWith<IllegalArgumentException> { generate(destination = dir, id = "second") }
    }
    @Test fun existingEventsFileFailsWithoutChangingIt() {
        val dir = root(); Files.writeString(dir.resolve("Gigs"), "keep me")
        assertFailsWith<IllegalArgumentException> { generate(destination = dir) }
        assertEquals("keep me", Files.readString(dir.resolve("Gigs")))
    }
    @Test fun stalePreviewCannotGenerateAfterSavedEventOrBaselineChanges() {
        val p = project(); val preview = plan(p); val dir = root()
        assertFailsWith<IllegalArgumentException> { EventExporter.generate(p.copy(events = listOf(event().copy(title = "Changed"))), preview, dir.toString(), true, true) }
        val changed = p.copy(masters = listOf(master(guitar = listOf(chart(alpha, "C")))))
        assertFailsWith<IllegalArgumentException> { EventExporter.generate(changed, preview, dir.toString(), true, true) }
        assertFalse(Files.exists(dir.resolve("Gigs")))
    }
    @Test fun invalidXmlFailsBeforeCreatingOrPublishingOutput() {
        val p = project().copy(catalog = Catalog(tunes = listOf(alpha.copy(title = "bad\u0001"), beta)))
        val dir = root()
        assertFailsWith<IllegalArgumentException> { generate(p, dir) }
        assertFalse(Files.exists(dir.resolve("Gigs")))
    }
    @Test fun existingStagingFolderIsNotTouchedOnFailure() {
        val dir = root(); val events = Files.createDirectory(dir.resolve("Gigs")); val stage = Files.createDirectory(events.resolve(".bandlogger-event-run"))
        Files.writeString(stage.resolve("owner-file"), "preserve")
        assertFailsWith<FileAlreadyExistsException> { generate(destination = dir) }
        assertEquals("preserve", Files.readString(stage.resolve("owner-file")))
        assertFalse(Files.exists(events.resolve(plan().folderName)))
    }
    @Test fun projectIsolationRejectsOtherProjectEventAndMasters() {
        val other = Project("other", "Other", members = members, events = emptyList(), masters = project().masters)
        assertFailsWith<IllegalArgumentException> { plan(other) }
        val p = project().copy(masters = listOf(master().copy(projectId = "other")))
        assertTrue(plan(p).members.all { it.matchedCount == 0 })
    }
    @Test fun parserRejectsEmbeddedDataAndUnsafeReferences() {
        assertFailsWith<IllegalArgumentException> { EventExporter.validateXml(ForScore.header("bad") + "<score title=\"Bad\" path=\"a.pdf\" data=\"JVBER\"/>" + ForScore.footer) }
        assertFailsWith<IllegalArgumentException> { ForScore.referenceElement("Bad", "path/a.pdf") }
        assertFailsWith<IllegalArgumentException> { ForScore.placeholderElement("Bad\u0001") }
    }
    @Test fun copyVerificationIgnoresStaleReportedLengthAndChecksEveryByte() {
        val expected = ByteArray(4096) { (it % 251).toByte() }
        val cloudStream = object : java.io.ByteArrayInputStream(expected) {
            override fun available() = 1 // virtual-drive metadata can lag behind completed writes
        }
        EventExporter.verifyCopy(cloudStream, expected)
    }
    @Test fun copyVerificationRejectsTruncatedChangedAndExtraBytes() {
        val expected = byteArrayOf(1, 2, 3, 4)
        listOf(byteArrayOf(1), byteArrayOf(1, 2, 9, 4), byteArrayOf(1, 2, 3, 4, 5)).forEach { bytes ->
            assertFailsWith<IllegalArgumentException> { EventExporter.verifyCopy(java.io.ByteArrayInputStream(bytes), expected) }
        }
    }
    @Test fun completedReferenceArchivesValidateSequentiallyWithExactContents() {
        val p = project()
        val result = generate(p)
        result.zipNames.forEach { name ->
            val member = plan(p).members.single { "${it.baseName}.zip" == name }
            val path = result.folder.resolve(name)
            ZipFile(path.toFile()).use { zip ->
                val readme = zip.getInputStream(zip.getEntry("READ-ME.txt")).use { it.readBytes().decodeToString() }
                val bytes = Files.readAllBytes(path)
                EventExporter.verifyPackage(bytes, "${member.baseName}.4ss", EventExportPlanner.xml(member), readme)
                assertFailsWith<IllegalArgumentException> { EventExporter.verifyPackage(bytes, "wrong.4ss", EventExportPlanner.xml(member), readme) }
            }
        }
    }}
