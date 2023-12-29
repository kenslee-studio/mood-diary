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
        maven { url  = uri("https://jitpack.io") }
    }
}

rootProject.name = "Multi Module Diary"
include(":app")
include(":core:ui")
include(":core:utils")
include(":data:mongo")
include(":feature:auth")
include(":feature:home")
include(":feature:write")
