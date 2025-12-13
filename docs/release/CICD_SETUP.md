# CI/CD Setup Guide

This document explains how to set up automated releases via GitHub Actions.

## Overview

The release workflow (`.github/workflows/release.yml`) automatically:
1. ✅ Builds XCFramework
2. ✅ Packages and calculates checksum
3. ✅ Creates GitHub Release with assets
4. ✅ Publishes to CocoaPods Trunk (if token is configured)

## Prerequisites

### 1. CocoaPods Trunk Registration

If you haven't already registered with CocoaPods Trunk:

```bash
pod trunk register aviavi23@gmail.com 'Avi Levin'
```

Check your email and click the verification link.

### 2. Get CocoaPods Token

```bash
# Get your session token
pod trunk me
```

You'll see output like:
```
  - Name:          Avi Levin
  - Email:         aviavi23@gmail.com
  - Since:         October 3rd, 2024
  - Pods:
    - SpectraLogger
    - SpectraLoggerUI
  - Sessions:
    - October 9th, 00:22 - May 16th, 01:33. IP: xxx.xxx.xxx.xxx Description: macOS 14.6.1
```

To get the actual token for CI/CD:

```bash
# This will show your token (keep it secret!)
cat ~/.netrc | grep cocoapods.org -A 2
```

You'll see something like:
```
machine trunk.cocoapods.org
  login aviavi23@gmail.com
  password YOUR_TOKEN_HERE
```

Copy the token value.

### 3. Add GitHub Secret

1. Go to: https://github.com/snooky23/spectra/settings/secrets/actions
2. Click **"New repository secret"**
3. **Name:** `COCOAPODS_TRUNK_TOKEN`
4. **Value:** Paste the token from step 2
5. Click **"Add secret"**

---

## How to Trigger a Release

The workflow triggers automatically when you push a git tag with format `v*.*.*`.

### Manual Release Process

```bash
# 1. Ensure all changes are committed and pushed
git status  # Should be clean

# 2. Create and push a version tag
VERSION="1.0.0"
git tag -a "v$VERSION" -m "Release v$VERSION"
git push origin "v$VERSION"

# 3. Watch the workflow run
# Go to: https://github.com/snooky23/spectra/actions
```

### What Happens Next

1. **GitHub Actions starts** (~5 minutes total)
   - Checks out code
   - Sets up Java and Gradle
   - Builds XCFramework for all architectures
   - Creates zip and calculates checksum
   - Generates release notes
   - Creates GitHub Release
   - Uploads XCFramework asset

2. **CocoaPods publish** (if token is set)
   - Validates podspecs
   - Publishes `SpectraLogger`
   - Publishes `SpectraLoggerUI`

3. **Release is live!**
   - GitHub: https://github.com/snooky23/spectra/releases
   - CocoaPods: https://cocoapods.org/pods/SpectraLogger

---

## Re-running a Failed Release

If the release workflow fails, you can:

### Option 1: Delete tag and re-release

```bash
# Delete local tag
git tag -d v1.0.0

# Delete remote tag
git push origin :refs/tags/v1.0.0

# Delete the release on GitHub (if created)
# Go to: https://github.com/snooky23/spectra/releases
# Click the release, then "Delete this release"

# Fix the issue, then re-tag
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

### Option 2: Re-run workflow

1. Go to: https://github.com/snooky23/spectra/actions
2. Click on the failed workflow run
3. Click "Re-run all jobs"

---

## Current Status for v1.0.0

- ✅ Tag `v1.0.0` exists and is pushed
- ✅ XCFramework is built locally (8.5MB)
- ✅ Workflow file is ready
- ⏳ **Action needed:** Set `COCOAPODS_TRUNK_TOKEN` secret (see above)

Once the secret is set, you can either:
- **Option A:** Delete and re-push the v1.0.0 tag to trigger the workflow
- **Option B:** Create the release manually this time, then use CI/CD for future releases (v1.0.1, v1.1.0, etc.)

---

## Troubleshooting

### "Error: Resource not accessible by integration"

**Problem:** GitHub Actions doesn't have permission to create releases.

**Solution:** The workflow already has `permissions: contents: write` set. If this still fails, check repository settings:
1. Settings → Actions → General
2. "Workflow permissions" → Select "Read and write permissions"
3. Save

### "Pod validation failed"

**Problem:** Podspec has errors or XCFramework is not accessible.

**Solution:**
1. Ensure GitHub Release was created first (CocoaPods validates by downloading)
2. Check podspec locally: `pod spec lint shared/SpectraLogger.podspec --allow-warnings`
3. Verify XCFramework URL is accessible: `curl -I [URL]`

### "Authentication failed - COCOAPODS_TRUNK_TOKEN invalid"

**Problem:** Token expired or incorrect.

**Solution:**
1. Get fresh token: `cat ~/.netrc | grep cocoapods.org -A 2`
2. Update GitHub secret with new token
3. Re-run workflow

---

## Best Practices

1. **Always test locally first**
   ```bash
   ./scripts/build/build-xcframework.sh
   pod spec lint shared/SpectraLogger.podspec --allow-warnings
   ```

2. **Use semantic versioning**
   - Major: Breaking changes (v2.0.0)
   - Minor: New features (v1.1.0)
   - Patch: Bug fixes (v1.0.1)

3. **Update CHANGELOG.md** before releasing

4. **Monitor workflow runs** at https://github.com/snooky23/spectra/actions

5. **Verify release** after it completes:
   - Test SPM installation in a sample project
   - Test CocoaPods installation: `pod try SpectraLogger`

---

## Future Enhancements

Potential improvements to the CI/CD pipeline:

- [ ] Automated testing before release
- [ ] Upload to Maven Central for Android
- [ ] Generate changelog from git commits
- [ ] Notify Slack/Discord on release
- [ ] Create draft releases for review before publishing

---

**Last Updated:** 2025-10-09
