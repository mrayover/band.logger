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
