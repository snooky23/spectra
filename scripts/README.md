# Development Scripts

This directory contains shell scripts that replicate the GitHub Actions CI/CD workflows, allowing developers to run them locally.

## Available Scripts

### 🔧 `build.sh`
Builds the entire project.

```bash
./scripts/build.sh
```

**What it does:**
- Ensures `gradlew` is executable
- Runs `./gradlew build` with full stacktrace
- Compiles all modules (shared, examples)

---

### 🧪 `test.sh`
Runs the complete test suite.

```bash
./scripts/test.sh
```

**What it does:**
- Runs all unit tests
- Runs all instrumentation tests
- Reports test results

---

### 🔍 `code-quality.sh`
Performs code quality checks (ktlint + detekt).

```bash
./scripts/code-quality.sh
```

**What it does:**
- Runs **ktlint** to check Kotlin code style
- Runs **detekt** for static code analysis
- Fails if any violations are found

---

### ✨ `format.sh`
Auto-formats code with ktlint.

```bash
./scripts/format.sh
```

**What it does:**
- Automatically fixes code formatting issues
- Applies ktlint rules
- Modifies files in place

---

### 📊 `coverage.sh`
Generates code coverage reports.

```bash
./scripts/coverage.sh
```

**What it does:**
- Runs tests with coverage instrumentation
- Generates Jacoco HTML/XML reports
- Opens report in browser (macOS)
- Report location: `shared/build/reports/jacoco/jacocoTestReport/html/index.html`

---

### 🚀 `ci.sh`
Runs the complete CI pipeline locally.

```bash
./scripts/ci.sh
```

**What it does:**
1. **Build** - Compiles the project
2. **Test** - Runs all tests
3. **Code Quality** - Checks ktlint + detekt
4. **Coverage** - Generates coverage reports

This is the **recommended script** to run before pushing to ensure your changes will pass CI.

---

### 🧹 `clean.sh`
Cleans all build artifacts.

```bash
./scripts/clean.sh
```

**What it does:**
- Removes all `build/` directories
- Clears Gradle caches
- Resets build state

---

## Version Management Scripts

### sync-versions.sh
Validates that all Spectra packages (Core and UI) are properly configured.

```bash
./scripts/sync-versions.sh
```

**What it does:**
- ✅ Reads VERSION_NAME from gradle.properties
- ✅ Validates Package.swift files exist
- ✅ Ensures version consistency
- ✅ Displays current version across all packages

---

### bump-version.py
Automatically bump version across gradle.properties and validate.

**Syntax:**
```bash
python3 scripts/bump-version.py [OPTIONS]
```

**Options:**
- `--to VERSION` - Bump to specific version (e.g., `--to 0.0.2`)
- `--major` - Bump major version (0.0.1 → 1.0.0)
- `--minor` - Bump minor version (0.0.1 → 0.1.0)
- `--patch` - Bump patch version (0.0.1 → 0.0.2)
- `--snapshot` - Bump patch + add -SNAPSHOT (0.0.1 → 0.0.2-SNAPSHOT)
- `--dry-run` - Show what would change without applying

**Examples:**
```bash
# Bump to specific version
python3 scripts/bump-version.py --to 0.0.2

# Bump minor version (for features)
python3 scripts/bump-version.py --minor

# Bump patch version (for fixes)
python3 scripts/bump-version.py --patch

# Dry run to preview changes
python3 scripts/bump-version.py --patch --dry-run
```

**Version Management Workflow:**
```bash
# 1. Bump version
python3 scripts/bump-version.py --patch

# 2. Validate
./scripts/sync-versions.sh

# 3. Build and test
./scripts/ci.sh

# 4. Commit and tag
git add -A
git commit -m "release: bump to 0.0.2"
git tag -a v0.0.2 -m "Release v0.0.2"
git push origin main && git push origin v0.0.2
```

For detailed version management guide, see: [docs/VERSION_MANAGEMENT.md](../docs/VERSION_MANAGEMENT.md)

---

## Prerequisites

### Required Tools
- **JDK 17** - Java Development Kit
- **Bash** - Shell interpreter (macOS/Linux)

### Check Java Version
```bash
java -version
# Should show: openjdk version "17.x.x"
```

If you don't have JDK 17:
- **macOS**: `brew install openjdk@17`
- **Linux**: `sudo apt install openjdk-17-jdk`

---

## Usage

### Make Scripts Executable (First Time Only)
```bash
chmod +x scripts/*.sh
```

### Run Individual Scripts
```bash
# Run a specific check
./scripts/code-quality.sh

# Generate coverage
./scripts/coverage.sh
```

### Run Full CI Pipeline
```bash
# This is what GitHub Actions runs
./scripts/ci.sh
```

---

## Continuous Integration Workflow

These scripts replicate the GitHub Actions workflow defined in `.github/workflows/build.yml`:

| GitHub Actions Job | Local Script | Command |
|-------------------|--------------|---------|
| `build` | `build.sh` | `./scripts/build.sh` |
| `test` | `test.sh` | `./scripts/test.sh` |
| `code-quality` | `code-quality.sh` | `./scripts/code-quality.sh` |
| Coverage | `coverage.sh` | `./scripts/coverage.sh` |
| Full Pipeline | `ci.sh` | `./scripts/ci.sh` |

---

## Development Workflow

### Before Committing
```bash
# Format code
./scripts/format.sh

# Run full CI checks
./scripts/ci.sh
```

### After Making Changes
```bash
# Quick validation
./scripts/build.sh && ./scripts/test.sh
```

### Debugging Failures
```bash
# Clean and rebuild
./scripts/clean.sh
./scripts/build.sh
```

---

## Troubleshooting

### Permission Denied
```bash
chmod +x scripts/*.sh
chmod +x gradlew
```

### Java Version Issues
```bash
# Check Java version
java -version

# Set JAVA_HOME (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Set JAVA_HOME (Linux)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### Build Failures
```bash
# Clean and retry
./scripts/clean.sh
./scripts/build.sh
```

### Test Failures
```bash
# Run with verbose output
./gradlew test --no-daemon --stacktrace --info
```

---

## CI/CD Integration

These scripts are designed to:
1. **Run locally** - Test before pushing
2. **Match GitHub Actions** - Same checks as CI
3. **Fast feedback** - Catch issues early

**Best Practice**: Always run `./scripts/ci.sh` before pushing to remote!

---

## Script Exit Codes

All scripts use `set -e` and will exit with non-zero status on failure:
- **Exit 0** - Success
- **Exit 1** - Build/test/quality failure

This makes them safe to use in automation:
```bash
./scripts/ci.sh && git push || echo "CI failed, fix issues first"
```

---

## Platform Support

- ✅ **macOS** - Fully supported
- ✅ **Linux** - Fully supported
- ⚠️ **Windows** - Use Git Bash or WSL

---

## Performance Tips

### Speed Up Builds
```bash
# Use Gradle daemon (remove --no-daemon)
./gradlew build

# Run tests in parallel
./gradlew test --parallel
```

### Skip Tests During Development
```bash
# Build without tests
./gradlew build -x test
```

---

## Additional Resources

- [GitHub Actions Workflow](../.github/workflows/build.yml)
- [Gradle Documentation](https://docs.gradle.org/)
- [ktlint](https://pinterest.github.io/ktlint/)
- [detekt](https://detekt.dev/)
