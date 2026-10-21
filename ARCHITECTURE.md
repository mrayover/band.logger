# Architecture

## Shared foundation

Kotlin 2.2.20, Compose Multiplatform 1.9.3, serialization 1.9.0, Gradle Wrapper 8.14.3 and JDK 21. Existing toolchain retained. commonMain contains projects, members, themes, chart metadata, tunes, catalog reconciliation, review rules, JSON and forScore XML. No Windows or java.* filesystem dependencies enter commonMain. Desktop UI and storage/provider integration stay in desktopMain. Android foundation remains compatible shared code and provider interfaces; no Android application is delivered.

## Catalog and filenames

Each Project owns a Catalog with an opaque source reference, tunes and chart snapshots. Desktop resolves that reference to a real folder path; future Android providers must use URI-based references. ChartSource/ExportDestination remain future provider boundaries. Windows traversal and copy logic are isolated in LocalCharts.

Parser: optional configured prefix, title, -rNN- (2–4 digits), supported part and .pdf. Prefix, part, revision marker and extension are case-insensitive. Canonical parts: C, Bb, Eb, Bass, Drums, Guitar, Piano. Tune keys trim/collapse whitespace and lowercase; punctuation and diacritics remain significant to avoid automatic false merges. Manual metadata review can group an ambiguous file into a known title without renaming it.

Stable IDs are independent of filenames. Repeated path/content matches retain IDs and review state. Changed bytes preserve prior snapshots and create a new record. Different hashes claiming the same tune/part/revision in the same source are conflicts, including retained historical versions, until explicitly resolved. Missing files retain records. Switching folders retains old records in History. New well-named files are batch-reviewable; ambiguous/changed/conflicting files require individual attention. Exclusion affects catalog visibility only.

## Filesystem safety

Scans recursively read PDFs without following symbolic links, hash bytes in bounded buffers, inspect headers and compare size/modification metadata around reads. Any filesystem traversal/read error aborts the scan before reconciliation is saved. Invalid PDF headers are review entries. This is not a complete PDF renderer/validator.

Imports validate destination filenames, reject collisions and Windows reserved names, copy to a temporary file in the destination, verify hashes, then move to the final name without replacement. Temporary files created by that operation are cleaned up. Source files are never deleted or renamed. If copy succeeds but subsequent refresh fails, the user is told to refresh instead of importing again.

Scan/import IO runs off the UI thread. Project switching and competing operations are disabled while work is in progress, preventing stale project snapshots from overwriting new edits. Cancellation before catalog save leaves metadata unchanged; successfully copied files can be rediscovered on refresh.

## Persistence and migration

Versioned JSON in %LOCALAPPDATA%\band.logger\workspace.json. Schema 1 migrates to 2 with empty catalogs and all project data retained. First migration save creates an immutable workspace.chunk1.backup.json; every save preserves the previous version in workspace.backup.json. Writes validate, use a sibling temporary file and atomic replacement when supported. Unreadable data blocks editing rather than resetting. A process file lock prevents concurrent app writers. Prior binaries reject the new schema.

## forScore and Windows packaging

forScore XML remains unchanged: ordered score elements with escaped attributes and Base64 PDFs. The proof button now wraps .4ss plus instructions in a ZIP and refuses overwrites. Owner verified the ZIP → Drive → local extraction → forScore route with both placeholder and embedded-PDF diagnostics. Direct Drive handoff failed; conversion/corruption has not been established.

Compose createDistributable produces an executable with a bundled runtime. scripts/package-windows.ps1 runs tests, builds, smoke-tests storage/fonts/XML/ZIP/scanning, packages version 0.2.0, extracts into a path containing spaces and runs without developer Java. The GitHub Windows workflow provides the ZIP and test report after publication. No copyrighted charts, databases or private copy receipts are included in the repository or package.

## Next planned increment

Chunk 2 is owner accepted as of October 20, 2026. No implementation contracts changed in this planning update. CHUNK_3_PLAN.md proposes shared deterministic member selection plus desktop source validation, ZIP generation and durable export manifests, retaining platform boundaries. These additions are planned, not implemented; schema changes and failure/recovery behavior must be documented when built.

## Stage 3A implementation

MasterPreview.kt in commonMain is a pure catalog snapshot planner. It never reads files or updates acceptance state. It groups current records from the selected source by stable tune ID, sorts tunes deterministically, normalizes supported member preference labels and selects the highest included revision of the first available preferred part. Any unresolved latest candidate blocks that tune before fallback, preventing silent downgrade. Catalog conflict rules include non-excluded historical content. Identical duplicate content is resolved by stable path/ID order. Excluded charts may be skipped with visible file issues; unsupported preferences block that member.

MasterScreen.kt presents member lists, counts, exceptions, search and file issues. Desktop destination validation runs off the UI thread with existing busy/project-switch protection. Project.exportDestination is an optional opaque string in schema 2, default null; desktop stores a normalized folder reference. No migration is needed to read existing schema-2 data. Older binaries can drop this new setting when saving.

Version 0.3.0 packaging retains the standard Windows workflow and extends the smoke routine with preview/destination checks. Local compiled image generation passed, but Smart App Control blocked executable launch; manual archive assembly was used only to produce a clearly labeled candidate. No smoke-test bypass was added to the build script or CI. Actual source verification, staged export writes and persistent manifests remain 3B design work.

## Stage 3B

LibraryMaster/MasterMember/MasterItem are shared serializable manifest snapshots. Project.masters persists completed outputs. Schema 3 migrates schema 1/2; the first schema-2 save preserves workspace.pre-masters.backup.json. Earlier binaries reject the new schema to avoid losing export baselines.

MasterExporter is desktop-only. It rejects stale previews, unsafe/linked source paths, changed/missing PDFs and duplicate output PDF basenames. It streams individual PDF XML elements into each member ZIP, limiting a PDF to 64 MiB and member source data to 512 MiB. UTC-named new folders use unique identities and no replacement. Outputs are staged, reverified, then moved to the final folder with a manifest. Normal failures clean only this operation's staging files; abrupt termination can leave an incomplete folder.

Publication precedes workspace save. If that save fails, completed output remains available; recover validates the original folder, project/catalog IDs and archive hashes, then idempotently records the manifest. It is not recipient-import confirmation. Main's packaged smoke test exercises export and recovery. Both bundled and extracted Windows tests passed after the owner's security-setting change.

## October 21, 2026 — Homepage presentation and navigation

HomeScreen.kt is desktop presentation only: responsive equal-size Compose cards, decorative vector icons, existing bundled Lato and Material theme tokens. It switches to one column below 500 dp of available content width; existing page scrolling handles shorter windows. Card interactions use Compose click semantics plus explicit hover/focus borders. Coming soon dialog state is scoped to project identity. Main retains its existing page routing and adds Pending Distribution without persistent model changes. Library/master code, storage schema 3 and original chart files are untouched. Windows build remains the existing Compose/runtime and GitHub Actions process, now version 0.3.3 with a versioned checksum and an archive overwrite guard.

## October 21, 2026 — Pending calculation

PendingDistribution.kt in commonMain reuses MasterPlanner with ephemeral file-error overlays. PendingSources.kt performs a read-only LocalCharts scan on Dispatchers.IO; missing/hash-changed/unreadable files become blocking issues, not saved catalog updates. Unindexed PDFs produce a Refresh notice. PendingScreen.kt owns project-scoped transient checks/search/expansion; it exposes no save/export operation. Main routes the existing homepage/sidebar to it. No dependencies or schema changes.

Baseline selection is per member: structurally valid same-project masters with UTC timestamps and SHA-256 fields; latest non-empty member package by normalized UTC timestamp then master ID. Empty/skipped later packages do not erase an earlier baseline. Historical manifests remain untouched and archived ZIP availability is not rechecked. Comparison matches stable tune IDs, part, revision and SHA-256; equivalent renamed/duplicate chart identities with identical part/revision/content remain unchanged. A tune absent from the latest baseline is New Tunes. A different part never exported to that member for that tune is New Parts. Other content/revision or previously exported-part changes are Revised Charts. Baselines are full snapshots, not a union of past masters; history is consulted only to distinguish a new part from a return to an earlier selection. No valid baseline means initial distribution, excluded from supplement counts. Missing/blocked choices and baseline tunes absent from current source become attention entries. Summary uses member–tune counts; file issues are counted separately, so both can refer to the same underlying problem.

File verification is point-in-time, not monitoring. Newly discovered files still require Library Refresh/review. Existing export records describe generated packages only, never confirmed receipt. Actual future supplements must reverify files at export and extend baseline semantics to include recorded supplements.

## October 21, 2026 — Event storage and editing

Events.kt is shared serializable domain logic: BandEvent has ID, owning project ID, title, required enum type, optional validated ISO calendar date and exactly one EventSet with ID. EventTuneEntry stores its own ID and an existing catalog tune ID. New IDs are generated once; edits retain IDs, appending creates an entry ID, and removing affects only that entry. Project.events holds records; saveEvent replaces by event ID. Workspace validation rejects foreign ownership/references and duplicate event/set/entry identities within a project. No tune/chart snapshots or duplicate tune records are created. Dates use a Gregorian calendar check in shared code with no java.* dependencies.

Workspace schema 4 reads schemas 1–3 with default empty event lists and leaves disk untouched until an explicit workspace write. FileWorkspaceStore adds immutable workspace.pre-events.backup.json for any original schema 1–3 file before first replacement, while preserving earlier migration backups and the existing rolling backup/atomic-write behavior. Existing master records/catalog/settings remain intact. Prior binaries reject schema 4. The normal process workspace lock remains in force.

EventDraft compares immutable current/baseline events. saveTo validates ownership/selected project, builds the next workspace and performs storage.save before returning a new clean baseline. Failures leave UI/workspace/draft unchanged. Main owns the draft and a deferred navigation action; all sidebar navigation/project actions and Window.onCloseRequest pass through the dirty guard. Save and continue runs the deferred action only after a successful save. Busy file operations also prevent premature normal window closure. Drafts are memory-only until explicit save; forced termination is outside normal close protection.

EventScreen.kt uses existing Material tokens and bundled Lato. Upcoming Events shows only Project.events (including past/undated); dated events sort ascending, then title/ID, undated last. Catalog choices join the current active chart tune IDs to the one authoritative catalog and sort by title. Approval is independent of planning. Added entries cannot be duplicated; append/remove is the only selection control. Existing selections without active charts are retained and labeled. No source-file IO or distribution planner changes occur here. The interface uses the existing scrollable workspace layout.

Windows version 0.4.0 retains scripts/build-local.ps1 and package-windows.ps1. New --event-restart-check runs in a second actual executable process after --smoke-test, for both the app image and extracted ZIP with developer Java absent. Synthetic smoke fixtures also migrate a schema-3 workspace with master records and verify immutable backup, events and isolation. No dependency or remote workflow change.

## October 21, 2026 — Multiple sets and bounded editor panels

BandEvent retains serialized initialSet and appends additionalSets (default empty); computed sets/entries preserve list order. Removing the first set promotes the next existing EventSet, retaining its ID/entries. Display numbers are list indices + 1 and are never identities. New sets/occurrences get new IDs; reorder moves the existing entry without recreating it. Repeated tune IDs are valid; entry IDs remain unique across sets/events in a project. All-set validation checks ownership and existing catalog references. EventSetSelection is transient UI state, resolving a removed destination to the first remaining set; new sets are selected explicitly. Selecting alone does not dirty the event. Non-empty removeSet requires an explicit confirmed flag; final-set removal always fails.

Schema 5 is required because the schema-4 writer ignores additionalSets and could drop data. Schemas 1–4 migrate on read without writes; schema-4 initialSet events load directly with no identity conversion. Before the first write, FileWorkspaceStore preserves original bytes in immutable workspace.pre-multisets.backup.json; prior immutable pre-events/pre-masters/chunk1 backups, rolling backup and atomic replacement remain. Master/catalog/settings data are unchanged. Old binaries reject schema 5. EventDraft.saveTo and Main's navigation/project/close save/keep/discard guards remain the save boundary.

EventScreen.kt lays out a compact header and independently bounded LazyColumn library/builder panels: 38/62 horizontal split at editor widths >=700 dp, stacked weighted panels below it. Main disables its outer page scroll for Event editor only and supplies bounded remaining height; other pages retain their prior scroll layout. Builder virtualizes set headers and entry rows keyed by stable IDs. Active header uses the project accent border and the library names the destination. Add Song/header selects and focuses the library's search FocusRequester. Occurrence moves are Up/Down only; first/last controls disable at boundaries. Nonempty removal uses a confirmation dialog; cancellation has no mutation. Catalog planning uses the same active chart/tune join as 0.4.0 without changing approval or reading chart files. Event lists count all sets/entries.

Version 0.4.1 uses the same Windows workflow/toolchain/runtime. Both packaged and extracted --smoke-test plus second-process --event-restart-check now exercise two sets, three repeated occurrences, actual saved ordering, identities, unchanged masters and project isolation. UI destination/search/scroll/window state is intentionally transient; complete musical event data/order persists. No new dependencies or export operations.
