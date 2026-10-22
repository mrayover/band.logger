package bandlogger

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.nio.file.Path

@Composable
fun SupplementControls(project: Project, busy: Boolean, setBusy: (Boolean) -> Unit, save: (Project) -> Unit) {
    var preview by remember(project) { mutableStateOf<SupplementPreview?>(null) }
    var notice by remember { mutableStateOf("") }
    var receipt by remember { mutableStateOf<LibrarySupplement?>(null) }
    var recovery by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun recover(location: String) {
        setBusy(true)
        scope.launch {
            try {
                val record = withContext(Dispatchers.IO) { SupplementExporter.recover(project, location) }
                save(project.recordSupplement(record)); runCatching { SupplementExporter.clearRecordedIntent(project.recordSupplement(record)) }; receipt = record; notice = "Supplement history recovered. No new packages generated."
            } catch (e: Exception) { notice = "Recovery failed: ${e.message}" }
            finally { setBusy(false) }
        }
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(enabled = !busy, onClick = {
            setBusy(true)
            scope.launch {
                try { preview = withContext(Dispatchers.IO) { SupplementPlanner.preview(project, PendingSources.check(project).issues) } }
                catch (e: Exception) { notice = "Preview failed: ${e.message}" }
                finally { setBusy(false) }
            }
        }) { Text("Preview next supplement") }
        TextButton(enabled = !busy, onClick = { history = !history }) { Text("Distribution history") }
        TextButton(enabled = !busy, onClick = { recovery = "" }) { Text("Recover supplement") }
    }
    if (notice.isNotBlank()) Text(notice, color = MaterialTheme.colorScheme.secondary)
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    if (history) {
        Text("Export records do not confirm installation or receipt.")
        project.masters.sortedByDescending { it.createdAt }.forEach { master ->
            Text("Library Master · ${master.createdAt} · ${master.members.sumOf { it.charts.size }} selections")
            TextButton(enabled = !busy, onClick = { runCatching { java.awt.Desktop.getDesktop().open(Path.of(master.folder).toFile()) }.onFailure { notice = it.message ?: "Could not open folder." } }) { Text("Open master folder") }
        }
        project.supplements.asReversed().forEach { record ->
            TextButton(enabled = !busy, onClick = { receipt = record }) { Text("Supplement ${record.number.toString().padStart(3, '0')} · ${record.publication.createdAt} · ${record.publication.members.sumOf { it.charts.size }} selections") }
        }
        if (project.masters.isEmpty() && project.supplements.isEmpty()) Text("No recorded distributions.")
    }
    preview?.let { plan ->
        var acknowledged by remember(plan) { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { if (!busy) preview = null }, title = { Text("Library Supplement ${plan.number.toString().padStart(3, '0')} · Preview") },
            text = { Column(Modifier.width(620.dp).heightIn(max = 500.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Embedded-PDF packages for affected musicians only. Previous masters, supplements and originals are preserved. Export does not confirm installation.")
                Text("Destination: ${project.exportDestination ?: "Not configured — choose in Library master"}/Supplements/ (project folder)/Supplement ${plan.number.toString().padStart(3, '0')}")
                plan.packages.members.forEach { m ->
                    Text("${m.member.name} · ${m.selectedCount} selected" + if (m.selectedCount == 0) " — no ZIP" else "", style = MaterialTheme.typography.titleMedium)
                    m.choices.forEach { choice -> choice.chart?.let { chart ->
                        Text("${choice.tune.title} · ${chart.metadata!!.part} r${chart.metadata.revision}\n${chart.relativePath}\nSHA-256 ${chart.sha256}", style = MaterialTheme.typography.bodySmall)
                    } }
                }
                plan.warnings.distinct().forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                if (plan.selectedCount == 0) Text("No eligible pending charts; nothing can be published.")
                Row { Checkbox(acknowledged, { acknowledged = it }, enabled = !busy); Text("I reviewed the exact allocations, excluded/blocked selections and initial-master requirements.") }
                if (notice.isNotBlank()) Text(notice, color = MaterialTheme.colorScheme.error)
            } },
            confirmButton = { Button(enabled = !busy && acknowledged && plan.selectedCount > 0 && project.exportDestination != null, onClick = {
                setBusy(true)
                scope.launch {
                    var generated: LibrarySupplement? = null
                    try {
                        val result = withContext(Dispatchers.IO) { SupplementExporter.generate(project, plan) }
                        generated = result
                        save(project.recordSupplement(result))
                        runCatching { SupplementExporter.clearRecordedIntent(project.recordSupplement(result)) }
                        preview = null; receipt = result; notice = "Published and recorded Supplement ${result.number}."
                    } catch (e: Exception) {
                        notice = if (generated != null) "Packages completed but history was not saved. Recover ${generated.publication.folder} before publishing again. ${e.message}" else "Publication failed: ${e.message}"
                        generated?.let { recovery = it.publication.folder; preview = null }
                    } finally { setBusy(false) }
                }
            }) { Text("Publish supplement") } }, dismissButton = { TextButton(enabled = !busy, onClick = { preview = null }) { Text("Cancel") } })
    }
    recovery?.let { initial ->
        var location by remember(initial) { mutableStateOf(initial) }
        FormDialog("Recover completed supplement", { recovery = null }, { val selected = location; recovery = null; recover(selected) }) {
            Field("Original completed supplement folder", location) { location = it }
            TextButton(onClick = { chooseChartFolder("Choose completed supplement folder")?.let { location = it } }) { Text("Browse folders") }
            Text("Verifies project, number, history chain, package checksums and chart identities. Repeating recovery does not duplicate history. Do not choose an incomplete staging folder.")
        }
    }
    receipt?.let { record ->
        val manifest = record.publication
        AlertDialog(onDismissRequest = { receipt = null }, title = { Text("Supplement ${record.number.toString().padStart(3, '0')} · Receipt") },
            text = { Column(Modifier.width(620.dp).heightIn(max = 500.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Exported ${manifest.createdAt}\n${manifest.folder}\nID ${manifest.id}\nSelection SHA-256 ${record.selectionHash}")
                Text("Recorded export only; musician installation is unconfirmed.")
                manifest.members.forEach { m ->
                    Text("${m.memberName}: ${m.zipName ?: "No package (unaffected or excluded)"}")
                    m.charts.forEach { Text("${it.title} · ${it.part} r${it.revision}\n${it.filename}\n${it.sha256}", style = MaterialTheme.typography.bodySmall) }
                }
                manifest.warnings.forEach { Text(it) }
                TextButton(onClick = { runCatching { java.awt.Desktop.getDesktop().open(Path.of(manifest.folder).toFile()) }.onFailure { notice = it.message ?: "Could not open folder." } }) { Text("Open supplement folder") }
            } }, confirmButton = { TextButton(onClick = { receipt = null }) { Text("Done") } })
    }
}
