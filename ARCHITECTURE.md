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
