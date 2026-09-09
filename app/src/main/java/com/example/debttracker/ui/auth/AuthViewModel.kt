package com.example.debttracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.debttracker.SessionManager
import com.example.debttracker.data.repository.Repository
import com.example.debttracker.ui.theme.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: Repository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.update { it.copy(errorMessage = AppStrings.authEmptyFieldsError) }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.login(username.trim(), password)
                sessionManager.onLoggedIn()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = mapAuthError(e))
                }
            }
        }
    }

    fun register(username: String, password: String) {
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
                sessionManager.onLoggedIn()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = mapAuthError(e))
                }
            }
        }
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
        fun factory(repository: Repository, sessionManager: SessionManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(repository, sessionManager) as T
                }
            }
    }
}
