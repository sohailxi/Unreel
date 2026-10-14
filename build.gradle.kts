plugins {
    // Declare plugins at root level without applying them,
    // so subprojects' classloaders don't load them multiple times.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
}

tasks.register<Exec>("installGitHooks") {
    description = "Configures git to use repository hooks in .githooks for Conventional Commits validation"
    group = "help"
    commandLine("git", "config", "core.hooksPath", ".githooks")
    doLast {
        println("Git hooks installed successfully: core.hooksPath set to .githooks")
    }
}