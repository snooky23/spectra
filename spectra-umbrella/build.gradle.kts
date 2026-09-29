import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.publish)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // Android target for umbrella module
    android {
        namespace = "com.spectra"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }

        withHostTestBuilder {}.configure {
            enableCoverage = false
        }
    }

    // iOS targets - Umbrella XCFramework: Spectra.xcframework
    val iosFrameworkName = "Spectra"
    val xcf = XCFramework(iosFrameworkName)

    val activeArch = project.findProperty("spectra.activeArch") as? String
    val isIosArm64Enabled = activeArch == null || activeArch == "iosArm64"
    val isIosSimulatorArm64Enabled = activeArch == null || activeArch == "iosSimulatorArm64"

    val iosTargets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
    if (isIosArm64Enabled) iosTargets.add(iosArm64())
    if (isIosSimulatorArm64Enabled) iosTargets.add(iosSimulatorArm64())

    val isMac = org.jetbrains.kotlin.konan.target.HostManager.hostIsMac
    iosTargets.forEach { iosTarget ->
        if (isMac) {
            iosTarget.binaries.framework {
                baseName = iosFrameworkName
                isStatic = true
                export(project(":spectra-core"))
                export(project(":spectra-ui"))
                xcf.add(this)
                binaryOption("bundleId", "com.spectra.logger")
            }
        }
    }

    // Desktop/JVM target for Compose Hot Reload and desktop support
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":spectra-core"))
                api(project(":spectra-ui"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val androidMain by getting {
            dependencies {
                api(project(":spectra-core"))
                api(project(":spectra-ui"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api(project(":spectra-core"))
                api(project(":spectra-ui"))
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(project(":spectra-core"))
                api(project(":spectra-ui"))
            }
        }

        val iosTest by creating {
            dependsOn(commonTest)
        }

        iosTargets.forEach { target ->
            getByName("${target.name}Main").dependsOn(iosMain)
            getByName("${target.name}Test").dependsOn(iosTest)
        }
    }
}

// Publishing configuration
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("Spectra")
        description.set("Unified Kotlin Multiplatform Umbrella SDK for Spectra Logger (Core + UI)")
        url.set(project.findProperty("POM_URL") as String)

        licenses {
            license {
                name.set(project.findProperty("POM_LICENCE_NAME") as String)
                url.set(project.findProperty("POM_LICENCE_URL") as String)
            }
        }

        developers {
            developer {
                id.set(project.findProperty("POM_DEVELOPER_ID") as String)
                name.set(project.findProperty("POM_DEVELOPER_NAME") as String)
            }
        }

        scm {
            connection.set(project.findProperty("POM_SCM_CONNECTION") as String)
            developerConnection.set(project.findProperty("POM_SCM_DEV_CONNECTION") as String)
            url.set(project.findProperty("POM_SCM_URL") as String)
        }
    }
}
