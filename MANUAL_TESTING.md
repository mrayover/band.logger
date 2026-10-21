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
## October 21, 2026 — Homepage / 0.3.3 checklist

This is the current checklist; the Stage 3B instructions above are retained as history. Extract the entire band.logger-0.3.3-windows-x64.zip and launch band.logger/band.logger.exe beside app/ and runtime/. Close older app windows first. Java and development tools are not required.

- [ ] Home shows Create Rehearsal / Create Gig above Upcoming Events / Pending Distribution, with equal cards, Lato, primary accents above and secondary accents below.
- [ ] Click each of the first three cards: an explicit Coming soon dialog appears; dismiss it and confirm no event was created.
- [ ] Pending Distribution opens its dedicated explanation, with no invented counts or records. Open Library master and return using Home. Check the sidebar destination too.
- [ ] Tab through cards, check the visible focus border, and activate with Enter/Space. Check mouse hover borders.
- [ ] Resize wide and narrow: two columns become one below 500 dp of content width, all cards remain accessible by scrolling.
- [ ] Switch projects: names, configured colors and the existing accent-filled active-project highlight follow the selection. Edit project remains accessible.
- [ ] Check Members, Theme, Library browsing/search/review and Library master; restart and check project selection/data persist.
- [ ] When testing imports/exports, use a temporary synthetic library; verify ZIP/.4ss, manifest and recovery through the existing checklist above. Original charts must remain unchanged.

Owner report received October 21: the actual Library Master imported successfully into forScore on the owner's iPad and the tunes were present. This supersedes the earlier pending-import statements; no additional iPad tests or annotation behavior are claimed.

## October 21, 2026 — Pending Distribution / 0.3.4

This supersedes the 0.3.3 placeholder checklist; older entries remain historical. Extract band.logger-0.3.4-windows-x64.zip completely and run band.logger/band.logger.exe with its app/ and runtime/ directories. Close old app windows first.

- [ ] Open Pending Distribution from Home and the sidebar. Wait for the source check. Verify the project name/theme and no changes to saved review decisions.
- [ ] Inspect member baselines: latest valid non-empty master per member, export timestamp then ID. A skipped member retains their earlier non-empty baseline; a member without one requires an initial master.
- [ ] In a synthetic test library with a recorded master, approve a new tune, add a new preferred part, and approve a revision. Check New Tunes, New Parts and Revised Charts respectively. Counts are member–tune selections, not distinct tunes.
- [ ] Confirm unchanged selections are absent by default; toggle Include unchanged to inspect them. Search by title, part, musician and status; expand details and check previous revision/part and current hash.
- [ ] Leave a newer preferred chart unapproved or conflicting: it must show attention, never silently fall back. Follow Library / Needs review and inspect the review tab.
- [ ] Remove or change a synthetic PDF after scanning, then Recheck source files. It must be blocked with a missing/changed warning; this check must not save catalog changes. Restore it and use Library Refresh/review to update the authoritative catalog.
- [ ] Check a project with no master and a project with no pending changes; verify explanatory empty states. Switch projects and check independent allocations and themes.
- [ ] Confirm existing Library Master exports/recovery, members, themes and four-card Home remain reachable. Exporting is not confirmation of musician receipt/import.

No supplemental ZIP/.4ss generation, supplement numbering, persistent supplement history or optional per-member Supplemental Export Preview is included. Source checks are point-in-time; repeat after external changes. The screen does not poll files continuously or revalidate historical export ZIP files.
