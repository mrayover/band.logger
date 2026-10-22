package bandlogger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun EventsScreen(project: Project, create: (EventType) -> Unit, open: (BandEvent) -> Unit, busy: Boolean, setBusy: (Boolean) -> Unit, saveProject: (Project) -> Unit) {
    var deleting by remember { mutableStateOf<BandEvent?>(null) }
    var view by remember { mutableStateOf(EventView.UPCOMING) }
    var typeFilter by remember { mutableStateOf<EventType?>(null) }
    Text("Events", style = MaterialTheme.typography.headlineMedium)
    Text("Plan upcoming events or reopen completed and canceled records for ${project.name}.")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        EventView.entries.forEach { item -> FilterChip(selected = view == item, onClick = { view = item }, label = { Text(if (item == EventView.UPCOMING) "Upcoming" else "History") }) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(selected = typeFilter == null, onClick = { typeFilter = null }, label = { Text("All types") })
        EventType.entries.forEach { type -> FilterChip(selected = typeFilter == type, onClick = { typeFilter = type }, label = { Text(type.label() + "s") }) }
    }
    Text(if (view == EventView.UPCOMING) "Planned events, including past or undated plans. Dates never mark an event Completed automatically."
        else "Completed and canceled records, newest dated first. Completed details come from the recorded performance.")
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(enabled = !busy, onClick = { create(EventType.REHEARSAL) }) { Text("Create Rehearsal") }
        OutlinedButton(enabled = !busy, onClick = { create(EventType.GIG) }) { Text("Create Gig") }
    }
    if (project.events.isEmpty()) Text("No saved events yet. Create a rehearsal or gig to get started.")
    val visible = project.eventsFor(view, typeFilter)
    if (project.events.isNotEmpty() && visible.isEmpty()) Text("No events match this view.")
    visible.forEach { event ->
        key(event.id) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(event.historyTitle(), style = MaterialTheme.typography.titleLarge)
                        val record = event.performance.takeIf { event.status == EventStatus.COMPLETED }
                        Text("${event.historyType().label()} · ${event.historyDate() ?: "Date not set"} · ${event.status.label()}")
                        Text("${record?.sets?.size ?: event.sets.size} sets · ${record?.entries?.size ?: event.entries.size} tune occurrences · ${if (event.notes.isBlank()) "No notes" else "Notes saved"}")
                        if (record?.isTest ?: event.isTest) Text("Test event · excluded from performance counts", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(enabled = !busy, onClick = { open(event) }) { Text("Edit") }
                    TextButton(enabled = !busy, onClick = { deleting = event }) { Text("Delete") }
                }
            }
        }
    }
    EventManagement(project, setBusy, saveProject)
    deleting?.let { event -> EventDeleteDialog(project, event, setBusy, saveProject) { deleting = null } }
}
fun EventType.label() = if (this == EventType.REHEARSAL) "Rehearsal" else "Gig"

@Composable
fun EventEditorScreen(project: Project, draft: EventDraft, error: String?, change: (BandEvent) -> Unit, save: () -> Unit, back: () -> Unit, setBusy: (Boolean) -> Unit, saveProject: (Project) -> Unit, modifier: Modifier = Modifier) {
    val event = draft.value
    var details by remember(event.id) { mutableStateOf(false) }
    var exportOpen by remember(event.id) { mutableStateOf(false) }
    var search by remember(event.id) { mutableStateOf("") }
    var selection by remember(event.id) { mutableStateOf(EventSetSelection(event.initialSet.id)) }
    var removeSetId by remember(event.id) { mutableStateOf<String?>(null) }
    val focusSearch = remember(event.id) { FocusRequester() }
    val active = selection.resolve(event)
    val activeNumber = event.sets.indexOfFirst { it.id == active.id } + 1
    val activeIds = remember(project.catalog) { project.catalog.active().mapNotNull { it.tuneId }.toSet() }
    val tunes = remember(project.catalog, activeIds) { project.catalog.tunes.filter { it.id in activeIds }.sortedWith(compareBy<Tune> { it.title.lowercase() }.thenBy { it.id }) }
    val titles = remember(project.catalog) { project.catalog.tunes.associate { it.id to it.title } }
    fun selectSet(id: String) { selection = selection.select(event, id); focusSearch.requestFocus() }
    fun removeSet(id: String, confirmed: Boolean = false) {
        val updated = event.removeSet(id, confirmed)
        selection = selection.reconcile(updated)
        change(updated)
    }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val narrow = maxWidth < 700.dp
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (draft.isDirty) "Unsaved changes" else "All changes saved", Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                TextButton(onClick = back) { Text("Back to events") }
                Button(enabled = draft.isDirty, onClick = save) { Text("Save event") }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(event.title, { change(event.copy(title = it)) }, label = { Text("Title (required)") }, singleLine = true, modifier = Modifier.weight(1.4f))
                OutlinedTextField(event.date ?: "", { change(event.copy(date = it.ifBlank { null })) }, label = { Text("Date · YYYY-MM-DD") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                EventType.entries.forEach { type -> FilterChip(selected = event.type == type, onClick = { change(event.copy(type = type)) }, label = { Text(type.label()) }) }
                Spacer(Modifier.weight(1f))
                OutlinedButton(enabled = !draft.isDirty, onClick = { exportOpen = true }) { Text(if (draft.isDirty) "Save to export" else "Export for Members") }
                OutlinedButton(onClick = {
                    val updated = event.addSet(::newId)
                    selection = EventSetSelection(updated.sets.last().id)
                    change(updated)
                }) { Text("+ Add Set") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(selected = !details, onClick = { details = false }, label = { Text("Setlist") })
                FilterChip(selected = details, onClick = { details = true }, label = { Text("Notes & status") })
            }
            if (details) {
                EventDetails(project, event, change, Modifier.weight(1f))
            } else {
            val library: @Composable (Modifier) -> Unit = { panelModifier ->
                Card(panelModifier) {
                    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tune library", style = MaterialTheme.typography.titleMedium)
                        Text("Adding to Set $activeNumber", color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(search, { search = it }, singleLine = true, label = { Text("Search tunes") }, modifier = Modifier.fillMaxWidth().focusRequester(focusSearch))
                        val matches = tunes.filter { it.title.contains(search.trim(), ignoreCase = true) }
                        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                            if (tunes.isEmpty()) item { Text("No tunes with active charts. Connect or refresh this project's catalog in Library.") }
                            else if (matches.isEmpty()) item { Text("No matching tunes.") }
                            items(matches, key = { it.id }) { tune ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(tune.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    TextButton(onClick = { change(event.addTune(project, tune.id, ::newId, active.id)) }, modifier = Modifier.semantics { contentDescription = "Add ${tune.title} to Set $activeNumber" }) { Text("+") }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            val builder: @Composable (Modifier) -> Unit = { panelModifier ->
                Card(panelModifier) {
                    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        event.sets.forEachIndexed { setIndex, set ->
                            item(key = "set:${set.id}") {
                                Card(onClick = { selectSet(set.id) }, border = if (set.id == active.id) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text("Set ${setIndex + 1}", style = MaterialTheme.typography.titleMedium)
                                            if (set.id == active.id) Text("Selected", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                        }
                                        TextButton(onClick = { selectSet(set.id) }, contentPadding = PaddingValues(6.dp)) { Text("Add Song") }
                                        TextButton(enabled = event.sets.size > 1, onClick = {
                                            if (set.entries.isEmpty()) removeSet(set.id) else removeSetId = set.id
                                        }, contentPadding = PaddingValues(6.dp)) { Text("Remove Set") }
                                    }
                                }
                            }
                            if (set.entries.isEmpty()) item(key = "empty:${set.id}") { Text("Select this set, then add tunes with + in the library.", Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall) }
                            items(set.entries.size, key = { "entry:${set.entries[it].id}" }) { index ->
                                val entry = set.entries[index]
                                Column(Modifier.fillMaxWidth()) {
                                    Text("${index + 1}. ${titles[entry.tuneId] ?: "Unavailable tune"}", Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyMedium)
                                    if (entry.tuneId !in activeIds) Text("No active charts; selection retained.", style = MaterialTheme.typography.bodySmall)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(enabled = index > 0, onClick = { change(event.moveEntry(set.id, entry.id, -1)) }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("Up") }
                                        TextButton(enabled = index < set.entries.lastIndex, onClick = { change(event.moveEntry(set.id, entry.id, 1)) }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("Down") }
                                        TextButton(onClick = { change(event.removeEntry(entry.id)) }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("Remove") }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
            if (narrow) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    library(Modifier.weight(1f).fillMaxWidth())
                    builder(Modifier.weight(1.3f).fillMaxWidth())
                }
            } else {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    library(Modifier.weight(0.38f).fillMaxHeight())
                    builder(Modifier.weight(0.62f).fillMaxHeight())
                }
            }
            }
        }
    }
    if (exportOpen) EventExportDialog(project, event.id, setBusy, saveProject) { exportOpen = false }
    removeSetId?.let { id ->
        val target = event.sets.find { it.id == id }
        if (target != null) AlertDialog(onDismissRequest = { removeSetId = null },
            title = { Text("Remove Set ${event.sets.indexOf(target) + 1}?") },
            text = { Text("Remove this set and its ${target.entries.size} song entries? Save the event to keep this change.") },
            confirmButton = { TextButton(onClick = { removeSet(id, confirmed = true); removeSetId = null }) { Text("Remove set and songs") } },
            dismissButton = { TextButton(onClick = { removeSetId = null }) { Text("Keep set") } })
    }
}