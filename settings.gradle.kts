rootProject.name = "Spectra"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// Core KMP module (logging functionality)
include(":spectra-core")

// Unified UI module (Compose Multiplatform)
include(":spectra-ui")

// Example applications
include(":examples:android-native:app")
// include(":examples:kmp-app:shared")
// include(":examples:kmp-app:androidApp")
