plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    id("maven-publish")
    id("jacoco")
}

kotlin {
    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
        publishLibraryVariants("release", "debug")
    }

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SpectraLogger"
            isStatic = true
        }
    }

    sourceSets {
        // Common
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.kotlinx)
                implementation(libs.ktor.client.core)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
                // Note: MockK doesn't support iOS native, will add platform-specific tests later
            }
        }

        // Android
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.okhttp)
                implementation(libs.androidx.activity.compose)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }

        // iOS
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting

        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.spectra.logger"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = project.findProperty("GROUP") as String
            artifactId = "logger"
            version = project.findProperty("VERSION_NAME") as String

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
    }

    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

// Jacoco configuration for code coverage
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/androidMain/kotlin"))
    classDirectories.setFrom(files("build/tmp/kotlin-classes/debugUnitTest"))
    executionData.setFrom(files("build/jacoco/testDebugUnitTest.exec"))
}

// Fix for IntelliJ IDEA duplicate content roots warning
// Compose Multiplatform creates composeResources for test source sets which causes conflicts
// This comprehensive fix prevents the directories from being created in the first place

// 1. Clean up any existing test composeResources directories
tasks.register("cleanTestComposeResources") {
    doLast {
        // Delete all test-related composeResources directories
        val testResourceDirs = listOf(
            "src/commonTest/composeResources",
            "src/androidUnitTest/composeResources",
            "src/androidInstrumentedTest/composeResources",
            "src/iosTest/composeResources",
            "src/iosX64Test/composeResources",
            "src/iosArm64Test/composeResources",
            "src/iosSimulatorArm64Test/composeResources"
        )
        testResourceDirs.forEach { delete(it) }
    }
}

// 2. Run cleanup before build and before IntelliJ sync
tasks.named("preBuild") {
    dependsOn("cleanTestComposeResources")
}

// Run cleanup when IntelliJ/Android Studio syncs the project
tasks.configureEach {
    if (name == "prepareKotlinIdeaImport" || name == "generateProjectStructureMetadata") {
        dependsOn("cleanTestComposeResources")
    }
}

// 3. Disable Compose resource generation for test source sets
afterEvaluate {
    // Find and disable all Compose resource generation tasks for test source sets
    tasks.configureEach {
        val taskName = name.lowercase()
        val isComposeResourceTask = taskName.contains("generatecomposeresclass") ||
                taskName.contains("composeresources")
        val isTestTask = taskName.contains("test")

        if (isComposeResourceTask && isTestTask) {
            enabled = false
        }
    }

    // Also clean up after any task that might create these directories
    tasks.configureEach {
        if (name.contains("generateComposeResClass")) {
            finalizedBy("cleanTestComposeResources")
        }
    }
}
