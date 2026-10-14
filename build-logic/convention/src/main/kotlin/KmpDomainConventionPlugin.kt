import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for pure Kotlin KMP domain modules.
 * No Android imports allowed — enforced by not applying any Android plugin.
 */
class KmpDomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<KotlinMultiplatformExtension> {
                jvm()
                listOf(
                    iosArm64(),
                    iosSimulatorArm64()
                )

                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.findLibrary("coroutines-core").get())
                        implementation(libs.findLibrary("datetime").get())
                        implementation(libs.findLibrary("serialization-json").get())
                        implementation(libs.findLibrary("kermit").get())
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
