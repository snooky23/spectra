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

// Core shared module
include(":shared")

// Example applications
include(":examples:android-native:app")
include(":examples:kmp-app:shared")
include(":examples:kmp-app:androidApp")
