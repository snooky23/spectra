---
id: SPEC-codebase-cleanup
title: Spectra Codebase & Repository Cleanup
companions:
  - SDD.md
  - stories.yaml
sources:
  - docs/design/PRD.md
  - TASKS.md
---

> **Canonical contract.** This SPEC and the files in `companions:` are the complete, preservation-validated contract for what to build, test, and validate for the repository cleanup initiative.

# Spectra Codebase & Repository Cleanup

## Why

Across Phases 1 through 10 of Spectra development (including the KMP UI SDK consolidation and the Universal Okio I/O migration), various transitional scripts (`fix_imports_2.py`, `fix_imports_3.py`, `refactor.py`, `add_build_phase.rb`), scratch files (`test.kt`), build logs (`build-xcframework.log`), and leaked IDE caches (`examples/.idea/caches/`) accumulated in the repository. 

This cleanup project eliminates operational debt, hardens `.gitignore` rules against IDE leakage, purges obsolete compiler configurations, and leaves a pristine, production-ready codebase without affecting any core logging or UI functionality.

## Capabilities

- **CAP-1: Root Scratch & Cruft Deletion**
  - **intent:** The repository root is stripped of all transitional migration scripts, temporary test files, and stray build logs.
  - **success:** `fix_imports_2.py`, `fix_imports_3.py`, `refactor.py`, `add_build_phase.rb`, `test.kt`, and `build-xcframework.log` are removed from the filesystem and Git index without leaving orphaned dependencies.

- **CAP-2: Git & IDE Configuration Hygiene**
  - **intent:** Prevent IDE and local tool caches in subdirectories (such as `examples/.idea/`) from leaking into Git tracking while preserving required project configs.
  - **success:** `.gitignore` includes generalized patterns for nested `.idea/` caches; tracked IDE cache files like `examples/.idea/caches/deviceStreaming.xml` are untracked; stale module entries in `.idea/compiler.xml` are purged.

- **CAP-3: Build & Project Settings Harmonization**
  - **intent:** Build script configurations accurately reflect active project modules and ignore obsolete declarations.
  - **success:** `settings.gradle.kts` and root `build.gradle.kts` have stale or dead commented inclusions resolved and formatted according to project conventions.

- **CAP-4: Build Verification & Health Gate**
  - **intent:** The cleanup is verified against the multiplatform build and test suite.
  - **success:** Core unit tests pass, and multiplatform targets compile without missing file errors or unresolved references.

## Constraints

- **Zero API Impact**: No modifications to public APIs or behaviors in `spectra-core` or `spectra-ui`.
- **Preserve Production Assets**: Do not remove `SpectraFrameworks/` (required for SPM distribution), `_bmad/` configs, or `docs/`.
- **KDoc & Code Style**: Retain all comments, annotations, and formatting in production Kotlin and Swift code.

## Non-goals

- Refactoring internal logging pipelines, database/storage backends, or UI components.
- Upgrading Gradle, Kotlin (2.2.10), or Compose Multiplatform dependencies.
- Modifying release or publishing workflows in `.github/workflows/`.

## Success signal

Running `git status` on a clean checkout shows a streamlined root directory with only legitimate project assets, zero untracked scratch artifacts, and green compilation across all modules.
