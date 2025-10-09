# Release Process Quick Reference

This is a quick reference for creating new releases of SpectraLogger.

## Prerequisites Checklist

Before creating a release, ensure:

- [ ] All tests pass (`./gradlew test`)
- [ ] Code quality checks pass (`./gradlew ktlintCheck detekt`)
- [ ] Documentation is up to date
- [ ] CHANGELOG.md is updated
- [ ] Version number follows semantic versioning

---

## Release Steps

### 1. Update Version

Edit `gradle.properties`:
```properties
VERSION_NAME=1.0.0
```

Commit:
```bash
git add gradle.properties
git commit -m "chore: bump version to 1.0.0"
git push origin main
```

### 2. Create and Push Tag

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release 1.0.0: [Brief description]"

# Push tag (triggers CI/CD)
git push origin v1.0.0
```

### 3. Wait for CI/CD

GitHub Actions will automatically:
1. Build XCFramework
2. Create zip file
3. Calculate checksum
4. Create GitHub Release
5. Upload artifacts

Monitor at: https://github.com/snooky23/Spectra/actions

### 4. Update Package.swift

After CI completes, go to the GitHub Release page and copy the checksum.

Update `Package.swift`:
```swift
.binaryTarget(
    name: "SpectraLogger",
    url: "https://github.com/snooky23/Spectra/releases/download/v1.0.0/SpectraLogger.xcframework.zip",
    checksum: "<CHECKSUM_FROM_RELEASE>"
)
```

Commit and push:
```bash
git add Package.swift
git commit -m "chore: update Package.swift for v1.0.0"
git push origin main
```

### 5. Announce Release

- [ ] Post to Twitter/X
- [ ] Post to LinkedIn
- [ ] Share in relevant Slack channels
- [ ] Update project website (if applicable)

---

## Manual Release (Alternative)

If you need to create a release manually:

```bash
# 1. Build XCFramework and prepare artifacts
./scripts/release/create-release.sh 1.0.0

# 2. Review the output in build/releases/1.0.0/

# 3. Create tag
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0

# 4. Create GitHub Release manually or using gh CLI
gh release create v1.0.0 \
    build/releases/1.0.0/SpectraLogger.xcframework.zip \
    --title "Release 1.0.0" \
    --notes-file build/releases/1.0.0/release-summary.txt

# 5. Update Package.swift (see step 4 above)
```

---

## Version Number Guide

### Semantic Versioning (MAJOR.MINOR.PATCH)

#### When to increment MAJOR (1.0.0 → 2.0.0)
- Breaking API changes
- Removed functionality
- Major architectural changes

Example:
```kotlin
// Breaking change: renamed method
// OLD: SpectraLogger.getLogger(category)
// NEW: SpectraLogger.createLogger(category)
```

#### When to increment MINOR (1.0.0 → 1.1.0)
- New features (backward compatible)
- New APIs added
- Deprecations (but not removals)

Example:
```kotlin
// New feature added
SpectraLogger.exportLogs(format: ExportFormat)
```

#### When to increment PATCH (1.0.0 → 1.0.1)
- Bug fixes
- Performance improvements
- Documentation updates
- Internal refactoring (no API changes)

Example:
```kotlin
// Bug fix: thread safety issue resolved
// No API changes
```

---

## Pre-release Versions

For beta/RC releases:

```bash
# Beta
git tag -a v1.0.0-beta.1 -m "Beta 1 for 1.0.0"

# Release Candidate
git tag -a v1.0.0-rc.1 -m "Release Candidate 1 for 1.0.0"
```

Users can install pre-releases:
```swift
.package(url: "https://github.com/snooky23/Spectra.git", exact: "1.0.0-beta.1")
```

---

## Rollback Process

If a release has critical issues:

### Option 1: Patch Release (Recommended)
1. Fix the issue
2. Release as patch version (e.g., 1.0.1)
3. Announce the fix

### Option 2: Yanking Release (Emergency Only)
1. Delete the GitHub Release
2. Delete the tag:
   ```bash
   git tag -d v1.0.0
   git push origin :refs/tags/v1.0.0
   ```
3. Communicate to users why the release was yanked

**Note:** Yanking is disruptive. Prefer patch releases.

---

## Checklist Template

Copy this for each release:

```markdown
## Release vX.Y.Z Checklist

### Pre-Release
- [ ] All tests passing
- [ ] Code quality checks passing
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Version number decided
- [ ] Breaking changes documented (if any)

### Release
- [ ] Version updated in gradle.properties
- [ ] Version committed and pushed
- [ ] Tag created and pushed
- [ ] CI/CD completed successfully
- [ ] GitHub Release created
- [ ] XCFramework uploaded
- [ ] Checksum verified

### Post-Release
- [ ] Package.swift updated with checksum
- [ ] Package.swift committed and pushed
- [ ] Release tested in sample project
- [ ] Release announced
- [ ] Close related issues/PRs
- [ ] Update project board/milestones

### Verification
- [ ] SPM installation works
- [ ] Checksum validation passes
- [ ] All platforms supported (iOS device + simulators)
- [ ] No regressions in example apps
```

---

## Common Issues

### Issue: Tag Already Exists
```bash
# Delete local tag
git tag -d v1.0.0

# Delete remote tag
git push origin :refs/tags/v1.0.0

# Create tag again
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

### Issue: CI Build Failed
1. Check GitHub Actions logs
2. Fix the issue
3. Delete tag (see above)
4. Re-create tag after fix

### Issue: Wrong Checksum in Package.swift
1. Download the zip from GitHub Release
2. Recalculate:
   ```bash
   swift package compute-checksum SpectraLogger.xcframework.zip
   ```
3. Update Package.swift
4. Commit and push

---

## Release Cadence

Recommended schedule:
- **Major releases:** 1-2 per year
- **Minor releases:** Every 1-2 months
- **Patch releases:** As needed (for critical bugs)

---

## Support Window

| Version | Status | Support Until |
|---------|--------|---------------|
| 2.x     | Active | TBD           |
| 1.x     | Active | 1 year after 2.0 release |
| 0.x     | Deprecated | None (upgrade to 1.x) |

---

For detailed documentation, see [SPM_DISTRIBUTION_GUIDE.md](./SPM_DISTRIBUTION_GUIDE.md).

---

**Last Updated:** 2025-10-08
