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
