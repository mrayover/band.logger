# 0.10.1 acceptance — Event notes, status and history

Ready-to-launch program: C:/Users/mrove/Projects/band.logger/band.logger/dist/band.logger-0.10.1-windows-x64/band.logger.exe. Local builds extract automatically; no manual ZIP extraction is needed. Future successful local builds update dist/Launch current build.lnk to their verified version. Older release folders remain intact. Close any older app using the production workspace before opening a newer version; existing process locking remains in force. Program files are separate from %LOCALAPPDATA%/band.logger and require no Java/Gradle installation to launch.

Use a synthetic project/event for acceptance. Do not delete or regenerate production exports for testing.

1. Open the executable. In Events > Upcoming, confirm existing events remain Planned regardless of date. Switch History and gig/rehearsal filters; confirm no old event was silently completed.
2. Create a Gig, set a title/date and add a tune twice, optionally across two numbered sets. Open Notes & status. For a synthetic event, check Test event before completing it.
3. Select Completed. Review the confirmation that the current setlist reflects what actually happened; confirm. Add two lines of post-show notes, then Save event (or Back to events > Save and continue). Confirm the dirty guard also protects an unsaved notes edit.
4. Close and reopen the app. Open Events > History > Gigs and reopen that same event. Verify Completed, Notes saved, original event identity, setlist order/repetitions and both lines of notes. Review recorded setlist; ensure its tune labels and occurrence order match completion. A test record is excluded from future counts.
5. Edit notes and save again. Optionally change a planned tune/title/date/type after completion: the recorded performance must remain unchanged. To correct actual history, first make the intended details/setlist edits, select Correct record from current setlist, review/confirm, and Save event. Review recorded setlist to see the new revision and retained previous one. Test classification changes to recorded facts also need that explicit correction.
6. Mark a synthetic rehearsal Canceled and save. Verify it stays in History and retains notes/setlist, never qualifies as a performance, and can still be reopened/exported/deleted normally. Returning to Planned puts it back in Upcoming without erasing prior recorded revisions.
7. Export a synthetic event with a distinctive private note, once with covers off and once with covers on. Notes must not appear in XML, READ-ME, cover PDFs or event-export.json. Existing member filenames/reference order/repeats and optional covers remain unchanged; actual forScore import/re-import and duplicate-cover behavior still use the earlier iPad checklist.
8. If checking cleanup, delete only a synthetic event with known synthetic tracked exports. Confirm exact listed folders; unrelated files/charts/library distributions must remain untouched. Existing pending cleanup remains retryable.
9. For future local development builds, run scripts/build-local.ps1 with a fresh version. Expect a ZIP plus a verified ready-to-launch version folder and updated Launch current build shortcut. Existing ZIP/version collisions must fail safely. A broken extraction must preserve the prior shortcut/build; scripts/test-local-delivery.ps1 verifies this automatically.

Migration: first save of schema 1–9 creates workspace.pre-event-history.backup.json without changing prior immutable backups. Load alone does not write. Older binaries reject schema 10; retain backups and previous builds. Dates and performance facts are user-confirmed, not inferred from distribution or planned tunes. Unsaved drafts still do not survive forced termination. Analytics/advanced Archive, physical iPad testing and cloud synchronization guarantees are outside this increment.

Earlier acceptance checklists below are historical and remain useful for regression review.

---

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

## October 21, 2026 — Member event exports / 0.5.0 (current checklist)

The owner approved 0.4.1 and updated Git before this increment. Extract all of band.logger-0.5.0-windows-x64.zip, close older app windows and run band.logger/band.logger.exe with app/ and runtime/ together.

- [ ] Open a saved multi-set rehearsal/gig. Export for Members is available; after editing it becomes Save to export (disabled). Save before previewing so output matches the saved event.
- [ ] Open Export for Members. Check saved date/title, every musician's current preference label, baseline ID, non-empty set count, total/matched/missing occurrence counts and exact recorded filenames. Repeats count individually; fallbacks remain the recorded part.
- [ ] Use the configured export root or Browse/paste an existing writable folder. Review Events/<saved date and event title>/ beneath it. This one-export override does not change project settings.
- [ ] For missing references or members without a master, verify the explicit warnings and required acknowledgement before Generate is enabled. Missing occurrences become Missing chart — <tune> placeholders in their original positions. No-baseline output is placeholder-only apart from set headings.
- [ ] For an undated event, verify Undated filenames require separate acknowledgement. Otherwise return, enter a date and save; the current date must never be substituted.
- [ ] An entirely empty event or no project members must show an error with no generation. Empty sets within a musical event are warned/skipped; later set headings retain their saved numbers.
- [ ] Generate: one new event folder, one ZIP per current member, each containing one complete .4ss plus READ-ME.txt. Verify all sets/repeats in one .4ss per musician, no PDFs or data attributes. Complete result lists ZIPs/warnings and Open Export Folder.
- [ ] Generate again; a distinguishable new event folder appears and prior files remain unchanged. Invalid/unavailable destination must report failure without successful output. Check original workspace/catalog/master records remain unchanged.
- [ ] Regression-check multi-set editing, save/restart/project isolation, Library Master and Pending Distribution. Use disposable synthetic data for destructive/failure tests.

### Owner iPad gate (not yet verified for reference-only event output)

Transfer each member ZIP through Google Drive, save locally in iPad Files, extract, then share its .4ss (not READ-ME.txt) to forScore. Verify the named setlist, Set 1/Set 2/Set 3 placeholders, exact tune sequence including repeated occurrences, fallback member parts and chart opening. Each referenced filename must already be installed; a generated master does not prove that. Test explicit missing-chart placeholders and a known uninstalled filename. Record any forScore duplicate-occurrence/title/placeholder behavior. No claim of event-export iPad compatibility until this owner test passes.

No event PDFs are read/copied/embedded/modified. Existing Library Master remains a separate embedded-PDF workflow. No supplements, templates, cover pages, per-song overrides or persistent event-export history. Event export does not save/edit an event or mark charts distributed/received. Workspace schema remains 5. Save explicitly; forced termination can lose unsaved drafts. An interrupted export can leave a hidden .bandlogger-event-* staging folder; do not distribute incomplete output. Package filename labels may sanitize/truncate Windows-unsafe text without editing saved titles. Long destinations can exceed Windows path limits; use a shorter root if needed.

## October 21, 2026 — Google Drive ZIP fix / 0.5.1

Use band.logger-0.5.1-windows-x64.zip instead of 0.5.0 when exporting to Google Drive for desktop. The reported zip END header not found error was reproduced on G: immediately after writing a valid archive; sequential reads of the same archive succeeded.

- [ ] Extract the entire 0.5.1 ZIP and close the old app before launching the new executable.
- [ ] Open the saved Eden Wine Lounge event, Export for Members, and keep the existing Google Drive destination.
- [ ] Generate member ZIPs. Verify completion and Open Export Folder, one ZIP per member and no overwrite of previous exports.
- [ ] Copy a generated ZIP to a local folder/extract it and inspect its one complete .4ss and READ-ME.txt. No PDFs/data attributes; saved headings, order and chart references remain.
- [ ] Repeat once to verify a new export folder; check existing event/catalog/master data unchanged.
- [ ] Continue the owner iPad gate above. The fix changes archive validation, not reference matching or the file format.

Archives are now completed and their contents validated in memory, then written and compared byte-for-byte through sequential reads. This avoids Drive's stale random-access file-length behavior. Reference-only payloads stay small; one member archive is held in memory at a time. Workspace schema/metadata, source PDFs, member baselines and output naming remain unchanged. Interruptions can still leave hidden incomplete staging folders; do not distribute them.

## October 21, 2026 — Library supplements / 0.6.0

Owner reports 0.5.1 event export imported correctly on iPad. That is owner-reported verification of the existing event workflow; it does not verify supplements. Extract the full 0.6.0 Windows ZIP, close older apps and launch the new EXE beside app/ and runtime/.

- [ ] With a recorded initial master, approve a new tune or newer chart in Library and open Pending Distribution. Verify per-member new tune/new part/revision counts; unchanged parts stay excluded.
- [ ] Preview next supplement. Inspect every included title, member, part, revision, path/hash and excluded/blocked/initial-master warnings. Configure destination via Library master if absent. Acknowledge allocations before Publish.
- [ ] Publish: affected members only receive embedded-PDF .4ss + READ-ME ZIPs under <root>/Supplements/Project-<identity hash>/Supplement 001 (then 002...). No original PDFs/master/older supplements are changed.
- [ ] Receipt shows number/time/output/member allocations/chart hashes; open its folder. Restart and reopen Pending → Distribution history → receipt. Pending delivered selections are now unchanged, independent per tune/part/member.
- [ ] Approve one part's revision; verify unrelated parts/members are not re-exported. Switching back to a part already delivered in the same master epoch must not re-export it unchanged. Members lacking initial masters are excluded and warned.
- [ ] In disposable test data, remove/change a selected source after preview or leave a conflict unresolved. Publication must fail/recheck or exclude with warnings, never silently downgrade. Empty eligible selection cannot publish.
- [ ] Repeated Publish cannot duplicate or overwrite. A completed but unrecorded output blocks publishing even after restart or destination changes. Use Recover supplement with its original folder; checksums/identities/number/chain are checked and repeated recovery is idempotent. Do not move folders or delete the pending-publication journal manually.
- [ ] If interrupted before completion, incomplete stages are not distributable; retry uses the same next number and a fresh stage. A completed folder is recoverable without reading source PDFs. Do not use Recover Library Master for a supplement.
- [ ] Confirm event exports still use only Library Masters. This task intentionally does not make them consume supplements. Check event editor/master/pending/theme/member regressions.
- [ ] Transfer a supplement ZIP using Drive → local iPad Files → extract → share .4ss to forScore; verify actual chart imports separately. Export history does not confirm installation/receipt.

Schema 6 preserves old schemas 1–5 in workspace.pre-supplements.backup.json on first write, retaining prior backups. Older apps reject schema 6. Recovery/history are project-specific; copying an app workspace alone is not a backup of PDFs or export folders. Pending calculation uses latest valid nonempty master per member plus matching recorded supplement deltas, independently by instrument part; a later master establishes a new epoch. UI history lists all masters/supplements but is not an installation tracker. Existing size limits remain (64 MiB/PDF, 512 MiB/member). No templates, covers, calendars or event-export supplement consumption.

## October 21, 2026 — Cumulative event references / 0.6.1

Owner reports production Library Master import, the reference-only Eden gig setlist import, Supplement 001/Night Dreamer import and persisted supplemental history successful. These are owner-reported results. Work has not performed a new iPad import for 0.6.1.

Extract all of band.logger-0.6.1-windows-x64.zip; close older app windows and run band.logger/band.logger.exe with app/ and runtime/ intact. Workspace schema stays 6; this integration has no migration.

- [ ] Open Quaternity's existing saved Test event containing Night Dreamer; select Export for Members. Marcus, Xavier and Ethan should match Q Night Dreamer -r00- C.pdf, Jordan Q Night Dreamer -r00- Bb.pdf, as recorded in production Supplement 001. Any genuinely unallocated tune remains missing.
- [ ] Preview each matched row's Library Master/Supplement NNN source, distribution identity, part and revision. Current preferences label filenames but never choose undelivered charts. Independently acknowledge Undated for the Test event, or save a date first; missing-reference acknowledgement is only needed for real missing entries/no-master members.
- [ ] Export to the usual existing destination. Each member ZIP has exactly one whole-event .4ss plus READ-ME.txt, no PDFs/data payload. Original naming, Events folder/collision suffixes, set headings, repeated occurrences and positions remain unchanged. Previous outputs are preserved.
- [ ] Transfer through Drive, save locally in iPad Files, extract and share .4ss to forScore. Verify exact Night Dreamer matching for the appropriate installed member chart, plus master tunes, headings, repeat positions and all sets. Export records do not confirm installation.
- [ ] In synthetic data only, record successive revisions/parts and verify latest applicable publication wins by numbered history (not timestamps). A newer nonempty master starts a new member-specific baseline; older anchored supplements do not override it. A skipped member retains their own earlier master and applicable supplements.
- [ ] Missing/no-master output retains existing placeholders/acknowledgements. Invalid supplement chain/anchor/manifest metadata must report an inconsistent record and stop export rather than guess. Unsafe recorded filenames remain missing. Do not edit production JSON to test corruption.
- [ ] Restart; saved events, supplement history, Pending Distribution counts and Library Master behavior remain intact. No original PDFs/master/supplement packages should be changed by event export.

Automated tests and packaged/extracted smoke cover cumulative resolution, per-part/member isolation, no PDF embedding and workspace immutability. Event generation resolves only recorded metadata; it does not verify installed iPad files or reread distribution ZIPs/source PDFs. Real owner event/Drive/iPad verification remains required for this integration.

## October 21, 2026 — File import/export organization / 0.7.0

Extract all of the new ZIP, close older versions and run the EXE with app/runtime intact. Schema 7 adds only the optional per-project Events export root. First save of schemas 1–6 preserves workspace.pre-event-destinations.backup.json without replacing earlier backups. Historical guidance above describes older releases; the following routing/naming applies to future exports only.

- [ ] Library → Import PDF starts at C:\Users\mrove\OneDrive\Desktop\Quaternity\band.logger\sorted whenever available. Navigate elsewhere if needed. Cancel does nothing. Import still copies into the authoritative chart source and requires normal review; staging originals remain. If staging is unavailable, the chooser uses its usual starting folder.
- [ ] Home → Edit project shows Events export root separately from Library Master destination. Default, if available: G:\My Drive\Quartet\band.logger\03 - Events. Choose an existing root, save, restart and verify it persists for this project only. In Export for Members, choose a root and use Remember Events root for this project to save it explicitly; generating alone does not save a temporary override.
- [ ] Saved gig exports land in <Events root>\Gigs\YYYYMMDD Title; rehearsals in <Events root>\Rehearsals\YYYYMMDD Title. Preview must show the route. An unavailable configured root reports an error and does not fall back to the master destination. Undated still needs its own acknowledgement. Repeated exports add a readable Export date/time/short-identity suffix and preserve earlier folders.
- [ ] One whole-event reference-only .4ss plus READ-ME per member ZIP; exact master-plus-supplement references, all sets/headings/repeats and member filename conventions remain. No event PDF embedding. Confirm owner Drive/iPad behavior; this session's automated export checks use synthetic local data.
- [ ] Next pending supplement goes under <distribution root>\Supplements\<readable project name>\Supplement NNN. ZIP/.4ss names: Project - Supplement NNN - Member. Only affected members are packaged; PDF bytes/selections unchanged. Inspect receipt's actual folder/name/hash metadata before distributing.
- [ ] In synthetic data, identical or sanitized project names get a stable suffix only on collision; duplicate member package names get a numeric suffix only when needed. Preserve the hidden .bandlogger-project-id ownership file with its project folder. Do not change it manually.
- [ ] Historical Supplement 001/002 stay at their recorded paths with their original filenames. Open those receipts/folders and verify recovery remains available. Do not rename, move or regenerate them. A completed unrecorded old-convention output blocks new publication and must be recovered in place. Journals, numbering and no-overwrite checks remain.
- [ ] Recheck existing Library Master/Pending Distribution, saved events, theme/member/project isolation. Older binaries reject schema 7; preserve the migration backup and all distribution/source files independently.

Automated tests cover old-format recovery, naming/collision ownership, staging fallback/import preservation, schema-6 history migration, both event routes, missing roots and repeat exports. Packaged/extracted smoke covers persisted separate Events root, gig/rehearsal routing, readable supplemental naming and unchanged forScore content. Real production publishing and a new iPad import are owner checks, not claimed here.

## October 22, 2026 — 0.8.0 acceptance checklist

Extract the complete 0.8.0 ZIP, close older band.logger windows and run band.logger.exe. Keep app/ and runtime/ alongside it. Before reviewing, preserve the workspace and existing export folders. Older app versions reject schema 8; the immutable pre-event-exports backup is for deliberate rollback, not opening current data in an older app.

1. In Project details, verify the single Project export root. Choose it once; restart and confirm the event preview and Library master reuse it without a picker. The former separate Events root remains a legacy discovery location. An unavailable configured root must report an error.
2. Review export layout migration from Upcoming Events. Confirm original master/supplement paths are listed. Open previous receipts/recovery in place; do not regenerate or bulk-move production distributions. Next authorized future output should use Library/Master or Library/Supplements/Supplement NNN with member ZIPs directly inside.
3. Use a new synthetic test event and a separate test root for destructive checks. Export a dated gig and rehearsal. Expect Gigs/YYYY-MM-DD Title and Rehearsals/YYYY-MM-DD Title, readable member ZIPs, and persisted export history after restart.
4. Re-export: New export only must stop; Create revision must preserve old output and show Revision 2; Replace tracked exports must publish a new revision before cleaning prior tracked packages. Confirm unrelated/untracked folders remain.
5. From Upcoming Events, choose Edit. Change title/date/order, save, close/reopen and confirm the same event retains its old export paths. Verify the existing unsaved Save/Keep editing/Discard protections still work.
6. On the test event choose Delete, inspect the exact folder/ZIP list and cancel once. Then approve deletion yourself: the saved event and all its verified tracked packages should disappear; Library/Master, supplements and charts must remain.
7. With another synthetic test event, temporarily make its test destination unavailable, delete and verify cleanup remains pending rather than reporting completion. Restore it and use Retry export cleanup. Restart before retry to confirm persistence. Do not disconnect or rename production folders for this test.
8. Add an unrelated text file to a synthetic event export folder, then request deletion. All packages must be preserved for review, with pending cleanup. Remove only that synthetic extra file yourself and retry. Engine tests cover partial failures and failed metadata saves.
9. Discover older exports: verified manifests can associate automatically; old name/date-only folders must appear for review and remain untouched. Do not expect Eden's older untracked packages to be auto-deleted.
10. Open a new member ZIP locally in iPad Files and share .4ss to forScore, checking exact cumulative references, all sets/repeats and no embedded PDFs. Local Drive filesystem verification does not prove cloud synchronization or device import.

Agent validation: full 176-test regression suite; synthetic persistence/edit/deletion/revision/replacement/migration/unavailable/partial failure coverage; real Drive test isolated under 06 - Testing; UI inspected event actions and exact deletion confirmation and cancelled it. Final package and extracted-runtime smoke/restart verification are recorded in WORK_LOG. Full production owner acceptance and a new iPad import remain manual checks. Physical legacy relocation and ambiguous legacy ownership are not automated.

## October 22, 2026 — 0.9.0 branding and embedded covers acceptance

Extract the whole 0.9.0 ZIP and run band.logger.exe with app/runtime alongside it. Close older versions first. Schema 9 keeps an immutable pre-branding-covers backup on the first legacy save; older binaries reject the current workspace. Preserve historical production packages at their recorded locations.

1. On Quaternity Home, confirm the exact Q artwork with its original proportions/colors, ivory backing, Lato and unchanged selected-project highlight/navigation. Check compact editor/page headers. Switch to another project: its header has no Q logo. Project details can explicitly choose/clear Quaternity branding; renaming a branded project preserves its brand.
2. Use a new synthetic gig containing at least two nonempty sets, a repeated tune, and a missing reference if useful. Save it. Open Export for Members. Include branded set covers must initially be off. Existing plain-heading preview/export remains available with all previous acknowledgements and conflict choices.
3. Enable Include branded set covers. The choice is saved only for this project. Generate to an isolated test root. Each musician must receive exactly one ZIP containing one complete .4ss and READ-ME.txt, with no extra cover download or per-set file. Restart the app and confirm the setting and tracked export history persist.
4. Transfer one musician ZIP through Google Drive, save it locally under On My iPad in Apple Files, extract there, then share the extracted .4ss to forScore (14.3+). Do not directly import READ-ME.txt. Do not use a production event for this first gate.
5. Open the event setlist. At the start of each nonempty set, expect exactly one cover: official logo, band name, exact saved event title/date, correct Set 1/Set 2 number and gig/rehearsal designation. No invented performance time. Check readability, logo shape, accents, long-title wrapping and all pages on iPad.
6. After each cover, verify every musician-specific chart matches the already-installed exact recorded filename. Check all tune ordering/repeats and missing placeholders. Music PDFs must not be newly imported or duplicated. Empty sets should not acquire covers or duplicate headings.
7. Test a saved rehearsal, and optionally an undated synthetic rehearsal: its cover uses REHEARSAL, the saved title, Date not set when no date exists, and the correct set number. The existing separate Undated acknowledgement still applies to export filenames.
8. Re-import the same synthetic covered event once. Observe/report whether forScore creates renamed cover copies and which filenames/setlist entries it uses. The documented format adjusts existing embedded filenames; band.logger does not promise deduplication or silent replacement. Inspect covers separately from music and preserve annotated music. App Replace tracked exports does not delete cover copies on iPad.
9. Turn covers off, explicitly create a new export revision, transfer/import it and confirm the established Set N placeholders return, with no embedded cover PDFs and unchanged music order. Toggle/restart confirms the latest per-project preference persists.
10. In synthetic data only, exercise Create revision/Replace tracked exports and Delete event. Both covered/plain packages remain correctly tracked and verified cleanup removes their Drive ZIP folders; historical charts, masters, supplements and unrelated files remain. Retry failures through Upcoming Events. Earlier publication/recovery/location behavior must remain.

Agent validation: 194 automated tests, including the earlier Windows 8.3 TEMP regressions; PDFBox parsing/text/embedded font/logo pixel tests; independent Poppler rendering of gig/rehearsal/long-title PDFs; actual synthetic Drive mixed-cover and plain ZIP generation followed by separate-process cleanup; local Windows executable and UI checks. Full final package/extracted results are in WORK_LOG. The owner must still verify new forScore import appearance, set ordering, installed-chart matching and duplicate-cover behavior on an actual iPad. Unusually long text/unsupported Lato glyphs may require plain headings; no content is silently truncated.
