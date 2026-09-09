package com.example.debttracker.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.data.repository.Repository
import com.example.debttracker.ui.utils.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _mainState = MutableStateFlow(MainScreenState())
    val mainState: StateFlow<MainScreenState> = _mainState.asStateFlow()

    companion object {
        private const val TAG = "MainViewModel"

        fun factory(application: Application, repository: Repository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(application, repository) as T
                }
            }
        }
    }

    init {
        loadDebts()
    }

    fun loadDebts() {
        Log.d(TAG, "loadDebts() called")
        _mainState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val debts = repository.getDebts()
                _mainState.update {
                    it.copy(
                        isLoading = false,
                        debtList = debts.filter { d -> d.currentAmount != 0L }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadDebts() failed: ${e.message}")
                _mainState.update {
                    it.copy(isLoading = false, errorMessage = Repository.parseErrorMessage(e))
                }
            }
        }
    }

    fun createDebt(
        initialAmount: Long,
        name: String,
        debtType: DebtType,
        date: Long?,
        reminderIntervalDays: Int?
    ) {
        Log.d(TAG, "createDebt() called with initialAmount=$initialAmount, name='$name', date=$date, reminderIntervalDays=$reminderIntervalDays")
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.createDebt(name, initialAmount, debtType, date, reminderIntervalDays)
                if (reminderIntervalDays == -1) {
                    ReminderWorker.enqueueImmediateTest(context)
                }
                loadDebts()
            } catch (e: Exception) {
                Log.e(TAG, "createDebt() failed: ${e.message}")
                _mainState.update {
                    it.copy(isLoading = false, errorMessage = Repository.parseErrorMessage(e))
                }
            }
        }
    }

    fun showDebtDialog() {
        Log.d(TAG, "showDebtDialog() called")
        _mainState.update { it.copy(showDebtDialog = true) }
    }

    fun confirmAddDebt(
        amount: Long,
        name: String,
        debtType: DebtType,
        date: Long?,
        reminderIntervalDays: Int?
    ) {
        Log.d(TAG, "confirmAddDebt() called with amount=$amount, name='$name', date=$date, reminderIntervalDays=$reminderIntervalDays")
        _mainState.update { it.copy(showDebtDialog = false) }
        createDebt(amount, name, debtType, date, reminderIntervalDays)
    }

    fun dismissDialogs() {
        Log.d(TAG, "dismissDialogs() called")
        _mainState.update { it.copy(showDebtDialog = false) }
    }

    fun logout() {
        Log.d(TAG, "logout() called")
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                Log.e(TAG, "logout() failed: ${e.message}")
            }
        }
    }
}
