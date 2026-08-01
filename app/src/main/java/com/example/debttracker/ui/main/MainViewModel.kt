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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        observeDebtList()
    }

    private fun observeDebtList() {
        Log.d(TAG, "observeDebtList() started")
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true) }
            try {
                repository.getDebtListFlow().collect { debts ->
                    _mainState.update {
                        it.copy(
                            isLoading = false,
                            debtList = debts.filter { d -> d.currentAmount != 0L }
                        )
                    }
                }
            } catch (e: Exception) {
                _mainState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load debt data"
                    )
                }
            }
        }
    }

    fun createDebt(initialAmount: Long, name: String, debtType: DebtType, date: Long?, reminderIntervalDays: Int?) {
        Log.d(TAG, "createDebt() called with initialAmount=$initialAmount, name='$name', date=$date, reminderIntervalDays=$reminderIntervalDays")
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                withContext(Dispatchers.IO) {
                    repository.createOrUpdateDebt(
                        initialAmount,
                        name,
                        debtType,
                        date,
                        reminderIntervalDays = reminderIntervalDays
                    )
                }
                if (reminderIntervalDays == -1) {
                    ReminderWorker.enqueueImmediateTest(context)
                }
                _mainState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _mainState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to create debt"
                    )
                }
            }
        }
    }

    fun showDebtDialog() {
        Log.d(TAG, "showDebtDialog() called")
        _mainState.update { it.copy(showDebtDialog = true) }
    }

    fun confirmAddDebt(amount: Long, name: String, debtType: DebtType, date: Long?, reminderIntervalDays: Int?) {
        Log.d(TAG, "confirmAddDebt() called with amount=$amount, name='$name', date=$date, reminderIntervalDays=$reminderIntervalDays")
        _mainState.update { it.copy(isLoading = true) }
        createDebt(amount, name, debtType, date, reminderIntervalDays)
        _mainState.update { it.copy(isLoading = false, showDebtDialog = false) }
    }

    fun dismissDialogs() {
        Log.d(TAG, "dismissDialogs() called")
        _mainState.update { it.copy(showDebtDialog = false) }
    }
}