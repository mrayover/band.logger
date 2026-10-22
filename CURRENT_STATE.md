Delivered-executable visual verification PASS: opened dist/band.logger-0.11.0-windows-x64/band.logger.exe against a fresh isolated copy of the migrated workspace. Observed Quaternity identity, saved master (4 packages/466 chart selections) at 02 - Repertoire Distributions/Master; next Supplement 003 at 02 - Repertoire Distributions/Supplements/Supplement 003; restored Eden Wine Lounge with 2 sets/18 occurrences and saved project-root/gig output preview at 03 - Events/Gigs/2026-10-21 Eden Wine Lounge without browsing. Covers preference remains off for this production event; no package generated. Pending Distribution shows 474 unchanged member-tune selections, no new/revised selections and zero file issues. Six pre-existing missing-part allocation exceptions (Hang Up Your Hang Ups / Running Out of Time 2 for non-Bb players) are retained for owner review; no allocation/artwork/source edits were made.

# 0.11.0 release verification — October 22, 2026

Final normal scripts/build-local.ps1 PASS: delivery safeguard checks, complete 217-test suite (zero failures/errors/skips), Windows runtime/image packaging, image and extracted-ZIP smoke/restart, automatic flat extraction and atomic current-build shortcut publication. Independently compared all 211 archive files against the delivered extraction; all match. All six native diagnostic markers are present, including numbered routing/restart and event history/restart. Prior 23 watched release/artwork files and all 253 chart PDFs remain unchanged. Restored live workspace hash remained unchanged throughout isolated UI/package diagnostics.

Ready executable: C:/Users/mrove/Projects/band.logger/band.logger/dist/band.logger-0.11.0-windows-x64/band.logger.exe. Current shortcut: dist/Launch current build.lnk. ZIP: dist/band.logger-0.11.0-windows-x64.zip (65,805,668 bytes); SHA-256 393E3C396C4A5443F2CCB232A95B62F226CAF663AAD10F17B756D6D71FBDE105. Evidence: .tools/local-delivery-0.11.0.json, .tools/drive-routing-0.11.0/final-local-build.log, final-release-verification.json and final-preservation-check.json. No commits/pushes; hosted Windows result awaits owner push. Drive cloud synchronization is not established by local filesystem checks.

Fresh Windows checkout verification PASS: isolated Git checkout with core.autocrlf=true retained the exact SVG bytes; scripts/package-windows.ps1 ran all 217 tests (zero failures/errors/skips), created the bundled runtime/image, passed native smoke/restart, packaged ZIP, and passed extracted ZIP smoke/restart with no developer Java. TEMP/TMP used a genuine Windows 8.3 alias. Evidence: .tools/drive-routing-0.11.0/fresh-windows-packaging.log and fresh-windows-verification.json. This is local reproduction of the Actions entry point; hosted acceptance still requires owner push.

# Numbered Drive layout / 0.11.0 — October 22, 2026

Permanent convention: each opted-in project has one projectRoot. Quaternity uses G:/My Drive/Quartet/band.logger. Incoming PDF selection starts at 00 - Inbox; the existing authoritative source remains 01 - Chart Library. Masters use 02 - Repertoire Distributions/Master (later Revision N); supplements use 02 - Repertoire Distributions/Supplements/Supplement NNN; gigs and rehearsals use 03 - Events/Gigs or Rehearsals/YYYY-MM-DD Title. Test events use 06 - Testing with the same type/date layout. 04 - Catalog & Backups and 05 - Archive are reserved; no new backup-export or automatic archival feature was introduced. No redundant root-level Library/Events/Gigs/Supplements or project-hash namespace is created. Other projects retain independent settings; legacy projects retain historical paths until explicitly configured/migrated. Private workspace stays in %LOCALAPPDATA%/band.logger.

Actual migration completed: the verified master (4 member ZIPs) and Supplements 001/002 (4 ZIPs each) moved to those destinations. All 12 distributed ZIPs are byte-identical; only owned manifests/path records were rewritten. Original IDs, selections, supplement chain, recovery and distribution history remain. Removed only resolved empty legacy containers, after backing up their Drive desktop.ini. All 253 authoritative chart PDFs and 301 unmigrated files (including those charts) were hash-verified unchanged.

Recovery finding: two old 0.3.1 applications had been open; the current workspace had regressed to schema 2 with no distribution/event history and regenerated catalog IDs. The owner closed the old windows. A newer immutable pre-event-exports backup contained the original 253 charts, 120 tunes, master, two supplements, two planned events and accepted review metadata. Verified every current chart path/hash against that backup before restoring its original catalog identities and retained history. Preserved both original workspace versions and an identity mapping. No event completion was inferred from its date. Quaternity's chart source and the unrelated Solo project were preserved.

Durable migration backup/journal: %LOCALAPPDATA%/band.logger/layout-migrations/20261022-numbered-layout; phase DONE. Migration locks workspace, validates project/catalog/package ownership and hashes, stages copies, verifies bytes, atomically saves references, verifies recovery, then removes exact verified originals. It rejects occupied destinations/unknown content and changed workspace/source/target; interruption checkpoints resume safely. Drive directory enumeration can lag: required payloads are checked by explicit paths and streaming hashes, while listings reject foreign content. Provider desktop.ini is backed up and is not required as destination payload. Schema 11 reads 1–10 with an immutable pre-numbered-layout backup; existing backups remain.

Ambiguous legacy folders remain in place because they have no verifiable stable event-ID manifests. Filename similarity is insufficient for migration or association:

- `G:\My Drive\Quartet\band.logger\02 - Repertoire Distributions\Events\20261025 Test` (4 ZIPs).

- `G:\My Drive\Quartet\band.logger\03 - Events\Gigs\Events\20261021 Eden Wine Lounge` (4 ZIPs).

- `G:\My Drive\Quartet\band.logger\03 - Events\Gigs\.bandlogger-zip-check-21fa1457b4bf4984b36cc55f5e760e03` (1 ZIPs).

- `G:\My Drive\Quartet\band.logger\03 - Events\Gigs\.bandlogger-zip-check-396863d031844c38b865a7a4ad9d69d8` (9 ZIPs).

Validation: 217 regression tests pass, including migration interruption/restart, nested master revisions, numbered routes, supplement continuity, collision/foreign-content protection, changed payload/workspace rejection, provider metadata, schema backup and unchanged other-project settings. Native synthetic Drive smoke/restart passed under an isolated 06 - Testing child: master, supplement, gig, rehearsal, test event, branded covers, saved history, recovery and verified cleanup. Actual migrated workspace verified on restart; isolated UI copy showed saved Drive destinations automatically in master/supplement/event previews without a picker. No production package was regenerated. Local Drive verification does not establish cloud synchronization or iPad import success.

CI checksum investigation: downloaded failed Windows run 39821242502 test artifact. Exact LF source is 4113 bytes, SHA-256 7bbb4839c373391a24e9dce81ac7517e634c1428762fd2e884803150208af524. Windows CRLF checkout yields 4121 bytes and the exact reported failing hash 5960fa81ba672996dc685efc1963f858922ccf59534ee2fe3b45245673749a86. Reproduced using an isolated Git index with core.autocrlf=true. .gitattributes now marks only the supplied SVG -text, preserving its exact original bytes; the exact checksum assertion and artwork are unchanged. This was a checkout environment difference, not a rendering defect or outdated assertion. New rule also verified with true/false/input checkout modes. Hosted Actions acceptance remains pending the owner's push; no hosted green result is claimed.

Delivery target: dist/band.logger-0.11.0-windows-x64/band.logger.exe, automatically extracted and verified by scripts/build-local.ps1; prior releases preserved, current shortcut advances only after verification. No commits or pushes. Detailed local evidence: .tools/drive-routing-0.11.0/ (private workspace/journal evidence must not be committed).

---

# Current state — Event notes, performance history and automatic extraction / 0.10.1

Implemented private multiline Notes & status in the existing event editor, explicit Planned/Completed/Canceled statuses and Events Upcoming/History with type filters, recorded-date sorting, status/count/notes indicators. Completing an event explicitly captures immutable performed details with stable tune/set/occurrence IDs and label snapshots; corrections append retained revisions. Later planning edits do not change facts. Test snapshots and canceled/planned events are excluded from countable performance records. No analytics/advanced Archive.

Schema 10 reads 1–9 with Planned/empty defaults and no write on load; first save retains immutable workspace.pre-event-history.backup.json and all prior backups. Existing identity/history/path/branding opt-outs remain intact. Notes/facts/test/status metadata is sanitized out of exports and omitted from sidecars; existing forScore packages/covers/references and verified cleanup remain.

Permanent local build delivery now runs safeguard tests, standard packaging, hash/inventory verification, extraction and bundled-runtime smoke/restart before publishing a new dist/band.logger-VERSION-windows-x64/band.logger.exe and atomically updating dist/Launch current build.lnk. Old/running versions and ZIPs are preserved; failures retain the previous current build. CI still produces its normal downloadable ZIP. Data remains in %LOCALAPPDATA%/band.logger.

209 automated tests pass, zero failures/errors/skips. Native notes/history smoke and second-process restart/cleanup pass. Synthetic UI create gig > complete test snapshot > multiline post-show notes > guarded save > History succeeded. Final standard packaging, automatic extraction and ready-executable UI restart/correction verification passed. Full uncached normal build and a second full 209-test run under a real Windows 8.3 TEMP alias both passed. All 211 ZIP files match the final extracted image; 31 watched prior releases/source/live metadata files unchanged. Exact notes/event identity survive restart; a later 2-entry plan retained the original 1-entry fact record until explicit correction appended revision 2. A Windows shortcut-replacement null-path issue was fixed and regression-tested; prior current target survived the failure. Safe VerifyExisting recovery rechecked every delivered byte and reran executable diagnostics before atomically advancing the shortcut. No commits/pushes or production-data writes.

Ready executable: C:/Users/mrove/Projects/band.logger/band.logger/dist/band.logger-0.10.1-windows-x64/band.logger.exe. Stable shortcut: dist/Launch current build.lnk. ZIP: dist/band.logger-0.10.1-windows-x64.zip, 65,742,640 bytes; SHA-256 6FE00B584827472C443310790A3847605AFB3A93182455CFD74739BF28F61B9E. Evidence: .tools/event-history-0.10.1/verification.json and .tools/local-delivery-0.10.1.json. The earlier 0.10.0 verification image/ZIP is retained. All seven requested docs/manual checklist updated. Hosted CI follows the owner's push; no remote success claimed.

---

# Current state — Vector desktop logo rendering, October 22, 2026 / 0.9.2

Desktop Quaternity marks now replay cached Skia vector commands directly into Compose's native canvas at the fitted physical-pixel draw size. The authoritative SVG is parsed once for the shared immutable Picture; no PNG encoding/decoding or bitmap resizing occurs in the UI. Existing 20 dp sidebar, 36 dp compact header, 64 dp project header and 120 dp branding preview dimensions, layout, aspect ratio, colors/strokes, transparency, accessibility description and project-specific selection remain. No artificial background, border, padding or extra clipping. Exact SVG remains unchanged (SHA-256 7BBB4839C373391A24E9DCE81AC7517E634C1428762FD2E884803150208AF524).

PDF review: the installed Skiko 0.9.22.2 API exposes no PDF document writer. Retained the existing lossless transparent 1332 x 1411 logo at 220 pt width (435.93 dpi), sufficient for normal printing/iPad display, without a new SVG-to-PDF dependency stack or artwork translation. CoverGenerator, page backgrounds, fonts, layout and forScore export behavior are unchanged. PDF logo remains raster at extreme zoom; the smallest desktop mark remains constrained by available physical pixels.

PASS: 196 tests, zero failures/errors/skips; new vector/reference RGBA comparisons across all four UI sizes at 100/125/150/200% densities, non-square fit and caller-canvas restoration; retained source hash/proportions and all existing cover/export assertions. Added PDF resolution/alpha/background assertions. Standard Windows build/package and app-image/extracted smoke/restart checks passed with bundled Java only. Actual extracted UI inspected using process-local display-scale overrides at 100%, 125%, 150%, 200%, including Home/sidebar, compact event editor, Theme branding preview and unrelated-project isolation. This does not claim physical multi-monitor transition testing. Independently rendered/inspected actual packaged cover and test-generator covers. All 211 archive files match extraction; 29 watched prior releases/source/live metadata files unchanged.

Delivery: dist/band.logger-0.9.2-windows-x64.zip; SHA-256 2EE2CEBF634647DD4118047DFD1087DDB224E76FB55BF26461E37B1441639D46. Evidence: .tools/logo-vector-0.9.2/verification.json, density-review.png, packaged-cover.pdf/png and pdf-review/. Prior ZIPs retained. No production data edits, commit, push or additional features; owner handles GitHub Desktop and subsequent hosted CI.

---

# Current state — Transparent Quaternity UI logo, October 22, 2026 / 0.9.1

Removed the artificial ivory background and 4 dp padding from the shared QuaternityMark component. Home/project headers, selected-project navigation and compact event-editor headers now display the original transparent logo directly on their existing surfaces. Full artwork proportions, caller sizing and native-image lifecycle remain intact. The source SVG and bundled SVG are unchanged (SHA-256 7BBB4839C373391A24E9DCE81AC7517E634C1428762FD2E884803150208AF524). CoverGenerator remains unchanged: it independently paints the ivory page and navy/gold decorations before placing the transparent logo.

PASS: full 194-test suite, zero failures/errors/skips; standard Windows packaging; packaged and extracted smoke/restart checks with bundled Java only. Visually verified Home/sidebar/compact editor in the actual extracted executable against a separate synthetic workspace. Independently rendered a PDF extracted from the packaged executable's synthetic export: intended ivory background and navy/gold bands remain intact. Evidence: .tools/logo-transparency-0.9.1. Delivery: dist/band.logger-0.9.1-windows-x64.zip; SHA-256 23B09A51B7A1A28CF1CAA574EE3401999F0263F7C429D34FC44DD16EBF37A945. Existing 0.9.0 ZIP retained. No production data edits, commit or push. Hosted CI awaits the owner's push.

---
# Current state — Official branding and optional embedded covers, October 22, 2026 / 0.9.0

Baseline local/remote main 75bee714a323c69109d7e3265f2719243e959924; owner reports prior Windows correction pushed/green. Implemented only the authorized official Quaternity SVG branding and optional embedded event covers. The exact supplied asset is bundled unchanged (SHA-256 7BBB4839C373391A24E9DCE81AC7517E634C1428762FD2E884803150208AF524), rendered offline at its original proportions on ivory. Home/project/sidebar/editor headers retain Lato, existing palette/highlighting/navigation. Branding is persisted per project; other projects get no automatic Q logo.

Export preview: Include branded set covers is off by default, remembered per project. Enabled: one generated Letter PDF per nonempty set replaces its heading in the same complete .4ss, followed by unchanged member chart references/repeats/missing placeholders. One ZIP/member still contains only that .4ss and READ-ME; no loose covers/per-set files or embedded music. Covers show actual logo, band name, saved event/rehearsal title/date, gig/rehearsal designation and Set N; no invented time. Plain fallback retains the established placeholder XML. Readable stable cover identities may acquire duplicate/renamed copies when re-imported by forScore; device behavior remains owner verification.

Schema 9 reads 1–8, retains immutable pre-branding-covers backup and all earlier backups/atomic saves, IDs, histories and exact historical destinations. Old sidecars without cover fields remain compatible. New receipts track cover set identity/number, filename, PDF hash and source-logo hash. Existing revisions/replacement, Drive-safe ZIP verification and verified retryable cleanup remain. PDFBox 3.0.8 dependency family is bundled with notices, along with existing Skia and Lato; runtime needs no external SVG/PDF application or network.

PASS: 194 tests, including full uncached Windows 8.3 TEMP regression run; independently rendered/inspected gig, rehearsal and long-title cover PDFs; local executable cover/plain/fallback/restart smoke; actual isolated Google Drive mixed cover/reference export and separate-process deletion; synthetic UI logo isolation, persisted toggle and four single-ZIP member outputs. Final standard packaging/extracted verification recorded in WORK_LOG/completion entry below. Original 27 watched production-workspace/prior-ZIP/source-logo files remain unchanged.

Deliver 0.9.0 self-contained Windows x64 ZIP through the established packaging system. Owner follows the latest MANUAL_TESTING iPad steps: import a synthetic two-set covered event, verify all cover fields/order/repeats/exact installed references, re-import once to observe cover duplicates, then test covers-off plain fallback. Unsupported Lato glyphs/excessive text can use plain headings. No new iPad/cloud-sync success claimed; no commits/pushes or excluded features/next stage.

---
# Current state — Windows CI path-identity correction, October 22, 2026

Reviewed clean main a2c27049472a0124476ffd04c8e62223a51f2609 and failed Windows application run 39806491249 (build job 119920983778), including its downloaded test-results artifact. The earlier 176-pass local report was valid only for that local filesystem environment; it did not establish hosted Windows success. All six CI failures were reproduced locally before correction by using a real Windows 8.3 temporary-directory alias.

Root cause: the hosted runner TEMP uses C:/Users/RUNNER~1 while Path.toRealPath resolves the same directory to C:/Users/runneradmin. toAbsolutePath().normalize() does not expand 8.3 aliases. Four test assertions incorrectly compared raw and canonical path spellings. Two implementation boundaries incorrectly did the same: event discovery rejected a valid export manifest, and legacy master/supplement recovery rejected its original folder. None of the six failures was an outdated export-layout expectation; no layout, IDs, schema or manifests were changed to fix them.

Corrections: canonicalize the existing discovery root and compare existing directory identities for tracked/discovered records; canonicalize the recorded original path during receipt recovery while retaining original-folder existence, identity, project/catalog/hash and containment checks. Four expected-path assertions now use the canonical configured root plus the exact expected child layout; filename/PDF/ZIP/recovery/preservation assertions remain. Fixtures deliberately keep their raw temporary paths so CI continues to exercise alias handling. Added copied-manifest and copied/missing-original recovery rejection tests, plus stronger import/discovery checks.

PASS: 178 tests, zero failures/errors/skips in each of three runs: forced uncached normal local run, forced uncached 8.3-path run, and fresh tracked-source-copy packaging run under short TEMP/TMP. The exact unchanged scripts/package-windows.ps1 used by Actions completed all tests, runtime/image generation, packaged --smoke-test and --event-restart-check, ZIP extraction with no developer Java/system-only PATH, and both extracted executable checks. All 207 archive files match extracted bytes; all 26 pre-session watched release ZIPs/live workspace JSON files are unchanged. No production charts or exports were used for destructive tests.

Corrected verification ZIP retained separately to preserve the original 0.8.0 delivery:
C:/Users/mrove/Projects/band.logger/band.logger/.tools/ci-reconciliation-39806491249/fresh source checkout/dist/band.logger-0.8.0-windows-x64.zip
61,967,016 bytes; SHA-256 6F4DB55C093D9D56CE280DFC94A7A81605963035118FEC031CC4D7235245FEDF.
Evidence: .tools/ci-reconciliation-39806491249/correction-verification.json and fresh-packaging.log. Version stays 0.8.0; this is a correction to that increment, not an additional feature stage.

Changes remain uncommitted for the owner's GitHub Desktop workflow. No commit, push, workflow dispatch/rerun or production-data migration was performed. The requested final acceptance gate is a successful hosted Windows application run on the owner's correction commit after push; that remote result is still pending. The old failed run cannot validate uncommitted changes. See the latest WORK_LOG entry for each failure and reproduction commands.

---
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
0.9.0 final delivery verification: standard scripts/build-local.ps1 packaged/extracted smoke and separate-process restart PASS, including bundled SVG/Lato/PDF generation and mixed cover/reference complete-event ZIPs. ZIP 65,663,131 bytes; SHA-256 F76F6359ED533FF7E37CEDCC50BE2704CDE91E00FB59185B1DA2297BBA5C2637. All 211 extracted files and the bundled SVG source hash verified; all 27 watched originals unchanged; final 194 tests, 0 failures/errors/skips. Final extracted UI confirms logo rendering and persisted covers-on preference/history. Source left uncommitted for GitHub Desktop; exact iPad acceptance steps shipped in MANUAL_TESTING.md. Actual import/re-import remains owner verification.