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
        val previewProject = store.load().projects.single().copy(exportDestination = dir.toString())
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
        Files.writeString(dir.resolve("smoke-passed.txt"), "Storage, fonts, XML, ZIP, chart scan, preview, master generation and recovery passed.")
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
        Window(onCloseRequest = ::exitApplication, title = "band.logger", state = rememberWindowState(width = 1120.dp, height = 800.dp)) {
            App()
        }
    }
}
@Composable fun App() {
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
                            onClick = { runCatching { persist(workspace.select(p.id)) }; page = "Home" },
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
                            Text((if (selected) "• " else "") + p.name)
                        }
                    }

                    Button(enabled = !busy, onClick = { projectEditor = Project(newId(), "") }, modifier = Modifier.fillMaxWidth()) { Text("Create project") }
                    if (workspace.projects.none { it.name == "Quaternity" }) {
                        TextButton(enabled = !busy, onClick = { projectEditor = quaternity(newId(), ::newId) }) { Text("Start with Quaternity") }
                    }
                    Spacer(Modifier.height(20.dp))
                    if (project != null) listOf("Home", "Library", "Library master", "Pending Distribution", "Members", "Theme", "Technical proof").forEach { item ->
                        TextButton(enabled = !busy, onClick = { page = item }) { Text(if (page == item) "• $item" else item) }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("0.3.3 · PREVIEW", style = MaterialTheme.typography.labelSmall)
                    Text("Saved locally. No account needed.", style = MaterialTheme.typography.bodySmall)
                }
                Column(Modifier.weight(1f).padding(36.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (project == null) {
                        Text("Make room for the music.", style = MaterialTheme.typography.displaySmall)
                        Text("Create a project for your band or ensemble. Each project has its own members, chart preferences, and visual identity.")
                        Text("Choose “Start with Quaternity” to review the suggested four-member setup.")
                    } else {
                        Text(project.name, style = MaterialTheme.typography.displaySmall)
                        Text("$page  /  Project workspace", color = hex(theme.secondary))
                        when (page) {
                            "Home" -> key(project.id) { HomeScreen(project, { projectEditor = project }, { page = "Pending Distribution" }) }
                            "Pending Distribution" -> PendingDistributionScreen({ page = "Library master" }, { page = "Home" })
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
                                Text("Brand placeholder · ${project.name}")
                                Text("Logo reference: ${project.logoReference ?: "Not set"}. SVG rendering and approved artwork integration are pending.")
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
    var logo by remember { mutableStateOf(draft.logoReference ?: "") }
    FormDialog("Project details", dismiss, { save(draft.copy(name = name.trim(), prefix = prefix.trim(), logoReference = logo.trim().ifEmpty { null })) }) {
        Field("Project name", name) { name = it }
        Field("Filename prefix", prefix) { prefix = it }
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
