package bandlogger

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.nio.file.Path
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun chooseChartFolder(title: String = "Choose your chart library folder"): String? {
    val chooser = JFileChooser().apply { dialogTitle = title; fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
    return if (chooser.showOpenDialog(java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow) == JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath else null
}
fun choosePdf(): Path? {
    val chooser = JFileChooser().apply { dialogTitle = "Choose a PDF to copy into the chart library"; fileFilter = FileNameExtensionFilter("PDF charts", "pdf"); isAcceptAllFileFilterUsed = false }
    return if (chooser.showOpenDialog(java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow) == JFileChooser.APPROVE_OPTION) chooser.selectedFile.toPath() else null
}
@Composable fun LibraryScreen(project: Project, busy: Boolean, setBusy: (Boolean) -> Unit, save: (Project) -> Unit) {
    val scope = rememberCoroutineScope()
    var query by remember(project.id) { mutableStateOf("") }
    var view by remember(project.id) { mutableStateOf("Tunes") }
    var notice by remember(project.id) { mutableStateOf("") }
    var folderDraft by remember { mutableStateOf<String?>(null) }
    var importDraft by remember { mutableStateOf<Path?>(null) }
    var reviewDraft by remember { mutableStateOf<ChartRecord?>(null) }
    val catalog = project.catalog
    fun runJob(action: suspend () -> Pair<Project, String>) {
        if (busy) return
        setBusy(true)
        notice = "Working… You can keep this window open."
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) { action() }
                save(result.first)
                notice = result.second
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { notice = "Could not finish: ${e.message}. The saved catalog has been preserved." }
            finally { setBusy(false) }
        }
    }
    fun refresh(source: String) = runJob {
        val root = LocalCharts.folder(Path.of(source))
        val scanned = LocalCharts.scan(root)
        val revised = catalog.copy(source = root).reconcile(scanned, project.prefix, System.currentTimeMillis(), ::newId)
        project.copy(catalog = revised) to "Refresh complete: ${scanned.size} PDFs found; ${revised.needsReview().size} need review."
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Chart library", style = MaterialTheme.typography.headlineSmall)
        Text(catalog.source ?: "Connect a folder from Google Drive for desktop, or another local folder.", style = MaterialTheme.typography.bodyMedium)
        Text("Scanning reads PDFs without changing them. Import copies a chosen PDF into this folder; Google Drive handles syncing.", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = !busy, onClick = { folderDraft = catalog.source ?: "" }) { Text(if (catalog.source == null) "Connect folder" else "Change folder") }
            Button(enabled = !busy && catalog.source != null, onClick = { refresh(catalog.source!!) }) { Text("Refresh") }
            OutlinedButton(enabled = !busy && catalog.source != null, onClick = { importDraft = choosePdf() }) { Text("Import PDF") }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (notice.isNotBlank()) Text(notice, color = MaterialTheme.colorScheme.secondary)
        catalog.lastScan?.let { Text("Last successful scan: ${java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().withNano(0)}", style = MaterialTheme.typography.bodySmall) }
        if (catalog.source != null) {
            val active = catalog.active()
            val review = catalog.needsReview()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Tunes", "Needs review", "History").forEach { label ->
                    TextButton(enabled = !busy, onClick = { view = label }) { Text((if (view == label) "• " else "") + label + if (label == "Needs review") " (${review.size})" else "") }
                }
            }
            OutlinedTextField(query, { query = it }, label = { Text("Search titles, parts or filenames") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            val acceptCount = review.count { catalog.status(it) == "New chart" }
            if (view == "Needs review" && acceptCount > 0) {
                Text("$acceptCount new charts have recognized names and no revision conflicts. Review their list before accepting.", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(enabled = !busy, onClick = { runCatching { save(project.copy(catalog = catalog.acceptNew())); notice = "Accepted $acceptCount new charts." }.onFailure { notice = "Could not save: ${it.message}" } }) { Text("Accept $acceptCount recognized new charts") }
            }
            val records = when (view) {
                "Needs review" -> review
                "History" -> catalog.charts.filter { !it.current || !it.present || it.ignored || it.source != catalog.source }
                else -> active
            }.filter { query.isBlank() || (it.relativePath + " " + (it.metadata?.title ?: "") + " " + (it.metadata?.part ?: "")).contains(query, true) }
            if (records.isEmpty()) Text(if (view == "Needs review") "Nothing needs review." else "No matching charts. Connect a folder, import a PDF, or refresh after adding files.")
            Box(Modifier.fillMaxWidth().height(440.dp)) {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (view == "Tunes") {
                        val groups = records.groupBy { it.tuneId ?: "unclassified" }.entries.sortedBy { entry -> catalog.tunes.find { it.id == entry.key }?.key ?: "~" }
                        items(groups, key = { it.key }) { group ->
                            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(catalog.tunes.find { it.id == group.key }?.title ?: "Unclassified files", style = MaterialTheme.typography.titleLarge)
                                group.value.sortedWith(compareBy({ it.metadata?.part }, { it.metadata?.revision })).forEach { record ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(Modifier.weight(1f)) {
                                            Text(record.metadata?.let { "${it.part} · r${it.revision.toString().padStart(2, '0')} · ${catalog.status(record)}" } ?: catalog.status(record))
                                            Text(record.relativePath, style = MaterialTheme.typography.bodySmall)
                                        }
                                        TextButton(enabled = !busy, onClick = { reviewDraft = record }) { Text("Details") }
                                    }
                                }
                            } }
                        }
                    } else {
                        items(records, key = { it.id }) { record ->
                            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(record.relativePath, style = MaterialTheme.typography.titleMedium)
                                Text(catalog.status(record), color = MaterialTheme.colorScheme.secondary)
                                if (record.fileError != null) Text(record.fileError)
                                if (view == "History") Text("Folder: ${record.source}", style = MaterialTheme.typography.bodySmall)
                                TextButton(enabled = !busy, onClick = { reviewDraft = record }) { Text("Review details") }
                            } }
                        }
                    }
                }
                VerticalScrollbar(rememberScrollbarAdapter(listState), Modifier.align(androidx.compose.ui.Alignment.CenterEnd).fillMaxHeight())
            }
            Text("${active.mapNotNull { it.tuneId }.distinct().size} tunes · ${active.size} current PDF files. Prior revisions stay in the catalog. Different punctuation stays separate until you review it.", style = MaterialTheme.typography.bodySmall)
        }
    }
    folderDraft?.let { selected ->
        var location by remember(selected) { mutableStateOf(selected) }
        FormDialog("Connect a chart folder", { folderDraft = null }, {
            require(location.isNotBlank()) { "Choose or paste a folder location." }
            val confirmed = location.trim()
            folderDraft = null
            refresh(confirmed)
        }) {
            Field("Folder location", location) { location = it }
            OutlinedButton(onClick = { chooseChartFolder()?.let { location = it } }) { Text("Browse folders") }
            Text("Save connects this folder and scans its PDFs, including subfolders. Existing files are not renamed or modified. Previous folder records stay in History.")
        }
    }
    importDraft?.let { source ->
        var filename by remember(source) { mutableStateOf(source.fileName.toString()) }
        FormDialog("Copy PDF into chart library", { importDraft = null }, {
            require(ChartNames.parse(filename, project.prefix) != null) { "Use ${project.prefix.ifBlank { "[Title]" }} Song Title -r00- C.pdf, with a supported part." }
            val destination = requireNotNull(catalog.source)
            val confirmed = filename
            importDraft = null
            runJob {
                val copied = LocalCharts.importPdf(source, destination, confirmed)
                try {
                    val revised = catalog.reconcile(LocalCharts.scan(destination), project.prefix, System.currentTimeMillis(), ::newId)
                    project.copy(catalog = revised) to "Copied ${copied.fileName}. Review its catalog entry; the original file is unchanged."
                } catch (e: Exception) {
                    project to "PDF copied successfully, but refresh failed: ${e.message}. It is safe to refresh again; do not import it twice."
                }
            }
        }) {
            Text("From: $source", style = MaterialTheme.typography.bodySmall)
            Text("To: ${catalog.source}", style = MaterialTheme.typography.bodySmall)
            Field("Destination filename", filename) { filename = it }
            Text("Saving confirms a copy into the connected folder. Existing files are never overwritten. Parts: ${chartParts.joinToString()}. Google Drive may upload the copy if this is a synced folder.")
        }
    }
    reviewDraft?.let { record ->
        var title by remember(record.id) { mutableStateOf(record.metadata?.title ?: "") }
        var part by remember(record.id) { mutableStateOf(record.metadata?.part ?: "C") }
        var revision by remember(record.id) { mutableStateOf(record.metadata?.revision?.toString() ?: "0") }
        var note by remember(record.id) { mutableStateOf(record.note) }
        FormDialog("Chart details · ${catalog.status(record)}", { reviewDraft = null }, {
            val canonicalPart = chartParts.find { it.equals(part.trim(), true) } ?: error("Choose a supported chart part.")
            val rev = revision.toIntOrNull() ?: error("Revision must be a whole number.")
            save(project.copy(catalog = catalog.review(record.id, ChartMetadata(title, canonicalPart, rev), note, ::newId)))
            reviewDraft = null
            notice = "Catalog details saved. The source filename and PDF are unchanged."
        }) {
            Text(record.relativePath, style = MaterialTheme.typography.titleMedium)
            Text("${record.size} bytes · SHA-256 ${record.sha256.take(12)}…", style = MaterialTheme.typography.bodySmall)
            if (catalog.conflicts(record)) Text("Different content uses this tune, part and revision. Check the files and assign the correct revision or exclude an unwanted catalog record.")
            if (record.changed) Text("File contents changed since discovery. The prior checksum and metadata are retained in History.")
            Field("Tune title", title) { title = it }
            Field("Part: C, Bb, Eb, Bass, Drums, Guitar, Piano", part) { part = it }
            Field("Revision number", revision) { revision = it }
            Field("Review note (optional)", note) { note = it }
            Text("These edits classify this catalog record only; they do not rename, move or edit the PDF.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = {
                runCatching { save(project.copy(catalog = catalog.exclude(record.id, !record.ignored))); reviewDraft = null; notice = "Catalog visibility updated. The PDF is unchanged." }.onFailure { notice = "Could not save: ${it.message}" }
            }) { Text(if (record.ignored) "Include in review again" else "Exclude from catalog (keep PDF)") }
        }
    }
}