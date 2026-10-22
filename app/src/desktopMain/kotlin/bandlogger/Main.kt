package bandlogger

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.util.UUID
import java.awt.FileDialog
import java.awt.Frame
import kotlin.system.exitProcess

fun newId() = UUID.randomUUID().toString()
fun hex(value: String) = Color(0xFF000000 or value.removePrefix("#").toLong(16))
val lato = FontFamily(Font("fonts/Lato-Regular.ttf"), Font("fonts/Lato-Bold.ttf", FontWeight.Bold))

fun main(args: Array<String>) {
    if (args.firstOrNull() == "--scan-source") {
        val source = LocalCharts.folder(java.nio.file.Path.of(args[1]))
        val files = LocalCharts.scan(source)
        val catalog = Catalog(source = source).reconcile(files, "Q", System.currentTimeMillis(), ::newId)
        val dir = dataDirectory()
        Files.createDirectories(dir)
        Files.writeString(dir.resolve("scan-audit.json"), WorkspaceCodec.encode(Workspace().saveProject(Project("audit", "Read-only audit", "Q", catalog = catalog))))
        Files.writeString(dir.resolve("scan-summary.txt"), "Files: ${files.size}\nTunes: ${catalog.tunes.size}\nUnrecognized names: ${catalog.charts.count { it.metadata == null }}\nInvalid PDF headers: ${files.count { it.error != null }}\nConflicts: ${catalog.charts.count { catalog.conflicts(it) }}")
        exitProcess(0)
    }
    if ("--event-restart-check" in args) {
        val dir = dataDirectory()
        val store = FileWorkspaceStore(dir.resolve("event-smoke.json"))
        val workspace = store.load()
        val project = workspace.projects.first()
        check(project.events.map { it.type }.toSet() == EventType.entries.toSet())
        check(project.events.all { it.sets.size == 2 && it.entries.size == 3 && it.entries.all { entry -> entry.tuneId == project.catalog.tunes.single().id } })
        check(workspace.projects.last().events.isEmpty())
        val idsBefore = project.events.flatMap { it.sets }.map { it.id } to project.events.flatMap { it.entries }.map { it.id }.toSet()
        val event = project.events.first()
        val edited = event.copy(title = "Reopened and edited").moveEntry(event.sets.last().id, event.sets.last().entries.last().id, -1)
        val saved = EventDraft(edited, event).saveTo(workspace, store)
        check(!saved.draft.isDirty)
        val after = FileWorkspaceStore(dir.resolve("event-smoke.json")).load()
        check((after.projects.first().events.flatMap { it.sets }.map { it.id } to after.projects.first().events.flatMap { it.entries }.map { it.id }.toSet()) == idsBefore)
        check(after.projects.first().events.first() == edited)
        check(after.projects.first().events.first().title == "Reopened and edited")
        check(after.projects.first().masters == project.masters)
        check(after.projects.first().eventsExportRoot == project.eventsExportRoot && project.eventsExportRoot != null)
        val cumulative = FileWorkspaceStore(dir.resolve("cumulative-event-smoke.json")).load().projects.single()
        check(cumulative.supplements.size == 1)
        check(EventExportPlanner.preview(cumulative, cumulative.events.single().id).members.all {
            it.matchedCount == 1 && it.entries.single().distribution?.supplementNumber == 1 && it.entries.single().filename == "Q New Tune -r00- C.pdf"
        })
        val lifecycleStore = FileWorkspaceStore(dir.resolve("management-smoke.json"))
        var lifecycle = lifecycleStore.load().projects.single()
        if (lifecycle.events.isNotEmpty()) {
            check(lifecycle.eventExports.count { it.state == EventExportState.ACTIVE } == 1)
            val lifecycleEvent = lifecycle.events.single()
            lifecycle = lifecycle.saveEvent(lifecycleEvent.copy(title = "Reopened lifecycle event"))
            lifecycleStore.save(Workspace().saveProject(lifecycle))
            check(lifecycle.eventExports.single { it.state == EventExportState.ACTIVE }.event.id == lifecycleEvent.id)
            EventExports.deleteEvent(lifecycle, lifecycleEvent.id) { lifecycleStore.save(Workspace().saveProject(it)) }
        } else {
            EventExports.cleanup(lifecycle, { lifecycleStore.save(Workspace().saveProject(it)) })
        }
        check(lifecycleStore.load().projects.single().eventExports.all { it.state == EventExportState.DELETED })
        Files.writeString(dir.resolve("management-restart-passed.txt"), "A separate executable process reopened tracked packages and retained the link through a title edit.")
        CoverSmoke.restart(dir)
        EventHistorySmoke.restart(dir)
        Files.writeString(dir.resolve("event-restart-passed.txt"), "New process reopened multiple sets and repeated tunes, edited order, and preserved identities, masters and project isolation.")
        exitProcess(0)
    }
    if ("--smoke-test" in args) {
        val dir = dataDirectory()
        Files.createDirectories(dir)
        val store = FileWorkspaceStore(dir.resolve("smoke.json"))
        val initial = Workspace().saveProject(quaternity(newId(), ::newId))
        store.save(initial)
        check(store.load() == initial)
        check(Thread.currentThread().contextClassLoader.getResource("fonts/Lato-Regular.ttf") != null)
        Files.writeString(dir.resolve("synthetic-proof.4ss"), proofXml())
        val proofZip = dir.resolve("synthetic-proof.zip")
        if (!Files.exists(proofZip)) writeProofZip(proofZip)
        val charts = dir.resolve("test-charts")
        Files.createDirectories(charts)
        Files.write(charts.resolve("Q Smoke Tune -r00- C.pdf"), syntheticPdf())
        val catalog = Catalog(source = charts.toString()).reconcile(LocalCharts.scan(charts.toString()), "Q", 1L, ::newId).acceptNew()
        check(catalog.active().size == 1 && catalog.status(catalog.active().single()) == "Ready")
        store.save(initial.copy(projects = initial.projects.map { it.copy(catalog = catalog) }))
        check(store.load().projects.single().catalog == catalog)
        val previewProject = store.load().projects.single().copy(exportDestination = dir.toString(), eventsExportRoot = Files.createDirectories(dir.resolve("event-exports")).toString())
        store.save(initial.copy(projects = listOf(previewProject)))
        val master = MasterPlanner.preview(store.load().projects.single())
        check(master.members.size == 4 && master.members.all { it.selectedCount == 1 })
        check(master.members.all { it.fallbackCount == 1 })
        check(store.load().projects.single().exportDestination == dir.toString())
        val exportDir = Files.createDirectories(dir.resolve("master-exports"))
        val exportProject = previewProject.copy(exportDestination = exportDir.toString())
        val exported = MasterExporter.generate(exportProject, MasterPlanner.preview(exportProject))
        check(exported.members.count { it.zipName != null } == 4)
        check(MasterExporter.recover(exportProject, exported.folder) == exported)
        store.save(Workspace().saveProject(exportProject.recordMaster(exported)))
        check(store.load().projects.single().masters.single() == exported)
        val baselineProject = store.load().projects.single()
        check(PendingPlanner.calculate(baselineProject).count(PendingKind.UNCHANGED) == 4)
        check(PendingSources.check(baselineProject).issues.isEmpty())
        Files.write(charts.resolve("Q New Tune -r00- C.pdf"), syntheticPdf())
        val pendingProject = baselineProject.copy(catalog = baselineProject.catalog.reconcile(LocalCharts.scan(charts.toString()), "Q", 2L, ::newId).acceptNew())
        check(PendingPlanner.calculate(pendingProject).count(PendingKind.NEW_TUNE) == 4)
        val eventPath = dir.resolve("event-smoke.json")
        val eventStore = FileWorkspaceStore(eventPath)
        val oldWorkspace = Workspace().saveProject(baselineProject).saveProject(Project("event-other", "Other band")).select(baselineProject.id)
        val legacyText = WorkspaceCodec.encode(oldWorkspace).replace("\"schemaVersion\": 10", "\"schemaVersion\": 3")
        Files.writeString(eventPath, legacyText)
        var eventWorkspace = eventStore.load()
        EventType.entries.forEach { type ->
            var event = newEvent(baselineProject, type, ::newId).copy(title = "Smoke ${type.name}", date = "2026-10-21")
                .addTune(baselineProject, catalog.tunes.single().id, ::newId)
            event = event.addSet(::newId)
            val destination = event.sets.last().id
            event = event.addTune(baselineProject, catalog.tunes.single().id, ::newId, destination)
                .addTune(baselineProject, catalog.tunes.single().id, ::newId, destination)
            eventWorkspace = EventDraft(event).saveTo(eventWorkspace, eventStore).workspace
        }
        check(eventWorkspace.projects.first().masters == baselineProject.masters)
        check(Files.readString(eventPath.resolveSibling("workspace.pre-events.backup.json")) == legacyText)
        check(eventStore.load() == eventWorkspace)
        val eventProject = eventWorkspace.projects.first()
        val eventPlan = EventExportPlanner.preview(eventProject, eventProject.events.first().id)
        val eventSnapshot = WorkspaceCodec.encode(eventWorkspace)
        val eventOutput = EventExporter.generate(eventProject, eventPlan, System.getProperty("bandlogger.eventSmokeDestination") ?: requireNotNull(eventProject.eventsExportRoot))
        check(eventOutput.zipNames.size == eventProject.members.size)
        check(eventOutput.folder.parent.fileName.toString() == FileLocations.eventTypeFolder(eventPlan.event.type))
        eventProject.events.filter { it.id != eventPlan.event.id }.forEach { savedEvent ->
            val routedPlan = EventExportPlanner.preview(eventProject, savedEvent.id)
            val routed = EventExporter.generate(eventProject, routedPlan, requireNotNull(eventProject.eventsExportRoot))
            check(routed.folder.parent.fileName.toString() == FileLocations.eventTypeFolder(savedEvent.type))
        }
        eventOutput.zipNames.forEach { name ->
            val member = eventPlan.members.single { "${it.baseName}.zip" == name }
            java.util.zip.ZipInputStream(Files.newInputStream(eventOutput.folder.resolve(name))).use { zip ->
                var count = 0
                var sawXml = false
                var sawReadme = false
                while (true) {
                    val entry = zip.nextEntry ?: break
                    count++
                    check(!entry.name.endsWith(".pdf", true))
                    val content = zip.readBytes().toString(Charsets.UTF_8)
                    if (entry.name == "${member.baseName}.4ss") {
                        check(!sawXml); sawXml = true
                        check(content == EventExportPlanner.xml(member))
                        EventExporter.validateXml(content)
                        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                        val root = factory.newDocumentBuilder().parse(org.xml.sax.InputSource(java.io.StringReader(content))).documentElement
                        check(root.getElementsByTagName("score").length == 3)
                        val headings = root.getElementsByTagName("placeholder")
                        check(headings.length == 2)
                        check((headings.item(0) as org.w3c.dom.Element).getAttribute("title") == "Set 1")
                        check((headings.item(1) as org.w3c.dom.Element).getAttribute("title") == "Set 2")
                        val scores = root.getElementsByTagName("score")
                        for (i in 0 until scores.length) {
                            val score = scores.item(i) as org.w3c.dom.Element
                            check(!score.hasAttribute("data") && score.getAttribute("path") == "Q Smoke Tune -r00- C.pdf")
                        }
                    } else { check(entry.name == "READ-ME.txt" && !sawReadme); sawReadme = true }
                    zip.closeEntry()
                }
                check(count == 2 && sawXml && sawReadme)
            }
        }
        check(WorkspaceCodec.encode(eventStore.load()) == eventSnapshot)
        Files.writeString(dir.resolve("event-export-passed.txt"), "Reference-only member event ZIPs parsed; complete multiple sets, repeats, filenames, no PDF payloads and workspace preservation passed.")
        val supplementPlan = SupplementPlanner.preview(pendingProject, PendingSources.check(pendingProject).issues)
        check(supplementPlan.selectedCount == 4)
        val supplement = SupplementExporter.generate(pendingProject, supplementPlan)
        val recordedSupplement = pendingProject.recordSupplement(supplement)
        store.save(Workspace().saveProject(recordedSupplement))
        SupplementExporter.clearRecordedIntent(recordedSupplement)
        check(store.load().projects.single().supplements.single() == supplement)
        check(java.nio.file.Path.of(supplement.publication.folder).parent.fileName.toString() == "Supplements")
        supplement.publication.members.filter { it.zipName != null }.forEach { member ->
            check(member.zipName == "${EventExportPlanner.safeLabel(recordedSupplement.name, 50)} - Supplement 001 - ${EventExportPlanner.safeLabel(member.memberName, 32)}.zip")
        }
        check(SupplementPlanner.preview(store.load().projects.single()).selectedCount == 0)
        check(SupplementExporter.recover(recordedSupplement, supplement.publication.folder) == supplement)
        supplement.publication.members.filter { it.zipName != null }.forEach { member ->
            java.util.zip.ZipInputStream(Files.newInputStream(java.nio.file.Path.of(supplement.publication.folder).resolve(member.zipName!!))).use { zip ->
                var xmlSeen = false
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.name.endsWith(".4ss")) {
                        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                        val xml = zip.readBytes()
                        val doc = factory.newDocumentBuilder().parse(java.io.ByteArrayInputStream(xml))
                        val scores = doc.getElementsByTagName("score")
                        check(scores.length == 1)
                        val score = scores.item(0) as org.w3c.dom.Element
                        check(score.getAttribute("path") == "Q New Tune -r00- C.pdf")
                        check(java.util.Base64.getDecoder().decode(score.getAttribute("data")).contentEquals(syntheticPdf()))
                        xmlSeen = true
                    }
                    zip.closeEntry()
                }
                check(xmlSeen)
            }
        }
        Files.writeString(dir.resolve("supplement-passed.txt"), "Numbered supplement ZIP/XML/PDF bytes, persistence, cumulative pending and recovery passed.")
        val cumulativeStore = FileWorkspaceStore(dir.resolve("cumulative-event-smoke.json"))
        val cumulativeEvent = newEvent(recordedSupplement, EventType.GIG, ::newId).copy(title = "Supplement reference smoke", date = "2026-10-21")
            .addTune(recordedSupplement, recordedSupplement.catalog.tunes.single { it.title == "New Tune" }.id, ::newId)
        cumulativeStore.save(Workspace().saveProject(recordedSupplement.saveEvent(cumulativeEvent)))
        val cumulativeSnapshot = Files.readAllBytes(dir.resolve("cumulative-event-smoke.json"))
        val cumulativeProject = cumulativeStore.load().projects.single()
        val cumulativePlan = EventExportPlanner.preview(cumulativeProject, cumulativeEvent.id)
        check(cumulativePlan.members.all { it.matchedCount == 1 && it.missingCount == 0 && it.entries.single().distribution?.supplementNumber == 1 })
        val cumulativeOutput = EventExporter.generate(cumulativeProject, cumulativePlan, requireNotNull(cumulativeProject.eventsExportRoot))
        cumulativeOutput.zipNames.forEach { name ->
            java.util.zip.ZipInputStream(Files.newInputStream(cumulativeOutput.folder.resolve(name))).use { zip ->
                var sawXml = false
                var entries = 0
                while (true) {
                    val entry = zip.nextEntry ?: break
                    entries++
                    check(!entry.name.endsWith(".pdf", true))
                    val text = zip.readBytes().decodeToString()
                    if (entry.name.endsWith(".4ss")) {
                        EventExporter.validateXml(text)
                        check(!text.contains("data=") && !text.contains("JVBER"))
                        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                        val doc = factory.newDocumentBuilder().parse(org.xml.sax.InputSource(java.io.StringReader(text)))
                        val scores = doc.getElementsByTagName("score")
                        check(scores.length == 1)
                        check((scores.item(0) as org.w3c.dom.Element).getAttribute("path") == "Q New Tune -r00- C.pdf")
                        sawXml = true
                    } else check(entry.name == "READ-ME.txt")
                    zip.closeEntry()
                }
                check(entries == 2 && sawXml)
            }
        }
        check(Files.readAllBytes(dir.resolve("cumulative-event-smoke.json")).contentEquals(cumulativeSnapshot))
        Files.writeString(dir.resolve("cumulative-event-passed.txt"), "Master -> recorded supplement -> saved/reopened event -> exact reference-only XML, no PDF payload and unchanged workspace passed.")
        // Dedicated durable lifecycle fixture: all charts are synthetic, all metadata local.
        val managementStore = FileWorkspaceStore(dir.resolve("management-smoke.json"))
        val managedRoot = System.getProperty("bandlogger.managementSmokeDestination") ?: Files.createDirectories(dir.resolve("managed-exports")).toString()
        var managed = cumulativeProject.copy(exportDestination = managedRoot, eventExports = emptyList())
        fun saveManaged(value: Project) { managementStore.save(Workspace().saveProject(value)); managed = value }
        saveManaged(managed)
        val managedEvent = managed.events.single()
        val managedPlan = EventExportPlanner.preview(managed, managedEvent.id)
        val managedOne = EventExports.publish(managed, managedPlan, managedRoot, true, true, EventConflict.NEW, ::saveManaged)
        val managedTwo = EventExports.publish(managed, managedPlan, managedRoot, true, true, EventConflict.REVISION, ::saveManaged)
        check(managedTwo.folder.fileName.toString().endsWith("Revision 2"))
        check(managementStore.load().projects.single().eventExports.size == 2)
        val afterEdit = managed.saveEvent(managedEvent.copy(title = "Edited lifecycle event", date = "2026-11-01"))
        saveManaged(afterEdit)
        check(managed.eventExports.all { it.event.id == managedEvent.id })
        EventExports.deleteEvent(managed, managedEvent.id, ::saveManaged)
        check(managed.events.isEmpty() && managed.eventExports.all { it.state == EventExportState.DELETED })
        check(!Files.exists(managedOne.folder) && !Files.exists(managedTwo.folder))
        // Keep a separate tracked event for a genuine second-process restart check.
        saveManaged(managed.saveEvent(managedEvent))
        EventExports.publish(managed, EventExportPlanner.preview(managed, managedEvent.id), managedRoot, true, true, EventConflict.NEW, ::saveManaged)
        Files.writeString(dir.resolve("event-management-passed.txt"), "Durable tracking, explicit revision, title/date edit, verified deletion and new restart fixture passed.")
        Files.writeString(dir.resolve("file-organization-passed.txt"), "Readable supplement folders/ZIPs, separate persisted Events root and both saved-type routes passed.")
        CoverSmoke.run(cumulativeProject,dir)
        EventHistorySmoke.run(cumulativeProject,dir)
        Files.writeString(dir.resolve("smoke-passed.txt"), "Storage, fonts, XML, ZIP, scan, master generation/recovery, pending, events and reference-only member exports passed.")
        exitProcess(0)
    }
    // Keep two windows/processes from overwriting the same metadata snapshot.
    Files.createDirectories(dataDirectory())
    val channel = java.nio.channels.FileChannel.open(dataDirectory().resolve("workspace.lock"),
        java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.WRITE)
    val lock = channel.tryLock()
    if (lock == null) {
        javax.swing.JOptionPane.showMessageDialog(null, "band.logger is already open. Use its existing window.")
        channel.close()
        return
    }
    Runtime.getRuntime().addShutdownHook(Thread { lock.release(); channel.close() })
    application {
        var closeRequested by remember { mutableStateOf(false) }
        Window(onCloseRequest = { closeRequested = true }, title = "band.logger", state = rememberWindowState(width = 1120.dp, height = 800.dp)) {
            App(closeRequested, { closeRequested = false }, ::exitApplication)
        }
    }
}
@Composable fun App(closeRequested: Boolean = false, resetClose: () -> Unit = {}, close: () -> Unit = {}) {
    val directory = remember { dataDirectory() }
    val store = remember { FileWorkspaceStore(directory.resolve("workspace.json")) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var workspace by remember { mutableStateOf(try { store.load() } catch (e: Exception) { loadError = e.message; Workspace() }) }
    var message by remember { mutableStateOf("") }
    var projectEditor by remember { mutableStateOf<Project?>(null) }
    var memberEditor by remember { mutableStateOf<Member?>(null) }
    var themeEditor by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf("Home") }
    var busy by remember { mutableStateOf(false) }
    var eventDraft by remember { mutableStateOf<EventDraft?>(null) }
    var pendingLeave by remember { mutableStateOf<(() -> Unit)?>(null) }
    var eventError by remember { mutableStateOf<String?>(null) }
    val project = workspace.projects.find { it.id == workspace.selectedId }
    val theme = project?.theme ?: Theme()
    val typography = Typography().let { t -> t.copy(
        displaySmall = t.displaySmall.copy(fontFamily = lato), headlineMedium = t.headlineMedium.copy(fontFamily = lato),
        headlineSmall = t.headlineSmall.copy(fontFamily = lato), titleLarge = t.titleLarge.copy(fontFamily = lato),
        titleMedium = t.titleMedium.copy(fontFamily = lato), bodyLarge = t.bodyLarge.copy(fontFamily = lato),
        bodyMedium = t.bodyMedium.copy(fontFamily = lato), bodySmall = t.bodySmall.copy(fontFamily = lato),
        labelLarge = t.labelLarge.copy(fontFamily = lato), labelMedium = t.labelMedium.copy(fontFamily = lato),
        labelSmall = t.labelSmall.copy(fontFamily = lato)
    ) }
    fun persist(next: Workspace) {
        try { store.save(next); workspace = next; message = "Saved on this computer." }
        catch (e: Exception) { message = "Could not save: ${e.message}"; throw e }
    }
    fun leave(action: () -> Unit) {
        if (eventDraft?.isDirty == true) pendingLeave = action
        else { eventDraft = null; eventError = null; action() }
    }
    fun saveEventDraft(): Boolean {
        val draft = eventDraft ?: return true
        return try {
            val saved = draft.saveTo(workspace, store)
            workspace = saved.workspace
            eventDraft = saved.draft
            message = "Event saved on this computer."
            eventError = null
            true
        } catch (e: Exception) { eventError = e.message ?: "Could not save event."; false }
    }
    fun openEvent(value: BandEvent, existing: Boolean) {
        eventDraft = EventDraft(value, if (existing) value else null)
        eventError = null
        page = "Event editor"
    }
    LaunchedEffect(closeRequested) {
        if (closeRequested) {
            resetClose()
            if (busy) message = "Please wait for the current operation to finish before closing."
            else leave(close)
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(
        primary = hex(theme.accent), onPrimary = hex(theme.background),
        secondary = hex(theme.secondary), background = hex(theme.background),
        onBackground = hex(theme.foreground), surface = hex(theme.surface), onSurface = hex(theme.foreground), surfaceVariant = hex(theme.surface), surfaceContainerHighest = hex(theme.surface), surfaceContainerHigh = hex(theme.surface)
    ), typography = typography) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (loadError != null) {
                Column(Modifier.padding(40.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("Your saved workspace could not be opened", style = MaterialTheme.typography.headlineMedium)
                    Text("Your data has not been overwritten. Close the application and preserve workspace.json and workspace.backup.json in $directory.")
                    Text(loadError!!)
                }
            } else Row(Modifier.fillMaxSize()) {
                Column(Modifier.width(250.dp).fillMaxHeight().background(hex(theme.surface)).padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("band.logger", style = MaterialTheme.typography.titleLarge, color = hex(theme.accent))
                    Text("YOUR MUSIC. TOGETHER.", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(24.dp))
                    Text("PROJECTS", color = hex(theme.secondary))

                    workspace.projects.forEach { p ->
                        val selected = p.id == project?.id
                        OutlinedButton(
                            enabled = !busy,
                            onClick = { leave { runCatching { persist(workspace.select(p.id)); page = "Home" } } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (selected) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = hex(p.theme.accent),
                                    contentColor = hex(p.theme.background)
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            if (Branding.hasLogo(p)) { QuaternityMark(Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
                            Text((if (selected) "• " else "") + p.name)
                        }
                    }

                    Button(enabled = !busy, onClick = { leave { projectEditor = Project(newId(), "") } }, modifier = Modifier.fillMaxWidth()) { Text("Create project") }
                    if (workspace.projects.none { it.name == "Quaternity" }) {
                        TextButton(enabled = !busy, onClick = { leave { projectEditor = quaternity(newId(), ::newId) } }) { Text("Start with Quaternity") }
                    }
                    Spacer(Modifier.height(20.dp))
                    if (project != null) listOf("Home", "Events", "Library", "Library master", "Pending Distribution", "Members", "Theme", "Technical proof").forEach { item ->
                        TextButton(enabled = !busy, onClick = { leave { page = item } }) { Text(if (page == item) "• $item" else item) }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("0.10.1 · PREVIEW", style = MaterialTheme.typography.labelSmall)
                    Text("Saved locally. No account needed.", style = MaterialTheme.typography.bodySmall)
                }
                Column(Modifier.weight(1f).fillMaxHeight().padding(if (page == "Event editor") 20.dp else 36.dp).then(if (page == "Event editor") Modifier else Modifier.verticalScroll(rememberScrollState())), verticalArrangement = Arrangement.spacedBy(if (page == "Event editor") 8.dp else 20.dp)) {
                    if (project == null) {
                        Text("Make room for the music.", style = MaterialTheme.typography.displaySmall)
                        Text("Create a project for your band or ensemble. Each project has its own members, chart preferences, and visual identity.")
                        Text("Choose “Start with Quaternity” to review the suggested four-member setup.")
                    } else {
                        ProjectIdentity(project, compact = page == "Event editor")
                        Text("$page  /  Project workspace", color = hex(theme.secondary))
                        when (page) {
                            "Home" -> key(project.id) { HomeScreen(project, { projectEditor = project }, { page = "Pending Distribution" }, { openEvent(newEvent(project, it, ::newId), false) }, { page = "Events" }) }
                            "Events" -> key(project.id) { EventsScreen(project, { openEvent(newEvent(project, it, ::newId), false) }, { openEvent(it, true) }, busy, { busy = it }, { persist(workspace.saveProject(it)) }) }
                            "Event editor" -> eventDraft?.let { draft ->
                                key(project.id, draft.value.id) {
                                    EventEditorScreen(project, draft, eventError, { eventDraft = draft.copy(value = it); eventError = null }, { saveEventDraft() }, { leave { page = "Events" } }, { busy = it }, { updated -> persist(workspace.saveProject(updated)) }, Modifier.weight(1f))
                                }
                            }
                            "Pending Distribution" -> key(project.id) { PendingDistributionScreen(project, { page = "Library master" }, { page = "Library" }, busy, { busy = it }) { updated -> persist(workspace.saveProject(updated)) } }
                            "Library" -> key(project.id) { LibraryScreen(project, busy, { busy = it }) { updated -> persist(workspace.saveProject(updated)) } }
                            "Library master" -> key(project.id) { MasterScreen(project, busy, { busy = it }) { updated -> persist(workspace.saveProject(updated)) } }
                            "Members" -> {
                                Text("Roles describe what each person plays. Chart preferences are ordered, with the preferred part first.")
                                Button(onClick = { memberEditor = Member(newId(), "", emptyList(), emptyList()) }) { Text("Add member") }
                                project.members.forEach { m ->
                                    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(m.name, style = MaterialTheme.typography.titleLarge)
                                        Text(m.roles.joinToString(" · ").ifBlank { "No roles yet" })
                                        Text("Charts: " + m.preferences.joinToString(" → ").ifBlank { "Not set" }, color = hex(theme.secondary))
                                        TextButton(onClick = { memberEditor = m }) { Text("Edit member") }
                                    } }
                                }
                            }
                            "Theme" -> {
                                Text("Project colors", style = MaterialTheme.typography.headlineSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    listOf(theme.background, theme.foreground, theme.accent, theme.secondary, theme.surface).forEach {
                                        Box(Modifier.size(60.dp).background(hex(it)).border(1.dp, hex(theme.foreground)))
                                    }
                                }
                                Button(onClick = { themeEditor = true }) { Text("Edit colors") }
                                if (Branding.hasLogo(project)) QuaternityMark(Modifier.size(120.dp))
                                Text(if (Branding.hasLogo(project)) "Official Quaternity branding" else "Project name branding")
                                Text("Additional logo reference: ${project.logoReference ?: "Not set"}. Official project branding uses the bundled artwork.")
                            }
                            "Technical proof" -> {
                                Text("Try a synthetic forScore import", style = MaterialTheme.typography.headlineSmall)
                                Text("Creates two ordered PDF entries in an open-format .4ss file. These contain test text only. The ZIP transfer route and embedded PDF import have been verified on your iPad. Requires forScore 14.3 or later.")
                                Button(onClick = {
                                    val dialog = FileDialog(null as Frame?, "Save synthetic forScore proof", FileDialog.SAVE)
                                    dialog.file = "band.logger-synthetic-proof.zip"
                                    dialog.isVisible = true
                                    if (dialog.file != null) {
                                        runCatching {
                                            val filename = if (dialog.file.endsWith(".zip", true)) dialog.file else dialog.file + ".zip"
                                            val target = java.nio.file.Path.of(dialog.directory, filename)
                                            writeProofZip(target)
                                            message = "Proof saved: $target"
                                        }.onFailure { message = "Could not export; choose a new filename. ${it.message}" }
                                    }
                                    dialog.dispose()
                                }) { Text("Save forScore test ZIP") }
                                Text("Transfer the ZIP through Drive, save it locally on the iPad, extract it in Apple Files, then share the extracted .4ss to forScore and check both entries open in order. This is a format proof, not a band library export.")
                            }
                        }
                    }
                    if (message.isNotBlank()) Text(message, color = hex(theme.secondary))
                }
            }
        }
        if (pendingLeave != null) {
            AlertDialog(onDismissRequest = { pendingLeave = null },
                title = { Text("Unsaved event changes") },
                text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save this event before leaving, or discard your changes.")
                    eventError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                } },
                confirmButton = { Button(onClick = {
                    if (saveEventDraft()) {
                        val action = pendingLeave; pendingLeave = null; eventDraft = null; action?.invoke()
                    }
                }) { Text("Save and continue") } },
                dismissButton = { Row {
                    TextButton(onClick = { pendingLeave = null }) { Text("Keep editing") }
                    TextButton(onClick = {
                        val action = pendingLeave; pendingLeave = null; eventDraft = null; eventError = null; action?.invoke()
                    }) { Text("Discard changes") }
                } })
        }
        projectEditor?.let { draft ->
            ProjectDialog(draft, { projectEditor = null }) { updated ->
                persist(workspace.saveProject(updated)); projectEditor = null; page = "Home"
            }
        }
        if (project != null) {
            memberEditor?.let { draft ->
                MemberDialog(draft, { memberEditor = null }) { updated ->
                    val members = if (project.members.any { it.id == updated.id }) project.members.map { if (it.id == updated.id) updated else it } else project.members + updated
                    persist(workspace.saveProject(project.copy(members = members))); memberEditor = null
                }
            }
            if (themeEditor) ThemeDialog(project.theme, { themeEditor = false }) {
                persist(workspace.saveProject(project.copy(theme = it))); themeEditor = false
            }
        }
    }
}
@Composable fun FormDialog(title: String, dismiss: () -> Unit, save: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    var error by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) },
        text = { Column(Modifier.width(480.dp).heightIn(max = 500.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content(); if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        } }, confirmButton = { Button(onClick = { try { save() } catch (e: Exception) { error = e.message ?: "Could not save." } }) { Text("Save") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}
@Composable fun Field(label: String, value: String, change: (String) -> Unit) {
    OutlinedTextField(value, change, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
}
@Composable fun ProjectDialog(draft: Project, dismiss: () -> Unit, save: (Project) -> Unit) {
    var name by remember { mutableStateOf(draft.name) }
    var prefix by remember { mutableStateOf(draft.prefix) }
    var eventsRoot by remember { mutableStateOf(FileLocations.eventsInitialRoot(draft) ?: "") }
    var logo by remember { mutableStateOf(draft.logoReference ?: "") }
    var officialBrand by remember { mutableStateOf(draft.brandId == Branding.QUATERNITY) }
    FormDialog("Project details", dismiss, { save(draft.copy(name = name.trim(), prefix = prefix.trim(), logoReference = logo.trim().ifEmpty { null }, brandId = if (officialBrand) Branding.QUATERNITY else null, exportDestination = eventsRoot.trim().ifEmpty { null })) }) {
        Field("Project name", name) { name = it }
        Field("Filename prefix", prefix) { prefix = it }
        Field("Project export root (Library, Gigs, Rehearsals)", eventsRoot) { eventsRoot = it }
        TextButton(onClick = { chooseChartFolder("Choose Events export root")?.let { eventsRoot = it } }) { Text("Browse export root") }
        Text("Future exports use Library/Master, Library/Supplements, Gigs or Rehearsals. Historical paths remain unchanged.")
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(officialBrand,{ officialBrand = it }); Text("Use official Quaternity branding for this project")
        }
        Field("Optional logo reference (stored for later)", logo) { logo = it }
        Text("New projects use the Quaternity palette as an editable starting point. Change colors under Theme.")
        if (draft.members.isNotEmpty()) Text("Includes ${draft.members.size} editable suggested members.")
    }
}
@Composable fun MemberDialog(draft: Member, dismiss: () -> Unit, save: (Member) -> Unit) {
    var name by remember { mutableStateOf(draft.name) }
    var roles by remember { mutableStateOf(draft.roles.joinToString(", ")) }
    var prefs by remember { mutableStateOf(draft.preferences.joinToString(", ")) }
    fun split(value: String) = value.split(',').map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    FormDialog("Member details", dismiss, { save(draft.copy(name = name.trim(), roles = split(roles), preferences = split(prefs))) }) {
        Field("Name", name) { name = it }
        Field("Roles, separated by commas", roles) { roles = it }
        Field("Chart preferences, in order", prefs) { prefs = it }
        Text("Examples: Guitar, C or Bb, C. These are editable preferences, not song assignments.")
    }
}
@Composable fun ThemeDialog(draft: Theme, dismiss: () -> Unit, save: (Theme) -> Unit) {
    var colors by remember { mutableStateOf(listOf(draft.background, draft.foreground, draft.accent, draft.secondary, draft.surface)) }
    FormDialog("Project colors", dismiss, { val t = Theme(colors[0], colors[1], colors[2], colors[3], colors[4]); t.validate(); save(t) }) {
        listOf("Background", "Text", "Gold / primary accent", "Secondary accent", "Raised surface").forEachIndexed { i, label ->
            Field(label, colors[i]) { text -> colors = colors.mapIndexed { j, old -> if (i == j) text else old } }
        }
        TextButton(onClick = { colors = listOf("#171B24", "#F4F2EA", "#E7C66A", "#7698AD", "#242B36") }) { Text("Use Quaternity palette") }
    }
}
