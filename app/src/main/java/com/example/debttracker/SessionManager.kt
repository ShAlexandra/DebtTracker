package com.example.debttracker

import com.example.debttracker.data.remote.AuthTokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Хранит состояние авторизации в UI. Обновляется после логина/логаута,
 * а также при сбросе сессии из-за истёкшего refresh-токена.
 */
class SessionManager(private val tokenStore: AuthTokenStore) {

    private val _isLoggedIn = MutableStateFlow(tokenStore.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun onLoggedIn() {
        _isLoggedIn.value = true
    }

    fun onLoggedOut() {
        _isLoggedIn.value = false
    }
}
