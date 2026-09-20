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
        // VK ID SDK: репозиторий gradle-плагина vkid.manifest.placeholders.
        maven(url = "https://nexus-external.vkteam.ru/repository/vkid-sdk-android/")
        maven(url = "https://nexus-external.vkteam.ru/repository/maven/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // VK ID SDK: библиотека com.vk.id:* не публикуется в Maven Central.
        maven(url = "https://nexus-external.vkteam.ru/repository/vkid-sdk-android/")
        maven(url = "https://nexus-external.vkteam.ru/repository/maven/")
        maven(url = "https://nexus-external.vkteam.ru/repository/vk-id-captcha/android/")
    }
}

rootProject.name = "DebtTracker"
include(":app")
 