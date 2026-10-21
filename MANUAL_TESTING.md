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

Data is in %LOCALAPPDATA%\band.logger. The previous save is workspace.backup.json; the pre-migration copy is workspace.chunk1.backup.json. Keep backups. Review and approve Chunk 2 before starting Chunk 3.