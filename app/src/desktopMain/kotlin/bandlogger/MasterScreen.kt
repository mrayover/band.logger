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
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Library master", style = MaterialTheme.typography.headlineSmall)
        if (!started) Text("Preview which chart each member would receive. Stage 3A does not generate library packages.")
        Text("Uses the last saved library scan. Refresh in Library after changing PDFs. Only accepted charts can be selected.", style = MaterialTheme.typography.bodySmall)
        if (!started) Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Export destination", style = MaterialTheme.typography.titleMedium)
                Text(project.exportDestination ?: "Choose where future library packages will be saved.")
                OutlinedButton(enabled = !busy, onClick = { destinationDraft = project.exportDestination ?: "" }) {
                    Text(if (project.exportDestination == null) "Choose destination" else "Change destination")
                }
                Text("Saving this setting creates no export files. You can preview before choosing a folder.", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (started) {
            Text("Preview only · Destination: " + (project.exportDestination ?: "Not chosen"), style = MaterialTheme.typography.bodySmall)
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
            Text("Preview only. ZIP generation and final confirmation arrive in Stage 3B. No master has been recorded.", style = MaterialTheme.typography.bodySmall)
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
                    save(project.copy(exportDestination = destination))
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