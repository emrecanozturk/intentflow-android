plugins {
    kotlin("jvm")
    application
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":intentflow-ai"))
    implementation(project(":intentflow-core"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("dev.intentflow.generator.IntentFlowGenerate")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
