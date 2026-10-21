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

## October 21, 2026 — Events / 0.4.0 (current checklist)

This supersedes earlier event-placeholder checklists. Extract the entire band.logger-0.4.0-windows-x64.zip and run band.logger/band.logger.exe beside app/ and runtime/. Close older windows first. Runtime and Lato are bundled.

- [ ] Home → Create Rehearsal: editor opens with Rehearsal selected, a blank required title, optional YYYY-MM-DD date, Set 1 and Unsaved changes.
- [ ] Try saving a blank title or invalid date (2026-02-30). A visible error appears and the draft remains editable. Leave the date empty or enter a valid date.
- [ ] Search the current project's catalog, add two tunes, confirm Added disables repeat addition. Remove one; add another. Existing catalog and chart records must remain unchanged.
- [ ] Save event; All changes saved appears. Return to Upcoming Events and reopen; title/type/date/tune selections match. Past and undated saved events are also shown, dated first.
- [ ] Change title, date and event type; save, close the application, relaunch and reopen. Existing event, Set 1 and retained entry identities must remain stable in workspace.json.
- [ ] Create Gig on Home presets Gig. Save an empty, undated gig and reopen it.
- [ ] With unsaved changes, use Home, Back to events, another project, Create project and the window close button. Each must offer Save and continue, Keep editing and Discard changes. Escape/outside dismissal keeps editing. Invalid/failed Save and continue must retain the dialog and draft.
- [ ] Switch projects and confirm each lists only its own events and catalog. Create an event in a second project and restart again.
- [ ] Before the first schema-4 save, preserve an independent copy of your old workspace for owner review. Verify workspace.pre-events.backup.json holds the original schema 1/2/3 data after migration; existing immutable backups stay unchanged. Older binaries reject schema 4: deliberate rollback requires an appropriate preserved pre-migration workspace, which lacks later event edits.
- [ ] Verify Library browsing/review, Library Master preview/export/recovery, Pending Distribution, Members, Theme and project editing remain reachable. Use synthetic charts for export regression checks.

Selections use catalog tune identities with current non-excluded chart records; chart approval is not required for planning. No file reads/export or chart assignment occurs in the event editor. Previously selected tunes without active charts remain visible with a warning. Save is explicit; normal navigation/close is guarded, but forced termination or a power failure can lose an unsaved draft. There is one initial set, append/remove only, with no event deletion, multiple sets, ordering controls, templates, reference-only .4ss, member/event ZIP export or cover pages.

## October 21, 2026 — Multiple sets / 0.4.1 (current checklist)

This supersedes the single-set limitations above. Extract all of band.logger-0.4.1-windows-x64.zip, close older app windows and run band.logger/band.logger.exe beside app/ and runtime/.

- [ ] Open an existing rehearsal/gig. Its former Set 1 retains all tunes, title/type/date and identities. New events start with Set 1.
- [ ] At desktop width, verify Tune library on the left (38%) and sets on the right (62%), with compact event controls above. Each panel scrolls independently. Narrow the window; panels stack and remain scrollable.
- [ ] Search in the library; click + and verify insertion into the highlighted destination. Repeat + on the same tune; each occurrence is independent.
- [ ] Add Set; the new next-numbered set becomes selected. Click another set's header/Add Song; verify its highlight, library destination label and search focus. Add tunes to both sets without leaving the editor.
- [ ] Move entries Up/Down. First Up and last Down are disabled. Repeat occurrences move/remove independently; unrelated sets stay unchanged.
- [ ] Remove an empty set. Remaining sets renumber without changing their IDs/content/order. Remove a populated set: Keep set/dismissal retains it; explicit Remove set and songs removes it. The last set cannot be removed. Removing the active set selects a remaining destination.
- [ ] Save, close, relaunch, open Upcoming Events and reopen. All set/entry ordering, repeated tunes, title/type/date match. Reopening selects the first set; destination selection/search/scroll position are transient editor state.
- [ ] Verify Unsaved changes and normal navigation/project-switch/close save/keep/discard behavior after ordering/set edits. Invalid/failed saves retain edits.
- [ ] Switch projects; events and catalog stay isolated. Verify Library review, Library Master, Pending Distribution, members/themes still work with synthetic charts for destructive/export tests.
- [ ] On the first save of older data, verify workspace.pre-multisets.backup.json retains original schema 1–4 bytes. Existing pre-events/pre-masters/chunk1 backups stay untouched. Older binaries reject schema 5; deliberate rollback requires the preserved matching older workspace.

No drag/drop, cross-set move command, event exports, templates or cover pages. Move between sets by removing an occurrence and adding it to the desired set. Source PDFs and chart approval are untouched. Save explicitly; forced termination can lose unsaved drafts. Use a comfortable window height (approximately 650 px or taller) for both panels; short windows leave less panel space.
