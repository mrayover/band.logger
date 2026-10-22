# Numbered Drive layout / 0.11.0 — October 22, 2026

Permanent convention: each opted-in project has one projectRoot. Quaternity uses G:/My Drive/Quartet/band.logger. Incoming PDF selection starts at 00 - Inbox; the existing authoritative source remains 01 - Chart Library. Masters use 02 - Repertoire Distributions/Master (later Revision N); supplements use 02 - Repertoire Distributions/Supplements/Supplement NNN; gigs and rehearsals use 03 - Events/Gigs or Rehearsals/YYYY-MM-DD Title. Test events use 06 - Testing with the same type/date layout. 04 - Catalog & Backups and 05 - Archive are reserved; no new backup-export or automatic archival feature was introduced. No redundant root-level Library/Events/Gigs/Supplements or project-hash namespace is created. Other projects retain independent settings; legacy projects retain historical paths until explicitly configured/migrated. Private workspace stays in %LOCALAPPDATA%/band.logger.

Actual migration completed: the verified master (4 member ZIPs) and Supplements 001/002 (4 ZIPs each) moved to those destinations. All 12 distributed ZIPs are byte-identical; only owned manifests/path records were rewritten. Original IDs, selections, supplement chain, recovery and distribution history remain. Removed only resolved empty legacy containers, after backing up their Drive desktop.ini. All 253 authoritative chart PDFs and 301 unmigrated files (including those charts) were hash-verified unchanged.

Recovery finding: two old 0.3.1 applications had been open; the current workspace had regressed to schema 2 with no distribution/event history and regenerated catalog IDs. The owner closed the old windows. A newer immutable pre-event-exports backup contained the original 253 charts, 120 tunes, master, two supplements, two planned events and accepted review metadata. Verified every current chart path/hash against that backup before restoring its original catalog identities and retained history. Preserved both original workspace versions and an identity mapping. No event completion was inferred from its date. Quaternity's chart source and the unrelated Solo project were preserved.

Durable migration backup/journal: %LOCALAPPDATA%/band.logger/layout-migrations/20261022-numbered-layout; phase DONE. Migration locks workspace, validates project/catalog/package ownership and hashes, stages copies, verifies bytes, atomically saves references, verifies recovery, then removes exact verified originals. It rejects occupied destinations/unknown content and changed workspace/source/target; interruption checkpoints resume safely. Drive directory enumeration can lag: required payloads are checked by explicit paths and streaming hashes, while listings reject foreign content. Provider desktop.ini is backed up and is not required as destination payload. Schema 11 reads 1–10 with an immutable pre-numbered-layout backup; existing backups remain.

Ambiguous legacy folders remain in place because they have no verifiable stable event-ID manifests. Filename similarity is insufficient for migration or association:

- `G:\My Drive\Quartet\band.logger\02 - Repertoire Distributions\Events\20261025 Test` (4 ZIPs).

- `G:\My Drive\Quartet\band.logger\03 - Events\Gigs\Events\20261021 Eden Wine Lounge` (4 ZIPs).

- `G:\My Drive\Quartet\band.logger\03 - Events\Gigs\.bandlogger-zip-check-21fa1457b4bf4984b36cc55f5e760e03` (1 ZIPs).

- `G:\My Drive\Quartet\band.logger\03 - Events\Gigs\.bandlogger-zip-check-396863d031844c38b865a7a4ad9d69d8` (9 ZIPs).

Validation: 217 regression tests pass, including migration interruption/restart, nested master revisions, numbered routes, supplement continuity, collision/foreign-content protection, changed payload/workspace rejection, provider metadata, schema backup and unchanged other-project settings. Native synthetic Drive smoke/restart passed under an isolated 06 - Testing child: master, supplement, gig, rehearsal, test event, branded covers, saved history, recovery and verified cleanup. Actual migrated workspace verified on restart; isolated UI copy showed saved Drive destinations automatically in master/supplement/event previews without a picker. No production package was regenerated. Local Drive verification does not establish cloud synchronization or iPad import success.

CI checksum investigation: downloaded failed Windows run 39821242502 test artifact. Exact LF source is 4113 bytes, SHA-256 7bbb4839c373391a24e9dce81ac7517e634c1428762fd2e884803150208af524. Windows CRLF checkout yields 4121 bytes and the exact reported failing hash 5960fa81ba672996dc685efc1963f858922ccf59534ee2fe3b45245673749a86. Reproduced using an isolated Git index with core.autocrlf=true. .gitattributes now marks only the supplied SVG -text, preserving its exact original bytes; the exact checksum assertion and artwork are unchanged. This was a checkout environment difference, not a rendering defect or outdated assertion. New rule also verified with true/false/input checkout modes. Hosted Actions acceptance remains pending the owner's push; no hosted green result is claimed.

Delivery target: dist/band.logger-0.11.0-windows-x64/band.logger.exe, automatically extracted and verified by scripts/build-local.ps1; prior releases preserved, current shortcut advances only after verification. No commits or pushes. Detailed local evidence: .tools/drive-routing-0.11.0/ (private workspace/journal evidence must not be committed).

---

# band.logger

**A project-based workspace for musicians and working bands.**

**Project status:** Event notes, explicit status/history and automatic ready-to-launch local delivery (0.10.1). See the latest October 22 section; earlier deliveries are historical.
**Established:** October 20, 2026  
**Primary platform:** Windows desktop  
**Planned companion platform:** Android  
**First musical project:** Quaternity

## Overview

band.logger is an application for organizing the practical work of making music with other people.

Its long-term purpose is to bring tune libraries, charts, members, rehearsals, performances, recordings, and band operations into one coherent workspace.

The first development milestone focuses on **managing and distributing sheet music libraries**.

The application will connect to a project's source PDFs in Google Drive, organize those charts into a searchable catalog, and generate forScore-compatible library packages for members.

Development will proceed in small, working increments.

This README documents both the intended application and its verified implementation status. It must be updated as the project matures.

## Core design principle: Project first

A **Project** is the top-level organizational object.

Projects represent independent bands, ensembles, or other musical undertakings.

Each Project owns its own:

- Identity and branding
- Theme
- Members and roles
- Tune catalog
- Chart files and revisions
- Source and export locations
- Distribution history
- Future rehearsals and performances

The first Project is **Quaternity**.

Quaternity must be created using the same project system available to future groups. Its identity should not be hardcoded into the application's underlying logic.

The application must support multiple projects and switching among them.

## Initial development scope

The first development milestone will provide:

1. Project creation, selection, and editing
2. Project-specific themes and branding
3. Member and role management
4. Google Drive for desktop source-folder connection
5. Direct chart imports
6. Discovery of charts added to Drive outside the application
7. A searchable tune catalog
8. Chart-part and revision tracking
9. Initial forScore library master exports
10. Subsequent supplemental exports
11. Export history and receipts

These capabilities will be implemented across the four chunks described in `BUILD_CHUNKS.md`.

### Not yet included

Rehearsal and performance setlist creation is deliberately deferred.

Future versions will include setlist planning, numbered sets, per-member chart assignments, venues, performance actuals, rehearsal histories, and other band-management features.

Those future needs should inform the data model but must not inflate the first development milestone.

## The first Project: Quaternity

Quaternity is a four-member ensemble:

| Member | Primary instrument or role |
|---|---|
| Marcus | Guitar |
| Jordan | Tenor saxophone / EWI |
| Xavier | Bass |
| Ethan | Drums |

The application must allow members to have more than one role or chart preference.

Initial suggested chart preferences are:

- Marcus: Guitar, then C
- Jordan: Bb
- Xavier: Bass, then C
- Ethan: Drums, then C

These will remain editable. Later versions will support individual chart assignments for specific songs and events.

## Design and branding

### Typography

Use the **Lato** font family across the application.

Do not rely on Lato already being installed on the user's computer. Bundle or otherwise reliably supply the required weights and include the applicable font license.

### Quaternity palette

Initial theme values:

| Color | Hex value | Intended use |
|---|---|---|
| Navy | `#171B24` | Main backgrounds |
| Ivory | `#F4F2EA` | Main text and contrast |
| Gold | `#E7C66A` | Primary accents and selected states |
| Steel blue | `#7698AD` | Secondary accents and information |
| Raised surface | `#242B36` | Panels and cards |

The design should use accent colors purposefully rather than as constant decoration.

The development agent has discretion over initial spacing, control shapes, states, and visual treatments.

Color and branding choices must be centralized as reusable theme values.

### Brand assets

The owner will provide:

- Quaternity Q logo as SVG
- Potentially the complete Quaternity wordmark or logo as SVG

Until those files are supplied, use a temporary, clearly labeled placeholder.

Do not invent a substitute Quaternity logo.

Brand assets should be integrated only when their intended use and public-repository status are confirmed.

### Multiple project themes

Other projects must be able to use different names, logos, and colors.

Quaternity is the first theme configuration, not the only available appearance.

## Tune catalog and chart files

A tune is a musical work or arrangement represented by a catalog entry.

Each tune may have multiple associated charts, including:

- Concert pitch (`C`)
- B-flat transposition (`Bb`)
- E-flat transposition (`Eb`)
- Bass part
- Drum part
- Guitar part
- Piano part

Not every tune will have every chart type.

Each chart has its own revision history.

### Filename standard

The established filename convention is:

`Q Song Title -r00- Tuning.pdf`

Examples:

- `Q Along Came Betty -r00- C.pdf`
- `Q Along Came Betty -r00- Bb.pdf`
- `Q Black Dog -r00- Bass.pdf`
- `Q Black Dog -r00- Drums.pdf`
- `Q Nicas Dream -r01- Bb.pdf`

`Q` identifies the Quaternity-controlled version.

`r00`, `r01`, and subsequent values represent revisions of that particular chart or part.

The final label identifies the transposition or instrument part.

The system should make naming consistent for future files and flag existing exceptions for review.

It should not silently rename the owner's established files.

### Catalog behavior

The application will index existing PDFs and group related parts under one tune.

Source files must not be overwritten, deleted, moved, or modified during ordinary scanning.

New files can enter the catalog through two routes:

1. Import directly through band.logger.
2. Add the file to the connected Google Drive folder and have band.logger discover it.

New or ambiguous material should appear in a review workflow.

A physical `Inbox` directory is not required.

## Storage and Google Drive

Google Drive is the source of truth for the original chart PDFs.

The initial desktop application will work with the owner's locally synchronized Google Drive folder.

The project source and export destinations must be selected through the application and stored as project settings.

Do not assume a particular drive letter.

### Suggested project folders

Within the existing Quaternity Drive area, the application can use:

- `Charts/` — authoritative source PDFs
- `Exports/Library/Master/` — initial master distribution packages
- `Exports/Library/Supplements/` — later additions and revisions

The `Charts` directory may be flat. The agreed filename convention provides the chart classification.

Do not create a mandatory transposition-folder hierarchy.

Application database files and temporary build products should remain outside the public source-chart folder.

### GitHub is separate

The public GitHub repository will contain the application's code, configuration, documentation, tests, and authorized assets.

It must not contain the band's copyrighted chart collection.

## forScore exports

The first export system will generate `.4ss` packages for importing scores into forScore.

The documented open `.4ss` format is XML. It can include Base64-encoded PDF files for forScore 14.3 and newer.

The initial master package will establish a member's distributed library.

Later supplemental packages will include only new or revised charts appropriate to that member.

The application must track which chart revisions were included in each successful export.

### Library masters

The first distribution for a project will be the **Library Master**.

Member packages are created according to initial chart preferences.

A master must have a recorded manifest.

### Supplements

Supplements will:

- Be sequentially numbered
- Include only appropriate new or revised chart entries
- Preserve the original master
- Preserve previous supplemental exports
- Maintain a permanent export record
- Produce a receipt

An unchanged chart must not be repeatedly exported as though it were newly added.

### Import and annotations

forScore can match reference-only open setlist entries with existing library files using the stored filename.

Embedded-PDF exports behave differently: they introduce PDF data and may create additional copies on filename conflicts.

Therefore, band.logger must not assume a chart's annotations automatically transfer to a revised PDF.

A future setlist-generation feature should prioritize references to charts already installed in each musician's forScore library.

This behavior must be verified through actual device testing.

Official documentation: https://forscore.co/developers-file-types/

## Technical direction

The preferred architecture is:

- Kotlin Multiplatform
- Compose Multiplatform
- Shared domain models and business logic
- Windows desktop application first
- Android-compatible application foundation
- Local application persistence
- Filesystem integration for the initial desktop Drive workflow
- Future Google Drive API integration for Android

The initial application is designed to operate without a custom cloud backend.

The development agent must verify the chosen framework and dependencies against the actual build environment.

Technical decisions and deviations must be recorded in `ARCHITECTURE.md` and `DECISIONS.md`.

### XML and PDF packaging

The application should create `.4ss` files directly from the official XML specification.

A separate proprietary conversion tool is not assumed or required.

All needed build dependencies, testing tools, font resources, and fixtures must be acquired and configured by the development agent.

The owner should not be expected to troubleshoot SDK or XML dependencies independently.

## Development process

The project is being built in chunks so that development decisions can respond to real use.

The staged plan is defined in `BUILD_CHUNKS.md`.

| Chunk | Scope | Status |
|---|---|---|
| 1 | Application, projects, branding, members, `.4ss` technical proof | Owner reviewed; ZIP iPad diagnostics passed |
| 2 | Drive integration, imports, and tune catalog | Owner approved October 20, 2026 |
| 3 | Library master export | 3B implemented and Windows package tested; owner/iPad review pending |
| 4 | Supplements, history, and receipts | Not started |

Every chunk must have a successful build, relevant tests, and a documented verification result.

Work Mode sessions must update the project documentation and leave the repository in a reviewable state.

### Documentation

- `README.md` — project overview and current capabilities
- `BUILD_CHUNKS.md` — development instructions and acceptance criteria
- `CURRENT_STATE.md` — precise implementation state
- `WORK_LOG.md` — dated development session record
- `ARCHITECTURE.md` — technical design
- `DECISIONS.md` — confirmed and pending design decisions

These implementation documents are now maintained in the repository. See `CURRENT_STATE.md` for verified scope and `WORK_LOG.md` for test results.

## Getting started — project owner

The intended local directory is:

`C:\Users\mrove\Projects\band.logger\band.logger`

The owner uses **GitHub Desktop** to manage the repository.

Original setup workflow (completed; retained for reference):

1. Create or clone the `band.logger` repository using GitHub Desktop.
2. Ensure its local directory is the intended location.
3. Place `README.md` and `BUILD_CHUNKS.md` in the repository root.
4. Make the initial commit through GitHub Desktop.
5. Publish the repository publicly when ready.
6. Provide Work Mode with access to the existing repository and instruct it to begin Chunk 1.

If the project already exists, do not create a second repository with the same purpose.

The development agent should provide any additional setup instructions required for the owner's actual machine.

The owner will review and publish subsequent changes through GitHub Desktop.

## Future roadmap

Later milestones may include:

- Rehearsal setlists
- Performance setlists with Set 1, Set 2, Set 3, and additional sets
- Member-specific charts for individual tunes
- Setlist title pages and summaries
- Venue records
- Planned versus actual performance records
- Tune performance and rehearsal history
- Rehearsal notes
- Recording capture and organization
- Content scheduling
- Android companion application
- Cross-device synchronization

These capabilities are planned, not implemented.

## Contribution and licensing

The repository is intended to be public.

A public repository does not automatically establish an open-source software license.

The owner will choose the software license before inviting third-party reuse or contributions.

Third-party dependencies and bundled font assets must retain their required license notices.

Do not commit copyrighted chart PDFs or private material.

## Living documentation policy

This README is not a frozen specification.

As each development chunk is completed:

1. Replace planned claims with verified implementation descriptions.
2. Update the development status table.
3. Document changes to setup or dependencies.
4. Update the architecture description when necessary.
5. Identify significant limitations.
6. Preserve the overall project vision unless the owner changes it.

**The repository is the long-term source of truth for band.logger's design and implementation.**
## Permanent Windows delivery and acceptance requirement

Every completed development chunk must produce a usable, downloadable Windows desktop application. Build and supply an `.exe` with all required runtime components and dependencies, preferably inside a self-contained ZIP that launches without a development environment. Establish automated Windows packaging in GitHub Actions in Chunk 1 and deliver an updated package after every subsequent chunk. Test compilation, packaging, and functionality; preserve all previously completed functionality; include a simple manual testing checklist with every delivery. A chunk is not fully accepted until the owner has had the opportunity to run and review its Windows application. Do not begin the next chunk without owner approval. Local build packages may be delivered before publication; GitHub Actions artifacts require the owner to commit and push through GitHub Desktop.

The actual local repository is `C:\Users\mrove\Projects\band.logger\band.logger`. Use this existing checkout; do not recreate it.

## Running the Chunk 2 Windows preview

Obtain `dist/band.logger-0.2.0-windows-x64.zip` from the local build, or the Windows application workflow artifact after the owner commits and pushes. Extract the whole ZIP and launch `band.logger/band.logger.exe`. Keep its app and runtime folders together. Windows 10/11 x64 is the target. No Java or development environment is needed. This preview is unsigned.

Project creation/editing/switching, editable Quaternity starter members, ordered chart preferences, per-project colors, local persistence, bundled Lato and synthetic `.4ss` export are implemented. SVG rendering is pending; branding is a labeled text placeholder. Chunk 2 adds a per-project source folder, searchable tune catalog, safe PDF imports, refresh, revision history and Needs Review. Google Drive for desktop performs cloud synchronization.

Use `MANUAL_TESTING.md` for review. Local data is under `%LOCALAPPDATA%\band.logger`. See `CURRENT_STATE.md` for limitations and `WORK_LOG.md` for results. Chunks 1 and 2 are owner approved. Chunk 2 was accepted October 20, 2026. Stage 3A preview is implemented; see CURRENT_STATE.md for the Windows launch blocker and CHUNK_3_PLAN.md for remaining stages.

### Development builds

Run `scripts/build-local.ps1` to use the local toolchain (or download it if absent), run tests and package the application. The Gradle Wrapper and pinned dependencies provide reproducible build configuration. Internet is required for the first build. `.tools`, build output and `dist` are ignored.
### Chunk 2 library workflow

Open Library → Connect folder and choose your locally synchronized chart folder. Scanning does not modify PDFs. Refresh after adding charts outside the app. Review new or ambiguous entries under Needs review. Import PDF copies one chosen PDF into that folder under the filename you confirm and refuses to overwrite an existing file. Metadata corrections do not rename originals. Different projects keep independent catalog records.

The forScore proof now saves a ZIP. Transfer it through Drive, save locally on iPad, extract in Apple Files, then open the .4ss in forScore. Production library masters remain Chunk 3 work.

## Stage 3A — Library master preview (historical delivery notes)

Version 0.3.0 adds Library master → Create Library Master · Preview, member allocations based on ordered preferences, explicit fallbacks and omissions, searchable chart lists, file issues, and a project-specific export destination. It uses accepted charts from the saved catalog. Newer unresolved or missing preferred revisions block a tune instead of silently falling back or using an older revision. Review charts in Library first. No production master files or manifests are generated in 3A.

Candidate package: dist/band.logger-0.3.0-windows-x64.zip, with executable, runtime, dependencies, launch-status note and manual checklist. Extract the entire ZIP and keep band.logger.exe beside app/ and runtime/. Close any older band.logger process before normal use. No development tools are required by this package.

**Launch verification is blocked on the owner machine:** Windows Smart App Control rejected the unsigned 0.3.0 executable. Compilation, 42 tests, development UI checks and ZIP integrity passed; packaged executable launch did not pass. The earlier 0.2.0 ZIP is retained. Stage 3A is not fully accepted until its Windows package can launch and the owner can review it. No security settings were changed. See CURRENT_STATE.md for evidence and supported signing references.

## Historical Windows delivery — Stage 3B / 0.3.1

The prior Windows signing block is resolved after the owner disabled Smart App Control. Both packaged and extracted launch tests now pass. Download the local dist/band.logger-0.3.1-windows-x64.zip, extract all files, close older band.logger windows and run band.logger/band.logger.exe. The runtime is included.

Library master now previews and generates confirmed member ZIPs with .4ss files, instructions and a saved manifest. Review/accept charts in Library, choose an export destination, inspect all members and confirm. Members with zero selected charts are explicitly skipped. Source PDFs and previous exports remain unchanged. A completed export can be recovered if its app-record save fails.

51 tests passed, including prior features, XML/PDF correctness, source-change rejection, collision/partial-failure handling, recovery and schema migration. A real-chart ZIP passed byte verification and persisted after app restart. Actual iPad import remains pending; see MANUAL_TESTING.md and CURRENT_STATE.md. Schema 3 preserves a pre-master backup; older applications reject it. This supersedes the earlier blocked/preview-only notes.

## October 21, 2026 — Homepage increment (0.3.3)

Current behavior supersedes the earlier homepage and pending-iPad notes above. Home now has four large equal cards, arranged in two columns at desktop widths and one column when content width is below 500 dp. Bundled Lato, project primary/secondary accents, hover/focus borders, the project header, project editing and the existing accent-filled selected-project button are retained.

Create Rehearsal, Create Gig and Upcoming Events each show a Coming soon dialog. Pending Distribution opens a dedicated sidebar destination explaining that queue tracking and supplements are not implemented, with links to the working Library master screen and Home. There are no fabricated distribution counts. Existing library, member, theme, master export, manifest and recovery functionality remains in place; no schema, source-PDF or dependency change.

The owner confirmed that the actual Library Master successfully imported into forScore on their iPad and the tunes were present. This is owner-reported production import verification, not an agent-performed device test or proof of annotation transfer. The remaining Stage 3C checklist is not automatically marked complete.

Current delivery: dist/band.logger-0.3.3-windows-x64.zip; extract all contents and run band.logger/band.logger.exe. See CURRENT_STATE.md and WORK_LOG.md for final verification. Earlier version-specific sections are historical.

## October 21, 2026 — Pending Distribution / 0.3.4

Pending Distribution now calculates approved per-member selections from the single catalog and recorded masters. It shows New Tunes, New Parts, Revised Charts, attention exceptions, initial-master requirements, search, expandable details and baseline inspection. Counts are member–tune selections. Source PDFs are checked read-only on entry/recheck; changed/missing files block selection without changing review state. Use Library / Needs review, then its Needs review tab, to resolve issues (Refresh first after external file changes).

Baseline: latest valid non-empty master for each member, sorted by UTC export time then ID. Skipped members keep earlier non-empty baselines. Export records do not prove receipt/import. No supplemental files, numbering, history or optional Supplemental Export Preview. Schema 3, existing workflows, Home and project highlights remain unchanged. Current ZIP: dist/band.logger-0.3.4-windows-x64.zip; executable: dist/band.logger-0.3.4-windows-x64/band.logger/band.logger.exe. Prior delivery sections remain historical.

## October 21, 2026 — Event foundation / 0.4.0

The owner explicitly authorized a bounded event-creation increment, superseding the earlier deferral for this scope only. Create Rehearsal and Create Gig open an editor; Upcoming Events (Home/sidebar) lists this project's saved events. Title is required, type is editable, date is optional YYYY-MM-DD. Search existing catalog tunes and append/remove selections in one initial Set 1. Save, reopen and continue editing after restart. Empty/undated events are allowed. The list deliberately retains past and undated records, sorted by date then title, with undated records last.

Uses the existing theme/Lato, project/catalog and workspace store. Stable event/set/entry IDs survive saves and edits; entries reference existing tunes. Save is explicit, with dirty status and Save and continue / Keep editing / Discard changes guards for navigation, project creation/switching and normal window close. Failed saves retain the draft. Forced termination/power loss can lose unsaved edits. Catalog choices require current non-excluded chart records; approval is not required for planning. Selections whose active charts later disappear remain retained with a warning.

Schema 4 migrates schemas 1–3 on read, preserves their full original bytes in workspace.pre-events.backup.json before the first write, and keeps existing backups plus the rolling previous-save backup. Older binaries reject schema 4; intentional rollback requires the appropriate pre-migration workspace and loses newer event edits.

Delivery: dist/band.logger-0.4.0-windows-x64.zip. Extract all files and run dist/band.logger-0.4.0-windows-x64/band.logger/band.logger.exe; bundled runtime, no developer Java required. Close old app windows first. This is an unsigned Windows x64 preview. All 82 tests and packaged/extracted smoke tests, including separate-process event reopening/editing, passed. Extracted UI creation/save/restart/reopen/edit, project isolation and unsaved guards passed with synthetic data. See CURRENT_STATE.md and WORK_LOG.md for precise coverage. No new dependencies, PDF changes, library/master/pending behavior changes, commits or pushes.

Deferred: multiple sets, ordering/drag-and-drop, templates, member assignments, event deletion, reference-only .4ss/event ZIP exports, cover pages and automatic draft recovery. Next: owner review of this event foundation, then a separately authorized initial-set ordering increment before larger setlist/export work. Previous roadmap and delivery sections remain historical.

## October 21, 2026 — Two-column setlist editor / 0.4.1

This supersedes the single-set restrictions in 0.4.0. Open any rehearsal/gig from Upcoming Events. The compact event header retains title/type/date, save status and Back to events. Library search and + actions occupy 38% of desktop content width; the ordered setlist occupies 62%. Both use independently scrolling lists; below 700 dp editor width the panels stack. Existing project sidebar, theme and Lato remain.

Add Set creates/selects the next numbered set. Select a header or Add Song to choose the destination and focus search. + appends immediately; repeated tunes are allowed as distinct stable entries. Up/Down changes the persisted order in that set, with boundary actions disabled. Remove affects only the chosen occurrence. Remove Set requires confirmation for populated sets, renumbers remaining sets without changing their IDs/content, and cannot remove the last set. Removing the destination selects another remaining set. Save/restart preserves all event/set/entry data and order; active destination, search, scroll and window size are transient (reopening selects the first set).

Schema 5 retains the existing initialSet representation and adds ordered additionalSets. Existing single-set events load as Set 1 without identity/song loss. First write from schemas 1–4 preserves original bytes in workspace.pre-multisets.backup.json; earlier migration backups and rolling/atomic-save protections remain. Older apps reject schema 5 instead of silently dropping sets. Existing catalog/master/pending behavior is unchanged; no new dependencies or source-PDF changes.

Delivery: dist/band.logger-0.4.1-windows-x64.zip; extracted EXE: dist/band.logger-0.4.1-windows-x64/band.logger/band.logger.exe. Extract all, keep app/ and runtime/, close older app windows and launch; Java/Gradle is not required. Unsigned Windows x64 preview. All 97 tests pass; packaged/extracted second-process smoke and synthetic UI desktop/narrow/search/add/order/save/restart/reopen checks passed. Previous Windows archives remain byte-identical. See CURRENT_STATE.md, WORK_LOG.md and the included MANUAL_TESTING checklist for precise coverage.

No drag/drop, direct move-between-sets, exports, templates, cover pages, venues, notes, recordings or supplement generation. Cross-set moves use remove then add. Save explicitly; forced termination can lose unsaved drafts. Comfortable height is approximately 650 px or taller. Next: owner review using the new checklist, then a separately authorized reference-only forScore export design/device-matching validation. No commits, pushes or releases performed.

## October 21, 2026 — Member-specific reference event exports / 0.5.0

The owner tested/approved 0.4.1 and committed it before this increment. Saved rehearsals/gigs now offer Export for Members while retaining the two-column editor. Save unsaved edits first. Preview shows each current musician's preference label, latest valid non-empty Library Master baseline, exact recorded filenames, total/matched/missing occurrence counts, non-empty sets and destination. Missing entries/no baseline require explicit acknowledgement; undated events require separate Undated acknowledgement or a saved date. Empty events/no members are rejected; empty sets are warned/skipped with original heading numbers retained.

One ZIP per member contains one complete reference-only .4ss plus READ-ME.txt, preserving all non-empty sets and repeated occurrences. The XML uses official forScore score path references and Set N placeholders, with no data attributes/PDF content. References use recorded filenames by stable tune ID, including legitimate recorded fallbacks; current catalog revisions/preferences never replace that baseline selection. Current preferences describe output labels only. Missing matches become explicitly titled placeholders in position; no-master members receive acknowledged placeholder-only output. A generated master does not prove those filenames are installed in forScore.

Example: 20261021 Eden Wine Lounge (Marcus - Guitar).zip contains the same-named .4ss and READ-ME.txt; its internal setlist title omits the extension. Date comes from the saved event. Windows-unsafe labels are sanitized/truncated without changing event titles; duplicate member labels receive a deterministic suffix. Files go under <chosen export root>/Events/<event date and title>/; repeated exports create new timestamp/unique-ID-suffixed folders. Existing configured destination is the default; a dialog override is not saved to workspace settings.

All XML is parsed before writing, then member ZIPs are staged, reopened/parsed and published as one new completed folder. Nothing is recorded as distributed/received and workspace schema remains 5. No PDF or master archive access occurs in event generation. Existing master/pending behavior and all backups remain. No new dependencies installed. Official specification: https://forscore.co/developers-file-types/ .

Delivery: dist/band.logger-0.5.0-windows-x64.zip; EXE: dist/band.logger-0.5.0-windows-x64/band.logger/band.logger.exe. Extract all, retain app/ and runtime/, close older app windows and launch without installing Java/Gradle. Unsigned Windows x64 preview. All 120 tests (97 prior + 23 new) pass, as do packaged/extracted executable smoke tests with parsed reference-only event ZIPs and prior restart checks. Extracted synthetic UI preview/acknowledgements/generation/Open Export Folder passed; generated member XML and workspace immutability checked independently. All 207 extraction files and every prior Windows ZIP match SHA-256 checks. No commit/push/release/upload.

Owner iPad verification is still required: ZIP through Drive → save locally in Files → extract → share .4ss to forScore; check set headings, repeats, exact installed filename matches and missing placeholders. Prior successful Library Master import does not establish this new reference-only workflow. Next: perform that device gate and report results before separately authorizing more export work. No supplements, templates, cover pages, embedded-PDF event output, per-song overrides or persistent event-export history implemented. Interrupted exports can leave hidden staging folders; do not distribute them. Long destination paths may need a shorter root.

## October 21, 2026 — Drive ZIP validation correction / 0.5.1

Owner reported 0.5.0 failed with zip END header not found while generating to Google Drive for desktop. Reproduced on the actual G: provider with an isolated synthetic archive: immediate ZipFile reopen failed, while sequential reads returned a valid complete archive. One metadata probe reported length 1 for 358 readable bytes; querying metadata before opening could mask the timing issue. The previous local-only verification did not catch this virtual-drive behavior.

0.5.1 completes each reference-only ZIP in memory, validates its entries/XML sequentially, writes the complete bytes, then compares every byte through a sequential input stream without using the destination's reported random-access length. Normal staging/no-overwrite protections remain. No schema, naming, reference-resolution, chart, event or distribution changes.

Use dist/band.logger-0.5.1-windows-x64.zip and dist/band.logger-0.5.1-windows-x64/band.logger/band.logger.exe. Extract all, close the 0.5.0 window, then launch 0.5.1 and retry the saved event with the same destination. All 123 tests pass; normal packaged/extracted smoke and additional packaged/extracted synthetic exports on G: pass. Local extracted UI with four members/two sets/18 occurrences generated valid complete packages. The additional UI click to G: was rejected by automatic approval review as an external transfer; it was not retried or bypassed. A local-only UI test completed instead. No real event export or event edits were performed by the agent; owner should retry their actual event. iPad verification remains pending. Prior builds preserved, no commit/push.

## October 21, 2026 — Supplemental distribution publishing / 0.6.0

Owner confirmed 0.5.1 event exports imported correctly on iPad and committed all prior work (baseline 9f284c9). This is owner-reported event import verification; new supplement imports still need review.

Pending Distribution now previews and publishes numbered Library Supplements from accepted eligible changes, independently per musician/tune/instrument part. Latest valid nonempty master per member establishes a baseline epoch; matching recorded supplement deltas accumulate independently for each part. New tunes, newly required parts and revised bytes/revisions are eligible; unchanged delivered parts are excluded, including switching back to a part already delivered in the same epoch. New masters establish new epochs without deleting history. Event exports intentionally remain master-only in this release.

Open Pending Distribution → Preview next supplement, review exact member/chart/part/revision/path/hash selections and exclusions, acknowledge, then Publish. Only affected musicians receive ZIPs, each with embedded-PDF .4ss and instructions. Source paths/hashes are rechecked before embedding and publication. Conflicts/unapproved/missing charts block their selections; musicians without valid initial masters are warned/excluded. No eligible selections means no publication.

Permanent output: <configured export root>/Supplements/Project-<stable project hash>/Supplement 001, 002, etc. Existing masters/previous supplements/source PDFs are preserved. Receipt/history persist number, timestamps, baseline IDs, member preferences/allocations, chart identities/revisions/hashes, package names/hashes and selection fingerprint. Distribution history opens previous master folders and supplement receipts. Export is not confirmation of import/receipt.

A local durable pending-publication journal blocks duplicate generation after completed files but failed workspace save, including restart/project rename/destination changes. Recover supplement verifies packages, project/catalog identity and number/history chain, then records idempotently without regeneration. Incomplete publication never advances saved numbering; retry can leave an old incomplete stage for inspection. Do not distribute stages or delete/move journal/output files manually. Schema 6 adds supplements and preserves schemas 1–5 bytes in immutable workspace.pre-supplements.backup.json before first write; earlier backups retained. Older binaries reject schema 6.

Delivery: dist/band.logger-0.6.0-windows-x64.zip; EXE: dist/band.logger-0.6.0-windows-x64/band.logger/band.logger.exe. Extract all, keep app/ and runtime/, close older apps and launch without Java/Gradle installation. Unsigned Windows x64 preview. All 136 tests, packaged/extracted smoke, synthetic UI publication/zero-pending/restart/history receipt and 207-file archive verification passed. Previous ZIPs are byte-identical. No dependencies installed or GitHub Actions changes; no commit/push.

Limits: no event-export consumption of supplements, templates/covers/calendar or installation tracking. Existing master limits apply (64 MiB/PDF, 512 MiB/member). Production Drive publishing and supplement iPad import were not manually performed this session; test locally/synthetically first, then owner verify the normal ZIP → Drive → local Files extraction → forScore route. Next: owner supplement review/import, then separately authorize event exports to consume the cumulative distribution history.

## October 21, 2026 — Event references include supplements / 0.6.1

Event Export for Members now resolves each musician's exact recorded repertoire from their latest valid nonempty Library Master and successfully recorded supplements anchored to that master. Supplements apply in consecutive recorded publication/chain order, never timestamp order. A later master starts a new member-specific epoch; older anchored supplements cannot override it, while skipped members retain their own earlier baseline. Each tune/part retains its own delivered revision; the last applicable delivery for a tune supplies its event reference. Current catalog revisions and preference changes never imply distribution. Invalid supplemental records stop preview/export with an explicit inconsistency message; genuinely missing or unsafe references retain placeholders.

The existing preview identifies Library Master or Supplement NNN, publication identity, part and revision for each matched occurrence. Event output remains one complete reference-only .4ss per member plus instructions, preserving sets/repetitions/saved identities/order/naming/destination/acknowledgements and Google Drive-compatible sequential ZIP verification. No PDFs are embedded or read, and no distribution history/event metadata is written. Workspace schema remains 6; no migration or publisher changes.

Production Supplement 001 manifest was inspected read-only: Night Dreamer is recorded for Marcus, Xavier and Ethan as Q Night Dreamer -r00- C.pdf, and Jordan as Q Night Dreamer -r00- Bb.pdf. Built-release verification used an isolated copy of the preserved 466-selection master snapshot plus the actual supplemental manifest and a test event; all four resolved 1 matched/0 missing and generated validated reference-only local ZIPs. Original workspace/backups and production manifest remained unchanged. This is not a new iPad test or a modification of the live saved Test event.

Owner reports production master import, reference-only Eden gig setlist import, Supplement 001/Night Dreamer import and persisted distribution history successful. Those reports establish earlier owner/device results; integration-specific Drive/iPad import remains an owner check in MANUAL_TESTING.md.

Delivery: dist/band.logger-0.6.1-windows-x64.zip and dist/band.logger-0.6.1-windows-x64/band.logger/band.logger.exe. Extract all and retain app/runtime. All 148 tests pass (136 previous retained/adapted plus 12 new), packaged/extracted executable smoke and separate-process cumulative-reference reopening pass. Prior versions and workflow retained. No new dependencies, commits, pushes or releases.

## October 21, 2026 — File organization / 0.7.0

Import PDF opens the owner's staging folder C:\Users\mrove\OneDrive\Desktop\Quaternity\band.logger\sorted when it exists/readable, with the normal chooser fallback otherwise. This is only a starting-location preference: accepted PDFs are still safely copied into the authoritative catalog source, reviewed normally, and never moved/deleted from staging.

Future supplements use <distribution root>\Supplements\<readable project name>\Supplement NNN. A hidden .bandlogger-project-id ownership file reserves each readable folder for its stable internal project ID; a short stable suffix is used only when the readable name is occupied by another identity or unowned nonempty output. ZIP and contained .4ss names are Project - Supplement NNN - Member, with numeric suffixes only for genuine member-name collisions. Embedded PDFs/selections are unchanged and timestamps/hashes/IDs remain in receipts/manifests. Historical hash-folder publications are not renamed/moved/regenerated; receipts/open/recovery keep their recorded paths. Unrecorded legacy targets and durable intents still block duplicate publication.

Each project now has an optional persistent Events export root, separate from Library Master exports. Default when available: G:\My Drive\Quartet\band.logger\03 - Events. Set it under Home → Edit project, or select a root in Export for Members and explicitly choose Remember Events root for this project. Generating alone uses a temporary override without saving it. Unavailable configured roots report errors; there is no silent fallback to library destinations. Saved gig/rehearsal types route to Gigs/Rehearsals below the root. Date/title/member basenames and complete reference-only event content remain; repeat folders use a readable Export timestamp/short-identity suffix without overwriting.

Schema 7 adds only eventsExportRoot. Schemas 1–6 load safely with null/unconfigured value and first save preserves immutable workspace.pre-event-destinations.backup.json, retaining earlier backup files and distribution/event/catalog identities. Older apps reject schema 7. 161 tests (148 retained/adapted + 13 new) pass; Windows packaged/extracted smoke covers both routes, separate setting persistence, readable supplements and exact forScore content. Release: dist/band.logger-0.7.0-windows-x64.zip; EXE dist/band.logger-0.7.0-windows-x64/band.logger/band.logger.exe. Extract all with app/runtime; all previous builds retained.

No production chart/event/distribution changes or new iPad/Drive publishing test claimed. Owner should follow the latest MANUAL_TESTING checklist for their actual roots, historical Supplement 001/002 receipts and next future supplement. Preserve folder ownership markers; unmarked nonempty same-name folders are treated as collisions, not adopted. No unrelated functionality, dependencies, workflow changes or commits/pushes/releases.

## October 22, 2026 — Export organization and event management / 0.8.0

Use one remembered Project export root, editable in Project details, Library master or the event preview. Future exports use Library/Master (later masters in readable Revision N subfolders), Library/Supplements/Supplement NNN, Gigs/YYYY-MM-DD Title, or Rehearsals/YYYY-MM-DD Title. ZIPs are directly in their package folder. Event/member ZIP titles keep their established date labels. Hidden staging names and manifests retain internal IDs; completed user-facing folder names contain no IDs/hashes or duplicate project namespace. A root's Library ownership marker rejects reuse by another project; choose a separate root for each project.

Upcoming Events has Edit and Delete directly on each event. Edit opens the unchanged editor/save protections. Export records persist stable event identity, original saved event snapshot, exact root/folder/stage paths, member IDs/names/ZIP filenames/SHA-256 values, timestamps, lifecycle and cleanup progress. Title/date/setlist edits retain the associations. New export only stops on conflicts; Create revision preserves prior output as Revision N; Replace tracked exports first publishes a complete new revision, then cleans all previous verified tracked packages. Untracked folders are never replaced. Partial replacement cleanup is reported and retryable from Upcoming Events.

Deletion confirmation lists the event, exact folders and member ZIPs. Save the event removal and cleanup tombstones before removing files. Preflight manifest identity, root/type containment, normal paths, package hashes and an exact permitted-file inventory. Delete only those ZIPs, then the manifest and empty event folder, saving progress after each package. No recursive deletion. Changed/unrelated files or unavailable destinations retain cleanup records/errors. Retry after reconnecting; failed initial metadata save deletes nothing. Completed tombstones retain original identity and paths. Recover pending exports resolves publication/save interruptions without a file picker.

Discover older exports scans configured current and legacy roots read-only, with at most 1000 immediate children per known Events/Gigs/Rehearsals location. Automatically associate only matching project/event stable IDs, original paths, manifests and verified package hashes. Older versions did not write event IDs/manifests: those outputs, including ambiguous name/date matches, appear for review and remain untouched. Names alone never prove ownership. Review export layout migration shows existing master/supplement/event paths and the future layout; it performs no moves.

Schema 8 reads schemas 1–7 without writing and retains immutable workspace.pre-event-exports.backup.json on the first old-schema save, plus all earlier backups and atomic storage. Historical master/supplement manifests, absolute paths, chains and recovery remain authoritative and unchanged. Safe migration is future-only organization while keeping historical paths in place. Physical relocation is deliberately not automated: it requires a separately reviewed mapping, metadata/manifest updates, backups and recovery verification. Do not bulk-move production folders.

Windows package: dist/band.logger-0.8.0-windows-x64.zip. Extract all and run band.logger/band.logger.exe with app/runtime alongside it; no developer Java is needed. Unsigned x64 preview. No new dependencies, commits, pushes, releases, branding, covers, analytics, Archive or post-show notes.

Drive cleanup detail (0.8.0): Drive for desktop may inject a hidden UTF-16 desktop.ini after publication. Only its bounded, exact known GoogleDriveFS icon/ViewState profile is recognized; arbitrary desktop.ini or other files still block deletion. Persist the exact Base64 bytes in providerMetadata before removing that decoration, retaining them in the completed tombstone. Changed profiles remain for review. Synthetic local regressions and actual Drive retry passed with no event folders/ZIPs remaining.

## October 22, 2026 — Official Quaternity branding and optional embedded covers / 0.9.0

The supplied QuaternityLogo.svg is bundled byte-for-byte (SHA-256 7BBB4839C373391A24E9DCE81AC7517E634C1428762FD2E884803150208AF524) and rendered offline with the existing Skia SVG runtime. Preserve its 1332:1411 proportions, geometry and original colors. Quaternity appears in the selected-project identity, home/page headers and compact event-editor header; existing Lato, project palette/highlighting/navigation remain. Other projects retain text identity unless explicitly assigned the Quaternity brand in Project details. The Quaternity starter sets that brand. Migration assigns it once to legacy projects whose exact name/prefix are Quaternity/Q, retaining stable project IDs; current-schema opt-out and subsequent renames persist. External logoReference is retained as metadata and is not read or substituted for the official asset.

Export for Members adds one optional Include branded set covers checkbox, off by default and remembered per project. Enabled output creates one portrait US Letter cover PDF for each nonempty saved set, replacing its plain Set N placeholder within the existing complete event .4ss. Each cover has the official logo on ivory, navy/gold bands, embedded Lato, project name, saved event title, saved ISO date (or Date not set), gig/rehearsal designation and actual Set N. Long titles wrap/scale within bounded space. No performance time is invented because saved events do not support it. Other projects use their own project name without the Quaternity image.

Cover entries use the documented forScore score data attribute with Base64 PDF bytes (forScore 14.3+). Music entries continue to use the exact existing master/supplement filenames with no data attribute; order, repetitions, member allocations and missing placeholders stay intact. There is exactly one whole-event .4ss plus READ-ME.txt per member ZIP, with no loose cover files, extra cover ZIPs or per-set .4ss files. Disabled covers retain the established plain Set N placeholder XML. Empty sets remain skipped with their original display numbers.

Stable readable cover names use band.logger Cover - Project - Gig/Rehearsal - Date - Event - Set N.pdf, with safe bounded labels and no technical IDs in the filename. A cover name conflicting with a known chart reference is blocked. forScore's published specification says embedded PDF filenames are adjusted to avoid existing filenames. Consequently re-imports may create additional cover copies; stable names do not prove deduplication, and no overwrite/deduplication guarantee is claimed. The preview and instructions disclose this; final iPad import/re-import testing remains required. App export-folder replacement/cleanup affects only tracked Drive packages, never an iPad's library.

Schema 9 adds project brand/cover preference and optional cover receipt metadata. Schemas 1–8 load without writing; first old-schema save preserves immutable workspace.pre-branding-covers.backup.json plus all earlier backups/atomic saves. Old event manifests lacking covers still decode and verify with an empty cover list. New export records retain set IDs/numbers, cover filenames/PDF hashes and source-logo hash. All original event/distribution identities, historical locations, master/supplement chains, publication intent/recovery, numeric revisions/replacement and retryable cleanup remain. Existing production folders are never moved or regenerated.

Windows 0.9.0 uses the established self-contained packaging workflow. PDFBox 3.0.8/FontBox/pdfbox-io/Commons Logging are the only new runtime dependency family; their Apache notices remain in bundled JARs, and the existing Lato license remains included. No external renderer, logo application or network connection is needed at runtime. Full synthetic tests and package/extracted checks are recorded in WORK_LOG; owner acceptance uses the latest MANUAL_TESTING section.


## October 22, 2026 — Sharp vector desktop logos / 0.9.2

Quaternity marks now draw directly from cached vector commands at the window's actual display density, replacing bitmap scaling. Original artwork, transparent edges, proportions and all existing header/sidebar/branding-preview sizes are preserved. Packaged Windows app inspected at process-local 100%, 125%, 150% and 200% display-scale overrides; 196 automated tests and full packaging/extracted checks pass. The small sidebar mark still has finite pixels for its fine detail.

PDF covers retain the existing lossless transparent logo at approximately 436 dpi, with independent ivory/navy/gold page backgrounds and unchanged layout, text and forScore behavior. PDF vector conversion is not exposed by the bundled Skia API; no new converter dependencies were added. Deliverable: dist/band.logger-0.9.2-windows-x64.zip. Existing releases and production data preserved; no commits or pushes.


## October 22, 2026 — Event notes, trustworthy history and automatic local delivery / 0.10.1

Events now has Upcoming (all Planned events, including past/undated) and History (Completed/Canceled) views, with gig/rehearsal filters. History sorts by recorded date newest first, undated last. Cards show status, set/tune-occurrence counts and whether notes exist. Notes & status is separate from Setlist in the same event editor; multiline notes and status changes share explicit Save event and existing dirty/navigation/close guards. Notes stay internal to band.logger, including exclusion from export manifests, XML/ZIP instructions and generated PDFs.

All new and migrated events default to Planned; dates never imply completion. Canceled preserves the event. Confirming Completed captures a performed-setlist revision with the saved event identity, title/type/date/test classification, ordered sets, stable tune and occurrence IDs, and tune-label snapshots. Later planning edits do not rewrite those facts. Review recorded setlist shows every revision. To correct actual facts, edit the current setlist/details and explicitly choose Correct record from current setlist, confirm, then Save event. Corrections append a revision; earlier records remain. Only a Completed event whose latest confirmed snapshot is not marked Test qualifies for future performance counting. Repeated occurrences and distinct events remain separate. No analytics dashboard or advanced Archive was added.

Workspace schema 10 reads schemas 1–9 without writing on load. First old-schema save preserves immutable workspace.pre-event-history.backup.json, all earlier immutable backups and the previous-save backup. Original events/sets/entries/catalog/export identities and original distribution paths remain. Schema-9 branding opt-outs stay explicit; older branding migration runs only for schemas <=8. Existing covers, vector logos, multiple sets/repeats, member distribution/reference rules, export tracking/revision/replacement and verified cleanup remain.

Permanent local delivery: run scripts/build-local.ps1. It runs delivery-safety tests and the normal Windows tests/package pipeline, then scripts/publish-local.ps1 verifies every archive file by SHA-256, checks the runtime/application inventory, smoke-tests the extracted copy with no developer Java and isolated synthetic data, and publishes a fresh version folder. For this release the direct executable is dist/band.logger-0.10.1-windows-x64/band.logger.exe (no extra band.logger nesting). dist/Launch current build.lnk advances atomically only after success. Previous extracted versions/ZIPs and running older apps are preserved; existing target versions are never overwritten. Incomplete/unsafe archives leave the prior current shortcut/build intact. Failed staging stays available for diagnosis. Program files are separate from %LOCALAPPDATA%/band.logger. GitHub Actions still calls package-windows.ps1 and publishes the normal ZIP; local extraction/shortcuts are not run in CI. Package version is read from app/build.gradle.kts.

Verification and exact final checksum are recorded in WORK_LOG and CURRENT_STATE. Use the latest MANUAL_TESTING checklist. No Git commit/push, production-data edits or unrelated features.


Local extraction recovery: if packaging succeeded but final delivery designation failed, rerun `scripts/publish-local.ps1 -Archive dist/band.logger-VERSION-windows-x64.zip`. If the new version folder already exists from a late-stage failure, add `-VerifyExisting`: it verifies exact inventory/hashes and reruns isolated bundled-runtime diagnostics before updating the current shortcut; it never overwrites existing program files. An existing version remains rejected by default. Failed verification retains the previous working shortcut/build.
