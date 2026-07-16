pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "intentflow-android"

include(":intentflow-core")
include(":intentflow-ai")
include(":intentflow-generator")
include(":examples:compose-example")
include(":examples:viewmodel-example")
include(":examples:migration-mvvm")
