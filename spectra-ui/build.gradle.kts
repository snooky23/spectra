import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.publish)
}

kotlin {
    // Android target for KMP UI
    android {
        namespace = "com.spectra.logger.ui"
        compileSdk = 35
        minSdk = 24

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }

        // Enable unit testing on the JVM for this target
        withHostTestBuilder {}.configure {
            enableCoverage = false
        }
    }

    // iOS targets
    val iosFrameworkName = "SpectraLoggerUI"
    val xcf = XCFramework(iosFrameworkName)

    val activeArch = project.findProperty("spectra.activeArch") as? String
    val isIosX64Enabled = activeArch == null || activeArch == "iosX64"
    val isIosArm64Enabled = activeArch == null || activeArch == "iosArm64"
    val isIosSimulatorArm64Enabled = activeArch == null || activeArch == "iosSimulatorArm64"

    val iosTargets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
    if (isIosX64Enabled) iosTargets.add(iosX64())
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
            }
        }

        iosTarget.compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-Xopt-in=kotlin.time.ExperimentalTime")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":spectra-core"))
                implementation(libs.androidx.lifecycle.viewmodel.compose)

                // Using stable coordinates to avoid deprecated Compose plugin accessors
                implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
                implementation("org.jetbrains.compose.foundation:foundation:1.7.3")
                implementation("org.jetbrains.compose.material3:material3:1.7.3")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
                implementation("org.jetbrains.compose.components:components-resources:1.7.3")
                implementation("org.jetbrains.compose.components:components-ui-tooling-preview:1.7.3")

                // Adaptive UI libraries
                implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.0.1")
                implementation("org.jetbrains.compose.material3.adaptive:adaptive-layout:1.0.1")
                implementation("org.jetbrains.compose.material3.adaptive:adaptive-navigation:1.0.1")
                implementation("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:1.7.3")

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }

        iosTargets.forEach { target ->
            getByName("${target.name}Main").dependsOn(iosMain)
        }
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
