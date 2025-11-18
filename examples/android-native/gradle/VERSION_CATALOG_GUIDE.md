# Gradle Version Catalog Guide

## Overview

This project uses Gradle Version Catalogs for centralized dependency management. All versions, libraries, and plugins are defined in `gradle/libs.versions.toml`.

## Benefits

✅ **Single Source of Truth**: All dependency versions in one place
✅ **Type-Safe**: IDE autocomplete for dependencies
✅ **Consistent Versions**: Avoid version conflicts across modules
✅ **Easy Updates**: Update one version, affects all usages
✅ **Bundles**: Group related dependencies together

---

## How to Use

### In build.gradle.kts Files

#### Using Plugins
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
}
```

#### Using Individual Libraries
```kotlin
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.ktor.client.core)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}
```

#### Using Bundles (Recommended for Common Groups)
```kotlin
dependencies {
    // All Kotlinx essentials in one line
    implementation(libs.bundles.kotlinx)

    // All AndroidX core dependencies
    implementation(libs.bundles.androidx.core)

    // All Compose UI components
    implementation(libs.bundles.compose.ui)

    // Complete testing suite
    testImplementation(libs.bundles.testing)
}
```

---

## Available Bundles

### `kotlinx`
Common Kotlinx dependencies for all modules:
- kotlinx-coroutines-core
- kotlinx-serialization-json
- kotlinx-datetime
- kotlinx-atomicfu

**Usage:**
```kotlin
commonMain.dependencies {
    implementation(libs.bundles.kotlinx)
}
```

### `androidx-core`
Essential AndroidX core libraries:
- androidx-core-ktx
- androidx-lifecycle-runtime-ktx
- androidx-lifecycle-viewmodel-ktx

**Usage:**
```kotlin
androidMain.dependencies {
    implementation(libs.bundles.androidx.core)
}
```

### `androidx-compose`
AndroidX Compose integration:
- androidx-activity-compose
- androidx-navigation-compose

**Usage:**
```kotlin
androidMain.dependencies {
    implementation(libs.bundles.androidx.compose)
}
```

### `compose-ui`
Compose Multiplatform UI components:
- compose-ui
- compose-foundation
- compose-material3
- compose-runtime

**Usage:**
```kotlin
commonMain.dependencies {
    implementation(libs.bundles.compose.ui)
}
```

### `ktor`
Ktor client base:
- ktor-client-core

**Usage:**
```kotlin
commonMain.dependencies {
    implementation(libs.bundles.ktor)
}
```

### `testing`
Complete testing framework (Android & JVM):
- kotlin-test
- kotlinx-coroutines-test
- junit
- turbine
- mockk

**Usage:**
```kotlin
androidUnitTest.dependencies {
    implementation(libs.bundles.testing)
}
```

### `testing-core`
Core testing without MockK (iOS compatible):
- kotlin-test
- kotlinx-coroutines-test
- turbine

**Usage:**
```kotlin
commonTest.dependencies {
    implementation(libs.bundles.testing.core)
}
```

---

## Adding New Dependencies

### 1. Add Version (if new)
```toml
[versions]
new-library = "1.0.0"
```

### 2. Add Library Definition
```toml
[libraries]
new-library = { module = "com.example:new-library", version.ref = "new-library" }
```

### 3. Use in build.gradle.kts
```kotlin
dependencies {
    implementation(libs.new.library)
}
```

### 4. (Optional) Add to Bundle
```toml
[bundles]
my-bundle = [
    "existing-library",
    "new-library"
]
```

---

## Updating Dependencies

### Update a Single Library
1. Open `gradle/libs.versions.toml`
2. Change version in `[versions]` section
3. Sync Gradle

**Example:**
```toml
# Before
kotlin = "1.9.22"

# After
kotlin = "1.9.30"
```

### Check for Updates
Run Gradle versions plugin (if configured):
```bash
./gradlew dependencyUpdates
```

Or manually check:
- Kotlin: https://kotlinlang.org/docs/releases.html
- Compose: https://github.com/JetBrains/compose-multiplatform/releases
- AndroidX: https://developer.android.com/jetpack/androidx/versions

---

## Best Practices

### ✅ DO
- Use bundles for commonly grouped dependencies
- Keep version numbers in `[versions]` section
- Use semantic grouping with comments
- Update this guide when adding new bundles

### ❌ DON'T
- Don't hardcode versions in build.gradle.kts
- Don't duplicate version declarations
- Don't skip updating the catalog when adding dependencies
- Don't create bundles with unrelated dependencies

---

## Troubleshooting

### "Unresolved reference: libs"
**Solution:** Sync Gradle project in your IDE

### "Cannot access libs before accessors have been generated"
**Solution:**
1. Run `./gradlew clean`
2. Sync Gradle project
3. Rebuild

### Library not found in catalog
**Solution:** Check spelling in `libs.versions.toml`. Dots in library names become underscores in accessors:
```toml
androidx-core-ktx → libs.androidx.core.ktx
```

---

## Current Version Catalog Structure

```
gradle/libs.versions.toml
├── [versions]      # Version numbers
├── [libraries]     # Individual library definitions
├── [plugins]       # Build plugins
└── [bundles]       # Dependency bundles
```

**Last Updated:** 2025-10-08
**Documentation Version:** 1.0
