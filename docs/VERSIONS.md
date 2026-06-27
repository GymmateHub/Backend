# GymMateHub — Document Versions (Central Registry)

Single source of truth for documentation versioning. **All documents share one baseline version.** GymMateHub is **pre-launch**, so every document is **v0.1**. Versions advance together; the first public release will be **v1.0**.

| Document | File | Version | Last Updated |
|----------|------|---------|--------------|
| Product Requirements Document (PRD) | [`GymMateHub_PRD_v0.1.md`](GymMateHub_PRD_v0.1.md) · [`.docx`](GymMateHub_PRD_v0.1.docx) | 0.1 | June 27, 2026 |
| Business Requirements Document (BRD) | [`GymMateHub_BRD_v0.1.md`](GymMateHub_BRD_v0.1.md) · [`.docx`](GymMateHub_BRD_v0.1.docx) | 0.1 | June 27, 2026 |
| Product State Report | [`PRODUCT_STATE_REPORT.md`](PRODUCT_STATE_REPORT.md) | 0.1 | June 27, 2026 |

## Rules

- **One version for the whole set.** Bump all documents together; do not version them independently.
- **Pre-launch = v0.1.** Increment to **v0.2, v0.3, …** for pre-launch revisions. First production release = **v1.0**.
- **Filenames carry the version** (`*_v0.1.md` / `*_v0.1.docx`). Rename all on each bump, then update this table.
- **Root `docs/` is the single source of truth.** Duplicate copies under `gymmate-backend/docs/` and `gymmatehub*/docs/` are slated for removal.
- Each document also keeps a short **Document History** section for its own change summary.

## How to bump the version

1. Edit content in the `.md` source files.
2. Update the `Version` / `Last Updated` fields and Document History in each `.md`.
3. Rename files to the new version suffix.
4. Regenerate the `.docx` (pandoc) at the new filenames.
5. Update this table.
