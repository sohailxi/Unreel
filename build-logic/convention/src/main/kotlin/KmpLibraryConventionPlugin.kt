import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for KMP library modules that include Android as a target.
 * Used by shared/data, shared/presentation, and similar modules.
 *
 * Note: The android {} block is configured in each module's build.gradle.kts
 * because the KMP multiplatform library plugin API differs from the standard
 * android library plugin and the DSL is not available in convention plugins
 * compiled with kotlin-dsl.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<KotlinMultiplatformExtension> {
                listOf(
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = project.name
                        isStatic = true
                    }
                }

                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.findLibrary("coroutines-core").get())
                        implementation(libs.findLibrary("datetime").get())
                        implementation(libs.findLibrary("serialization-json").get())
                        implementation(libs.findLibrary("kermit").get())
                        implementation(libs.findLibrary("koin-core").get())
                    }
                    commonTest.dependencies {
                        implementation(libs.findLibrary("kotlin-test").get())
                        implementation(libs.findLibrary("coroutines-test").get())
                        implementation(libs.findLibrary("turbine").get())
                        implementation(libs.findLibrary("kotest-property").get())
                        implementation(libs.findLibrary("kotest-assertions-core").get())
                    }
                }
            }
        }
    }
}

private val Project.libs
    get() = extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
        .named("libs")
