# Chunk 1 review checklist

Extract the entire band.logger-0.1.0-windows-x64.zip. Open its band.logger folder and double-click band.logger.exe. Keep app/ and runtime/ beside the executable. No Java, Gradle, Android SDK or development environment is needed. This preview is unsigned; Windows may display a publisher warning.

- [ ] Application opens with legible Lato typography and no missing text.
- [ ] Start with Quaternity, review the suggested four members, then Save.
- [ ] Edit the project name and prefix; confirm both persist after closing and reopening.
- [ ] Create a second project and switch between projects.
- [ ] Add a member; edit multiple roles and ordered chart preferences.
- [ ] Change one project's colors; verify the other project's colors and members stay unchanged.
- [ ] Enter an invalid color or empty name; confirm a useful error appears.
- [ ] Cancel an edit; confirm the saved value is unchanged.
- [ ] Close and reopen; verify both projects, members, colors and selection persist.
- [ ] Save synthetic .4ss under Technical proof. Canceling the save dialog should be harmless.
- [ ] Attempt the same export filename again; confirm the existing file is preserved.
- [ ] On an iPad with forScore 14.3+, share the proof file to forScore. Confirm two ordered entries open with synthetic test text. Record any duplicate-name behavior.

Data lives in %LOCALAPPDATA%\band.logger\workspace.json, with the previous save in workspace.backup.json. Back up this folder before experimenting. The app does not scan or modify chart folders in Chunk 1.

Record your review and issues before approving Chunk 1. Chunk 2 requires explicit approval. iPad compatibility is awaiting owner verification.
### If the iPad opens forScore but no setlist appears

Record the forScore version and exact downloaded filename. Embedded PDFs require 14.3+. In Files, long-press the downloaded .4ss, choose Share, then forScore. Look in the Setlists menu for band.logger & forScore — technical proof. Another officially documented route is dragging the .4ss from Files directly into forScore's Setlists menu.

Diagnostic A (BL-Test-A.4ss) should create BL Test A - Setlist Only with one placeholder. Diagnostic B (BL-Test-B.4ss) should create BL Test B - Embedded PDF with one readable synthetic PDF. If A works but B fails, investigate embedded PDF handling; if neither works, investigate file transfer/import handling or XML recognition. Do not treat either hypothesis as established without device results.

### Verified transfer route (owner tested October 20, 2026)

Both diagnostic A and B passed on the owner's iPad using ZIP → Drive → save locally → extract in Apple Files → share extracted .4ss to forScore. Use this route for subsequent device testing. Direct opening from Drive failed; this does not prove that Drive changed the file contents. The current app produces bare .4ss files, so ZIP wrapping is currently a separate step.
