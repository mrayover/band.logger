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

Pending: owner review of Chunk 1; real iPad proof import; final SVG use and public repository status; final icon; application license; signing and permanent GitHub Release distribution.
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
