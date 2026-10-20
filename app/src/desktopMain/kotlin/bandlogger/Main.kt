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
    if ("--smoke-test" in args) {
        val dir = dataDirectory()
        Files.createDirectories(dir)
        val store = FileWorkspaceStore(dir.resolve("smoke.json"))
        val initial = Workspace().saveProject(quaternity(newId(), ::newId))
        store.save(initial)
        check(store.load() == initial)
        check(Thread.currentThread().contextClassLoader.getResource("fonts/Lato-Regular.ttf") != null)
        Files.writeString(dir.resolve("synthetic-proof.4ss"), proofXml())
        Files.writeString(dir.resolve("smoke-passed.txt"), "Storage, bundled fonts and XML generation passed.")
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
                        OutlinedButton(onClick = { runCatching { persist(workspace.select(p.id)) }; page = "Home" }, modifier = Modifier.fillMaxWidth()) {
                            Text((if (p.id == project?.id) "• " else "") + p.name)
                        }
                    }
                    Button(onClick = { projectEditor = Project(newId(), "") }, modifier = Modifier.fillMaxWidth()) { Text("Create project") }
                    if (workspace.projects.none { it.name == "Quaternity" }) {
                        TextButton(onClick = { projectEditor = quaternity(newId(), ::newId) }) { Text("Start with Quaternity") }
                    }
                    Spacer(Modifier.height(20.dp))
                    if (project != null) listOf("Home", "Members", "Theme", "Technical proof").forEach { item ->
                        TextButton(onClick = { page = item }) { Text(if (page == item) "• $item" else item) }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("CHUNK 1 · PREVIEW", style = MaterialTheme.typography.labelSmall)
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
                            "Home" -> {
                                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("A place for your ensemble", style = MaterialTheme.typography.headlineSmall)
                                    Text("${project.members.size} members · Filename prefix: ${project.prefix.ifBlank { "None" }}")
                                    Text("Brand placeholder · ${project.name}", color = hex(theme.secondary))
                                    Button(onClick = { projectEditor = project }) { Text("Edit project") }
                                } }
                                Text("Next chapters", style = MaterialTheme.typography.titleLarge)
                                Text("Chart library and Drive connection — planned for Chunk 2.\nLibrary masters and supplements — planned for later chunks.")
                            }
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
                                Text("Creates two ordered PDF entries in an open-format .4ss file. These contain test text only. Requires forScore 14.3 or later; actual iPad import is awaiting your review.")
                                Button(onClick = {
                                    val dialog = FileDialog(null as Frame?, "Save synthetic forScore proof", FileDialog.SAVE)
                                    dialog.file = "band.logger-synthetic-proof.4ss"
                                    dialog.isVisible = true
                                    if (dialog.file != null) {
                                        runCatching {
                                            val filename = if (dialog.file.endsWith(".4ss", true)) dialog.file else dialog.file + ".4ss"
                                            val target = java.nio.file.Path.of(dialog.directory, filename)
                                            Files.writeString(target, proofXml(), CREATE_NEW)
                                            message = "Proof saved: $target"
                                        }.onFailure { message = "Could not export; choose a new filename. ${it.message}" }
                                    }
                                    dialog.dispose()
                                }) { Text("Save synthetic .4ss") }
                                Text("Transfer the file to your iPad using Files or AirDrop, share it to forScore, and check both entries open in order. This is a format proof, not a band library export.")
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