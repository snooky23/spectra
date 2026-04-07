plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.publish) apply false
    alias(libs.plugins.skie) apply false
}

allprojects {
    group = project.findProperty("GROUP") as String? ?: "com.spectra.logger"
    version = project.findProperty("VERSION_NAME") as String? ?: "0.0.1-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
    }
}

// Apply quality tools globally but apply them lazily
val excludedProjects = listOf(
    "android-native",
    "ios-native",
    "kmp-shared",
    "app"
)

subprojects {
    if (project.name !in excludedProjects && !project.path.contains("examples")) {
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            apply(plugin = "org.jlleitschuh.gradle.ktlint")
            apply(plugin = "io.gitlab.arturbosch.detekt")

            extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
                version.set("1.1.1")
                android.set(true)
                outputToConsole.set(true)
                ignoreFailures.set(false)
                filter {
                    exclude("**/generated/**")
                    exclude("**/build/**")
                }
            }

            extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
            }

            // Configure reports on Detekt tasks instead of extension to avoid deprecation
            tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
                reports {
                    html.required.set(true)
                    xml.required.set(true)
                }
            }

            dependencies {
                "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.5")
            }
        }
    }
}

// Dokka configuration
tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
