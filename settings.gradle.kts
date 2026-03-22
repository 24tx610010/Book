pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
<<<<<<< HEAD
=======
        maven { url = uri("https://jitpack.io") } // THÊM DÒNG NÀY ĐỂ TẢI THƯ VIỆN BIỂU ĐỒ
>>>>>>> 0d5c59f (22/3)
    }
}

rootProject.name = "Bài1"
include(":app")
<<<<<<< HEAD
 
=======
>>>>>>> 0d5c59f (22/3)
