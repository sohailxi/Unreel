plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.plugins.kotlinMultiplatform.toDep())
    compileOnly(libs.plugins.androidMultiplatformLibrary.toDep())
    compileOnly(libs.plugins.androidApplication.toDep())
    compileOnly(libs.plugins.composeMultiplatform.toDep())
    compileOnly(libs.plugins.composeCompiler.toDep())
    compileOnly(libs.plugins.kotlinSerialization.toDep())
    compileOnly(libs.plugins.sqldelight.toDep())
    compileOnly(libs.plugins.kover.toDep())
    compileOnly(libs.plugins.detekt.toDep())
    compileOnly(libs.plugins.spotless.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("unreelKmpDomain") {
            id = "unreel.kmp.domain"
            implementationClass = "KmpDomainConventionPlugin"
        }
        register("unreelKmpLibrary") {
            id = "unreel.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
    }
}
