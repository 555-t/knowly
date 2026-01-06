pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        /*resolutionStrategy {
            eachPlugin {
                if (requested.id.id == "com.android.application") {
                    useModule("com.android.tools.build:gradle:8.13.2")
                }
            }
        }*/
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

rootProject.name = "Knowly"
include(":app")
 