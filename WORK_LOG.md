# Work log

## October 20, 2026 — Chunk 1

- Accessed the actual existing repository; initial git status was clean. It contained README.md, BUILD_CHUNKS.md and .gitattributes.
- Read README.md and BUILD_CHUNKS.md completely before implementation. Both already contained the correct nested local path.
- Appended the permanent Windows package and owner review requirement to both documents before creating application code.
- Downloaded Eclipse Temurin JDK 21 and Gradle 8.14.3 locally under ignored .tools; no system Java configuration changed.
- Added Gradle Wrapper, Kotlin/Compose application, shared domain/persistence contracts, Windows storage, Lato and OFL, synthetic PDF/XML proof and acceptance tests.
- Added Windows ZIP packaging and GitHub Actions artifact workflow. No commit or push performed.
- Added CURRENT_STATE.md, ARCHITECTURE.md, DECISIONS.md, MANUAL_TESTING.md and third-party notices.

### Verification

- `git -C C:\Users\mrove\Projects\band.logger\band.logger status --short`: passed under authorized repository access; initially clean.
- `.tools\gradle-8.14.3\bin\gradle.bat wrapper --gradle-version 8.14.3 --distribution-type bin`: passed.
- `gradlew.bat :app:desktopTest :app:createDistributable`: passed; final build completed in 7 seconds with all 9 tests passing.
- Packaged executable and ZIP: passed, including extracted-archive test without developer Java.
- Windows UI smoke test: passed for launch, starter project save, member/theme views and persistence after restart.
- GitHub Actions remote execution: not run; owner has not published the changes.
- Android SDK build: not run; documented compatible foundation only.
- forScore iPad import: awaiting owner verification.

Chunk 1 is not yet accepted. Chunk 2 must not begin until explicit owner approval.
### Final checks and delivery

- `scripts/package-windows.ps1 -SkipBuild`: passed initial packaged-executable diagnostic and ZIP creation.
- `scripts/build-local.ps1`: passed after concurrency protection and theme-surface refinement; invokes `gradlew.bat :app:desktopTest :app:createDistributable`, image smoke test, ZIP creation and extracted ZIP smoke test.
- DomainTest: 4 passed. DesktopTest: 5 passed. Tests cover project isolation, selection, validation, schema handling, editable defaults, member persistence, on-disk restart/backup, corrupt-data preservation, XML structure/order/escaping/Base64, bundled font loading and unsafe XML/path rejection.
- Fresh ZIP extraction into a directory containing spaces: executable diagnostic exited 0 with JAVA_HOME empty and PATH restricted to Windows system directories. Completion marker, synthetic .4ss and persisted diagnostic data verified.
- UI inspection used the built Windows executable. Created Quaternity via its starter form, inspected members and theme, closed/reopened, and confirmed saved project/four-member state. Full owner manual checklist remains pending.
- A checksum-download helper initially failed because PowerShell returned bytes instead of text; corrected decoding and added Gradle distribution checksum successfully. No application build/test failure resulted.
- `git diff --check` initially detected trailing spaces on the updated README status; removed them and reran the check.
- Windows ZIP: dist/band.logger-0.1.0-windows-x64.zip. SHA-256: dist/SHA256.txt. Runtime and dependencies included. Git ignores local tools, build products, archive and runtime data.
- GitHub Actions workflow is configured but not remotely executed: no commit or push is authorized. Owner can review changes in GitHub Desktop, commit and push when ready; then download the successful Windows application workflow artifact from GitHub Actions.
- Updated README.md, BUILD_CHUNKS.md, CURRENT_STATE.md, WORK_LOG.md, ARCHITECTURE.md and DECISIONS.md. Added MANUAL_TESTING.md and THIRD_PARTY_NOTICES.md.
## Owner review — forScore import issue

Owner reports that the Windows features work, but opening the exported .4ss on iPad launches forScore at the previous score without an apparent new setlist. Real-device import has therefore NOT passed. Chunk 1 acceptance remains pending; Chunk 2 is not authorized.

Rechecked the generator and local packaged proof against https://forscore.co/developers-file-types/: required root attributes, score path and Base64 data match the documented structure. No root cause or fix has been established. Need the forScore version, import route and, if necessary, the exact file transferred to the iPad.

Diagnostic files in ignored dist/forscore-diagnostics: BL-Test-A.4ss contains one placeholder and no PDF; BL-Test-B.4ss contains one synthetic PDF with ASCII-only names. Both parse as XML and the second preserves the original Base64 payload. These are diagnostic alternatives, not a confirmed application fix. The Windows executable is unchanged.

### Drive transfer follow-up

Owner confirms forScore newer than version 15 and transfer through Google Drive on iPad (download, then open with forScore). forScore returns to its previous score; no imported score or setlist appears. Windows screenshot shows correct .4ss filenames for the original proof and both diagnostics. A reported XML type label is consistent with the documented open XML format; it does not establish invalid content.

Prepared dist/band.logger-iPad-import-test.zip with unchanged diagnostic A and B. Read both entries back from the archive, verified exact text equality and XML parsing. Next device check: save ZIP into Apple Files / On My iPad, extract locally, then share the extracted A file to forScore. This isolates Drive's individual-file handoff; Drive type handling is a hypothesis, not a confirmed cause. No application code changed and no import fix is claimed. Chunk 1 remains pending.

## Verified iPad import — October 20, 2026

Owner confirmed that BOTH diagnostic files imported successfully after transfer as a ZIP through Google Drive, saving locally on the iPad, extracting in Apple Files, and opening the extracted .4ss in forScore. Test A verified setlist/placeholder creation. Test B verified a setlist with an embedded synthetic PDF. The synthetic open-format import gate has passed for these diagnostic fixtures on the owner's iPad. Production chart export and the original two-entry special-character proof are not independently established by these results.

Use ZIP as the default transfer packaging for future band.logger exports: ZIP → Google Drive → save locally on iPad → extract → share extracted .4ss to forScore. The ZIP contains the actual .4ss; it is a transport wrapper, not a different forScore format. Direct Drive handoff failed in the owner's tests; file corruption or conversion by Drive has not been proven.

This supersedes earlier notes that diagnostic iPad import was unresolved. No application code or executable changed in this documentation update. The current Chunk 1 export button still saves a bare .4ss; automatic ZIP export remains to be implemented. Chunk 2 remains unauthorized pending explicit owner approval.

## Chunk 2 — October 20, 2026

- Checked actual repository; clean main at 7188efb (Update Chunk 1), tracking origin/main. Read README and BUILD_CHUNKS before implementation. No AGENTS.md found in checkout.
- Owner explicitly authorized Chunk 2 and requested updates and tidy files. Preserved no-commit/no-push preference.
- Added shared catalog/parser/reconciliation, safe desktop scanning/import, Library/Needs Review/History, schema migration backup and default proof ZIP packaging.
- Owner designated source/destination chart folders, then clarified COPY ONLY. Copied 249 PDFs (79,363,295 bytes) to the designated Drive library, verified source and destination SHA-256, retained all originals. No existing destination content overwritten. Receipt kept under local application transfer-receipts, outside Git. Local verification does not confirm cloud sync completion.
- `gradlew.bat :app:desktopTest`: passed after UI integration; final current test run 28 tests, 0 failures, 0 skipped (CatalogTest 11, LocalChartsTest 8, existing tests 9).
- Packaging, real-library scan and UI review are in progress; final results follow below.

### Chunk 2 final verification

- `scripts/build-local.ps1`: passed after the folder-picker improvement; 28 tests, zero failures/skips; Compose application image and version 0.2.0 ZIP built. Packaged smoke tests cover storage, bundled fonts, XML, ZIP and chart scanning. ZIP extraction/launch passed with JAVA_HOME empty, system-only PATH and spaces in the extraction path.
- Packaged `band.logger.exe --scan-source <owner-selected Drive folder>`: passed, 249 PDFs, 117 tunes, one unrecognized name, zero invalid PDF headers and zero revision conflicts. Audit outputs remained in ignored local tooling storage.
- GitHub Actions API read: published Chunk 1 commit 7188efbdd4ad9dd356f46c963b58d29c6149ca88 Windows application run completed successfully: https://github.com/mrayover/band.logger/actions/runs/39484440944. Chunk 2 remote workflow has not run because these changes are uncommitted/unpushed.
- UI smoke tests: extracted 0.2.0 executable launches; folder confirmation and real scan work; search finds the requested tune; review dialog saves an unchanged recognized Bass classification; repeat Refresh retains 249 records and review state; switching to Marcus - Solo shows no inherited library.
- Improved folder selection after observing the unowned native chooser could fall behind the main window. Added a path entry field and supplied an owner for Browse dialogs. Rebuilt and reran all tests/package checks.
- Recovery: old running app showed Marcus - Solo and Quaternity while workspace.json was an earlier one-project snapshot. workspace.backup.json contained the newer two-project setup. Preserved both originals in the local app-data recovery directory before restoring the newer backup. Verified two projects, schema 2 and workspace.chunk1.backup.json after the app's first catalog save. Root cause of the pre-existing discrepancy is unknown; no silent data-loss claim is made.
- A preview replacement initially encountered a process-held JAR. Waited for the exact preview process to exit, replaced only that generated version directory, and verified the new application. Saved workspaces and charts were untouched by preview cleanup.
- Final independent readback: schema 2, two projects, 249 Quaternity chart records, 117 tunes, one accepted sample, zero Solo charts, immutable migration backup present. Rehashed all 249 desktop originals and Drive destinations after UI tests: every checksum matched the private receipt.
- Current app still requires owner acceptance. No production export or Chunk 3 implementation. No commit or push performed.

- Final visual restart check passed: both projects, the selected Quaternity project, connected Drive folder, 249 chart records and 248 remaining review entries persisted. Left the extracted Chunk 2 preview open on Library for owner review.
- Cleanup completed: seven disposable build smoke/extraction directories and redundant downloaded toolchain archives removed; extracted toolchains, test reports, both Windows release ZIPs, originals in sorted and all workspace recovery/migration backups retained. Git diff whitespace check passed. No charts or local workspace data are staged/tracked.
