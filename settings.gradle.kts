pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
    }
}

plugins {
    id("com.gradle.develocity") version "3.19.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        // List repository hosting the vast majority of dependencies first to minimize 404s
        mavenCentral()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android\\.arch.*")
            }
        }
        ivy("https://nodejs.org/dist") {
            name = "Node Distributions at https://nodejs.org/dist"
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
        }
    }
}

// Core KMP module (logging functionality)
include(":spectra-core")

// Unified UI module (Compose Multiplatform)
include(":spectra-ui")

// Unified Umbrella module (Core + UI)
include(":spectra-umbrella")

// Example applications
include(":examples:android-native:app")

// Note: kmp-app example is temporarily suspended pending CMP 1.10.3 / Material 3 Adaptive migration
// include(":examples:kmp-app:shared")
// include(":examples:kmp-app:androidApp")
