# Current state — Stage 3A, October 20, 2026

Chunks 1 and 2 are owner approved. The owner explicitly authorized "Implement 3a." Stage 3A code is implemented and tested, but Windows executable launch is blocked by Smart App Control. Stage 3A is not fully accepted; 3B, 3C and Chunk 4 have not started.

## Implemented

- All existing project/member/theme, library scanning/import/review and synthetic ZIP proof features remain.
- Library master preview shows each member's ordered part preferences, selected charts, fallbacks, missing parts and blocked tunes.
- Deterministic one-chart-per-tune selection uses the highest included current revision for the first available preferred part. A missing, unreadable, unreviewed or conflicting latest preferred revision blocks the tune; it does not silently downgrade or switch to a fallback. Identical-content duplicate paths are represented once.
- Explicitly excluded charts are not selected; unavailable and unclassified records appear in File issues. Unknown member preferences block selection with an explanation.
- Searchable allocation lists, an exceptions filter and per-member counts.
- Project-specific export destination with existing-folder validation, saved locally. No export folders/files or master records are created.
- Shared planning logic has no desktop filesystem dependencies. Preview uses saved catalog metadata; actual source-content verification and generation remain Stage 3B.

## Verification

- PASS: :app:desktopTest — 42 tests, zero failures/errors/skips (28 existing, 14 new).
- PASS: :app:createDistributable — Windows executable image and bundled runtime compiled.
- PASS: :app:run --args=--smoke-test in isolated development storage — persistence, fonts, XML, proof ZIP, catalog, member allocation and destination readback.
- PASS: development UI inspection via :app:run with isolated synthetic fixtures plus a copy of the real catalog. Verified primary/fallback choices, missing/review omissions, exceptions filter, file issues, destination save and project isolation. Copied Quaternity preview correctly showed 248 file issues and 117 classified tunes; no catalog entries were auto-accepted.
- PASS: archive extraction and SHA-256 comparison of all 207 files. Expected executable, runtime DLL, app configuration and checklist present.
- BLOCKED: scripts/build-local.ps1 stopped at the first packaged executable launch. Windows Code Integrity event 3077 named VerifiedAndReputableDesktop (Smart App Control), policy GUID {0283ac0f-fff1-49ae-ada1-8a933130cad6}, rejecting the unsigned 0.3.0 executable. No successful bundled/extracted executable smoke test is claimed.
- NOT RUN: remote GitHub Actions for these uncommitted changes. Existing workflow still requires tests and executable smoke checks.
- PENDING: owner Windows package review, production forScore import (3B/3C), Android compilation.

## Delivery and blocker

Candidate: dist/band.logger-0.3.0-windows-x64.zip (61,431,650 bytes at creation). Checksum: dist/SHA256-0.3.0.txt. Extract all files; the application entry point is band.logger/band.logger.exe. It includes its runtime and dependencies. The ZIP contains LAUNCH_STATUS.txt and MANUAL_TESTING.md explaining the incomplete launch gate. Existing 0.2.0 delivery remains available.

The owner confirmed they did not knowingly enable restrictions and want the agent to have access. Project authorization does not remove Windows signing enforcement. No policy, registry setting, security exclusion or certificate trust setting was changed. No purchase, signing enrollment or external upload was performed.

Microsoft describes trusted code signing as the supported path for the unsigned-app issue:
https://learn.microsoft.com/en-us/windows/apps/develop/smart-app-control/code-signing-for-smart-app-control
Smart App Control has no individual-app exception:
https://support.microsoft.com/en-us/windows/security/threat-malware-protection/smart-app-control-frequently-asked-questions

Resolve an approved signing/distribution approach and repeat the executable launch/package checks before calling 3A delivered and accepted. The unsigned candidate is not guaranteed to launch on this machine.

## Data and limitations

The production workspace was byte-identical before and after testing. Tests used .tools/stage3a-ui and .tools/stage3a-development-smoke; these are ignored local test data, not the owner's live workspace. No source PDF was moved, renamed or changed. The earlier copy-only transfer of 249 PDFs into Drive remains unchanged.

The last copied real catalog has 249 records, 117 tune groups, one accepted chart and 248 records needing review (including one Bb-2 filename). The owner should inspect/accept recognized new entries in Library before expecting broad allocations. Application approval does not approve catalog entries.

Schema 2 receives an optional exportDestination field. Older data loads with no destination; a save from an older application can discard this setting. Prefer the new version once its launch gate is resolved. Retain backups and the accepted 0.2.0 release.

No production master export, manifests, supplements, song-specific assignments, direct cloud uploader, Android app or SVG rendering was added. 3B/3C remain future stages. All changes are left uncommitted for GitHub Desktop.
