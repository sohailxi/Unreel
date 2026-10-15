plugins {
    id("unreel.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "app.unreel.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(project(":shared:core-model"))
            export(project(":shared:core-time"))
            export(project(":shared:domain"))
            export(project(":shared:data"))
            export(project(":shared:presentation"))
        }
        iosTarget.binaries.all {
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:core-model"))
            api(project(":shared:core-time"))
            api(project(":shared:domain"))
            api(project(":shared:data"))
            api(project(":shared:presentation"))
        }
        iosMain.dependencies {
            implementation(libs.multiplatformSettings)
            implementation(libs.sqldelight.native)
        }
        commonTest.dependencies {
            implementation(project(":shared:testing"))
        }
    }
}
