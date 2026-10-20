# Current state — October 20, 2026

Chunk 1 Windows preview is implemented, built and tested. Owner acceptance is pending. Chunk 2 is not authorized.

Implemented: Kotlin Multiplatform domain layer; Compose Windows UI; project creation/editing/switching; optional Quaternity starter with four editable members; project-specific colors; member names, multiple roles and ordered preferences; bundled Lato; versioned JSON persistence with previous-save backup; non-destructive handling of unreadable data; synthetic open-format .4ss generation; automated tests; Windows packaging script and GitHub Actions workflow.

Branding uses an explicit typographic placeholder. Optional logo references are stored but artwork rendering is not implemented. The supplied SVG has not been copied or published.

Android foundation: models, validation, serialization, export XML and storage/source/destination interfaces are in commonMain without Windows or java.* dependencies. No Android UI, APK or SDK target is delivered or claimed tested in Chunk 1. A future Android module will bind these interfaces to app-private storage and Android file/Drive providers. Desktop UI is intentionally isolated.

Known limitations: no Drive scanning, catalog, production chart exports, user accounts or synchronization; no deletion interface; no automatic backup recovery; no app installer/signing/auto-update; no actual iPad import verification. Custom colors can reduce readability. Logo reference is metadata only.

Windows deliverable target: dist/band.logger-0.1.0-windows-x64.zip. Run the executable after extracting the whole archive. Build output and local toolchains are ignored by Git.

Review checklist: MANUAL_TESTING.md. Verified build and test results are recorded in WORK_LOG.md. Do not infer successful checks from implementation descriptions.
## Verified delivery

- Compilation and 9 automated tests passed (0 failures, 0 skipped).
- Windows app image and 61 MB ZIP created successfully.
- Packaged executable smoke test passed, including storage, font resources and synthetic export.
- Extracted ZIP executable passed with JAVA_HOME cleared and a system-only PATH, from a path containing spaces.
- Agent visually verified launch, Quaternity starter form/save, member view, theme view, and persistence after application restart. Quaternity was saved in the local workspace during this check.
- Remote GitHub Actions execution, Android application compilation, full owner checklist and real iPad import have not been performed.

Next step: owner extracts and reviews the Windows ZIP using MANUAL_TESTING.md, reports issues, and approves Chunk 1 before any Chunk 2 work.
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
