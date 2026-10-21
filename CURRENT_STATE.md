# Current state — Chunk 2, October 20, 2026

Chunk 1 was reviewed by the owner. Both synthetic iPad diagnostics passed using ZIP transfer through Drive followed by local extraction. The owner explicitly authorized Chunk 2. Chunk 3 is not authorized.

## Implemented

- All Chunk 1 project/member/theme features remain.
- Per-project chart folder connection and switching, recursive read-only PDF scans, manual Refresh, title/part/filename search and tune grouping.
- Stable tune and chart identities, independent part revisions, SHA-256 content hashes, file metadata, previous-content history, missing-file states and folder history.
- Needs Review for new, malformed, changed, conflicting or invalid-PDF records. Accept recognized new charts in a batch, edit individual catalog metadata, or exclude a catalog record without deleting its PDF.
- Import PDF copies one chosen file under the confirmed filename into the source folder. Existing files cannot be overwritten. Google Drive for desktop performs any cloud synchronization.
- Synthetic forScore export now produces a ZIP containing the .4ss and iPad instructions.
- Schema 1 data migrates to schema 2, preserving projects, members and themes. The first save preserves workspace.chunk1.backup.json as well as the rolling backup.

## Verification

Compilation and all 28 automated tests passed (0 failures, 0 skipped). The Windows ZIP passed bundled and extracted executable smoke tests with development Java removed from PATH. UI checks passed for folder connection, real-library scan, search, individual review/save, repeat refresh and project isolation. Saved data verifies two projects, 249 chart records, 117 tunes and the migration backup. Full owner review remains pending.

249 chart PDFs were copied from the owner-designated sorted folder to the requested Google Drive library. Every source/destination SHA-256 matched and all originals were retained. A private receipt is stored under %LOCALAPPDATA%\band.logger\transfer-receipts. This verifies local copies, not completion of Google Drive cloud synchronization.

## Delivery and limits

Windows preview: dist/band.logger-0.2.0-windows-x64.zip. Extract all files and run band.logger.exe. No development tools are needed. Manual review: MANUAL_TESTING.md.

Manual refresh only; no live filesystem watcher. PDF header checks do not establish full PDF renderability. Symbolic links are skipped. Network or permission errors abort the scan and preserve the saved catalog. Very large or online-only libraries may take time to hydrate and hash. Catalog metadata corrections do not rename source files. Historical records retain checksums/metadata, not backup copies of overwritten source PDF bytes.

No production library-master exports, supplements, direct cloud API, Android app, automatic sync status, SVG rendering, signing or installer. APK compilation is not claimed. Public software license remains undecided.

Do not run the older Chunk 1 binary against schema 2 data; it will reject the newer schema. The preserved Chunk 1 backup supports deliberate rollback. Owner acceptance of Chunk 2 and approval of Chunk 3 are still required.
## Owner machine setup

Quaternity is connected to the requested Drive library. 249 PDF files were indexed into 117 tune groups. One filename ending in Bb-2.pdf is unrecognized and remains for owner review; no files were renamed. One recognized Bass record was accepted during the UI save test; 247 other recognized new records can be batch accepted after review, and the ambiguous file requires an individual decision.

During upgrade verification, the old running preview showed two projects while the main saved file contained an earlier single-project snapshot. The newer rolling backup contained both Marcus - Solo and Quaternity. Preserved both files under the local app data recovery directory, restored the newer backup, and verified both projects in Chunk 2. The cause of the prior discrepancy is not established. The subsequent migration backup and saved catalog were verified.

Final checksum recheck confirmed every desktop original and Drive copy still matched the transfer receipt after scanning/UI checks. Google Drive cloud synchronization remains outside the app's verification.
Final visual restart verification passed. The extracted 0.2.0 preview is open on Library for owner review. Changes remain uncommitted and unpushed.
