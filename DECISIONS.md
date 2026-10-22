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

# Decisions

- 2026-10-20: Keep the existing repository at C:\Users\mrove\Projects\band.logger\band.logger. The two planning files already contained this correct path; explicitly reaffirm it.
- Every chunk delivers a runnable Windows executable with dependencies, preferably ZIP. Owner runtime review is part of acceptance. No automatic progression to the next chunk.
- Follow Kotlin Multiplatform/Compose. Pin Kotlin 2.2.20, Compose 1.9.3, serialization 1.9.0 and Gradle 8.14.3 as a stable compatible baseline, rather than requiring an IDE.
- Install build tools locally in ignored .tools; do not change machine-wide Java settings. The owner does not need development tools to run the ZIP.
- Use versioned JSON for the small Chunk 1 metadata workspace, with validation and previous-save backup. A database migration may be appropriate when catalogs/export history arrive.
- Provide a compatible Android foundation through commonMain and platform interfaces; defer Android application/UI/SDK packaging. Android compilation is not claimed.
- Use a portable Windows app image, avoiding installer tooling and admin installation. This is an unsigned x64 preview.
- Use editable Quaternity defaults as a convenience action. No Quaternity-specific rule is embedded in generic project validation.
- Resolve the conflicting Jordan default in the planning documents using the detailed Chunk 1 table: Bb, then C. The preference remains editable.
- Bundle actual Lato regular/bold files and SIL OFL. Use a plainly labeled text brand placeholder per the explicit user request. The separately mentioned SVG remains untouched; its integration/publication is pending.
- Optional logo reference is stored as metadata; rendering arbitrary SVGs is deferred until artwork integration.
- Generate .4ss directly from the official XML format using synthetic PDFs only. Device verification is pending.
- No commits, pushes, releases or uploads are performed on the owner's behalf.

Current pending decisions: final SVG use and public repository status; final icon; application license; signing and permanent GitHub Release distribution. Chunk 1 synthetic iPad diagnostics passed and Chunks 1 and 2 are owner approved; production master import remains a Chunk 3 verification gate.
## Verified iPad import — October 20, 2026

Owner confirmed that BOTH diagnostic files imported successfully after transfer as a ZIP through Google Drive, saving locally on the iPad, extracting in Apple Files, and opening the extracted .4ss in forScore. Test A verified setlist/placeholder creation. Test B verified a setlist with an embedded synthetic PDF. The synthetic open-format import gate has passed for these diagnostic fixtures on the owner's iPad. Production chart export and the original two-entry special-character proof are not independently established by these results.

Use ZIP as the default transfer packaging for future band.logger exports: ZIP → Google Drive → save locally on iPad → extract → share extracted .4ss to forScore. The ZIP contains the actual .4ss; it is a transport wrapper, not a different forScore format. Direct Drive handoff failed in the owner's tests; file corruption or conversion by Drive has not been proven.

This supersedes earlier notes that diagnostic iPad import was unresolved. No application code or executable changed in this documentation update. The current Chunk 1 export button still saves a bare .4ss; automatic ZIP export remains to be implemented. Chunk 2 remains unauthorized pending explicit owner approval.

## Chunk 2 decisions — October 20, 2026 (supersedes pending authorization above)

- Owner explicitly approved Chunk 2 after both ZIP/iPad diagnostic tests passed. Chunk 3 remains gated.
- Connect to Google Drive for desktop via a chosen local folder; let Drive upload/sync. No direct cloud uploader or authentication added.
- Owner's later clarification controls the chart transfer: COPY ONLY, retain every original in sorted. 249 PDFs copied and SHA-256 verified; private receipts remain outside Git.
- Manual Refresh is the deterministic discovery mechanism for this chunk. Recursive scanning includes subfolders and ignores symbolic links.
- Conservative normalization: case and repeated whitespace merge; punctuation/diacritics remain distinct pending review.
- New recognized names can be accepted in bulk. Conflicts and changed files need explicit resolution. Catalog metadata edits leave filenames/PDFs untouched.
- Preserve prior metadata/checksums for changed files; this is not a PDF backup system.
- Schema 2 adds per-project catalog data, migrating schema 1 and preserving an immutable pre-migration backup.
- ZIP wrapper is now the default for the synthetic forScore proof. No library master/supplement implementation yet.
- Keep the previous Windows ZIP for rollback. Remove only clearly generated temporary build copies when safe; retain development tools and private chart originals.

## Chunk 2 acceptance and usage-conscious planning — October 20, 2026

- Owner accepted Chunk 2 and requested a plan before implementation of the next phase. Current approval supersedes earlier pending Chunk 2 review notes.
- Plan Chunk 3 in three bounded stages with tested Windows previews and concise handoffs. This does not lower full Chunk 3 acceptance requirements or authorize Chunk 4.
- Reuse member preference order, the existing forScore generator, per-project catalog and verified ZIP transfer route. Make fallbacks and missing/ambiguous charts visible before generation.
- Preserve chart-review decisions: owner acceptance of the application does not automatically accept all indexed chart records for production export.
- Active-work estimates do not predict Codex quota consumption. Reassess after each stage rather than promise the whole chunk fits in a remaining allowance.

## Stage 3A — October 20, 2026

- Owner authorized 3A only. Implement member allocation preview and destination persistence; no production generation or master records.
- Use one chart per tune/member, ordered preferred parts, then independent highest revision for that part. Block unresolved latest preferred charts instead of silently using older data or a fallback. Explicit exclusions can be skipped; their records remain visible.
- Normalize supported preference labels case-insensitively. Any unsupported label blocks allocation with a visible explanation, preserving the owner's intended order.
- Add optional destination metadata within schema 2. Older saved data remains readable; warn that older application saves can discard the new field.
- Version 0.3.0 is a Stage 3A preview candidate, not full Chunk 3. The accepted 0.2.0 ZIP is retained.
- Local Windows Smart App Control blocked the unsigned candidate. No Windows security changes or per-app workarounds were made. Trusted signing is now an unresolved delivery dependency. Do not call 3A fully accepted until executable launch and owner review pass.

## Stage 3B delivery

- Owner disabled Smart App Control and authorized 3B; agent made no security-setting changes. Previous launch blocker is resolved and executable tests passed.
- Explicit acknowledgement of all member selections/fallbacks/omissions before generation. Zero-chart members receive no ZIP; no misleading empty package.
- Preserve source bytes and previous output. Stream XML per PDF, verify hashes, stage files and publish a new folder. Store immutable chart/member identity snapshots for later supplements.
- Schema 3 protects saved masters from older application writers and preserves a pre-migration backup.
- Recover completed output after metadata-save failure instead of regenerating it. Recovery is checksum-checked and idempotent.
- 0.3.1 is the usable Windows Stage 3B delivery. Production iPad compatibility remains owner-verification work. Stop new work before the user's usage reserve is exhausted; finalization began at 9% remaining.

## October 21, 2026 — Homepage scope and owner verification

- Preserve the owner's existing accent-filled selected-project button (40bbc29); log it without reimplementing it.
- Use equal 184 dp cards, 16 dp gaps, centered vector icons and bundled Lato; collapse to one column below 500 dp content width. Clock for rehearsal and calendar for upcoming events follow the written request where the reference image differs.
- Future event cards show explicit Coming soon dialogs. Pending Distribution is an honest explanatory navigation destination, not an empty queue suggesting tracking exists. Link to the already functional Library master.
- No new data schema, dependencies, event/setlist editors, supplements or cover pages.
- Owner reports successful actual Library Master import and tunes present in forScore on iPad. Do not infer additional tests or annotation preservation.
- Deliver as 0.3.3 because a local 0.3.2 archive already existed despite source version 0.3.1. Guard against overwriting existing version ZIPs; retain versioned checksums. No commit or push.

## October 21, 2026 — Pending Distribution decisions

- Reuse the one catalog and MasterPlanner. No second library, migration, source modification or acceptance changes.
- Latest valid non-empty master per member; normalized UTC timestamp then ID breaks ties deterministically. Skipped members retain prior valid baselines. Historical exports are not erased or merged into a cumulative full-master baseline.
- Compare tune identity, selected part, revision and SHA-256; filenames/timestamps alone never establish distribution status. Equivalent duplicate/renamed chart records are unchanged when musical identity and bytes match.
- New part means a selected part never exported for this member/tune; switching back to an already exported part is a revised selection. Missing/unapproved/conflicting preferred charts block rather than silently fall back.
- Read-only live source checks run on entry/recheck and cannot save catalog state. Summary counts member–tune selections, with initial-master selections excluded. Generated exports do not imply imported/received.
- Deliver 0.3.4 without overwriting previous ZIPs. Optional Supplemental Export Preview deferred to protect delivery reserve. Latest usage checkpoint jumped from 36% to 13% remaining during UI checks; no additional features started.

## October 21, 2026 — Event foundation decisions

- The owner's current request takes precedence over the original rehearsal/setlist deferral for this minimal scope. The sketch informs later design; only title/type/date, catalog selection, one initial set, save and reopen are implemented now.
- Reuse project-owned JSON metadata and stable catalog tune references. No database replacement, PDF copies, chart acceptance changes, alternate catalog or master/pending behavior changes.
- Schema 4 prevents older writers dropping event metadata. Preserve original schemas 1–3 in an immutable pre-events backup on first write, retaining older backups. Migration alone does not write disk.
- One tune occurrence per event in this increment; append/remove only. Empty/undated events are valid; title/type and real optional YYYY-MM-DD date are validated. Event/type/date edits and retained tune entries keep all identities.
- Upcoming Events is the entry point to all saved events, including past/undated, so no saved record becomes inaccessible. No event deletion or calendar filtering in this scope.
- Explicit save with dirty indicators and save/keep/discard navigation/close guards. Mark clean only after storage succeeds. No automatic crash-recovery draft store; unsaved edits can be lost on forced termination/power failure.
- Select tunes with active current-source chart records; planning does not imply chart approval or distribution eligibility. Retain/warn about selections without active charts rather than silently dropping them.
- Deliver 0.4.0 without overwriting any prior version ZIP. Full setlist builder, multiple sets, ordering controls, drag/drop, templates, event/member .4ss/ZIP exports and cover pages require subsequent authorization.

## October 21, 2026 — Multiple-set decisions / 0.4.1

- Owner explicitly authorizes two-column building, multiple sets, repeated tunes and Up/Down controls; the screenshot sets the layout direction. Keep all export/template/cover-page/drag/drop work deferred.
- Extend the existing serialized event with ordered additionalSets rather than replace its initialSet. Schema 5 prevents old writers losing added sets; legacy event/set/entry IDs remain intact. Add immutable pre-multisets backup and retain every previous backup.
- Compute Set N from current ordered position. Keep at least one set; non-empty removal requires confirmation. Removing an active set resolves to the first remaining set; adding automatically selects the new last set. Do not impose a small set-count limit.
- Repeated catalog tunes have independent occurrence IDs; movement/removal affects the chosen occurrence only. Cross-set movement is remove/add in this scope.
- Use 38/62 desktop panels, independent lazy scrolling and a compact fixed header; stack below 700 dp editor content width. Preserve existing project sidebar/theme/Lato and all other screen scroll behavior.
- Destination/search/scroll/window state remains transient; reopen selects the first set. Stored event data/order and existing explicit save/dirty/close protection remain durable. No autosave or crash recovery added.
- Deliver unused version 0.4.1, preserve prior successful ZIPs byte-for-byte, retain GitHub Actions and leave Git operations to the owner. Next is owner review, then separately scoped reference-only forScore export planning/identity-matching validation.

## October 21, 2026 — Member event-export decisions / 0.5.0

- Owner-approved 0.4.1 baseline is clean commit 84f9204. Current task authorizes event exports only; preserve the existing multi-set editor and schema 5.
- Reuse PendingPlanner's exact latest valid non-empty master baseline by current member ID. Record filenames/fallbacks govern references; never allocate from newer local revisions or infer supplements/installation. Expose the helper to share semantics rather than maintain a parallel rule.
- One complete reference-only .4ss per member with official Set N placeholders. Preserve every repeated occurrence. Skip/warn empty sets; reject no songs/no members. Unsafe/missing filenames become missing-reference placeholders after acknowledgement. No-baseline output requires explicit acknowledgement too.
- Naming uses saved event date/title, current member name and first usable preferred part. Preference describes the package, not every included part. Undated requires separate acknowledgement. Sanitize/truncate Windows labels, retain original event metadata and suffix member-label collisions/new repeat-export folders.
- Use existing configured export root by default with Events subfolder. A dialog override is transient. Stage/validate all member ZIPs before publishing; never overwrite old packages. No direct Drive API, PDF IO/embedding/copying, chart acceptance/distribution updates or new export-history schema.
- Retain filename-matching caveat: master generation is not proof of installation. Owner must verify the new reference-only workflow on iPad; previously approved embedded Library Master does not establish placeholder/repetition/reference behavior here.
- Deliver new unused 0.5.0, preserve all earlier ZIP hashes and GitHub Actions. No commits/pushes/releases. Next task is owner iPad validation before further export scope.

## October 21, 2026 — ZIP validation defect / 0.5.1

- Treat the owner's 0.5.0 failure as a provider integration defect despite passing local tests. Reproduce at the actual Drive destination with synthetic files; never edit chart files or real event data for diagnosis.
- Complete reference-only ZIPs in memory and validate exact entries/XML; verify the written bytes sequentially rather than immediately trusting cloud-provider random-access file length. Preserve CRC/XML checks and no-overwrite staging.
- Add regression coverage for inaccurate stream availability and truncated/changed/extra copies. Extend executable integration smoke to an isolated G: output while keeping synthetic PDFs local.
- Release unused 0.5.1 and retain every prior ZIP. Additional Drive UI write was denied by automatic approval review and not retried; safe local UI alternative completed. No optional features or schema changes. Owner retry and iPad verification remain next steps.

## October 21, 2026 — Supplement decisions / 0.6.0

- Owner reports successful actual iPad event import from 0.5.1; record that without claiming new supplement-device verification. Baseline clean 9f284c9.
- Cumulative supplemental tracking is per member/tune/part, anchored to the latest valid nonempty initial master. Keep independent delivered part revisions, exclude unchanged bytes/revisions and retain all old receipts. Event exports remain master-only as explicitly excluded.
- Reuse master accepted-selection/source/hash/size/XML/ZIP/staging/recovery infrastructure. Publish only eligible affected members after exact owner review; warn/exclude unresolved selections/no-master members rather than fabricate a baseline or downgrade.
- Consecutive numbers are project history, with permanent stable project folders under Supplements. Fixed numbered destinations and a durable local pending journal prevent accidental duplicates even when metadata saving fails or destinations/names change. Recover complete packages instead of regenerating them.
- Schema 6 protects supplement records from older writers; immutable original schemas 1–5 backup before first save. No number/state advancement for failed/incomplete output, no receipt/import inference, no source PDF modifications.
- Deliver unused 0.6.0, retain all older archives/checksums and GitHub Actions. Next: owner supplement/iPad review, then separately authorize event reference resolution from cumulative history. No unrelated features or Git operations.

## October 21, 2026 — Cumulative event references / 0.6.1

- Integrate existing records rather than add a distribution database/schema. Keep latest valid nonempty master selection unchanged, then apply only recorded supplements anchored to that member's exact master ID in validated number/predecessor order. Timestamps do not order supplements.
- Preserve tune/part delivery history independently; use the latest applicable recorded tune selection for event references. Current preferences are descriptive only; no manual overrides, inferred delivery, local-revision substitution or PDF reads.
- Validate/replay existing supplement chain/anchor rules and stop on inconsistent records. Preserve no-master/missing/unsafe filename placeholders and separate Undated acknowledgement. A later master excludes old anchored deltas only for its affected members.
- Preserve one whole-event reference-only .4ss/member, XML format, exact filenames, all sets/repeats, naming/destinations/cloud ZIP validation and no-overwrite behavior. Provenance appears in existing preview and instructions; exported remains distinct from installed.
- Owner-reported production master/Eden event/Night Dreamer supplement iPad imports and persistent history are successful. Verify actual Supplement 001 allocations read-only in isolated copied records; do not regenerate production packages or edit live events. Require owner check of new integration on iPad.
- Deliver unused 0.6.1 with all prior builds/workflow intact, full tests/smoke and schema 6 unchanged. No optional functionality or source-control operations.

## October 21, 2026 — File organization / 0.7.0

- Treat requested staging/default Events directories as chooser/settings preferences, never authoritative chart-library replacements. Existing PDF copy/review behavior is unchanged; unavailable defaults fall back only when unconfigured.
- Persist optional Events root per project, schema 7 with immutable pre-event-destinations backup of schemas 1–6. Explicit project setting or Remember action saves; per-export override is transient otherwise. Unavailable explicit roots fail clearly; route by saved type under Gigs/Rehearsals without string replacement of library paths.
- Future supplements use readable sanitized project/member names and consecutive number; reserve readable folder ownership with stable ID marker. Only genuine project-name collisions use short stable suffixes, and member-name collisions use numeric suffixes. Preserve timestamp/hash/ID in manifests.
- Keep old output names/paths/receipts immutable and recovery independent of current naming. Block old-convention completed/unrecorded targets and preserve existing journals/numbering/no-overwrite protections across renames and destination changes.
- Preserve embedded-PDF library distributions and reference-only whole-event exports, all selections/sets/repeats, current catalog identities and workflow. Deliver unused 0.7.0, retain all earlier packages. Owner verifies actual Drive roots/imports; test only synthetic local data. No unrelated work or Git operations.

## October 22, 2026 — 0.8.0 decisions

- Use exportDestination as the single future root. Retain eventsExportRoot solely as a compatible fallback and discovery location; never silently rewrite historical paths. Successful event generation remembers its selected root. Other exports reuse it.
- Keep completed names readable: ISO event date/title; numeric Revision N conflicts; Library/Master and Library/Supplements without the redundant project folder. Library ownership is an internal marker; shared roots across projects fail safely rather than appending hashes.
- Replace means publish and track the new revision first, then remove old verified tracked packages. It does not overwrite bytes in place. Persist cleanup state before destructive work; preserve progress/errors through restart. Delete only manifest-proven direct files; foreign/changed files block cleanup.
- Maintain deleted-export tombstones for diagnosis/retry evidence. Keep event snapshot independent of the live event so removal does not orphan cleanup metadata. Existing event editing/save guards remain the boundary.
- Legacy filenames and READ-ME text cannot establish stable event identity. Show them for review; do not auto-adopt or delete them. Layout migration is a read-only review and future-only policy, not a bulk mover. Historical distribution recovery uses exact original paths.
- Preserve sequential cloud ZIP validation. Real Drive integration uses only newly created synthetic output under 06 - Testing, never Eden or production chart/library packages. Device/cloud synchronization verification remains separate from local Drive filesystem success.

Drive cleanup detail (0.8.0): Drive for desktop may inject a hidden UTF-16 desktop.ini after publication. Only its bounded, exact known GoogleDriveFS icon/ViewState profile is recognized; arbitrary desktop.ini or other files still block deletion. Persist the exact Base64 bytes in providerMetadata before removing that decoration, retaining them in the completed tombstone. Changed profiles remain for review. Synthetic local regressions and actual Drive retry passed with no event folders/ZIPs remaining.

## October 22, 2026 — Branding and cover decisions / 0.9.0

- Treat the supplied SVG as authoritative. Bundle and hash its exact bytes, render offline without recoloring/reconstructing/distorting the artwork, and place it on ivory so its black geometry remains legible. Keep Lato and existing project color/highlight/navigation behavior.
- Persist brand identity by project, never infer it repeatedly from the current name. Initialize the Quaternity starter; migrate exact legacy Quaternity/Q once; allow explicit per-project opt-in/opt-out. Other projects get no automatic logo. Preserve logoReference metadata without external reads.
- Store the optional cover choice per project, off by default until device acceptance. Each cover replaces one nonempty set heading; all music stays reference-only and the one ZIP/one complete .4ss transport remains unchanged.
- Use PDFBox 3.0.8 with embedded Lato and a lossless render of the exact source SVG. CoverGenerator accepts project/event/set metadata and supports text identity for other brands. No scheduling/time fields are added or invented.
- Whitelist generated cover PDF bytes during XML validation. Never permit arbitrary embedded music data. Keep sequential cloud ZIP verification and complete staged publication/recording/deletion behavior.
- Use readable stable cover filenames; do not append hashes/UUIDs. The official forScore format adjusts conflicting embedded filenames, so warn that repeated import may duplicate covers and do not claim deduplication or iPad library cleanup. This remains an owner device gate.
- Schema 9 and immutable pre-branding-covers backup preserve every earlier record/path/backup. Add optional cover receipts without rewriting historical packages. Package unused 0.9.0; no Git operations or next-stage features.


## October 22, 2026 — Event history and delivery decisions / 0.10.1

- Extend the existing event, not a separate notes store/archive. Explicit save protects notes/status just like setlists; notes never cross export boundaries.
- Status is explicit: Planned by default even on old dates; Completed only after confirmation; Canceled retains data. Use Upcoming/History in Events, gig/rehearsal filters and recorded-date chronology; no date-based completion inference.
- Capture immutable performed facts only through completion/correction. Ordered sets and stable entry/tune IDs preserve repetitions independently of event counts; snapshot labels/title/type/date/test classification resist later plan/catalog edits. Corrections append revisions. Reclassification of an existing record's test flag likewise needs explicit correction; only completed non-test snapshots are countable. No analytics/reporting interfaces.
- Schema 10 reads 1–9 with defaults and immutable pre-event-history backup; preserve all earlier backups, IDs, export records and schema-9 branding opt-outs.
- Sanitize internal metadata in EventExportPlanner and omit the fields from sidecar serialization. Reject external manifests containing private internal values. Keep reference-only music and optional cover behavior unchanged.
- Local builds permanently publish only new versioned extracted folders with a direct executable and bundled runtime. Verify archive inventory/hashes and smoke/restart before advancing a stable shortcut atomically. Never overwrite old/running program images or place user data in them. Preserve last working current target on failure; retained staging supports diagnosis/retry. CI retains ZIP-only packaging.
- Deliver 0.10.1 without committing/pushing, production modifications or future analytics/Archive/audio/venue features.
