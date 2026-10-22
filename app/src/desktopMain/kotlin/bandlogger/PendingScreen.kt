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

@Composable
fun PendingDistributionScreen(project: Project, openMaster: () -> Unit, openLibrary: () -> Unit, busy: Boolean, setBusy: (Boolean) -> Unit, save: (Project) -> Unit) {
    var check by remember(project) { mutableStateOf<PendingSourceCheck?>(null) }
    var refresh by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var showUnchanged by remember { mutableStateOf(false) }
    LaunchedEffect(project, refresh) {
        check = null
        check = withContext(Dispatchers.IO) { PendingSources.check(project) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SupplementControls(project, busy, setBusy, save)
        Text("Pending Distribution", style = MaterialTheme.typography.headlineSmall)
        Text("Compares approved catalog selections with each musician's recorded master plus published supplements, tracking each part independently. Generated exports are not confirmation of receipt or import.")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = openLibrary) { Text("Library / Needs review") }
            OutlinedButton(onClick = openMaster) { Text("Library master") }
            OutlinedButton(enabled = check != null, onClick = { refresh++ }) { Text("Recheck source files") }
        }
        Text("Read-only file check on entry and recheck. Refresh Library to discover new files or save review changes. Files can change after this check.", style = MaterialTheme.typography.bodySmall)
        val verified = check
        if (verified == null) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Checking source PDFs before calculating ready items…")
        } else {
            val result = remember(project, verified) { PendingPlanner.calculate(project, verified.issues) }
            Text("New tunes: ${result.count(PendingKind.NEW_TUNE)}   ·   New parts: ${result.count(PendingKind.NEW_PART)}   ·   Revised charts: ${result.count(PendingKind.REVISED)}", color = MaterialTheme.colorScheme.primary)
            Text("Counts are member–tune selections, not unique tunes. Attention: ${result.count(PendingKind.ATTENTION)} member–tune exceptions and ${result.fileIssues.size} file issues.", style = MaterialTheme.typography.bodySmall)
            (result.notices + verified.notices).distinct().forEach { Text(it, color = MaterialTheme.colorScheme.secondary) }
            val initial = result.baselines.filter { it.master == null }
            if (initial.isNotEmpty()) Text("Initial Library Master required: ${initial.joinToString { it.member.name }}. Their approved selections are listed separately and excluded from supplement counts.", color = MaterialTheme.colorScheme.secondary)
            var baselinesOpen by remember { mutableStateOf(false) }
            TextButton(onClick = { baselinesOpen = !baselinesOpen }) { Text(if (baselinesOpen) "Hide baselines" else "Inspect member baselines") }
            if (baselinesOpen) result.baselines.forEach { b ->
                Text("${b.member.name}: " + (b.master?.let { "${it.createdAt} · ${it.id}" } ?: "No valid non-empty master"), style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(query, { query = it }, singleLine = true, label = { Text("Search tune, part, musician or status") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(showUnchanged, { showUnchanged = it }); Text("Include unchanged selections (${result.count(PendingKind.UNCHANGED)})")
            }
            val visible = result.entries.filter { (showUnchanged || it.kind != PendingKind.UNCHANGED) &&
                (query.isBlank() || listOf(it.tune.title, it.member.name, it.chart?.metadata?.part.orEmpty(), it.kind.label, it.explanation).joinToString(" ").contains(query, true)) }
            val groups = visible.groupBy { it.tune.id }.values.toList()
            Box(Modifier.fillMaxWidth().height(380.dp)) {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (groups.isEmpty()) item {
                        Text(if (query.isNotBlank()) "No matching items." else if (initial.isNotEmpty()) "No supplemental queue yet. Create an initial master after resolving review issues." else "No pending selections. Check any warnings below; unchanged charts are excluded.")
                    }
                    items(groups, key = { it.first().tune.id }) { group ->
                        var expanded by remember(group.first().tune.id) { mutableStateOf(false) }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(group.first().tune.title, style = MaterialTheme.typography.titleMedium)
                                group.forEach { entry ->
                                    Text("${entry.member.name} · ${entry.kind.label}", color = MaterialTheme.colorScheme.secondary)
                                    Text(entry.chart?.metadata?.let { "${it.part} r${it.revision.toString().padStart(2, '0')}" } ?: "No eligible selection")
                                    entry.previous?.let { Text("Previously exported: ${it.part} r${it.revision.toString().padStart(2, '0')}", style = MaterialTheme.typography.bodySmall) }
                                    if (expanded || entry.kind == PendingKind.ATTENTION) Text(entry.explanation, style = MaterialTheme.typography.bodySmall)
                                    if (expanded) entry.chart?.let {
                                        Text("Chart ID: ${it.id}\nSHA-256: ${it.sha256}\n${it.relativePath}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide details" else "Inspect details") }
                            }
                        }
                    }
                    val issues = result.fileIssues.filter { query.isBlank() || (it.chart.relativePath + " " + it.reason).contains(query, true) }
                    if (issues.isNotEmpty()) item { Text("File issues — resolve in Library", style = MaterialTheme.typography.titleMedium) }
                    items(issues, key = { "file-${it.chart.id}" }) { issue -> Text("${issue.chart.relativePath}: ${issue.reason}", style = MaterialTheme.typography.bodySmall) }
                }
                VerticalScrollbar(rememberScrollbarAdapter(listState), Modifier.align(Alignment.CenterEnd).fillMaxHeight())
            }
            Text("Use Preview next supplement to review and publish eligible selections. Generated exports do not confirm import.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
