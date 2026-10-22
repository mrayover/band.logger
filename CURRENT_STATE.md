# Current state — Export organization and event management, October 22, 2026 / 0.8.0

Completed the authorized increment on top of the uncommitted 0.7.0 work; baseline source snapshot and prior ZIP/live JSON hashes preserved under .tools/event-management-baseline-20261022. No commits or pushes.

Future exports share the remembered project exportDestination root: Library/Master (later master Revision N), Library/Supplements/Supplement NNN, Gigs/YYYY-MM-DD Title and Rehearsals/YYYY-MM-DD Title. Member ZIPs sit directly in those folders. Completed folder names have no technical hashes/UUIDs or redundant project namespace. Old separate Events root stays available for legacy discovery/fallback. Library roots are project-owned; shared roots fail safely.

Upcoming Events has Edit/Delete, retained existing editor/save guards, persistent export history, recovery, cleanup retry, legacy discovery and read-only layout migration review. Event records survive restart/title/date/setlist edits by stable identity. Repeat exports explicitly choose New only, Create revision or Replace tracked exports; replacement publishes new output before verified old-file cleanup. Delete persists event removal and exact cleanup tombstones before touching files. Manifest/path/hash/inventory checks block foreign/changed content; no recursive deletion; unavailable/partial failures remain retryable. Cleanup records preserve the original event snapshot after event removal.

Schema 8 reads 1–7 safely and preserves immutable workspace.pre-event-exports.backup.json plus all earlier backups/atomic saves. Existing master/supplement/chart/history paths and recovery remain unchanged. No bulk migration; the in-app review lists original paths and recommends future-only layout. Older event folders without stable identity manifests are shown for review, never auto-adopted/deleted by name.

PASS: 176 tests, zero failures/errors/skips; built-executable local lifecycle smoke; isolated real Google Drive for desktop export/revision/edit/deletion and separate-process restart checks. Sequential ZIP validation retained. GUI inspected exact deletion confirmation and cancelled; actual destructive verification was synthetic engine/executable work. Final ZIP/extracted verification recorded below/in WORK_LOG.

Delivery: dist/band.logger-0.8.0-windows-x64.zip; extract all, keep app/runtime, launch band.logger.exe. Unsigned self-contained Windows x64 preview; no developer Java required. Owner acceptance checklist is the October 22 section of MANUAL_TESTING.md. Limitations: no automated physical legacy relocation or association of identity-free exports; no new iPad/cloud-sync confirmation. Preserve incomplete stages for review. No excluded features or next stage begun.

---
# Current state — File organization cleanup, October 21, 2026 / 0.7.0

Verified clean main 156b971 (Integrated Supplements to Main Library), baseline 0.6.1. Completed only requested file organization: Import PDF defaults to owner's available sorted staging directory; future supplements use readable project/member names; events have a separate saved root and automatic saved-type Gigs/Rehearsals routing. No additional music-management functionality.

PDF staging preference: C:/Users/mrove/OneDrive/Desktop/Quaternity/band.logger/sorted. Fall back to usual chooser when missing/unreadable; manual navigation remains. Authoritative chart source, safe copy/review/naming/identity behavior unchanged, no staging moves/deletes.

Future supplement example: <distribution root>/Supplements/Quaternity/Supplement 003/Quaternity - Supplement 003 - Marcus.zip, with corresponding .4ss/internal title. Sanitize project/member names; only genuine same/sanitized project folder collisions get short stable ID suffix, duplicate member basenames numeric suffix. Hidden .bandlogger-project-id ownership marker protects stable ID isolation across sessions. Historical Supplement 001/002 paths/ZIPs remain untouched and recover via exact recorded metadata. Legacy unrecorded numbered targets/journals block duplicate generation; no numbering advance on failure. Embedded PDFs and allocations unchanged, timestamp/hash/IDs retained in manifests.

Events export root: optional per-project eventsExportRoot, editable in Home/Edit project or explicitly saved by Remember Events root in export preview. Unconfigured default when available: G:/My Drive/Quartet/band.logger/03 - Events. Saved gig/rehearsal -> root/Gigs or root/Rehearsals, then YYYYMMDD saved title. Temporary override generates without silently saving. Missing configured roots error instead of falling back to Library Master destinations. Member ZIP names, exact cumulative references and one complete reference-only .4ss/member preserve sets/repeats/no PDFs. Repeats preserve old folders with readable Export UTC date/time/short-ID suffix. Existing cloud-compatible ZIP validation remains.

Schema 7 adds only optional Events root. Safely reads schemas 1–6 and first old-schema save retains immutable workspace.pre-event-destinations.backup.json plus prior backups and atomic saves. No historical schema data/IDs/records modified by migration; older apps reject 7.

PASS: 161 tests (148 retained/adapted + 13 new), zero failures/errors/skips. Final scripts/build-local.ps1 compile/tests/distributable PASS (Gradle 10 seconds); standard packaged and extracted smoke/restart PASS, including persisted Events root, both saved-type routes, readable supplement names and all previous XML/PDF/reference checks. New synthetic tests include actual old-format package/receipt recovery and unchanged old bytes. All 207 extracted files match ZIP SHA-256 streams; all 13 prior ZIPs unchanged. Extracted UI with synthetic workspace: separate Events root saved without altering library destination; chooser opens sorted and cancelled with no import. Owner workspace/backups unchanged. Additional UI restart/preview results appended to WORK_LOG.

ZIP: C:/Users/mrove/Projects/band.logger/band.logger/dist/band.logger-0.7.0-windows-x64.zip (61,879,446 bytes).
EXE: C:/Users/mrove/Projects/band.logger/band.logger/dist/band.logger-0.7.0-windows-x64/band.logger/band.logger.exe.
SHA-256: 50BE34B4CD0711FCD32F58BF79322F27951F3F9FA4CB0B870E1CBD4738AB6D8B (dist/SHA256-0.7.0.txt). Extract all, retain app/runtime; unsigned self-contained Windows x64 preview.

Next/limits: owner verifies actual roots, historical Supplement 001/002 receipt opening and next future supplement in normal use per MANUAL_TESTING. No production chart/event/history/package changes, Drive publishing or new iPad import performed. Preserve hidden ownership markers and historical folders; unowned nonempty readable folders get a collision suffix instead of adoption. Defaults are requested owner-local preferences, not a general preference editor for PDF staging. No unrelated work, dependencies, workflow change or commit/push/merge/release. All seven cumulative docs updated; older entries below remain historical.

---
# Current state — Cumulative event references, October 21, 2026 / 0.6.1

Baseline verified clean main 0372f06 (Added A Supplement and Tested), application 0.6.0. Owner reports successful production Library Master import, reference-only Eden gig setlist import, Supplement 001/Night Dreamer import on iPad and persisted supplement history. Those are owner-reported successes; no new iPad test performed by Work.

Completed: Event Export for Members now reads a deterministic cumulative repertoire: latest valid nonempty master per stable member ID, then only successfully recorded supplements anchored to that same master ID, applied in consecutive number/predecessor order, not timestamp order. Preserve independent (tune ID, part) revisions and unchanged selections; most recently delivered applicable tune selection supplies its exact recorded filename. Current catalog/preferences never imply delivery. A new master starts a new member epoch; skipped members retain their earlier epoch. Reusable DistributionResolver validates supplemental records/anchors/chain and stops inconsistent history before output rather than guessing.

Existing preview shows Library Master/Supplement NNN provenance, publication identity, part/revision. Missing/no-master/unsafe references remain placeholders; Undated acknowledgement stays separate. Output remains one whole-event reference-only .4ss plus READ-ME/member ZIP, preserving all sets/repeats/identities/order, saved naming, Events destination, collision protection and Google Drive-compatible sequential ZIP verification. No event-editor, master/supplement publisher, schema (still 6), source PDF, workspace/distribution write, dependency or GitHub workflow changes.

Real manifest inspection: Supplement 001 id 06d94c2f-767e-4b51-96a3-df78fc04bde4 anchors all four recipients to master ea4728c3-eb6b-457c-8ab5-c4cc597b35ce. Night Dreamer tune id 7a551e44-ae7c-4aff-ac8a-05d5d86459fc: Marcus/Xavier/Ethan Q Night Dreamer -r00- C.pdf; Jordan Q Night Dreamer -r00- Bb.pdf. Built-release isolated audit using preserved 466-selection master metadata + actual supplement manifest resolves 1 matched/0 missing for each, produces four validated local reference-only ZIPs, and preserves copied workspace bytes. Current live Test event was not edited or exported. Available workspace.json was an older schema-2 snapshot; live schema-6 Test event was not used as the audit fixture. Production manifest was inspected directly; immutable master backup supplied recorded baseline. No production master/supplement was regenerated.

PASS: 148 tests (136 prior retained/adapted + 12 new), zero failures/errors/skips. Final scripts/build-local.ps1 compilation/test/distribution PASS (Gradle 11 seconds); packaged and extracted executable smoke plus separate-process restart PASS. New smoke covers master -> supplement -> saved/reopened event -> exact reference-only filename/no embedding, unchanged workspace bytes and second-process cumulative resolution. Extracted UI copy shows correct matches/source labels for all four actual recipients. All 207 extracted files match ZIP streams; all 12 prior Windows ZIPs unchanged. Owner workspace/backups and production supplement manifest hashes unchanged.

ZIP: C:/Users/mrove/Projects/band.logger/band.logger/dist/band.logger-0.6.1-windows-x64.zip (61,868,847 bytes).
EXE: C:/Users/mrove/Projects/band.logger/band.logger/dist/band.logger-0.6.1-windows-x64/band.logger/band.logger.exe.
SHA-256: FA0B21D1B5F0DE3B7ECEBBF0FC90FE3773DCFCFA9869339C3326278BA30A0575; dist/SHA256-0.6.1.txt. Extract all, retain app/runtime, close older apps and launch. Unsigned Windows x64 preview with bundled runtime.

Remaining: owner opens existing saved Test event in 0.6.1, reviews allocations, independently acknowledges Undated, exports to usual destination and verifies the exact installed charts on iPad. This integration did not perform a new Drive write/iPad import or exhaustive old-screen UI regression. Event export validates saved metadata, not current iPad installation/package files; exports do not prove installation. Any malformed recorded supplemental chain is blocked for inspection. No optional features begun. All seven cumulative docs updated; no commit/push/merge/release.

---
# Current state — Supplemental distribution publishing, October 21, 2026 / 0.6.0

Owner reports 0.5.1 event exports imported correctly on iPad and all prior changes committed (clean main 9f284c9). This verifies the earlier event workflow by owner report; supplement iPad import remains to be checked. Historical states follow below.

Pending Distribution now previews/publishes numbered Library Supplements from accepted new tunes/new parts/revisions, independently per musician and instrument part. Latest valid nonempty master + matching recorded supplement deltas form each member's cumulative part-specific baseline; unchanged charts are excluded. Exact preview includes affected selections/hashes and blocked/no-initial-master warnings, with review acknowledgement. Only affected musicians receive embedded-PDF .4ss + instructions ZIPs under <root>/Supplements/Project-<stable hash>/Supplement 001, 002, etc. Numbered manifests/receipts/history persist allocations, chart/package hashes/revisions, timestamps, baseline anchors and chain. Previous masters/supplements/PDFs are preserved; exported is not installed.

Source bytes/paths and preview are revalidated, ZIP CRCs are sequentially checked and manifests staged before no-overwrite publication. Saved numbering advances only after history persistence. Durable pending intent blocks duplicate publication after files complete but saving fails, including restart/destination change; Recover supplement verifies original packages and records idempotently. Interrupted incomplete stages never count as distribution. History opens previous master folders and supplement receipts after restart. Event exports intentionally still use masters only.

Schema 6 safely loads 1–5 with empty supplement history and immutable workspace.pre-supplements.backup.json before first write; existing backups and atomic storage retained. Older binaries reject schema 6. No new dependency or GitHub Actions workflow changes.

PASS: 136 tests (123 prior retained/adapted + 13 new), zero failures/errors/skips; compilation; normal packaged/extracted smoke and prior event/restart checks. New smoke publishes embedded synthetic supplement, parses exact PDF bytes/XML, persists/reopens records, verifies cumulative pending zero and recovery. PASS final extracted local UI: preview allocations/acknowledgement, four affected-member ZIPs, persistent Supplement 001 receipt, pending counts zero, full close/restart and receipt reopened via history. All 207 extracted release files SHA-256 match ZIP streams; every earlier Windows ZIP unchanged. Test data was synthetic/local; no owner chart/event/master data or actual distributions changed.

ZIP: dist/band.logger-0.6.0-windows-x64.zip (61,853,721 bytes).
EXE: dist/band.logger-0.6.0-windows-x64/band.logger/band.logger.exe.
SHA-256: 869FD379992AC00BC2F454CEB9A83A766AA32096049B9778E487DAAD06096E2F (dist/SHA256-0.6.0.txt).
Extract all, close older apps, retain app/ and runtime/ and launch. No Java/Gradle install. Unsigned Windows x64 preview. No commits/pushes/releases/remote workflow runs.

Limits/remaining checks: production Drive publishing, supplement iPad imports, manual conflict/failure/recovery UI and all old-screen regressions were not manually performed; engine cases have automated coverage. Existing 64 MiB/PDF and 512 MiB/member limits apply. Incomplete stages and malformed journals need preservation/inspection, not distribution/deletion. Do not move published folders before recovery. No installation tracking, templates/covers/calendar or event-export supplement consumption.

Next: owner follows MANUAL_TESTING.md for supplement review and iPad import, then separately authorize event exports to resolve against cumulative distribution history. No additional functionality begun.

---
# Current state — Google Drive ZIP export fix, October 21, 2026 / 0.5.1

Supersedes the unqualified 0.5.0 export-success status below for Google Drive destinations. Owner committed 0.5.0 at clean main 9f4ccbf, then reported zip END header not found. Root cause reproduced: immediate random-access ZIP reopening on G: fails even though a sequential read returns valid complete archive bytes.

Fixed: complete/validate each member ZIP in memory, then write it to the existing stage and verify every byte sequentially. No reliance on transient destination file size; unchanged staging/publication/no-overwrite, exact XML contents/CRC checks and reference-only output. No event/catalog/master/schema/reference-naming changes or new dependencies. One small reference archive is held in memory per member.

PASS: 123 tests (120 existing + 3 new), zero failures/errors/skips; compilation; normal packaged/extracted smoke/restart; additional app-image and extracted executable smoke exporting all member reference ZIPs to an isolated child of the actual G: destination. Synthetic PDFs/master tests stayed local. PASS local extracted UI with four members, two sets split 8/10 and 18 song occurrences; generated/parsed four ZIPs with 18 score references/two correct heading placeholders each, no data attributes. Synthetic workspace unchanged. All 207 extracted release files match ZIP hashes, every prior Windows ZIP preserved byte-for-byte, existing app-data workspace unchanged. Owner's original app/dialog left open; no real event export or edits performed.

Additional G: UI Generate click was blocked by automatic approval review as an external synced-data write. No retry/bypass; the safe local-only UI alternative passed. Do not describe that extra Drive UI action as executed. Owner actual event retry, independent Drive sync/iPad delivery and iPad placeholder/repeat/reference verification remain outstanding.

ZIP: dist/band.logger-0.5.1-windows-x64.zip (61,771,654 bytes).
EXE: dist/band.logger-0.5.1-windows-x64/band.logger/band.logger.exe.
SHA-256: 6D4BB9CF27277D2F00159981957EF01A0C605BED13DA2259BD947E8E35CA14F4 (dist/SHA256-0.5.1.txt).
Extract all, close the 0.5.0 window, keep app/ and runtime/ together and launch 0.5.1. No Java/Gradle installation required. Unsigned Windows x64 preview. Retry the existing saved event with the same destination, then proceed to the owner iPad gate. Prior packages/history retained. No commits, pushes, releases or remote workflow runs; no additional functionality started.

---
# Current state — Member-specific event exports, October 21, 2026 / 0.5.0

Owner tested/approved 0.4.1 and updated Git; continued clean main at 84f9204. This supersedes earlier event-export deferrals below; history is retained.

Saved rehearsal/gig → Export for Members now previews every current project musician's recorded baseline, preference, total/matched/missing occurrence counts, exact filenames, set count and destination. Unsaved drafts must be saved first. Missing/no-baseline output and Undated filenames require separate explicit acknowledgements. Each musician receives one ZIP with one complete reference-only .4ss and READ-ME.txt. All saved nonempty sets/order/repeats are retained, Set N headings use official placeholders, and missing references occupy their original positions as Missing chart — <tune>. Empty sets are warned/skipped; entirely empty events/no members are rejected.

References come exclusively from each member's latest valid non-empty recorded Library Master by tune ID, reusing PendingPlanner's deterministic rule and recorded fallback filenames. Current catalog revisions do not replace references. Current preferred parts label filenames only. No PDF/source/master-ZIP IO, chart approval/distribution changes or workspace writes. Schema remains 5. Generation validates XML, stages/verifies all member ZIPs and publishes a new folder under <root>/Events/<saved date and event title>; repeat folders are uniquely suffixed. Destination overrides are transient. Success lists outputs/warnings and Open Export Folder.

PASS: all 120 tests (97 prior + 23 new), zero failures/errors/skips; compilation; standard Windows packaging; app-image/extracted smoke including proper parsing of member ZIP XML, heading/order/repeat/exact filename/no-data checks, workspace immutability and existing second-process restart tests. Extracted execution uses path with spaces and no developer Java. All 207 extracted release files match ZIP SHA-256; all previous Windows ZIPs unchanged. Live workspace unchanged.

PASS final extracted synthetic UI: saved-event export action/two panels retained, per-member preview (Marcus: 4 total/3 matched/1 missing with recorded C fallback under Guitar preference; new member: 4 missing/no baseline), missing+Undated independent acknowledgement gates, generation/success filenames/warnings, Open Export Folder, dirty-edit export disabling and clean reversion. Independent XML parsing verified exact paths, two sets, all repeats, titled missing placeholders and no PDF data; UI workspace remained byte-identical. Full owner real-library UI/export regression, manual failure/repeated-export/narrow-modal branches and actual iPad import were not performed; engine cases and existing feature regressions passed automated tests.

ZIP: dist/band.logger-0.5.0-windows-x64.zip (61,770,065 bytes).
EXE: dist/band.logger-0.5.0-windows-x64/band.logger/band.logger.exe.
SHA-256: 1818335CF9C5960C5D2F39F3AADE31C4D56E2803EBD3D8B49B3F3D463255180B (dist/SHA256-0.5.0.txt).
Extract all, retain app/ and runtime/, close old app windows and launch; no Java/Gradle installation. Unsigned Windows x64 preview. No new dependencies, original chart changes, remote Actions runs, commits/pushes/releases/uploads. Earlier successful packages preserved.

Limits: no supplements, templates, covers, per-song overrides, embedded-PDF event packages or persistent event-export history. No installation/receipt confirmation; actual iPad filenames must match. Long labels are sanitized/truncated and long roots may exceed Windows path limits. Interrupted exports can leave hidden incomplete staging directories; do not distribute them. Event editing/save/crash limits remain unchanged.

Next: owner transfers member ZIP via Drive → local iPad Files extraction → shares .4ss to forScore and verifies set headings, exact order/repeats, installed filename/fallback matches and missing placeholders. Prior successful Library Master import does not verify this new reference-only workflow. No further features started; await device findings before another export increment.

---
# Current state — Two-column editor and multiple sets, October 21, 2026 / 0.4.1

Supersedes older single-set restrictions/status below; all history is retained.

Event editor: compact title/type/date/save/status/back header, 38% searchable library with + buttons, 62% setlist builder. Panels scroll independently and stack below 700 dp editor width. Add Set creates/selects the next set; header/Add Song selects a highlighted destination and focuses search. Repeated tunes are independent occurrences. Up/Down changes persisted order within a set; boundary actions disable. Remove affects one occurrence. Populated-set removal asks for confirmation; last-set removal is disabled/invalid; remaining sets renumber with stable IDs/content. Event creation/reopen/edit, project isolation, existing explicit save/dirty/navigation/project/close guards, Library Master and Pending Distribution remain.

Schema 5 extends the existing initialSet with ordered additionalSets. Single-set schema-4 events retain their event/set/entry identities and songs as Set 1. Schemas 1–4 load without disk writes; first save preserves original bytes in workspace.pre-multisets.backup.json, retaining previous immutable/rolling/atomic backups. Older apps reject schema 5; intentional rollback needs the matching pre-migration workspace. Active destination/search/scroll/window state is transient; reopen selects Set 1. No owner workspace migration occurred during testing.

PASS: 97 tests (82 retained/adapted + 15 new), zero failures/errors/skips; compilation; standard Windows packaging; app-image and extracted smoke, including second-process reopen/reorder of multiple sets/repeats with stable IDs, unchanged masters and project isolation. Extracted checks run in a path with spaces with JAVA_HOME empty and system-only PATH. All 207 extracted files SHA-256 match the ZIP; every previous Windows archive is byte-identical to its before-session hash. Live workspace unchanged.

PASS synthetic extracted UI: desktop two columns, compact header, destination highlight/search focus, no-match/matching search, + into selected Set 2, repetition, Down control and actual saved ID order, Add Set 3/automatic destination, independent right scrolling, populated-set confirmation/Keep set, narrow stacked layout at about 800 px window width, save/close/restart/list/reopen with three sets/four entries. Readback confirmed unchanged unrelated Set 1/catalog and persisted reordered occurrence IDs. Actual destructive UI removal, library scrolling with a long catalog, every dirty-dialog branch, manual Up, full owner real-library regression and additional iPad tests were not run; removal/Up/dirty persistence and old feature regressions have automated coverage.

ZIP: dist/band.logger-0.4.1-windows-x64.zip (61,707,285 bytes).
EXE: dist/band.logger-0.4.1-windows-x64/band.logger/band.logger.exe.
SHA-256: 0E9D3F947880E0E0225E17DA4E1956599A1BC9159D00569AAB82B08AB530D87B (dist/SHA256-0.4.1.txt).
Extract all, keep app/ and runtime/, close older app windows and launch. No Java/Gradle installation required. Unsigned Windows x64 preview. No dependencies installed, original PDFs modified, remote workflow runs, commits, pushes or releases. Prior uncommitted work preserved and extended.

Limits: no drag/drop/direct cross-set move, exports, templates, cover pages, venue/calendar/notes/recording/supplement work. Move between sets by remove/add. No automatic draft recovery; forced termination can lose unsaved edits. Comfortable height is about 650 px or taller; very short windows reduce panel space. Next: owner review with MANUAL_TESTING.md, then separately authorize reference-only forScore export planning/device identity matching. No additional functionality begun.

---
# Current state — Event creation and persistence, October 21, 2026 / 0.4.0

This section supersedes older event-placeholder and schema-version statements below; historical entries are preserved.

Create Rehearsal / Create Gig now open the themed editor with required title/type and optional YYYY-MM-DD date. Search the current project's existing catalog, append/remove tunes in one initial Set 1, explicitly save, reopen from Upcoming Events (Home/sidebar), close/restart and continue editing. Event/set/tune-entry identities remain stable. Empty and undated events are allowed; the list also retains past events, dated first. Entries reference existing tunes without duplicating catalog records. No source charts, Library Master or Pending Distribution behavior changed.

Unsaved changes are explicit. Navigation, project creation/switching and normal window close offer Save and continue, Keep editing or Discard changes. Failed/invalid saves retain the draft. Forced termination or power failure can lose unsaved changes; no automatic draft recovery is implemented.

Schema 4 migrates schemas 1–3 in memory, preserving project settings/catalogs/master records. First write retains original bytes in workspace.pre-events.backup.json, alongside previous immutable backups and workspace.backup.json. Older binaries reject schema 4; deliberate rollback requires an appropriate pre-migration workspace without subsequent edits. Test runs never migrated or changed the live workspace.

PASS: all 82 tests (67 existing + 15 new), zero failures/errors/skips; compilation; standard Windows packaging; app-image and extracted smoke tests including event migration/creation/persistence plus a second executable process reopening/editing with identities/isolation preserved. Extracted check runs in a path containing spaces with JAVA_HOME empty and system-only PATH. Existing storage/font/XML/ZIP/catalog/master/recovery/pending smoke checks pass. All 207 extracted files match ZIP SHA-256.

PASS extracted synthetic UI: both homepage creation actions/preset types, title validation, tune addition, saved/unsaved indicators, project-switch guard/Keep editing, full close/restart, saved-event list/reopen/edit, close guard/Save and continue, identity readback, separate-project empty list, and empty undated gig save. Live workspace remained byte-identical. Full owner real-library UI regression, all dialog branches, narrow-window/accessibility review, manual date edge cases and additional iPad tests were not performed; automated regressions cover domain/storage and date validation. No commits, pushes or remote Actions run.

ZIP: dist/band.logger-0.4.0-windows-x64.zip (61,667,083 bytes).
EXE: dist/band.logger-0.4.0-windows-x64/band.logger/band.logger.exe.
SHA-256: 84A1EC25D12D9F47F1AEB7933895F02DFFDDBB92388780C80BD764EC532ECEB3 (also dist/SHA256-0.4.0.txt).
Extract all files, retain app/ and runtime/, close older app windows, then launch. No Java/Gradle installation required. Unsigned Windows x64 preview. Previous successful builds retained.

Limits: one set, one occurrence of each tune, append/remove only; no event deletion, multiple sets, advanced ordering, drag/drop, templates, member assignments, reference-only .4ss, event/member ZIP generation or cover pages. New choices require active current-source chart records, but approval is independent of planning; retained selections without active charts show a warning.

Next: owner review with the 0.4.0 MANUAL_TESTING checklist, then separately authorize simple ordering within Set 1 before broader setlist/export work. No additional functionality started.

---
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

Drive cleanup detail (0.8.0): Drive for desktop may inject a hidden UTF-16 desktop.ini after publication. Only its bounded, exact known GoogleDriveFS icon/ViewState profile is recognized; arbitrary desktop.ini or other files still block deletion. Persist the exact Base64 bytes in providerMetadata before removing that decoration, retaining them in the completed tombstone. Changed profiles remain for review. Synthetic local regressions and actual Drive retry passed with no event folders/ZIPs remaining.

0.8.0 final delivery verified: scripts/build-local.ps1 and packaged/extracted smoke + second-process restart PASS. ZIP 61,966,768 bytes; SHA-256 32AA85F26F31CCC7AE0A4BB8C3D2B642353BA6B9092458DA2CFE938A085A74E4 (dist/SHA256-0.8.0.txt). All 207 extracted files match archive streams. All 23 watched prior ZIP/live workspace JSON files unchanged; final 176 tests, zero failures/errors/skips; whitespace check PASS. Final extracted UI verified Edit, collapsed history and full explicit conflict choices without modifying a synthetic event. Self-contained EXE: C:/Users/mrove/Projects/band.logger/band.logger/dist/band.logger-0.8.0-windows-x64/band.logger/band.logger.exe. Source remains uncommitted for GitHub Desktop review.