plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    api(project(":intentflow-core"))
    testImplementation(kotlin("test"))
}
