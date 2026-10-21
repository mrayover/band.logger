# band.logger — Build Chunks & Work Mode Instructions

**Document version:** 0.1  
**Established:** October 20, 2026  
**Status:** Approved initial build plan  
**First target:** Windows desktop  
**Future target:** Android  
**Initial project:** Quaternity

## 1. Purpose

This document defines the staged development of **band.logger**, a desktop-first application for managing musical projects, chart libraries, members, rehearsals, performances, and eventually recordings and other band operations.

The initial development effort is limited to the project framework and chart-library distribution system.

**Do not implement rehearsal setlists, performance setlists, per-song member assignments, venue management, or performance actuals during these four chunks.**

The application must be architected so these capabilities can be added without replacing the foundational data model.

Every chunk must produce a usable, testable increment of the application.

## 2. Instructions for every Work Mode session

### Repository and documentation

The intended Windows working directory is:

`C:\Users\mrove\Projects\band.logger\band.logger`

The owner manages commits, branches, and publishing through **GitHub Desktop**.

1. Confirm access to the actual repository before making changes. Do not assume the stated Windows path is accessible from a remote execution environment.
2. Read `README.md` and this entire `BUILD_CHUNKS.md` before changing code.
3. Treat the repository as the authoritative source of development progress.
4. Do not recreate an existing project, discard previous implementation, or restart from the original specification.
5. Work on the specifically authorized chunk. Do not automatically begin later chunks.
6. Keep the application buildable after each chunk.
7. Never commit private sheet music PDFs, credentials, personal Google Drive contents, or locally generated databases to the public repository.

As development begins, create and maintain these additional documents:

| File | Purpose |
|---|---|
| `CURRENT_STATE.md` | What currently works, what does not, and the next authorized task |
| `WORK_LOG.md` | Dated history of changes, tests, failures, and decisions |
| `ARCHITECTURE.md` | Technical structure and important integration contracts |
| `DECISIONS.md` | Confirmed decisions, alternatives considered, and unresolved design questions |

At the end of every session, update the relevant documentation. Update `README.md` whenever the project's real capabilities, setup, architecture, or dependencies change.

### Testing requirement

No chunk is complete merely because code was written.

For every chunk:

- Run an automated build appropriate to the available environment.
- Run relevant automated tests.
- Perform a basic functional smoke test of the new functionality.
- Verify that earlier functionality still works.
- Record the exact commands executed and their results in `WORK_LOG.md`.
- Clearly distinguish **passed**, **failed**, **blocked**, and **not yet manually tested**.
- Explain any work that requires verification on the owner's Windows computer, Google Drive installation, or iPad.
- Update `CURRENT_STATE.md` with what has been demonstrated to work.

Never describe an unperformed test as passed.

### Dependency installation

The owner is a designer, not a software engineer. Do not assume that development tools, build systems, SDKs, libraries, or conversion utilities are installed.

The development agent is responsible for:

1. Determining the requirements of the selected toolchain.
2. Downloading and configuring required development dependencies using documented, reputable sources.
3. Adding the Gradle Wrapper and reproducible dependency declarations to the repository.
4. Including required font files and licenses.
5. Creating synthetic, legally distributable test fixtures.
6. Explaining unavoidable user-operated installation or permission steps clearly.

Request permission when required. Do not work around an access restriction or silently claim to have configured the owner's machine from a remote environment.

## 3. Architectural rules established from the beginning

### Project is the top-level object

A Project represents an independent musical group or musical undertaking.

Every project has its own:

- Unique internal ID
- Display name
- Optional short filename prefix
- Branding and theme
- Members and roles
- Chart source location
- Export destination
- Tune catalog
- Chart revision records
- Export history
- Future rehearsals, performances, and related data

**Quaternity is the first Project, not the application's hardcoded identity.**

The architecture must support creating and switching among multiple projects. Project data must never unintentionally cross between projects.

### Shared desktop/Android foundation

Use **Kotlin Multiplatform with Compose Multiplatform** as the initial architectural direction, provided the current supported toolchain passes the foundation build.

Separate:

- Shared project and music-library models
- Shared catalog and export business logic
- Shared validation and XML-generation logic
- Platform-specific file selection and filesystem access
- Future platform-specific Google Drive connectivity
- Platform UI and integration layers where required

Do not put Windows file paths, Windows filesystem operations, or desktop-only dependencies into shared domain logic.

The Windows desktop application is the first deliverable. Android does not require a complete interface during these chunks, but the project should include a viable Android target and demonstrate shared-code compilation where the available build environment permits.

If a technical obstacle requires deviating from this architecture, document the evidence and obtain approval before changing direction.

### Storage ownership

- **Google Drive:** authoritative copies of source chart PDFs.
- **Local application storage:** project settings, indexes, relationships, export manifests, and other application metadata.
- **GitHub:** source code, documentation, safe test fixtures, and permitted branding assets.
- **forScore:** each musician's imported charts, annotations, and setlists.

The application must never assume that a project source folder is itself a Git repository.

### Source integration

For the desktop build, the initial Google Drive integration should use the folder exposed by **Google Drive for desktop**.

The user chooses the folder. No hardcoded drive letters or user directory names.

The application must support:

- Importing PDFs through the application
- Discovering PDFs added directly to the selected Drive folder
- Recognizing newly discovered and changed files
- Reviewing entries requiring attention

The initial implementation does not need Google OAuth or direct Drive API access.

Provide an abstraction for a future Android Google Drive integration. Android will require a different mechanism from the Windows synced-folder approach.

### Safe file handling

Never rename, overwrite, delete, or relocate existing source charts automatically.

If two files claim the same song, part, and revision, but contain different content, identify the conflict for review.

Maintain stable internal IDs independently of filenames.

Record the source location and content checksum of indexed charts.

### Chart revisions

Each part has its own revision sequence.

A tune may simultaneously use:

- C chart `r00`
- Bb chart `r02`
- Bass chart `r01`
- Drums chart `r04`

Do not assume all parts of a tune share one revision number.

### Initial chart naming convention

`Q Song Title -r00- C.pdf`

General format:

`[Prefix] [Title] -r[Revision]- [Part].pdf`

Initially supported part labels include:

- `C`
- `Bb`
- `Eb`
- `Bass`
- `Drums`
- `Guitar`
- `Piano`

Parsing should tolerate capitalization differences where sensible but preserve the chosen canonical filename.

Malformed or ambiguous files must be flagged for review rather than silently dropped or renamed.

### Design system

Use **Lato** throughout the application's interface.

The development agent must obtain appropriate font files, bundle them when required, and include the applicable Open Font License information.

Quaternity's initial design palette:

| Token | Initial value |
|---|---|
| Navy | `#171B24` |
| Ivory | `#F4F2EA` |
| Gold | `#E7C66A` |
| Steel blue | `#7698AD` |
| Raised surface | `#242B36` |

These are initial working values, subject to adjustment when the actual Quaternity SVG branding is provided.

Gold and steel blue should generally serve as accents, highlights, and state indicators rather than overwhelming the interface.

Themes must be project-specific and configurable. Do not scatter hardcoded Quaternity colors through individual screens.

The owner will provide the Quaternity Q SVG. The owner may also provide the full Quaternity logo.

Until then, use a clearly identifiable temporary typographic placeholder. Do not invent replacement brand artwork.

### forScore compatibility

The initial delivery format is **open-format `.4ss` XML containing Base64-encoded PDFs**.

Use the documented forScore XML format, not an assumed proprietary conversion process.

Official specification: https://forscore.co/developers-file-types/

Important constraints:

- Embedded PDF data requires forScore **14.3 or newer**.
- A `.4ss` file generated by this application must be tested on an actual receiving iPad.
- The proprietary format produced when forScore shares an existing setlist is not the same as the documented open XML format.
- Embedded PDF imports can cause filename adjustments or additional library copies when names collide.
- Never promise that an import preserves or transfers a musician's existing annotations without verifying the exact workflow.

Eventually, rehearsal and performance setlists should reference charts already in the user's forScore library wherever possible. That is a later feature.

## 4. Chunk 1 — Application and project foundation

**Objective:** Establish the repository, runnable application, project-based structure, brand system, members, and technical proof of `.4ss` generation.

### Deliverables

1. A functional desktop application that launches successfully.
2. A Kotlin Multiplatform/Compose Multiplatform project structure.
3. An Android target or documented compatible Android foundation.
4. A project selection screen.
5. Create Project and Edit Project functionality.
6. Project-specific theme and branding settings.
7. Member creation and editing, including names and roles.
8. Local persistence of projects, themes, and members.
9. Lato font integration.
10. A technical proof that produces a valid open-format `.4ss` file using synthetic PDF data.
11. Reproducible builds and automated tests.
12. Initial architecture and work-log documentation.
13. Self-contained Windows executable ZIP, automated GitHub Actions packaging, package smoke test and manual review checklist.

### Initial screens

Keep the interface small:

1. Project selector / application opening screen
2. Create or edit project
3. Project home
4. Theme and branding settings
5. Members and roles

The project home may show future feature areas as clearly disabled or unimplemented. Do not add controls that appear functional but perform no action.

### Project information

At minimum, a project can store:

- Project name
- Filename prefix
- Theme selection
- Custom color overrides
- Optional logo reference
- Member records
- Member roles and preferred chart types

For Quaternity, the suggested initial members are:

| Member | Primary role | Initial chart preference |
|---|---|---|
| Marcus | Guitar | Guitar, then C |
| Jordan | Tenor saxophone / EWI | Bb, then C |
| Xavier | Bass | Bass, then C |
| Ethan | Drums | Drums, then C |

These are editable defaults, not permanent restrictions or song-specific assignments.

Support more than one role or preferred chart type per member.

### `.4ss` technical proof

Generate a small test `.4ss` document from synthetic PDFs with no copyrighted music.

The generator must:

- Produce valid UTF-8 XML.
- Follow the official `forScore` root element requirements.
- Embed PDF bytes using Base64 in a `score` element.
- Preserve filenames and ordering.
- Correctly escape special characters.
- Verify that decoding the embedded PDF reproduces the original bytes.

There is no requirement to purchase or install a separate `.4ss` conversion program. The implementation should generate documented XML directly using appropriate standard libraries.

### Automated acceptance tests

- Desktop application compiles.
- Desktop application launches in a supported test environment.
- Shared model and business-logic tests pass.
- Android shared target compiles when the required SDK is available.
- A new project persists after restart.
- A second project can be created.
- Project switching preserves independent project names, themes, and members.
- Member edits persist after restart.
- Theme changes affect the intended project only.
- Lato font resources load.
- Generated `.4ss` XML parses successfully.
- Embedded test PDF bytes survive Base64 encoding and decoding unchanged.
- Special characters in titles do not invalidate XML.

### Manual verification

The owner or agent should be able to:

1. Open the application.
2. Create Quaternity.
3. Select the Quaternity theme.
4. Add the four members.
5. Create a second temporary project.
6. Switch between projects without data mixing.
7. Close and reopen the application without losing data.

Also provide a short procedure for importing the synthetic `.4ss` file into forScore on an iPad running 14.3 or newer.

The iPad test is a separate verification gate. If the agent cannot access an iPad, mark it **awaiting owner verification**.

### Chunk 1 completion gate

Do not declare Chunk 1 complete unless the desktop application builds, the foundation tests pass, and a usable self-contained Windows executable package has been delivered. Full acceptance also requires the owner to have had the opportunity to run and review that package.

The forScore XML generator must pass its automated tests. Record actual iPad import verification separately; unresolved real-device compatibility must remain visible before production chart exports begin.

Update all required documentation and leave the repository ready for a GitHub Desktop commit.

---

## 5. Chunk 2 — Google Drive connection and tune catalog

**Objective:** Connect a project's source-chart folder and build a reliable searchable catalog.

### Deliverables

1. Choose or change a project's source folder.
2. Store the folder association per project.
3. Scan existing PDFs without moving them.
4. Parse the agreed naming convention.
5. Group chart parts and revisions under tune records.
6. Display a searchable, browsable tune library.
7. Detect newly added files.
8. Detect changed files and potential revision conflicts.
9. Import a PDF through the application into the selected source folder.
10. Provide a **Needs Review** interface for unrecognized or conflicting files.
11. Refresh the catalog without duplicating accepted records.

### Required behaviors

- The app discovers files already present when a folder is connected.
- A PDF added directly to the Drive folder appears as newly discovered after refresh or supported automatic rescan.
- A PDF imported through the application is copied into the selected source folder with the owner's confirmed name.
- Imported files retain their original content.
- Existing source files remain untouched.
- The catalog can distinguish a new tune, new part, new revision, unchanged file, and ambiguous entry.
- Source-folder unavailability is reported clearly rather than interpreted as an instruction to delete the catalog.

A virtual **Needs Review** queue is sufficient. Do not require a physical `Inbox` folder.

### Catalog data

Each tune has a stable identity and display title.

Each chart record stores at least:

- Related tune ID
- Part/instrument designation
- Revision
- Filename and relative source path
- File hash
- File size and last observed modification information
- Import/index status
- Time discovered
- Optional notes or resolution status

Retain prior revision records for traceability.

### Automated acceptance tests

- Filename parser accepts valid samples.
- Parser flags malformed filenames.
- Song-title case and punctuation differences are handled according to documented normalization rules.
- Multiple chart parts group under one tune.
- Different revisions of one part are preserved.
- Different revisions across parts are independent.
- Two conflicting files cannot silently replace one another.
- Repeated scans are idempotent.
- File changes are detected.
- Unavailable Drive folders do not erase indexed records.
- Different projects cannot see each other's catalog records without an explicit import.
- Application builds and Chunk 1 functionality still passes.

### Manual verification

Use a small controlled set of PDFs:

1. Add several through the application.
2. Add another directly to the synced Drive folder.
3. Refresh or wait for the supported scan behavior.
4. Verify the new chart is discovered.
5. Add one malformed name.
6. Confirm it appears under Needs Review.
7. Correct it using the approved workflow.
8. Restart the application and verify the catalog persists.

### Chunk 2 completion gate

The application can browse an actual project chart library, recognize new PDFs, and import new source files without destructive changes.

Tests and documentation are updated.

---

## 6. Chunk 3 — Initial library master export

**Objective:** Create forScore-ready initial library packages from the indexed source charts.

### Deliverables

1. Initiate **Create Library Master** for a project.
2. Identify the project's members and chart preferences.
3. Produce one `.4ss` package per member.
4. Include a sensible chart selection based on default role preferences.
5. Display a preview of the charts each member will receive.
6. Identify missing preferred parts or unsupported combinations.
7. Allow confirmation before generating files.
8. Select and remember a project-specific export destination.
9. Write generated files to a named export folder.
10. Store an export manifest recording included tune/part/revision identities.
11. Preserve the source PDFs.

### Chart selection

Use global member role preferences for this chunk.

For example:

- Jordan normally receives Bb charts, with C as an optional fallback..
- Xavier prefers a dedicated Bass chart, with C as an optional fallback.
- Ethan prefers a dedicated Drums chart, with C as an optional fallback.
- Marcus prefers a dedicated Guitar chart, with C as an optional fallback.

These are defaults. Song-by-song member assignments and overrides are intentionally deferred.

No silent fallback is permitted when the result would be ambiguous. Display exceptions for review.

### Export naming

Use consistent, descriptive names such as:

`Q Library Master - Jordan - 2026-10-20.4ss`

The generated folder name should identify the project, export type, and date.

Date formatting must be stable and sortable.

### Automated acceptance tests

- Every generated `.4ss` is valid XML.
- XML names and paths are correctly escaped.
- Each selected PDF is embedded without byte corruption.
- Chart order is deterministic.
- Member-specific preference logic selects expected parts.
- Missing parts produce warnings or documented fallbacks.
- Only the intended project's PDFs are included.
- No source PDF is modified.
- The manifest accurately identifies every exported chart and revision.
- Generating an export cannot silently overwrite an existing export folder.
- Failed or incomplete exports are clearly reported.
- All prior tests continue to pass.

### Manual verification

1. Preview Quaternity's initial master packages.
2. Inspect sample member allocations.
3. Generate the packages.
4. Open the destination folder.
5. Verify four appropriately named `.4ss` files exist.
6. Import at least one generated package into forScore on an iPad.
7. Confirm its charts appear and open correctly.

### Chunk 3 completion gate

Master exports must work from actual indexed PDFs.

**An actual successful forScore import is required before considering the production export workflow fully verified.**

If device verification is unavailable, report the completed automated work and the outstanding compatibility gate separately.

---

## 7. Chunk 4 — Supplements, export history, and receipts

**Objective:** Make subsequent library additions easy to distribute without rebuilding the original master.

### Deliverables

1. Track what each member has previously received.
2. Identify newly added and newly revised charts.
3. Generate the next numbered supplemental library export.
4. Include only eligible new or revised part/revision entries.
5. Produce one supplemental `.4ss` per affected member.
6. Keep historical master and supplement packages unchanged.
7. Record successful export manifests and package numbers.
8. Provide an export receipt screen.
9. Allow review of previous library exports.
10. Prevent accidental duplicate supplements.

### Export naming

Example:

`Q Library Supplement 001 - Jordan - 2026-10-20.4ss`

Supplement numbers must be stable within a project.

Rebuilding an identical supplement must not automatically create another publication or advance the sequence.

A revision to only the Bass chart of a tune must not force revised Bb, C, or Drum charts into that supplement.

### Receipt information

The receipt screen should show:

- Project name
- Export type
- Supplement number, if applicable
- Date/time generated
- Destination folder
- Member packages generated
- Newly included tunes
- New parts and revisions
- Skipped unchanged charts
- Warnings or omissions
- Success/failure for each file
- A way to locate the output folder

The receipt represents the application export, **not confirmation that each musician imported the package**.

### Automated acceptance tests

- First successful master publication establishes the initial distribution baseline.
- Unchanged charts are not included in later supplements.
- A genuinely new tune is included.
- An added part for an existing tune is included only where relevant.
- A new revision is included without modifying old revisions.
- Per-member export manifests remain independent.
- A failed export does not mark charts as successfully distributed.
- A repeated export attempt does not silently duplicate a published supplement.
- Supplement numbering survives application restart.
- A member newly added to a project does not receive a misleading empty historical delta; the workflow explains that they may need a fresh baseline.
- Receipt details match the actual generated files.
- Prior functionality and tests continue to pass.

### Manual verification

1. Generate and record a master export.
2. Add one new tune.
3. Add one updated Bass chart.
4. Leave the other charts unchanged.
5. Create Supplement 001.
6. Confirm it includes only the appropriate new entries per member.
7. Confirm the original master is unchanged.
8. Read the receipt.
9. Restart the application and verify the export history persists.
10. Import a sample supplement into forScore and inspect its resulting library entries.

### Chunk 4 completion gate

Library supplement generation, revision filtering, persistent export history, and receipts work reliably.

This completes the initial library-distribution milestone.

---

## 8. Required work handoff at the end of each chunk

Every Work Mode completion message must include:

1. **What works now**
2. **Files and screens created or changed**
3. **Dependencies installed or configured**
4. **Automated tests executed and results**
5. **Manual verification completed**
6. **Manual verification still required from the owner**
7. **Known issues and limitations**
8. **Documentation updated**
9. **Next recommended chunk**
10. **GitHub Desktop instructions**, including whether the working tree is ready for review and commit

Do not create commits or push to GitHub unless specifically authorized. The owner prefers to manage this through GitHub Desktop.

## 9. Deferred capabilities

The following are architectural considerations but not deliverables within Chunks 1–4:

- Rehearsal setlist builder
- Performance setlist builder
- Multiple numbered sets for one performance
- Venue database
- Assigning a specific chart version to each member for each tune
- First-page printable setlist summaries
- Historical setlists
- Planned performance versus actual performance records
- Rehearsal and performance frequency tracking
- Rehearsal notes
- Rehearsal audio recordings
- Content scheduling
- Android production interface
- Direct Android Google Drive API integration
- Multiuser collaboration or cloud backend

Future setlist imports should investigate reference-only `.4ss` entries so existing forScore library files and annotations can be reused without unnecessarily importing additional PDFs.

## 10. Current open decisions

These questions should remain editable and should not prevent Chunk 1 development:

- Final Quaternity palette tuning after receiving the SVG assets
- Final branded application icon
- Precise project theme customization interface
- Final member chart preferences and fallbacks
- User-selected Google Drive source and export directories
- Final public repository license
- Exact workflow for migrating annotations when individual charts are revised
- Long-term Android Google Drive authentication and synchronization

Decisions that materially change the data model or user workflow must be brought to the owner before implementation.

## 11. Authoritative technical references

- forScore open setlist specification: https://forscore.co/developers-file-types/
- Kotlin Multiplatform documentation: https://kotlinlang.org/docs/multiplatform/home.html
- Compose Multiplatform documentation: https://www.jetbrains.com/lp/compose-multiplatform/
- Google Fonts Lato family and licensing: https://fonts.google.com/specimen/Lato
- GitHub Desktop: https://desktop.github.com/

**This document must remain in the Git repository and must be amended when scope, chunk acceptance criteria, or technical decisions change. Do not replace its history with unexplained new plans.**
## Permanent Windows delivery and acceptance requirement

Every completed development chunk must produce a usable, downloadable Windows desktop application. Build and supply an `.exe` with all required runtime components and dependencies, preferably inside a self-contained ZIP that launches without a development environment. Establish automated Windows packaging in GitHub Actions in Chunk 1 and deliver an updated package after every subsequent chunk. Test compilation, packaging, and functionality; preserve all previously completed functionality; include a simple manual testing checklist with every delivery. A chunk is not fully accepted until the owner has had the opportunity to run and review its Windows application. Do not begin the next chunk without owner approval. Local build packages may be delivered before publication; GitHub Actions artifacts require the owner to commit and push through GitHub Desktop.

The actual local repository is `C:\Users\mrove\Projects\band.logger\band.logger`. Use this existing checkout; do not recreate it.

## October 20, 2026 — Chunk 2 authorization and delivery clarification

The owner reviewed Chunk 1, verified both forScore diagnostics via ZIP/local extraction and authorized Chunk 2. Default forScore transfer packaging is now ZIP with the original .4ss inside. Include this convenience fix in Chunk 2. Do not start Chunk 3 without the next owner approval.

For Chunk 2 use the existing Google Drive for desktop folder integration, not OAuth or a custom uploader. The owner requested copying charts into their chosen Drive library and explicitly clarified COPY ONLY: retain all originals in sorted. Keep private files outside Git. Every chunk still requires a self-contained Windows application and owner review.

## October 20, 2026 — Chunk 2 accepted; Chunk 3 planning

The owner explicitly approved Chunk 2 and requested updated progress documentation plus a plan and estimate before the next implementation phase. This supersedes earlier statements that Chunk 2 owner review is pending. Chunk 3 has not started. CHUNK_3_PLAN.md divides its existing scope into selection/preview, generation/manifests, and verification/handoff, estimated at 2 hours 15 minutes to 3 hours 45 minutes plus owner iPad review. Each stage provides a runnable Windows preview and a documented handoff; the full Chunk 3 acceptance criteria above remain in force. Account quota cannot be translated into a guaranteed number of development hours. No Chunk 4 work is authorized.

## October 20, 2026 — Stage 3A implementation checkpoint

Owner authorized 3A only. Member selection/preview, exceptions, file issues and saved export destination are implemented in 0.3.0. All 42 tests and compilation passed. The candidate Windows ZIP passed 207-file integrity verification. Windows Smart App Control blocked the unsigned executable during the package launch check; development UI verification does not replace this gate. Stage 3A is not fully accepted. Resolve trusted signing/distribution and rerun the packaged checks before acceptance. Production ZIP exports/manifests (3B), final production/device validation (3C), and Chunk 4 remain unimplemented.

## October 20, 2026 — Stage 3B Windows delivery

Owner authorized 3B and reported disabling Smart App Control. The prior executable blocker is resolved. Version 0.3.1 implements confirmed member ZIP generation, manifests, source validation and recovery. All 51 tests, packaged/extracted executable smoke tests and a real-chart packaged UI export/restart check passed. Owner Windows review and actual production forScore import remain pending. Full Chunk 3 acceptance and later chunks are not implied. Preserve the user's requested usage reserve; finalization began at 9% remaining.

## October 21, 2026 — Incremental homepage improvement / 0.3.3

Owner authorized a homepage redesign and Windows delivery, without expanding the established roadmap. Four equal themed cards replace Next chapters; three future event actions show Coming soon, and Pending Distribution has a dedicated explanatory destination linking to Library master. No event storage, editor, supplement generation or cover-page work was started. Existing selected-project accent highlight (baseline commit 40bbc29, New Color On Projects) is preserved and now documented.

Owner reports the actual Library Master imported into forScore on iPad and tunes were present, superseding the earlier pending production-import report. This does not imply additional device checks or blanket acceptance of remaining checklist items. Roadmap numbering and historical entries are unchanged. Version 0.3.3 avoids an existing local 0.3.2 archive; see WORK_LOG.md for packaging results and archive collision recovery.
