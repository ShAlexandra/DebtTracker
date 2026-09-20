package com.example.debttracker.ui.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * PKCE-параметры для авторизации через VK ID SDK "с обменом кода на бэкенде"
 * (https://id.vk.ru/about/business/go/docs/ru/vkid/latest/vk-id/connection/api-description).
 * codeVerifier и state генерируются на клиенте и передаются в VKIDAuthUiParams;
 * codeChallenge уходит в запрос авторизации, а сам codeVerifier + полученный
 * от SDK код авторизации пересылаются на бэкенд для обмена на токены.
 */
object VkIdAuth {

    data class PendingAuth(
        val codeVerifier: String,
        val codeChallenge: String,
        val state: String
    )

    fun generate(): PendingAuth {
        val codeVerifier = randomUrlSafeString(64)
        // VK ID требует state длиной не менее 32 символов.
        val state = randomUrlSafeString(32)
        return PendingAuth(
            codeVerifier = codeVerifier,
            codeChallenge = codeChallengeFor(codeVerifier),
            state = state
        )
    }

    private fun codeChallengeFor(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return base64UrlSafe(digest)
    }

    private fun randomUrlSafeString(byteLength: Int): String {
        val bytes = ByteArray(byteLength)
        SecureRandom().nextBytes(bytes)
        return base64UrlSafe(bytes)
    }

    private fun base64UrlSafe(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}
