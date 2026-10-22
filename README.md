# band.logger

**A project-based workspace for musicians and working bands.**

**Project status:** Supplemental distribution publishing delivered (0.6.0); owner confirms successful 0.5.1 event import on iPad. See the latest October 21 update below.
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
