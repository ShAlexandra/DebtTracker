package com.example.debttracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.debttracker.SessionManager
import com.example.debttracker.data.CredentialsStore
import com.example.debttracker.data.repository.Repository
import com.example.debttracker.ui.theme.AppStrings
import com.vk.id.auth.AuthCodeData
import com.vk.id.auth.VKIDAuthUiParams
import com.vk.id.VKIDAuthFail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val savedUsername: String = "",
    val savedPassword: String = ""
)

class AuthViewModel(
    private val repository: Repository,
    private val sessionManager: SessionManager,
    private val credentialsStore: CredentialsStore
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val pendingVkAuth = VkIdAuth.generate()
    private var vkAuthHandled = false

    /**
     * Параметры для кнопки OneTap. Передача codeChallenge переводит VK ID SDK в режим
     * "обмен кода на бэкенде" — onAuth не вызывается, вместо него сработает onAuthCode.
     */
    val vkAuthParams: VKIDAuthUiParams = VKIDAuthUiParams {
        codeChallenge = pendingVkAuth.codeChallenge
        state = pendingVkAuth.state
    }

    init {
        credentialsStore.load()?.let { (username, password) ->
            _state.update { it.copy(savedUsername = username, savedPassword = password) }
        }
    }

    fun onVkAuthCode(data: AuthCodeData) {
        if (vkAuthHandled) return
        vkAuthHandled = true
        val pending = pendingVkAuth
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.loginWithVk(
                    code = data.code,
                    codeVerifier = pending.codeVerifier,
                    state = pending.state,
                    deviceId = data.deviceId
                )
                credentialsStore.clear()
                sessionManager.onLoggedIn()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                vkAuthHandled = false
                _state.update { it.copy(isLoading = false, errorMessage = mapVkError(e)) }
            }
        }
    }

    private fun mapVkError(e: Throwable): String = when {
        e is HttpException && e.code() == 401 -> "Не удалось войти через VK. Попробуйте ещё раз"
        e is HttpException && e.code() == 429 -> "Слишком много попыток, повторите через минуту"
        e is HttpException && e.code() == 502 -> "VK ID временно недоступен, попробуйте позже"
        e is HttpException && e.code() == 503 -> "Вход через VK сейчас недоступен"
        else -> Repository.parseErrorMessage(e)
    }

    fun onVkAuthFail(fail: VKIDAuthFail) {
        vkAuthHandled = false
        if (fail is VKIDAuthFail.Canceled) return
        _state.update { it.copy(isLoading = false, errorMessage = "Не удалось войти через VK ID") }
    }

    fun login(username: String, password: String, rememberMe: Boolean) {
        if (username.isBlank() || password.isBlank()) {
            _state.update { it.copy(errorMessage = AppStrings.authEmptyFieldsError) }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.login(username.trim(), password)
                onAuthSuccess(username.trim(), password, rememberMe)
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = mapAuthError(e))
                }
            }
        }
    }

    fun register(username: String, password: String, rememberMe: Boolean) {
        if (username.isBlank() || password.isBlank()) {
            _state.update { it.copy(errorMessage = AppStrings.authEmptyFieldsError) }
            return
        }
        if (password.length < 6) {
            _state.update { it.copy(errorMessage = AppStrings.authPasswordShortError) }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.register(username.trim(), password)
                onAuthSuccess(username.trim(), password, rememberMe)
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = mapAuthError(e))
                }
            }
        }
    }

    private fun onAuthSuccess(username: String, password: String, rememberMe: Boolean) {
        if (rememberMe) {
            credentialsStore.save(username, password)
        } else {
            credentialsStore.clear()
        }
        sessionManager.onLoggedIn()
        _state.update { it.copy(isLoading = false) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun mapAuthError(e: Throwable): String = when {
        e is HttpException && e.code() == 401 -> "Неверный логин или пароль"
        e is HttpException && e.code() == 409 -> "Имя пользователя уже занято"
        else -> Repository.parseErrorMessage(e)
    }

    companion object {
        fun factory(
            repository: Repository,
            sessionManager: SessionManager,
            credentialsStore: CredentialsStore
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(repository, sessionManager, credentialsStore) as T
                }
            }
    }
}

