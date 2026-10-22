package bandlogger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant

fun EventStatus.label() = when (this) {
    EventStatus.PLANNED -> "Planned"
    EventStatus.COMPLETED -> "Completed"
    EventStatus.CANCELED -> "Canceled"
}

@Composable
fun EventDetails(project: Project, event: BandEvent, change: (BandEvent) -> Unit, modifier: Modifier = Modifier) {
    var capture by remember(event.id) { mutableStateOf<Boolean?>(null) }
    var reviewing by remember(event.id) { mutableStateOf(false) }
    var error by remember(event.id) { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Event status", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EventStatus.entries.forEach { status -> FilterChip(selected = event.status == status, onClick = {
                if (status == EventStatus.COMPLETED && event.status != status) capture = false
                else if (status != EventStatus.COMPLETED) change(event.copy(status = status))
            }, label = { Text(status.label()) }) }
        }
        Text("Only explicitly completed, non-test records qualify as performances. Canceled events are retained; changing status keeps earlier recorded revisions.", style = MaterialTheme.typography.bodySmall)
        Row {
            Checkbox(event.isTest, { change(event.copy(isTest = it)) })
            Text("Test event · exclude from performance counts", Modifier.padding(top = 12.dp))
        }
        event.performance?.let { record ->
            Text("Recorded: ${record.title} · ${record.type.label()} · ${record.date ?: "Date not set"} · revision ${record.revision}")
            Text("${record.sets.size} sets · ${record.entries.size} tune occurrences · ${if (record.isTest) "Test record" else "Real event record"}")
            Text("Later title, date, type, test flag and setlist edits do not change this record. Review it before making an explicit correction.", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { reviewing = true }) { Text("Review recorded setlist") }
                if (event.status == EventStatus.COMPLETED) OutlinedButton(onClick = { capture = true }) { Text("Correct record from current setlist") }
            }
        }
        HorizontalDivider()
        Text("Notes", style = MaterialTheme.typography.titleMedium)
        Text("Private to band.logger. Never included in PDFs, forScore exports or musician packages.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(event.notes, { change(event.copy(notes = it)) }, label = { Text("Event notes") }, minLines = 5,
            modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp))
        Text("Use Save event to keep notes and status changes. Unsaved changes use the same navigation and close protection as the setlist.", style = MaterialTheme.typography.bodySmall)
    }
    capture?.let { correction ->
        AlertDialog(onDismissRequest = { capture = null }, title = { Text(if (correction) "Correct the recorded performance?" else "Mark Completed and record this setlist?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (correction) "Replace the current performance facts with the title, date, type, test flag and setlist currently shown in this editor. Earlier revisions remain available. Edit the setlist first if needed."
                        else "Confirm this event took place and the current setlist records what was actually played or rehearsed. Edit the setlist first if needed.")
                    Text("${event.title} · ${event.date ?: "Date not set"} · ${event.sets.size} sets · ${event.entries.size} occurrences${if (event.isTest) " · TEST" else ""}")
                    Text("Save event afterward to persist the record.")
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }, confirmButton = { TextButton(onClick = {
                try { change(event.recordPerformance(project, Instant.now().toString(), correction)); error = null; capture = null }
                catch (e: Exception) { error = e.message }
            }) { Text(if (correction) "Confirm correction" else "Confirm Completed") } },
            dismissButton = { TextButton(onClick = { capture = null; error = null }) { Text("Keep editing") } })
    }
    if (reviewing) AlertDialog(onDismissRequest = { reviewing = false }, title = { Text("Recorded performance revisions") }, text = {
        Column(Modifier.width(580.dp).heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            event.performanceHistory.asReversed().forEach { record ->
                Text("Revision ${record.revision} · ${record.recordedAt}", style = MaterialTheme.typography.titleMedium)
                Text("${record.title} · ${record.type.label()} · ${record.date ?: "Date not set"}${if (record.isTest) " · Test event" else ""}")
                record.sets.forEachIndexed { index, set ->
                    Text("Set ${index + 1}")
                    if (set.entries.isEmpty()) Text("No tunes recorded")
                    set.entries.forEachIndexed { i, entry -> Text("${i + 1}. ${entry.title}", style = MaterialTheme.typography.bodySmall) }
                }
                HorizontalDivider()
            }
        }
    }, confirmButton = { TextButton(onClick = { reviewing = false }) { Text("Done") } })
}
