# Bundle Migration Examples

This document shows how to simplify your build.gradle.kts files using the new dependency bundles.

## Example 1: Common Dependencies

### ❌ Before (Verbose)
```kotlin
commonMain.dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.atomicfu)
}
```

### ✅ After (Using Bundle)
```kotlin
commonMain.dependencies {
    implementation(libs.bundles.kotlinx)
}
```

---

## Example 2: AndroidX Dependencies

### ❌ Before (Verbose)
```kotlin
androidMain.dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
```

### ✅ After (Using Bundles)
```kotlin
androidMain.dependencies {
    implementation(libs.bundles.androidx.core)
    implementation(libs.bundles.androidx.compose)
}
```

---

## Example 3: Compose UI

### ❌ Before (Verbose)
```kotlin
commonMain.dependencies {
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.runtime)
}
```

### ✅ After (Using Bundle)
```kotlin
commonMain.dependencies {
    // Note: Still use compose.* accessors from Compose Multiplatform plugin
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
}
```

**Note:** Compose Multiplatform plugin provides its own `compose.*` accessors. The `libs.bundles.compose.ui` bundle is available if you need explicit library references.

---

## Example 4: Testing Dependencies

### ❌ Before (Verbose)
```kotlin
commonTest.dependencies {
    implementation(libs.kotlin.test)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.turbine)
}

androidUnitTest.dependencies {
    implementation(libs.kotlin.test)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.junit)
    implementation(libs.turbine)
    implementation(libs.mockk)
}
```

### ✅ After (Using Bundles)
```kotlin
commonTest.dependencies {
    implementation(libs.bundles.testing.core)  // Without MockK (iOS compatible)
}

androidUnitTest.dependencies {
    implementation(libs.bundles.testing)  // Complete suite with MockK
}
```

---

## Complete Module Example

### Full build.gradle.kts Using Bundles

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // All Kotlinx essentials in one line
            implementation(libs.bundles.kotlinx)

            // Networking
            implementation(libs.bundles.ktor)

            // Compose UI from plugin accessors
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }

        commonTest.dependencies {
            // Testing without MockK (iOS compatible)
            implementation(libs.bundles.testing.core)
        }

        androidMain.dependencies {
            // All AndroidX essentials
            implementation(libs.bundles.androidx.core)
            implementation(libs.bundles.androidx.compose)

            // Platform-specific networking
            implementation(libs.okhttp)
        }

        androidUnitTest.dependencies {
            // Complete testing suite with MockK
            implementation(libs.bundles.testing)
        }

        iosMain.dependencies {
            // iOS-specific dependencies
            implementation(libs.ktor.client.darwin)
        }
    }
}
```

---

## Benefits Summary

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Lines of code | ~25 | ~12 | **52% reduction** |
| Readability | Medium | High | **Easier to scan** |
| Maintenance | Manual | Grouped | **Update once, use everywhere** |
| Error-prone | Medium | Low | **Fewer copy-paste errors** |

---

## When to Use Bundles vs Individual Libraries

### Use Bundles When:
- ✅ You always use these dependencies together
- ✅ Multiple modules need the same set of dependencies
- ✅ You want to reduce boilerplate
- ✅ Dependencies are semantically related

### Use Individual Libraries When:
- ✅ You only need one or two specific libraries
- ✅ Dependencies are unrelated
- ✅ Different modules need different subsets
- ✅ You want explicit control over each dependency

---

## Available Bundles Quick Reference

```kotlin
libs.bundles.kotlinx              // Kotlinx essentials (coroutines, serialization, datetime, atomicfu)
libs.bundles.androidx.core        // AndroidX core (core-ktx, lifecycle)
libs.bundles.androidx.compose     // AndroidX Compose integration (activity, navigation)
libs.bundles.compose.ui           // Compose Multiplatform UI (ui, foundation, material3, runtime)
libs.bundles.ktor                 // Ktor client core
libs.bundles.testing              // Complete testing suite (with MockK)
libs.bundles.testing.core         // Core testing (without MockK, iOS compatible)
```

---

**Last Updated:** 2025-10-08
