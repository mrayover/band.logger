package bandlogger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

@Composable
fun EventManagement(project: Project, setBusy: (Boolean) -> Unit, save: (Project) -> Unit) {
    var working by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var discovery by remember { mutableStateOf<ExportDiscovery?>(null) }
    val scope = rememberCoroutineScope()
    fun run(action: () -> String) {
        working = true; setBusy(true)
        scope.launch {
            try { message = withContext(Dispatchers.IO) { action() } }
            catch (e: Exception) { message = e.message }
            finally { working = false; setBusy(false) }
        }
    }
    val pending = project.eventExports.filter { it.state == EventExportState.PENDING }
    val cleanup = project.eventExports.filter { it.state == EventExportState.CLEANUP }
    Text("${project.eventExports.count { it.state == EventExportState.ACTIVE }} tracked exports Â· ${cleanup.size} awaiting cleanup")
    if (pending.isNotEmpty()) OutlinedButton(enabled = !working, onClick = { run { EventExports.recoverPending(project, save); "Pending exports recovered. Review history before exporting again." } }) { Text("Recover pending exports (${pending.size})") }
    if (cleanup.isNotEmpty()) {
        cleanup.forEach { Text("${it.event.title} Â· ${it.folder}\n${it.error ?: "Cleanup pending"}", color = MaterialTheme.colorScheme.error) }
        OutlinedButton(enabled = !working, onClick = { run {
            val updated = EventExports.cleanup(project, save)
            if (updated.eventExports.any { it.state == EventExportState.CLEANUP }) "Cleanup remains incomplete. Records retained for retry." else "Cleanup complete."
        } }) { Text("Retry export cleanup") }
    }
    TextButton(enabled = !working, onClick = { run {
        val result = EventExports.discover(project)
        discovery = result
        var updated = project
        result.verified.forEach { updated = updated.recordEventExport(it) }
        if (updated != project) save(updated)
        "${result.verified.size} verified exports associated; ${result.review.size} folders need review. Older exports without stable event IDs stay untouched."
    } }) { Text("Discover older exports") }
    message?.let { Text(it) }
    discovery?.review?.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
    var migrationOpen by remember { mutableStateOf(false) }
    TextButton(enabled = !working, onClick = { migrationOpen = true }) { Text("Review export layout migration") }
    if (migrationOpen) AlertDialog(onDismissRequest = { migrationOpen = false }, title = { Text("Export layout review") }, text = {
        Column(Modifier.width(580.dp).heightIn(max = 450.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Future root: ${FileLocations.eventsInitialRoot(project) ?: "Choose a project export root"}")
            Text("Numbered project folders: 02 - Repertoire Distributions/Master and Supplements/Supplement NNN; 03 - Events/Gigs or Rehearsals/YYYY-MM-DD Title; test event output under 06 - Testing. Legacy projects retain their recorded layout until configured/migrated.")
            Text("Existing distribution paths below remain authoritative. Keep them in place: receipts and interrupted-publication recovery use their exact recorded paths. This review does not move, regenerate or change any export.")
            project.masters.forEach { Text("Library Master: ${it.folder}", style = MaterialTheme.typography.bodySmall) }
            project.supplements.forEach { Text("Supplement ${it.number}: ${it.publication.folder}", style = MaterialTheme.typography.bodySmall) }
            project.eventExports.filter { it.state != EventExportState.DELETED }.forEach { Text("Event: ${it.folder}", style = MaterialTheme.typography.bodySmall) }
            Text("Configure the project root once for future output. Historical paths above remain recorded independently. Controlled physical relocation uses a backed-up, resumable path-and-manifest migration; unverified legacy folders stay in place. Do not bulk-move folders.")
        }
    }, confirmButton = { TextButton(onClick = { migrationOpen = false }) { Text("Done") } })
    var historyOpen by remember { mutableStateOf(false) }
    TextButton(enabled = !working, onClick = { historyOpen = !historyOpen }) { Text(if (historyOpen) "Hide export history" else "Show export history") }
    if (historyOpen) project.eventExports.filter { it.state == EventExportState.ACTIVE }.forEach { record ->
        Text("${record.event.title} Â· ${record.createdAt}\n${record.folder}", style = MaterialTheme.typography.bodySmall)
    }
    if (working) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
fun EventDeleteDialog(project: Project, event: BandEvent, setBusy: (Boolean) -> Unit, save: (Project) -> Unit, dismiss: () -> Unit) {
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val exports = project.eventExports.filter { it.event.id == event.id && it.state != EventExportState.DELETED }
    AlertDialog(onDismissRequest = { if (!working) dismiss() }, title = { Text("Delete ${event.title}?") }, text = {
        Column(Modifier.width(580.dp).heightIn(max = 450.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${event.type.label()} Â· ${event.date ?: "Undated"}. Remove the saved event and clean these verified export packages:")
            if (exports.isEmpty()) Text("No tracked exports. Older untracked folders remain untouched; use Discover older exports before deletion if needed.")
            exports.forEach { Text(it.folder); it.packages.forEach { pkg -> Text(pkg.filename, style = MaterialTheme.typography.bodySmall) } }
            Text("Library charts, masters, supplements and unrelated files are preserved. If Drive is unavailable or cleanup fails, the event is removed and its cleanup records remain here for retry.")
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (working) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }, confirmButton = {
        Button(enabled = !working && exports.none { it.state == EventExportState.PENDING }, onClick = {
            working = true; setBusy(true)
            scope.launch {
                try { withContext(Dispatchers.IO) { EventExports.deleteEvent(project, event.id, save) }; dismiss() }
                catch (e: Exception) { error = "Deletion could not finish: ${e.message}. Retained cleanup records are available in Events." }
                finally { working = false; setBusy(false) }
            }
        }) { Text("Delete event and exports") }
    }, dismissButton = { TextButton(enabled = !working, onClick = dismiss) { Text("Keep event") } })
}
