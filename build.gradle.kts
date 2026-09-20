// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties

// VK ID: Client ID и Client Secret приложения (id.vk.ru/business), читаются из secrets.properties (файл НЕ коммитится).
val vkIdSecrets = Properties().apply {
    val file = rootProject.file("secrets.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val vkIdClientId = vkIdSecrets.getProperty("VK_ID_CLIENT_ID") ?: ""
val vkIdClientSecret = vkIdSecrets.getProperty("VK_ID_CLIENT_SECRET") ?: ""

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("vkid.manifest.placeholders") version "1.1.0" apply true
}

vkidManifestPlaceholders {
    // vkidRedirectHost станет "vk.ru", а vkidRedirectScheme — "vk$vkIdClientId".
    init(
        clientId = vkIdClientId,
        clientSecret = vkIdClientSecret,
    )
}
