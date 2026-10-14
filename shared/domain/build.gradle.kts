plugins {
    id("unreel.kmp.domain")
}

dependencies {
    commonMainImplementation(project(":shared:core-model"))
    commonMainImplementation(project(":shared:core-time"))
}
