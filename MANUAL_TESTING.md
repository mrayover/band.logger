# Stage 3B Windows review checklist

Extract the entire band.logger-0.3.1-windows-x64.zip, close older band.logger windows, then run band.logger/band.logger.exe. Keep app/ and runtime/ beside it. No development tools or Java installation are required. The application is unsigned.

The owner disabled Smart App Control; the previously blocked Stage 3A packaged and extracted executables subsequently passed launch checks. Stage 3B includes production member ZIP generation; actual forScore import still needs owner verification.

## Review and export

- [ ] Open Library → Needs review. Inspect and accept recognized charts you want to distribute. Resolve ambiguous entries individually; originals remain unchanged.
- [ ] Open Library master → Create Library Master · Preview. Check every member's chart selection, fallbacks and omissions.
- [ ] Choose an export destination outside the public code repository, preferably your Drive export area.
- [ ] Click Review and generate member ZIPs. Check member counts and the acknowledgement before confirming. Members with no selected charts receive no ZIP.
- [ ] Open the saved master folder. Verify one ZIP per member with selections, plus manifest.json.
- [ ] Open a ZIP and verify its .4ss and READ-ME.txt. Do not edit the .4ss.
- [ ] Transfer a member ZIP through Drive; download and save locally on iPad, extract in Apple Files, then share the .4ss to forScore.
- [ ] In forScore, find the named Library Master setlist and open its charts. Report the result before full Chunk 3 acceptance.
- [ ] Restart band.logger and verify Last saved master, destination, members and catalog persist.
- [ ] Switch projects; confirm their destinations and master records remain separate.

The preview uses the saved scan. Generation rechecks selected source paths and content; if a PDF changed or disappeared, it fails and asks for refresh/review. Source charts and previous master folders are not overwritten. Repeated deliberate master generation creates a new complete master; it is not a supplement. Importing another master may create duplicate scores and does not transfer existing annotations automatically.

## Recovery and regression

- [ ] If files completed but the app record failed to save, choose Recover a completed master folder and select the original folder containing manifest.json. Recovery verifies project identity, catalog identities and ZIP checksums. Repeating recovery does not duplicate the app record.
- [ ] In a temporary synthetic library, change or remove a selected PDF after preview. Confirm export fails without recording a successful master.
- [ ] Recheck project switching, member edits, theme, Library search/Refresh, and safe PDF import.

Schema 3 adds durable master records. Before the first save from schema 2, workspace.pre-masters.backup.json is retained in %LOCALAPPDATA%\band.logger. Older app versions reject schema 3; keep backups for deliberate rollback. Do not edit or delete backups.

Limits: 64 MiB per PDF, 512 MiB of selected PDF data per member. No supplement generator, full history browser, setlist planner, Android app or direct cloud uploader is included. Drive performs synchronization; band.logger does not confirm recipients imported a package.