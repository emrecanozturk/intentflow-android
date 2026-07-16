plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
