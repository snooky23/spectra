pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "3.19.1"
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
        publishing.onlyIf { System.getenv("CI") != null }
    }
}

rootProject.name = "Spectra"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
