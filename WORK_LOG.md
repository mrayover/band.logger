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
