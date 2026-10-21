# band.logger

**A project-based workspace for musicians and working bands.**

**Project status:** Chunk 2 Windows preview built and tested; owner review pending
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
| 2 | Drive integration, imports, and tune catalog | Built and tested; owner review pending |
| 3 | Library master export | Not started |
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

Use `MANUAL_TESTING.md` for review. Local data is under `%LOCALAPPDATA%\band.logger`. See `CURRENT_STATE.md` for limitations and `WORK_LOG.md` for results. Chunk 1 was reviewed and Chunk 2 explicitly authorized. Chunk 2 needs owner review; do not begin Chunk 3 without approval.

### Development builds

Run `scripts/build-local.ps1` to use the local toolchain (or download it if absent), run tests and package the application. The Gradle Wrapper and pinned dependencies provide reproducible build configuration. Internet is required for the first build. `.tools`, build output and `dist` are ignored.
### Chunk 2 library workflow

Open Library → Connect folder and choose your locally synchronized chart folder. Scanning does not modify PDFs. Refresh after adding charts outside the app. Review new or ambiguous entries under Needs review. Import PDF copies one chosen PDF into that folder under the filename you confirm and refuses to overwrite an existing file. Metadata corrections do not rename originals. Different projects keep independent catalog records.

The forScore proof now saves a ZIP. Transfer it through Drive, save locally on iPad, extract in Apple Files, then open the .4ss in forScore. Production library masters remain Chunk 3 work.