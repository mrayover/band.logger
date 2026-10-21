# Current state — Pending Distribution, October 21, 2026 / 0.3.4

This supersedes the placeholder status below; all previous entries are retained as history.

Functional read-only Pending Distribution compares approved catalog selections against each member's latest valid non-empty recorded master. Includes New Tunes/New Parts/Revised Charts, explicit initial-master requirements, unchanged toggle, search, tune groups, expanded chart details, baseline inspection and attention/file warnings. Source bytes are checked on entry/recheck; unapproved/conflicting/missing/changed selections are blocked. Library navigation opens the existing Library screen; use Refresh and Needs review there. Counts are member–tune selections, not distinct tunes. Export inclusion does not establish receipt/import.

PASS: compile; 67 tests (51 existing + 16 new), zero failures/errors; packaged and extracted smoke tests including pending unchanged/new-tune checks, without developer Java; packaged synthetic UI counts (4 new, 1 new part, 3 revisions), initial-member warning, search, expansion, source-change blocking and Library navigation. Final ZIP/extraction integrity verified; live and synthetic workspaces unchanged. Full owner real-library review and new iPad tests were not performed. No schema/dependency changes or original PDFs/export modifications. Prior builds retained. No commit/push or remote Actions run.

ZIP: dist/band.logger-0.3.4-windows-x64.zip.
EXE: dist/band.logger-0.3.4-windows-x64/band.logger/band.logger.exe.
SHA-256: DF504F91D036B8786F5E54FF84DEAA8C499613C2D867DAC82EFC735D91E9FF9E.
Extract all files; runtime is bundled. Unsigned Windows x64 preview.

Optional Supplemental Export Preview deferred. No supplement packages/numbering/history, event editors or receipt confirmation. Source checks are point-in-time; historical ZIP files are not revalidated by this view. Baseline/classification rules are in ARCHITECTURE.md. Next: owner review with MANUAL_TESTING.md, then the read-only supplement preview. Usage reached 13% remaining after packaged verification; finalization only thereafter.

---

# Current state — Homepage increment, October 21, 2026

Version 0.3.3 is the current tested Windows delivery. Earlier Stage 3B status is retained below as historical context; this section supersedes its pending production-iPad-import and delivery-version statements.

- Home: four equal clickable cards, two columns on desktop / one below 500 dp content width, bundled Lato, primary accents for rehearsal/gig and secondary accents for events/distribution, visible hover/focus states.
- Create Rehearsal, Create Gig and Upcoming Events: explicit Coming soon dialogs only. No event records/editors.
- Pending Distribution: working dedicated navigation destination with an accurate explanation that queue tracking/supplements are not implemented; links to Library master and Home, and a sidebar entry. No fabricated records/counts.
- Project header, editing, sidebar and the owner's existing accent-filled selected-project highlight (commit 40bbc29) retained. Existing members, themes, library imports/scanning/review, masters, .4ss/ZIP exports, manifests and recovery code retained. No schema or dependency change.
- Owner-reported verification: the actual Library Master successfully imported into forScore on their iPad and the tunes were present. No additional device testing, annotation transfer or remaining Stage 3C acceptance is inferred.

## Current delivery and verification

- ZIP: dist/band.logger-0.3.3-windows-x64.zip (61,516,722 bytes).
- EXE: dist/band.logger-0.3.3-windows-x64/band.logger/band.logger.exe.
- SHA-256: 3878A41632466D7002088011D26E39659306D11C61A5204B1478A393FFB611F8; also dist/SHA256-0.3.3.txt.
- PASS: compilation and all 51 existing automated tests (zero failures/errors/skips).
- PASS: standard Windows packaging, bundled smoke test, extracted smoke test in a path containing spaces with JAVA_HOME empty/system-only PATH; storage/fonts/XML/ZIP/scan/preview/master generation/recovery.
- PASS: delivered ZIP versus extracted files SHA-256 equality (209 archive entries).
- PASS: packaged UI four-card layout, three Coming soon dialogs, dismissal, keyboard Tab/Enter and focus border, Pending Distribution card and Library master link, project switching/highlight, narrow single-column layout and scrolling. Tested using an isolated workspace copy; the live workspace remained byte-identical. Selection change persisted in the test copy.
- Not rerun manually this session: complete import/review/export/recovery UI flows, arbitrary custom palettes, full restart UI inspection or iPad tests. Existing automated regressions cover the data/export operations.

Extract the whole ZIP and keep app/ and runtime/ beside the executable. Close older app windows first. No installed Java/Gradle is needed. Unsigned Windows x64 preview; GitHub Actions is preserved but no remote build, commit or push was performed.

Archive note: an existing locally labeled 0.3.2 ZIP was inadvertently replaced during the first packaging pass. Its intact extracted contents were recompressed into that ZIP; original archive bytes are not retained. Its runnable extracted app and earlier 0.3.1/0.3.0/0.2.0/0.1.0 archives remain. The new package uses 0.3.3 and packaging now refuses existing version ZIPs. See WORK_LOG.md.

Next: owner review of 0.3.3 with MANUAL_TESTING.md, then finish any outstanding Stage 3C checks before separately authorizing supplements. Event database/editors, setlists, supplements and PDF cover pages remain deferred.

---

# Current state — Stage 3B, October 20, 2026

Stage 3B is implemented and packaged as 0.3.1. Owner review and actual production forScore import remain pending. Chunks 1 and 2 are accepted. The owner authorized 3B, reported disabling Smart App Control themselves, and requested delivery before usage exhaustion. No Windows security settings were changed by the agent. The prior executable launch blocker is resolved: packaged and extracted launch checks passed.

## What works

- Existing projects, members, themes, library scanning/import/review and 3A allocation previews.
- Explicit confirmation of member selections, fallback/omission counts and zero-chart members.
- One ZIP per member with selected charts, containing a named .4ss and iPad instructions. Members with zero selected charts receive no ZIP and are explicitly listed as skipped.
- Safe new master folders with manifest.json and persistent project master records. Source paths and SHA-256 content are checked before embedding and again before publishing. Previous exports and source PDFs are preserved.
- Streaming XML per PDF; files are completed in a staging directory before publication.
- Last saved master and Open master folder; recovery of completed output if local metadata saving fails. Recovery validates project/catalog identities and ZIP checksums and does not duplicate existing records.
- Schema 3 adds master records. Schema 1/2 migrate; schema-2 data is preserved in workspace.pre-masters.backup.json before the first save. Older binaries reject schema 3.

## Verified delivery

Windows ZIP: dist/band.logger-0.3.1-windows-x64.zip (61,492,901 bytes).
Extracted executable: dist/band.logger-0.3.1-windows-x64/band.logger/band.logger.exe.
SHA-256: 4364A206A5F6DEB04537753C5C1AEE5A5363479D41CDA8A264AE6247A002278C.
Extract the whole ZIP; keep app/ and runtime/ beside the executable. Close older band.logger windows first. No Java or development environment is needed.

PASS: 51 automated tests, zero failures/errors/skips; compilation; Windows packaging; bundled and extracted executable smoke tests with developer Java removed from PATH. Smoke tests include master generation, manifest persistence and recovery.

PASS: packaged UI confirmation, real-chart generation and restart. Used a private copy of the owner's workspace, preserving all review decisions. Only the previously accepted Bass chart was eligible; generated one Xavier ZIP, verified its decoded PDF against the original SHA-256 and manifest, and reopened the app to verify Last saved master. Live workspace remained byte-identical. The test export is under %LOCALAPPDATA%\band.logger\verification\stage3b-20261020-195541, outside Git. No actual Drive upload was performed.

The real library still has 248 entries needing review. Inspect/accept recognized entries in Library before expecting complete member packages. Application approval does not approve chart records.

## Remaining limits and next step

Actual iPad import of a production package is awaiting owner verification. Follow MANUAL_TESTING.md: transfer ZIP through Drive, save locally on iPad, extract in Files, then share .4ss to forScore. Filename collisions on import may create copies; annotation transfer is not promised.

Limits: 64 MiB per PDF, 512 MiB selected PDF data per member. Folder/file timestamps use UTC. No supplements, full history browser, per-song assignments, Android app or direct cloud uploader. Interrupted exports may leave an incomplete staging folder; do not distribute it. Completed folders can be recovered after app-record failure.

All required progress documents are updated. Changes are uncommitted and unpushed from baseline 798aa54. Remote Actions for 3B has not run. At the final usage checkpoint, 9% remained; feature work stopped and delivery was finalized. Next: owner Windows/iPad review, then the remaining 3C verification when authorized. Do not start Chunk 4 automatically.
