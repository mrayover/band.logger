package bandlogger

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.nio.file.Files
import java.nio.file.Path

@Composable fun MasterScreen(project: Project, busy: Boolean, setBusy: (Boolean) -> Unit, save: (Project) -> Unit) {
    val preview = remember(project) { MasterPlanner.preview(project) }
    var started by remember(project.id) { mutableStateOf(false) }
    var memberId by remember(project.id) { mutableStateOf(project.members.firstOrNull()?.id) }
    var issuesView by remember(project.id) { mutableStateOf(false) }
    var query by remember(project.id) { mutableStateOf("") }
    var exceptionsOnly by remember(project.id) { mutableStateOf(false) }
    var destinationDraft by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf("") }
    var confirmMaster by remember { mutableStateOf(false) }
    var recoveryDraft by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Library master", style = MaterialTheme.typography.headlineSmall)
        if (!started) Text("Preview which chart each member would receive. Review allocations, then confirm to generate member ZIP packages.")
        Text("Uses the last saved library scan. Refresh in Library after changing PDFs. Only accepted charts can be selected.", style = MaterialTheme.typography.bodySmall)
        if (!started) Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Export destination", style = MaterialTheme.typography.titleMedium)
                Text(FileLocations.masterDestination(project) ?: "Choose where future library packages will be saved.")
                OutlinedButton(enabled = !busy, onClick = { destinationDraft = project.exportDestination ?: "" }) {
                    Text(if (project.exportDestination == null) "Choose destination" else "Change destination")
                }
                Text("Saving this setting creates no export files. You can preview before choosing a folder.", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (started) {
            Text("Destination: " + (FileLocations.masterDestination(project) ?: "Not chosen"), style = MaterialTheme.typography.bodySmall)
            TextButton(enabled = !busy, onClick = { destinationDraft = project.exportDestination ?: "" }) { Text("Choose or change destination") }
        }
        if (notice.isNotBlank()) Text(notice, color = MaterialTheme.colorScheme.secondary)
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        Button(enabled = !busy, onClick = { started = true }) { Text(if (started) "Update preview" else "Create Library Master · Preview") }
        if (started) {
            preview.notices.forEach { Text(it, color = MaterialTheme.colorScheme.secondary) }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                preview.members.forEach { allocation ->
                    OutlinedButton(onClick = { memberId = allocation.member.id; issuesView = false }) {
                        Text((if (memberId == allocation.member.id && !issuesView) "• " else "") + allocation.member.name)
                    }
                }
                OutlinedButton(onClick = { issuesView = true }) { Text("File issues (${preview.issues.size})") }
            }
            OutlinedTextField(query, { query = it }, label = { Text("Search preview by title, part or filename") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            val member = preview.members.find { it.member.id == memberId } ?: preview.members.firstOrNull()
            if (!issuesView && member != null) {
                Text(member.member.name, style = MaterialTheme.typography.titleLarge)
                Text("Preferences: " + member.preferences.joinToString(" → ").ifEmpty { "Not set" })
                Text("${member.selectedCount} selected · ${member.fallbackCount} fallbacks · ${member.blockedCount} need attention · ${member.missingCount} missing")
                member.warnings.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(exceptionsOnly, { exceptionsOnly = it })
                    Text("Show fallbacks and omissions only")
                }
            }
            if (issuesView) Text("These records are not selected. Resolve them under Library → Needs review or History. Exclusions remain excluded.")
            val choices = member?.choices.orEmpty().filter { choice ->
                (!exceptionsOnly || choice.state != SelectionState.PRIMARY) &&
                    (query.isBlank() || (choice.tune.title + " " + choice.chart?.relativePath.orEmpty() + " " + choice.explanation).contains(query, true))
            }
            val issues = preview.issues.filter { query.isBlank() || (it.chart.relativePath + " " + it.reason).contains(query, true) }
            Box(Modifier.fillMaxWidth().height(360.dp)) {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (issuesView) {
                        if (issues.isEmpty()) item { Text("No matching file issues.") }
                        items(issues, key = { it.chart.id }) { issue ->
                            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) {
                                Text(issue.chart.relativePath, style = MaterialTheme.typography.titleMedium)
                                Text(issue.reason, color = MaterialTheme.colorScheme.secondary)
                            } }
                        }
                    } else {
                        if (choices.isEmpty()) item { Text("No matching tunes. Check Library and member preferences.") }
                        items(choices, key = { it.tune.id }) { choice ->
                            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(choice.tune.title, style = MaterialTheme.typography.titleMedium)
                                Text(when (choice.state) {
                                    SelectionState.PRIMARY -> "Selected"
                                    SelectionState.FALLBACK -> "Fallback selected"
                                    SelectionState.BLOCKED -> "Needs attention · omitted"
                                    SelectionState.MISSING -> "Missing preferred parts · omitted"
                                }, color = MaterialTheme.colorScheme.secondary)
                                choice.chart?.let { chart ->
                                    Text("${chart.metadata!!.part} · r${chart.metadata.revision.toString().padStart(2, '0')}")
                                    Text(chart.relativePath, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(choice.explanation, style = MaterialTheme.typography.bodySmall)
                            } }
                        }
                    }
                }
                VerticalScrollbar(rememberScrollbarAdapter(listState), Modifier.align(Alignment.CenterEnd).fillMaxHeight())
            }
            Button(enabled = !busy && project.exportDestination != null && preview.members.any { it.selectedCount > 0 },
                onClick = { confirmMaster = true }) { Text("Review and generate member ZIPs") }
            if (project.exportDestination == null) Text("Choose a destination before generating.")
            if (preview.members.none { it.selectedCount > 0 }) Text("No accepted charts selected. Review new charts in Library first.")
            Text("Each ZIP contains a .4ss file and iPad instructions. Review all fallbacks and omissions before confirming.", style = MaterialTheme.typography.bodySmall)
        }
    }
    project.masters.lastOrNull()?.let { master ->
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Last saved master", style = MaterialTheme.typography.titleMedium)
                Text(master.createdAt)
                Text("${master.members.count { it.zipName != null }} member packages · ${master.members.sumOf { it.charts.size }} chart selections")
                Text(master.folder, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(enabled = !busy, onClick = {
                    runCatching { java.awt.Desktop.getDesktop().open(Path.of(master.folder).toFile()) }
                        .onFailure { notice = "Could not open folder: ${it.message}" }
                }) { Text("Open master folder") }
                Text("Recorded file generation, not confirmation of iPad import.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    OutlinedButton(enabled = !busy, onClick = { recoveryDraft = "" }) { Text("Recover a completed master folder") }
    if (confirmMaster) {
        var acknowledged by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { confirmMaster = false }, title = { Text("Confirm Library Master") },
            text = {
                Column(Modifier.heightIn(max = 450.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Destination: ${FileLocations.masterDestination(project)}")
                    preview.members.forEach {
                        Text("${it.member.name}: ${it.selectedCount} charts, ${it.fallbackCount} fallbacks, ${it.blockedCount + it.missingCount} omitted." +
                            if (it.selectedCount == 0) " No ZIP will be generated." else "")
                    }
                    Text("${preview.issues.size} file issues remain in the catalog. Source PDFs are not modified.")
                    if (project.masters.isNotEmpty()) Text("A previous master exists. This creates another complete master, not a supplement. Reimporting may create duplicate scores in forScore.")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(acknowledged, { acknowledged = it })
                        Text("I reviewed the selections, fallbacks and omissions for every member.")
                    }
                }
            },
            confirmButton = {
                Button(enabled = acknowledged && !busy, onClick = {
                    confirmMaster = false
                    setBusy(true)
                    notice = "Verifying PDFs and creating member ZIPs…"
                    scope.launch {
                        var published: LibraryMaster? = null
                        try {
                            val master = withContext(Dispatchers.IO) { MasterExporter.generate(project, preview) }
                            published = master
                            save(project.recordMaster(master))
                            notice = "Saved ${master.members.count { it.zipName != null }} member ZIPs and the master record. Open the master folder below."
                        } catch (e: CancellationException) { throw e }
                        catch (e: Exception) {
                            notice = if (published == null) "Export failed: ${e.message}. No successful master was recorded."
                            else "Files are complete at ${published.folder}, but the app record could not be saved: ${e.message}. Use Recover a completed master folder; do not generate again."
                        } finally { setBusy(false) }
                    }
                }) { Text("Generate member ZIPs") }
            },
            dismissButton = { TextButton(onClick = { confirmMaster = false }) { Text("Back to preview") } })
    }
    recoveryDraft?.let { initial ->
        var location by remember(initial) { mutableStateOf(initial) }
        FormDialog("Recover completed master", { recoveryDraft = null }, {
            require(location.isNotBlank()) { "Choose the completed master folder containing manifest.json." }
            val folder = location.trim()
            recoveryDraft = null
            setBusy(true)
            scope.launch {
                try {
                    val master = withContext(Dispatchers.IO) { MasterExporter.recover(project, folder) }
                    save(project.recordMaster(master))
                    notice = "Master record recovered. No files regenerated."
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { notice = "Recovery failed: ${e.message}" }
                finally { setBusy(false) }
            }
        }) {
            Field("Completed master folder", location) { location = it }
            OutlinedButton(onClick = { chooseChartFolder("Choose completed master folder")?.let { location = it } }) { Text("Browse folders") }
            Text("Use this only when ZIP creation finished but saving the app record failed. Package checksums and project identity are verified. Repeating recovery does not duplicate the record.")
        }
    }
    destinationDraft?.let { initial ->
        var location by remember(initial) { mutableStateOf(initial) }
        FormDialog("Choose export destination", { destinationDraft = null }, {
            require(location.isNotBlank()) { "Choose or paste an existing folder." }
            val confirmed = location.trim()
            destinationDraft = null
            setBusy(true)
            scope.launch {
                try {
                    val destination = withContext(Dispatchers.IO) {
                        val folder = LocalCharts.folder(Path.of(confirmed))
                        require(Files.isWritable(Path.of(folder))) { "Choose a writable destination folder." }
                        folder
                    }
                    save(project.withExportRoot(FileLocations.numberedRoot(destination).toString()))
                    notice = "Destination saved for ${project.name}. No files generated."
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { notice = "Could not save destination: ${e.message}" }
                finally { setBusy(false) }
            }
        }) {
            Field("Folder location", location) { location = it }
            OutlinedButton(onClick = { chooseChartFolder("Choose export destination")?.let { location = it } }) { Text("Browse folders") }
            Text("Choose an existing local or Google Drive for desktop folder. Future exports will recheck access when writing.")
        }
    }
}