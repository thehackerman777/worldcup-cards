pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
        maven("https://repo1.maven.org/maven2")
    }
    plugins {
        kotlin("android") version "2.1.0"
        kotlin("plugin.compose") version "2.1.0"
        kotlin("plugin.serialization") version "2.1.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WorldCupCards"
include(":app")
