import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vanniktech.publish)
    id("jacoco")
}

// Generate version file from gradle.properties
abstract class GenerateVersionFileTask : DefaultTask() {
    @get:Input
    abstract val versionName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val versionFile = outputDir.file("Version.kt").get().asFile
        versionFile.parentFile.mkdirs()
        versionFile.writeText(
            """
            package com.spectra.logger

            /**
             * Auto-generated file from gradle.properties
             * Do not edit manually!
             */
            object Version {
                const val LIBRARY_VERSION = "${versionName.get()}"
            }
            """.trimIndent() + "\n",
        )
    }
}

val libVersion = project.findProperty("VERSION_NAME") as? String ?: "0.0.1-SNAPSHOT"
val generateVersionFile =
    tasks.register<GenerateVersionFileTask>("generateVersionFile") {
        this.versionName.set(libVersion)
        this.outputDir.set(project.layout.buildDirectory.dir("generated/kotlin/com/spectra/logger"))
    }

kotlin {
    // Android target using the modern KMP-first plugin DSL
    android {
        namespace = "com.spectra.logger"
        compileSdk = 35
        minSdk = 24

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }

        // Enable unit testing on the JVM for this target
        withHostTestBuilder {}.configure {
            enableCoverage = true
        }
    }

    // iOS targets
    val iosFrameworkName = "SpectraLogger"
    val xcf = XCFramework(iosFrameworkName)

    val activeArch = project.findProperty("spectra.activeArch") as? String
    val isIosX64Enabled = activeArch == null || activeArch == "iosX64"
    val isIosArm64Enabled = activeArch == null || activeArch == "iosArm64"
    val isIosSimulatorArm64Enabled = activeArch == null || activeArch == "iosSimulatorArm64"

    val iosTargets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
    if (isIosX64Enabled) iosTargets.add(iosX64())
    if (isIosArm64Enabled) iosTargets.add(iosArm64())
    if (isIosSimulatorArm64Enabled) iosTargets.add(iosSimulatorArm64())

    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = iosFrameworkName
            isStatic = true
            xcf.add(this)
        }

        // Add compiler flags to suppress expect/actual class warnings
        iosTarget.compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    // Desktop/JVM targets
    jvm()
    val macosTargets = listOf(macosX64(), macosArm64())
    val linuxTargets = listOf(linuxX64(), linuxArm64())
    val mingwTargets = listOf(mingwX64())

    // Web targets
    js {
        browser()
        nodejs()
    }

    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generateVersionFile)
            dependencies {
                api(libs.bundles.kotlinx)
                api(libs.ktor.client.core)
                api(libs.ktor.client.websockets)
                api(libs.okio.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
                implementation(libs.ktor.client.mock)
                implementation(libs.okio.fakefilesystem)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.okhttp)
                // Compose UI
                implementation(libs.bundles.compose.ui)
                implementation(libs.bundles.androidx.compose)
            }
        }

        val androidHostTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockwebserver)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        macosTargets.forEach { getByName("${it.name}Main").dependsOn(nativeMain) }
        linuxTargets.forEach { getByName("${it.name}Main").dependsOn(nativeMain) }
        mingwTargets.forEach { getByName("${it.name}Main").dependsOn(nativeMain) }
        iosTargets.forEach { getByName("${it.name}Main").dependsOn(nativeMain) }
    }
}

// Publishing configuration via Vanniktech Maven Publish Plugin
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("Spectra Logger")
        description.set(project.findProperty("POM_DESCRIPTION") as String)
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

// Jacoco configuration for code coverage
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testAndroidHostTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/androidMain/kotlin"))
    classDirectories.setFrom(files("build/classes/kotlin/android/main"))
    executionData.setFrom(files("build/outputs/unit_test_code_coverage/androidHostTest/testAndroidHostTest.exec"))
}
