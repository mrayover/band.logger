package bandlogger

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

@Composable
fun EventExportDialog(project: Project, eventId: String, setBusy: (Boolean) -> Unit, dismiss: () -> Unit) {
    val planned = remember(project, eventId) { runCatching { EventExportPlanner.preview(project, eventId) } }
    val plan = planned.getOrNull()
    var destination by remember { mutableStateOf(project.exportDestination ?: "") }
    var missingAck by remember { mutableStateOf(false) }
    var undatedAck by remember { mutableStateOf(false) }
    var generating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<EventExportResult?>(null) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = { if (!generating) dismiss() },
        title = { Text(if (result == null) "Export for Members · Preview" else "Event export complete") },
        text = {
            Column(Modifier.width(620.dp).heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (plan == null) Text(planned.exceptionOrNull()?.message ?: "Could not preview this event.", color = MaterialTheme.colorScheme.error)
                else {
                    Text(plan.event.title, style = MaterialTheme.typography.titleLarge)
                    Text("Reference-only setlists. No PDFs are included or read. Each musician must already have the recorded filenames installed in forScore. Recorded master and supplement exports do not prove installation.")
                    if (result == null) {
                        OutlinedTextField(destination, { destination = it; error = null }, enabled = !generating, label = { Text("Existing export destination") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        TextButton(enabled = !generating, onClick = { chooseChartFolder("Choose event export destination")?.let { destination = it } }) { Text("Browse folders") }
                        Text("Output: $destination/Events/${plan.folderName}/ (new suffix if already present)", style = MaterialTheme.typography.bodySmall)
                        Text("${plan.members.first().sets.size} non-empty sets · ${plan.event.entries.size} song entries per member")
                        plan.warnings.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                        plan.members.forEach { member ->
                            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(member.member.name, style = MaterialTheme.typography.titleMedium)
                                Text("Preferred: ${member.preferredPart ?: "Not set (member-only label)"} · ${member.entries.size} total · ${member.matchedCount} matched · ${member.missingCount} missing")
                                Text("Baseline: ${member.baselineId ?: "No recorded non-empty Library Master — placeholder-only output"}", style = MaterialTheme.typography.bodySmall)
                                Text("References use this master plus its recorded supplements.", style = MaterialTheme.typography.bodySmall)
                                Text("${member.baseName}.zip", style = MaterialTheme.typography.bodySmall)
                                member.sets.forEach { set -> set.entries.forEachIndexed { index, entry ->
                                    Text("Set ${set.number} · ${index + 1}. ${entry.title} → ${entry.filename ?: "Missing chart placeholder: ${entry.reason}"}", style = MaterialTheme.typography.bodySmall)
                                    entry.distribution?.takeIf { entry.filename != null }?.let { Text("${it.sourceLabel} · ${it.distributionId} · ${it.chart.part} r${it.chart.revision.toString().padStart(2, '0')}", style = MaterialTheme.typography.bodySmall) }
                                } }
                            } }
                        }
                        Text("Preferred part labels describe current preferences; the recorded chart may be a legitimate fallback.", style = MaterialTheme.typography.bodySmall)
                        if (plan.needsMissingAcknowledgement) Row {
                            Checkbox(missingAck, { missingAck = it }, enabled = !generating)
                            Text("I acknowledge missing references and any members without a master; preserve those entries as titled placeholders.")
                        }
                        if (plan.needsUndatedAcknowledgement) Row {
                            Checkbox(undatedAck, { undatedAck = it }, enabled = !generating)
                            Text("I acknowledge Undated filenames. To use a date, go back, enter it and save the event first.")
                        }
                    } else result?.let { completed ->
                        Text("Output: ${completed.folder}")
                        completed.zipNames.forEach { Text(it) }
                        completed.warnings.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                        TextButton(onClick = { runCatching { java.awt.Desktop.getDesktop().open(completed.folder.toFile()) }.onFailure { error = "Could not open folder: ${it.message}" } }) { Text("Open Export Folder") }
                        Text("ZIP → Google Drive → save locally in iPad Files → extract → share .4ss to forScore. Set-heading, repetition and matching behavior await owner iPad verification.")
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (generating) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Creating member ZIPs…") }
            }
        },
        confirmButton = {
            if (plan != null && result == null) Button(enabled = !generating && destination.isNotBlank() &&
                (!plan.needsMissingAcknowledgement || missingAck) && (!plan.needsUndatedAcknowledgement || undatedAck), onClick = {
                generating = true; error = null; setBusy(true)
                scope.launch {
                    try { result = withContext(Dispatchers.IO) { EventExporter.generate(project, plan, destination.trim(), missingAck, undatedAck) } }
                    catch (e: Exception) { error = "Export failed: ${e.message}" }
                    finally { generating = false; setBusy(false) }
                }
            }) { Text("Generate member ZIPs") }
            else TextButton(enabled = !generating, onClick = dismiss) { Text("Done") }
        },
        dismissButton = { if (result == null) TextButton(enabled = !generating, onClick = dismiss) { Text("Back to editor") } })
}
