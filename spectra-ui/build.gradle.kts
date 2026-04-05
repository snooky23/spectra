plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.skie)
    alias(libs.plugins.vanniktech.publish)
}

kotlin {
    // Android target for KMP UI
    android {
        namespace = "com.spectra.logger.ui"
        compileSdk = 35
        minSdk = 24
        
        androidResources.enable = true

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SpectraLoggerUI"
            isStatic = true
            // Export the core library to the UI framework if needed
            // export(project(":spectra-core"))
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":spectra-core"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                
                // Adaptive UI libraries
                implementation(libs.compose.adaptive)
                implementation(libs.compose.adaptive.layout)
                implementation(libs.compose.adaptive.navigation)
                implementation(libs.compose.adaptive.navigation.suite)
                
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
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
