package bandlogger

import java.nio.file.*

/** Numbered-root diagnostics only use synthetic sources and their own history store. */
object NumberedLayoutSmoke {
    fun run(seed: Project, dir: Path) {
        val root=Files.createDirectories(System.getProperty("bandlogger.numberedSmokeRoot")?.let(Path::of) ?: dir.resolve("numbered-project")).toRealPath()
        val actualDrive=FileLocations.defaultEvents.parent
        if(root.startsWith(actualDrive)) require(root.startsWith(actualDrive.resolve("06 - Testing"))) { "Synthetic Drive outputs must stay under 06 - Testing." }
        FileLocations.numberedFolders.forEach { ExportFolders.child(root,it) }
        val store=FileWorkspaceStore(dir.resolve("numbered-smoke.json"))
        var p=seed.withExportRoot(root.toString()).copy(masters=emptyList(),supplements=emptyList(),events=emptyList(),eventExports=emptyList(),includeBrandedSetCovers=true)
        val master=MasterExporter.generate(p,MasterPlanner.preview(p));p=p.recordMaster(master)
        check(Path.of(master.folder)==root.resolve("02 - Repertoire Distributions/Master"))
        check(master.members.filter { it.zipName!=null }.all { it.zipName!!.contains(" - Master - ") })
        val source=Path.of(p.catalog.source!!)
        Files.write(source.resolve("Q Numbered Routing Tune -r00- C.pdf"),syntheticPdf(),StandardOpenOption.CREATE_NEW)
        p=p.copy(catalog=p.catalog.reconcile(LocalCharts.scan(source.toString()),p.prefix,4,::newId).acceptNew())
        val supplement=SupplementExporter.generate(p,SupplementPlanner.preview(p),journalDirectory=dir)
        p=p.recordSupplement(supplement);SupplementExporter.clearRecordedIntent(p,dir)
        check(Path.of(supplement.publication.folder)==root.resolve("02 - Repertoire Distributions/Supplements/Supplement 001"))
        fun save(next: Project) { p=next;store.save(Workspace().saveProject(p)) }
        repeat(3) { index ->
            val type=if(index==1) EventType.REHEARSAL else EventType.GIG
            val event=newEvent(p,type,::newId).copy(title="Numbered route ${type.name} $index",date="2026-10-22",isTest=index==2,notes="Internal numbered routing notes")
                .addTune(p,p.catalog.tunes.first().id,::newId)
            save(p.saveEvent(event))
            val plan=EventExportPlanner.preview(p,event.id)
            val output=EventExports.publish(p,plan,root.toString(),true,true,EventConflict.NEW,::save)
            check(output.folder==FileLocations.eventDestination(p,plan,root.toString()))
            check(output.record.covers.size==1 && output.zipNames.size==p.members.size)
        }
        check(!Files.exists(root.resolve("Library")) && !Files.exists(root.resolve("Gigs")) && !Files.exists(root.resolve("Supplements")))
        check(FileLocations.inbox(p)==root.resolve("00 - Inbox"))
        Files.writeString(dir.resolve("numbered-routing-passed.txt"),"Numbered master/supplement/gig/rehearsal/test routes, short names, covers and persisted root verified.")
    }
    fun restart(dir: Path) {
        val store=FileWorkspaceStore(dir.resolve("numbered-smoke.json"));var p=store.load().projects.single()
        check(MasterExporter.recover(p,p.masters.single().folder)==p.masters.single())
        check(SupplementExporter.recover(p,p.supplements.single().publication.folder)==p.supplements.single())
        check(p.events.size==3 && p.events.all { it.notes=="Internal numbered routing notes" })
        p.events.toList().forEach { event -> EventExports.deleteEvent(p,event.id) { p=it;store.save(Workspace().saveProject(it)) } }
        check(p.events.isEmpty() && p.eventExports.all { it.state==EventExportState.DELETED })
        Files.writeString(dir.resolve("numbered-routing-restart-passed.txt"),"Separate process recovered numbered library distributions and verified cleanup of routed event packages.")
    }
}
