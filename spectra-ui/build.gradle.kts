import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.hot.reload)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.publish)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // Android target for KMP UI
    android {
        namespace = "com.spectra.logger.ui"
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

        // Enable unit testing on the JVM for this target
        withHostTestBuilder {}.configure {
            enableCoverage = false
        }
    }

    // iOS targets (iosArm64 for devices and iosSimulatorArm64 for Apple Silicon; iosX64 dropped upstream by Compose & Lifecycle)
    val iosFrameworkName = "SpectraLoggerUI"
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
                xcf.add(this)
                binaryOption("bundleId", "com.spectra.logger.ui")
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
                implementation(libs.jetbrains.lifecycle.viewmodel.compose)

                // Compose Multiplatform UI & Adaptive components from version catalog
                implementation(libs.bundles.compose.multiplatform.ui)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
                implementation(libs.kotlinx.datetime)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
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

compose.desktop {
    application {
        mainClass = "com.spectra.logger.core.ui.desktop.MainKt"
    }
}

// Global language settings for all source sets
kotlin.sourceSets.all {
    languageSettings.apply {
        optIn("kotlin.time.ExperimentalTime")
        optIn("androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi")
        optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

// Publishing configuration
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("Spectra Logger UI")
        description.set("Unified Compose Multiplatform UI for Spectra Logger")
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
