package bandlogger

import java.nio.file.*
import java.time.Instant
import java.util.zip.ZipInputStream

/** All history diagnostics use isolated synthetic workspaces and packages. */
object EventHistorySmoke {
    private const val PRIVATE = "INTERNAL-HISTORY-NOTE: audience response\nTry a quieter ending"
    fun run(seed: Project, dir: Path) {
        val store = FileWorkspaceStore(dir.resolve("event-history-smoke.json"))
        val root = Files.createDirectories(dir.resolve("history-exports"))
        var p = seed.copy(events = emptyList(), eventExports = emptyList(), exportDestination = root.toString(), includeBrandedSetCovers = true)
        val tune = p.catalog.tunes.first().id
        var event = newEvent(p, EventType.GIG, ::newId).copy(title = "Historical synthetic gig", date = "2026-10-22", notes = PRIVATE)
            .addTune(p,tune,::newId).addTune(p,tune,::newId).addSet(::newId)
        event = event.addTune(p,tune,::newId,event.sets.last().id).recordPerformance(p,Instant.now().toString())
        val planned = newEvent(p,EventType.REHEARSAL,::newId).copy(title = "Past planned rehearsal",date = "2000-01-01")
        val canceled = newEvent(p,EventType.GIG,::newId).copy(title = "Canceled synthetic gig",status = EventStatus.CANCELED)
        val test = newEvent(p,EventType.REHEARSAL,::newId).copy(title = "Test rehearsal",isTest = true).recordPerformance(p,Instant.now().toString())
        p = p.copy(events = listOf(event,planned,canceled,test))
        fun save(next: Project) { store.save(Workspace().saveProject(next));p=next }
        save(p)
        val plan = EventExportPlanner.preview(p,event.id)
        check(plan.event.notes.isEmpty() && plan.event.performanceHistory.isEmpty())
        val output = EventExports.publish(p,plan,root.toString(),true,true,EventConflict.NEW,::save)
        val manifest = Files.readString(output.folder.resolve("event-export.json"))
        check(!manifest.contains("INTERNAL-HISTORY-NOTE") && !manifest.contains("performanceHistory") && !manifest.contains("notes"))
        output.zipNames.forEach { name ->
            ZipInputStream(Files.newInputStream(output.folder.resolve(name))).use { zip ->
                while(true) { val entry = zip.nextEntry ?: break; check(!zip.readBytes().decodeToString().contains("INTERNAL-HISTORY-NOTE"));zip.closeEntry() }
            }
        }
        val edited = event.copy(title = "Changed future plan",date = "2027-01-01").removeEntry(event.entries.first().id)
        save(p.saveEvent(edited))
        check(store.load().projects.single().events.single { it.id == event.id }.performance == event.performance)
        check(p.events.count { it.countablePerformance != null } == 1)
        Files.writeString(dir.resolve("event-history-passed.txt"),"Private notes/status/snapshot/repeats persisted; exports exclude internal metadata; future plan edits preserve facts.")
    }
    fun restart(dir: Path) {
        val store = FileWorkspaceStore(dir.resolve("event-history-smoke.json"));var p=store.load().projects.single()
        val completed = p.events.single { it.title == "Changed future plan" }
        check(completed.notes == PRIVATE && completed.entries.size == 2)
        check(completed.performance?.entries?.size == 3 && completed.performance?.title == "Historical synthetic gig")
        check(p.eventsFor(EventView.UPCOMING).single().title == "Past planned rehearsal")
        check(p.eventsFor(EventView.HISTORY).size == 3 && p.events.count { it.countablePerformance != null } == 1)
        EventExports.deleteEvent(p,completed.id) { store.save(Workspace().saveProject(it));p=it }
        check(p.events.size == 3 && p.eventExports.all { it.state == EventExportState.DELETED })
        Files.writeString(dir.resolve("event-history-restart-passed.txt"),"Separate process reopened notes/status/immutable recorded repertoire; completed-event cleanup passed.")
    }
}
