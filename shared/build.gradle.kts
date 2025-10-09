plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
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
    val iosFrameworkName = "SpectraLogger"
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = iosFrameworkName
            isStatic = true
        }
    }

    sourceSets {
        // Common
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.kotlinx)
                implementation(libs.ktor.client.core)
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


// Task to create XCFramework for iOS
tasks.register("createXCFramework") {
    group = "build"
    description = "Creates an XCFramework for all iOS targets"
    
    val frameworkName = "SpectraLogger"
    val buildType = "Release"
    val xcframeworkPath = "${project.buildDir}/XCFrameworks/${buildType.lowercase()}/$frameworkName.xcframework"
    
    dependsOn(
        "linkReleaseFrameworkIosArm64",
        "linkReleaseFrameworkIosX64",
        "linkReleaseFrameworkIosSimulatorArm64"
    )
    
    doLast {
        exec {
            commandLine(
                "xcodebuild",
                "-create-xcframework",
                "-framework", "${project.buildDir}/bin/iosArm64/releaseFramework/$frameworkName.framework",
                "-framework", "${project.buildDir}/bin/iosSimulatorArm64/releaseFramework/$frameworkName.framework",
                "-framework", "${project.buildDir}/bin/iosX64/releaseFramework/$frameworkName.framework",
                "-output", xcframeworkPath
            )
        }
        println("✅ XCFramework created at: $xcframeworkPath")
    }
}
