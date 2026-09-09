package com.example.debttracker.data.remote

/**
 * Абстракция над хранилищем JWT-токенов.
 * Реализация в Android — [AuthTokenStore] (SharedPreferences),
 * в тестах можно подставить in-memory реализацию.
 */
interface TokenStore {
    var accessToken: String?
    var refreshToken: String?
    fun isLoggedIn(): Boolean
    fun saveTokens(access: String, refresh: String)
    fun clear()
}
