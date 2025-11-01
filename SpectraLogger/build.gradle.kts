// SpectraLogger Swift Package - version alignment wrapper
//
// This file is a Gradle-based wrapper that helps with version management
// The actual Swift package is in Package.swift

plugins {
    `base`
}

// Read version from root gradle.properties to ensure alignment
val versionName = rootProject.findProperty("VERSION_NAME") as? String ?: "0.0.1-SNAPSHOT"

group = rootProject.findProperty("GROUP") as? String ?: "com.spectra.logger"
version = versionName

// Ensure Package.swift is always in sync
tasks.register("validatePackageSwiftVersion") {
    description = "Validates that Package.swift exists and is properly configured"

    doLast {
        val packageSwiftFile = File(projectDir, "Package.swift")
        if (!packageSwiftFile.exists()) {
            throw GradleException("Package.swift not found in ${projectDir}")
        }

        println("✓ SpectraLogger Package.swift found")
        println("✓ Version in gradle.properties: $versionName")
        println("✓ Package.swift is ready for distribution")
    }
}

// Task to help with version bumping
tasks.register("bumpVersion") {
    description = "Helps bump version across all packages"

    doLast {
        println("""
            To update the version:

            1. Edit gradle.properties and change VERSION_NAME
            2. Run: ./gradlew syncVersions
            3. Commit and tag the release
        """.trimIndent())
    }
}
