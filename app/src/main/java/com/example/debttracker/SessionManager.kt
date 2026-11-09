package com.example.debttracker

import com.example.debttracker.data.local.DebtCache
import com.example.debttracker.data.remote.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Хранит состояние авторизации в UI. Обновляется после логина/логаута,
 * а также при сбросе сессии из-за истёкшего refresh-токена.
 * При каждой смене сессии сохранённая копия долгов сбрасывается, чтобы данные
 * одного аккаунта никогда не показывались в другом.
 */
class SessionManager(
    private val tokenStore: TokenStore,
    private val cache: DebtCache? = null
) {

    private val _isLoggedIn = MutableStateFlow(tokenStore.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun onLoggedIn() {
        cache?.clear()
        _isLoggedIn.value = true
    }

    fun onLoggedOut() {
        cache?.clear()
        _isLoggedIn.value = false
    }
}
