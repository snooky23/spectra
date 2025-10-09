# Publishing Guide

Guide for publishing Spectra Logger to various distribution channels.

## Distribution Channels

1. **Maven Central** - For Kotlin/JVM and Android
2. **CocoaPods** - For iOS
3. **GitHub Releases** - For direct downloads

---

## Maven Central Publishing

### Prerequisites

1. Create Sonatype JIRA account: https://issues.sonatype.org
2. Generate GPG key for signing
3. Add credentials to `~/.gradle/gradle.properties`:

```properties
signing.keyId=YOUR_KEY_ID
signing.password=YOUR_KEY_PASSWORD
signing.secretKeyRingFile=/Users/you/.gnupg/secring.gpg

ossrhUsername=YOUR_SONATYPE_USERNAME
ossrhPassword=YOUR_SONATYPE_PASSWORD
```

### Configuration

Add to root `build.gradle.kts`:

```kotlin
plugins {
    id("maven-publish")
    id("signing")
}

group = "com.spectra.logger"
version = "0.1.0"

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.spectra.logger"
            artifactId = "shared"
            version = "0.1.0"

            from(components["release"])

            pom {
                name.set("Spectra Logger")
                description.set("Kotlin Multiplatform logging framework for mobile")
                url.set("https://github.com/yourusername/spectra-logger")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("yourusername")
                        name.set("Avi Levin")
                        email.set("aviavi23@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/yourusername/spectra-logger.git")
                    developerConnection.set("scm:git:ssh://github.com/yourusername/spectra-logger.git")
                    url.set("https://github.com/yourusername/spectra-logger")
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = findProperty("ossrhUsername") as String?
                password = findProperty("ossrhPassword") as String?
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}
```

### Publishing Steps

```bash
# 1. Clean build
./gradlew clean

# 2. Build and test
./gradlew build

# 3. Publish to staging
./gradlew publish

# 4. Close and release on Sonatype
# Visit: https://s01.oss.sonatype.org/
# - Login
# - Find staging repository
# - Close
# - Release
```

---

## CocoaPods Publishing

### Prerequisites

1. Register for CocoaPods Trunk:
```bash
pod trunk register aviavi23@gmail.com 'Avi Levin'
```

2. Verify email and create session

### Podspec File

Create `SpectraLogger.podspec`:

```ruby
Pod::Spec.new do |spec|
  spec.name                     = 'SpectraLogger'
  spec.version                  = '0.1.0'
  spec.homepage                 = 'https://github.com/yourusername/spectra-logger'
  spec.source                   = { :git => "https://github.com/yourusername/spectra-logger.git", :tag => "#{spec.version}" }
  spec.authors                  = { 'Avi Levin' => 'aviavi23@gmail.com' }
  spec.license                  = { :type => 'Apache-2.0', :file => 'LICENSE' }
  spec.summary                  = 'Kotlin Multiplatform logging framework for mobile'
  spec.vendored_frameworks      = 'build/cocoapods/framework/shared.framework'
  spec.libraries                = 'c++'
  spec.ios.deployment_target    = '14.0'

  spec.pod_target_xcconfig = {
    'KOTLIN_PROJECT_PATH' => ':shared',
    'PRODUCT_MODULE_NAME' => 'shared',
  }

  spec.script_phases = [
    {
      :name => 'Build shared',
      :execution_position => :before_compile,
      :shell_path => '/bin/sh',
      :script => <<-SCRIPT
        set -ev
        REPO_ROOT="$PODS_TARGET_SRCROOT"
        "$REPO_ROOT/gradlew" -p "$REPO_ROOT" :shared:assembleSharedXCFramework
      SCRIPT
    }
  ]
end
```

### Publishing Steps

```bash
# 1. Validate podspec
pod spec lint SpectraLogger.podspec

# 2. Push to CocoaPods Trunk
pod trunk push SpectraLogger.podspec
```

---

## GitHub Releases

### Automated with GitHub Actions

Create `.github/workflows/release.yml`:

```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: macos-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build Release
        run: |
          ./gradlew build
          ./gradlew :shared:assembleRelease
          ./gradlew :shared:linkReleaseFrameworkIosArm64

      - name: Create AAR
        run: |
          ./gradlew :shared:assembleRelease
          cp shared/build/outputs/aar/shared-release.aar spectra-logger-android.aar

      - name: Create iOS Framework
        run: |
          ./gradlew :shared:linkReleaseFrameworkIosArm64
          cd shared/build/bin/iosArm64/releaseFramework
          zip -r ../../../../../spectra-logger-ios.framework.zip shared.framework

      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          files: |
            spectra-logger-android.aar
            spectra-logger-ios.framework.zip
          body: |
            ## Spectra Logger ${{ github.ref_name }}

            ### Installation

            **Android (Gradle)**
            ```kotlin
            dependencies {
                implementation("com.spectra.logger:shared:${{ github.ref_name }}")
            }
            ```

            **iOS (CocoaPods)**
            ```ruby
            pod 'SpectraLogger', '~> ${{ github.ref_name }}'
            ```

            ### Changelog
            See [CHANGELOG.md](CHANGELOG.md) for details.
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## Version Management

### Semantic Versioning

Follow [SemVer](https://semver.org/):
- **MAJOR**: Breaking API changes (1.0.0 → 2.0.0)
- **MINOR**: New features, backwards compatible (1.0.0 → 1.1.0)
- **PATCH**: Bug fixes, backwards compatible (1.0.0 → 1.0.1)

### Version Configuration

Update version in `gradle.properties`:

```properties
VERSION_NAME=0.1.0
GROUP=com.spectra.logger
```

Reference in `build.gradle.kts`:

```kotlin
group = findProperty("GROUP") as String
version = findProperty("VERSION_NAME") as String
```

---

## Release Checklist

### Pre-Release

- [ ] All tests passing
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Version bumped
- [ ] Examples tested
- [ ] Performance benchmarks run

### Release

- [ ] Create git tag
```bash
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin v0.1.0
```

- [ ] Build artifacts
```bash
./gradlew clean build
./gradlew publish
```

- [ ] Publish to Maven Central
- [ ] Publish to CocoaPods
- [ ] Create GitHub Release
- [ ] Update documentation website

### Post-Release

- [ ] Announce on social media
- [ ] Update README badges
- [ ] Monitor for issues
- [ ] Plan next release

---

## Continuous Deployment

### Automated Releases

1. **Tag-based releases**: Push tag triggers CI/CD
2. **Snapshot releases**: Automatic nightly builds
3. **Preview releases**: PR-based preview versions

### GitHub Actions Example

```yaml
name: Publish Snapshot

on:
  schedule:
    - cron: '0 0 * * *'  # Daily at midnight
  workflow_dispatch:

jobs:
  publish:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4

      - name: Publish Snapshot
        run: ./gradlew publish -PVERSION_NAME=0.1.0-SNAPSHOT
        env:
          ORG_GRADLE_PROJECT_ossrhUsername: ${{ secrets.OSSRH_USERNAME }}
          ORG_GRADLE_PROJECT_ossrhPassword: ${{ secrets.OSSRH_PASSWORD }}
          ORG_GRADLE_PROJECT_signingKeyId: ${{ secrets.SIGNING_KEY_ID }}
          ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.SIGNING_PASSWORD }}
```

---

## Documentation Publishing

### GitHub Pages

1. Build documentation:
```bash
./gradlew dokkaHtml
```

2. Deploy to gh-pages:
```bash
git checkout gh-pages
cp -r build/dokka/html/* .
git add .
git commit -m "Update documentation"
git push origin gh-pages
```

### Automated with Actions

```yaml
name: Documentation

on:
  push:
    branches: [ main ]

jobs:
  docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build docs
        run: ./gradlew dokkaHtml

      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./build/dokka/html
```

---

## Support and Maintenance

### Issue Triage

- Label issues: `bug`, `enhancement`, `documentation`
- Assign milestones
- Link to projects

### Release Schedule

- **Patch**: As needed for critical bugs
- **Minor**: Monthly feature releases
- **Major**: Yearly breaking changes

### Deprecation Policy

1. Mark as `@Deprecated` with replacement
2. Keep for at least 2 minor versions
3. Remove in next major version

---

## Useful Commands

```bash
# Check what will be published
./gradlew publishToMavenLocal

# Verify artifacts
ls -la ~/.m2/repository/com/spectra/logger/shared/

# Test pod locally
pod lib lint SpectraLogger.podspec --allow-warnings

# Create release tag
git tag -a v0.1.0 -m "Release 0.1.0"
git push origin v0.1.0

# Rollback release (if needed)
git tag -d v0.1.0
git push origin :refs/tags/v0.1.0
```
