# Chunk 3 plan — Library master exports

Prepared October 20, 2026, after explicit owner acceptance of Chunk 2. The owner subsequently authorized 3A. Its code, tests and development UI are implemented; Windows executable launch is blocked by Smart App Control and owner acceptance remains pending. 3B/3C are not started. Chunk 4 remains outside scope.

## Estimate and usage approach

Estimated active development time: 2 hours 15 minutes to 3 hours 45 minutes, plus owner review and actual iPad importing. Allow up to another hour if testing exposes export or device compatibility problems. These are planning estimates, not a quota guarantee. Account usage measures computation, not elapsed working minutes; it cannot reliably be converted into development hours.

Use three bounded stages. At each stage, run relevant tests, preserve existing behavior, update CURRENT_STATE and WORK_LOG, and deliver a runnable self-contained Windows ZIP with a short checklist. An intermediate package is a progress preview, not full Chunk 3 acceptance. Preserve the 0.2.0 package for rollback. Stop at a completed stage when requested or when the next session would be more appropriate; leave a precise handoff so work can resume without re-reading the entire conversation. Do not omit safety tests to save usage.

## Stage 3A — Selection and preview (45–75 minutes)

- Add Create Library Master and a per-project export destination setting.
- Build deterministic member allocations from accepted, current, available catalog records. Propose the highest eligible revision for each part, then apply each member's ordered part preferences.
- Show primary selections, explicit fallback selections, missing parts, unresolved reviews/conflicts and exclusions before confirmation. Do not silently select an ambiguous chart. No song-specific assignments.
- Show each member's chart list and counts, with warnings that explain omissions and fallbacks. Require explicit confirmation of the preview before generation.
- Test preference order, independent part revisions, missing/fallback behavior, deterministic ordering and project isolation.
- Deliver preview-only Windows package and checklist; it must clearly indicate that production generation is not yet available.

Exit: the owner can inspect who would receive which charts and why. No production master is recorded.

## Stage 3B — Generation and manifests (60–90 minutes)

- Generate one named .4ss per member using the existing XML generator and actual selected PDFs.
- Wrap each member's .4ss and plain-language iPad instructions in its own ZIP, using the verified Drive → local extraction → forScore route.
- Write a project/date-named master folder under the chosen destination. Refuse collisions and keep source PDFs untouched.
- Validate source paths and recheck file content against the preview before embedding. Report changes or missing files and require refresh/review instead of exporting a stale selection.
- Stage output safely, detect incomplete/failed exports, and persist a manifest only with a clear successful output result. Define recovery for a failure between writing files and saving metadata; do not silently claim distribution success.
- Store project/member/tune/chart/part/revision/hash identities and output details for future supplements. Generation is not proof that a musician imported anything.
- Test XML parsing/escaping, decoded PDF byte equality, manifest accuracy, changed/missing sources, collisions and partial failure handling.
- Deliver a usable Windows ZIP and member export ZIPs outside Git; no automatic uploading or publishing.

Exit: actual accepted charts export with accurate persistent manifests and preserved originals.

## Stage 3C — Verification and handoff (30–60 minutes)

- Run all automated tests, compile/package, and smoke-test an extracted Windows executable with its bundled runtime.
- Verify existing project/member/theme/catalog/import functionality and persistence. Exercise the real catalog without automatically accepting or reclassifying owner charts.
- Inspect all four member allocations. Use a small real-chart package for the first iPad test before distributing the full library. Keep private PDFs/packages out of Git.
- Provide a checklist for preview, fallback/missing-part review, destination persistence, ZIP contents, restart and opening charts in forScore.
- Update README, BUILD_CHUNKS, CURRENT_STATE, WORK_LOG, ARCHITECTURE, DECISIONS and MANUAL_TESTING. Leave changes for GitHub Desktop; no commit/push.

Exit: deliver the Windows app and export instructions. Production compatibility remains awaiting owner verification until at least one actual library package successfully imports into forScore and its charts open. Full acceptance requires owner review. Do not start Chunk 4 automatically.

## Dependencies and uncertainties

- The last saved catalog had 247 recognized new records and one ambiguous Bb-2 filename still needing review after one sample was accepted. Recheck live state before implementation; never auto-approve entries merely to create a larger export.
- Ordered preferences already exist. Default behavior will select one best available eligible chart per tune/member and visibly identify any fallback. Decisions that materially change this workflow must be discussed before implementation.
- Production package sizes, large-library memory behavior and actual iPad import time need measurement. A small verified export comes before full distribution.
- Reuse the existing pinned toolchain, XML generator, ZIP transfer and Windows workflow. No new cloud service, Google OAuth, Android app, supplement UI or setlist planner is included.

## Stage 3A checkpoint

Implemented: selection/preview, warnings, file issues, destination storage, tests and compiled 0.3.0 image. 42 tests and ZIP integrity passed. Windows launch gate remains blocked by Smart App Control, so the candidate is not a fully accepted delivery. See CURRENT_STATE.md for exact evidence and signing references. Resolve this release blocker before advancing to production exports. The original time estimates above predate this external signing dependency and do not cover certificate enrollment.
