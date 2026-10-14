plugins {
    id("unreel.kmp.domain")
}

dependencies {
    commonMainImplementation(project(":shared:core-model"))
    commonMainImplementation(project(":shared:core-time"))
    commonMainImplementation(project(":shared:domain"))
    commonMainImplementation(libs.coroutines.test)
    commonMainImplementation(libs.turbine)
}
