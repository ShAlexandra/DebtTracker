package com.example.debttracker.data.remote

import android.content.Context
import android.content.SharedPreferences

/**
 * Хранилище JWT-токенов в SharedPreferences.
 */
class AuthTokenStore(context: Context) : TokenStore {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    override var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    override var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    override fun isLoggedIn(): Boolean = accessToken != null || refreshToken != null

    override fun saveTokens(access: String, refresh: String) {
        prefs.edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}

