package bandlogger

import java.nio.file.Files
import java.util.zip.ZipInputStream
import kotlinx.serialization.json.*
import kotlin.test.*

class EventHistoryPersistenceTest {
    private var sequence = 0
    private fun id() = "hp-${++sequence}"
    private val p = Project("p", "Band", members = listOf(Member("m", "Player", emptyList(), listOf("C"))), catalog = Catalog(tunes = listOf(Tune("t", "Tune", "tune"))))
    private fun event() = newEvent(p, EventType.GIG, ::id).copy(title = "Venue", date = "2026-10-22").addTune(p, "t", ::id)
    private val now = "2026-10-22T18:00:00Z"
    private fun entries(path: java.nio.file.Path): Map<String,ByteArray> {
        val result = linkedMapOf<String,ByteArray>()
        ZipInputStream(Files.newInputStream(path)).use { z -> while(true) { val entry = z.nextEntry ?: break; result[entry.name] = z.readBytes(); z.closeEntry() } }
        return result
    }
    @Test fun notesStatusAndHistoricalFactsReopenUnderTheSameEventAcrossEdits() {
        val path = Files.createTempDirectory("history-restart").resolve("workspace.json")
        val other = event().copy(notes = "Other event")
        val complete = event().copy(notes = "Audience loved it\nTry a softer ending").recordPerformance(p, now)
        val events = listOf(complete, other, event().copy(status = EventStatus.CANCELED))
        val initial = Workspace().saveProject(p.copy(events = events))
        val store = FileWorkspaceStore(path); store.save(initial)
        val loaded = FileWorkspaceStore(path).load()
        assertEquals(initial, loaded)
        val edited = complete.copy(title = "Renamed plan", notes = complete.notes + "\nVenue note")
        EventDraft(edited, complete).saveTo(loaded, store)
        val again = store.load().projects.single()
        assertEquals(edited, again.events.single { it.id == complete.id })
        assertEquals(other, again.events.single { it.id == other.id })
        assertEquals(complete.performance, again.events.single { it.id == complete.id }.performance)
        assertEquals(2, again.eventsFor(EventView.HISTORY).size)
    }
    @Test fun schemasOneThroughNineDefaultToPlannedWithoutChangingLegacyIdentitiesAndBackups() {
        for (version in 1..9) {
            val directory = Files.createTempDirectory("history-migration-$version")
            val path = directory.resolve("workspace.json")
            val oldEvent = if(version < 5) event() else event().addSet(::id)
            val original = WorkspaceCodec.encode(Workspace().saveProject(p.copy(events = listOf(oldEvent))))
            val root = Json.parseToJsonElement(original).jsonObject.toMutableMap()
            root["schemaVersion"] = JsonPrimitive(version)
            val project = root.getValue("projects").jsonArray.single().jsonObject.toMutableMap()
            val e = project.getValue("events").jsonArray.single().jsonObject.filterKeys { it !in setOf("notes","status","isTest","performanceHistory") }.toMutableMap()
            if (version < 5) e.remove("additionalSets")
            project["events"] = JsonArray(listOf(JsonObject(e)))
            root["projects"] = JsonArray(listOf(JsonObject(project)))
            val legacy = JsonObject(root).toString()
            Files.writeString(path, legacy)
            val olderBackup = directory.resolve("workspace.chunk1.backup.json")
            Files.writeString(olderBackup, "Keep existing backup")
            val store = FileWorkspaceStore(path)
            val loaded = store.load()
            assertEquals(10, loaded.schemaVersion)
            assertEquals(oldEvent, loaded.projects.single().events.single())
            assertEquals(EventStatus.PLANNED, loaded.projects.single().events.single().status)
            assertEquals(legacy, Files.readString(path))
            store.save(loaded); store.save(store.load())
            assertEquals(legacy, Files.readString(directory.resolve("workspace.pre-event-history.backup.json")))
            assertEquals("Keep existing backup", Files.readString(olderBackup))
        }
    }
    @Test fun schemaNineBrandOptOutIsNotReappliedDuringHistoryMigration() {
        val original = WorkspaceCodec.encode(Workspace().saveProject(Project("q", "Quaternity", "Q", brandId = null)))
        val loaded = WorkspaceCodec.decode(original.replace("\"schemaVersion\": 10", "\"schemaVersion\": 9"))
        assertNull(loaded.projects.single().brandId)
    }
    @Test fun plainAndCoveredPackagesExcludeNotesStatusesAndRecordedPerformanceFacts() {
        for (covers in listOf(false, true)) {
            val root = Files.createTempDirectory("private-history-export")
            val original = event()
            val plain = p.copy(events = listOf(original), includeBrandedSetCovers = covers)
            val private = original.copy(notes = "PRIVATE-SENTINEL\nDo not distribute", isTest = true).recordPerformance(p, now)
            val rich = plain.copy(events = listOf(private))
            assertEquals(EventExportPlanner.preview(plain, original.id), EventExportPlanner.preview(rich, original.id))
            val output = EventExporter.generate(rich, EventExportPlanner.preview(rich, original.id), root.toString(), true, true)
            assertEquals(original, output.record.event)
            val manifest = Files.readString(output.folder.resolve("event-export.json"))
            listOf("PRIVATE-SENTINEL", "notes", "status", "isTest", "performanceHistory").forEach { assertFalse(manifest.contains(it), it) }
            assertEquals(output.record.identity(), EventRecordCodec.decode(manifest).identity())
            output.zipNames.forEach { name ->
                val contents = entries(output.folder.resolve(name))
                assertEquals(2, contents.size)
                contents.forEach { (_,bytes) -> assertFalse(bytes.toString(Charsets.UTF_8).contains("PRIVATE-SENTINEL")) }
                val xml = contents.entries.single { it.key.endsWith(".4ss") }.value.toString(Charsets.UTF_8)
                if(covers) {
                    val doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.byteInputStream())
                    val scores = doc.getElementsByTagName("score")
                    for(i in 0 until scores.length) {
                        val node = scores.item(i) as org.w3c.dom.Element
                        if(node.hasAttribute("data")) org.apache.pdfbox.Loader.loadPDF(java.util.Base64.getDecoder().decode(node.getAttribute("data"))).use { pdf ->
                            assertFalse(org.apache.pdfbox.text.PDFTextStripper().getText(pdf).contains("PRIVATE-SENTINEL"))
                        }
                    }
                } else {
                    val secondRoot = Files.createTempDirectory("plain-compare")
                    val baseline = EventExporter.generate(plain, EventExportPlanner.preview(plain, original.id), secondRoot.toString(), true, true)
                    val expected = entries(baseline.folder.resolve(name))
                    contents.forEach { (key,bytes) -> assertContentEquals(expected.getValue(key),bytes) }
                }
            }
        }
    }
    @Test fun completedEventDeletionRetainsVerifiedExportCleanupAndOtherNotes() {
        val root = Files.createTempDirectory("completed-cleanup")
        val event = event().copy(notes = "Post-show notes").recordPerformance(p, now)
        val other = event().copy(notes = "Keep this note")
        var current = p.copy(events = listOf(event,other), exportDestination = root.toString())
        val store = FileWorkspaceStore(root.resolve("workspace.json"))
        fun save(next: Project) { store.save(Workspace().saveProject(next)); current = next }
        val output = EventExports.publish(current, EventExportPlanner.preview(current,event.id),root.toString(),true,true,EventConflict.NEW,::save)
        EventExports.deleteEvent(current,event.id,::save)
        assertEquals(listOf(other),store.load().projects.single().events)
        assertFalse(Files.exists(output.folder))
        assertTrue(current.eventExports.all { it.state == EventExportState.DELETED })
    }
    @Test fun failedSaveKeepsNotesAndCompletionDirtyWithoutChangingStoredHistory() {
        val original = event();val initial = Workspace().saveProject(p.copy(events = listOf(original)))
        val draft = EventDraft(original.copy(notes = "Unsaved").recordPerformance(p, now), original)
        val store = object: WorkspaceStore { override fun load() = initial; override fun save(value: Workspace) { error("Disk full") } }
        assertFails { draft.saveTo(initial, store) }
        assertTrue(draft.isDirty)
        assertEquals(original,store.load().projects.single().events.single())
    }
}
