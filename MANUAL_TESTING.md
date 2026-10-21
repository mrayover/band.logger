# Stage 3A Windows review checklist

**Release verification blocked:** Windows Smart App Control rejected the unsigned 0.3.0 executable on this computer. Compilation and 42 automated tests passed; UI verification used the normal Gradle development task with isolated data. The ZIP contains the executable and runtime, but bundled/extracted executable launch has not passed. Do not treat this stage as fully accepted until its Windows application can be launched and reviewed. No security settings were changed. See CURRENT_STATE.md for the signing blocker.

Extract the entire dist/band.logger-0.3.0-windows-x64.zip. Open band.logger/band.logger.exe with its app/ and runtime/ folders beside it. No development tools are needed. This is an unsigned Windows x64 preview.

Stage 3A provides allocations and destination settings only. Production member ZIP generation and saved master manifests are Stage 3B.

- [ ] Open Library master, then Create Library Master · Preview.
- [ ] Select each member. Check preference order, selected count, fallback count, missing parts and items needing attention.
- [ ] Inspect a selected chart's filename, part and revision. An accepted highest revision should be selected for the first available preferred part.
- [ ] Inspect fallback entries: the preview must name the missing preferred part and the selected fallback.
- [ ] Check File issues. Unreviewed, excluded, missing or ambiguous records must remain visible and cannot be silently accepted.
- [ ] Search a tune or filename; try Show fallbacks and omissions only.
- [ ] Choose an existing export destination. Restart and verify the setting remains with this project.
- [ ] Switch projects and confirm that members, allocations and export destinations stay independent.
- [ ] Verify the screen clearly says preview only and does not create .4ss packages or a master record.
- [ ] Recheck Library search/Refresh and existing project/member/theme features.

For a controlled test folder: accept a C chart, then add a newer unreviewed C revision and refresh. Preview must omit that tune with a review explanation, rather than silently choosing the older C chart or another fallback.

The preview uses the saved catalog; refresh Library after changing files. Current catalog review decisions are preserved. Stage 3B will recheck actual PDF contents before export. Choosing a destination checks current accessibility but does not guarantee future write access.

The following accepted Chunk 2 checklist remains available for regression testing.

# Chunk 2 Windows review checklist

Extract the entire band.logger-0.2.0-windows-x64.zip. Open the band.logger folder and double-click band.logger.exe. Keep app/ and runtime/ beside it. No Java, Gradle or development environment is needed. This unsigned preview may show a Windows publisher warning.

## Existing data

- [ ] Existing Quaternity members, colors and project details appear unchanged.
- [ ] Create or switch to a second project; verify it has its own folder and library.
- [ ] Edit a member and a color; restart and verify they persist.

## Library and review

- [ ] Open Library and connect the intended Google Drive for desktop chart folder. Confirm the folder before scanning.
- [ ] Browse tune groups and search by title, filename and part (e.g. Bb).
- [ ] Open Needs review, inspect recognized charts and accept them when satisfied.
- [ ] Review malformed filenames by assigning the intended tune title, part and revision. Confirm the original filenames stay unchanged.
- [ ] Add a synthetic PDF outside the app, press Refresh and verify discovery.
- [ ] Import a synthetic PDF through Import PDF. Confirm the destination filename, verify the copy appears and the original remains.
- [ ] Try importing under an existing filename; verify it is rejected without overwriting anything.
- [ ] Refresh twice; verify no duplicate records appear.
- [ ] Restart; verify the folder, catalog and review decisions persist.

## Safety checks in a temporary test folder only

- [ ] Put different PDFs with the same tune/part/revision in two subfolders. Confirm Revision conflict appears and bulk acceptance does not accept them.
- [ ] Change a test PDF's contents. Confirm old metadata/checksum remains in History and the new content needs review.
- [ ] Temporarily make the test source folder unavailable. Refresh should report failure and preserve the catalog.
- [ ] Exclude a test record; confirm the PDF stays on disk. Restore it from History if needed.

## forScore delivery

- [ ] Under Technical proof, save the forScore test ZIP.
- [ ] Transfer ZIP through Drive, save it locally on iPad, extract in Apple Files and share the .4ss to forScore.
- [ ] Open Setlists and verify the proof's two PDF entries. Prior single-entry diagnostics already passed on the owner's iPad; the updated ZIP button needs owner review.

Google Drive handles syncing after a copy; band.logger cannot confirm cloud upload completion. No library masters or supplemental packages are generated in Chunk 2.

Data is in %LOCALAPPDATA%\band.logger. The previous save is workspace.backup.json; the pre-migration copy is workspace.chunk1.backup.json. Keep backups. Owner approved Chunk 2 on October 20, 2026. This checklist is retained for regression testing; approval does not establish that every individual checkbox was performed. Chunk 3 planning is in CHUNK_3_PLAN.md.
