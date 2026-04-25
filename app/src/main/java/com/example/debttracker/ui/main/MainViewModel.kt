package com.example.debttracker.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.debttracker.data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _mainState = MutableStateFlow(MainScreenState())
    var mainState: StateFlow<MainScreenState> = _mainState.asStateFlow()

    init {
        loadDebtList()
//        loadDebt()
    }

    fun loadDebtList() {
        viewModelScope.launch {
            _mainState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    paymentError = null
                )
            }
            try {
                val debtList = withContext(Dispatchers.IO) { repository.getDebtList() }
                _mainState.update {
                    it.copy(
                        isLoading = false,
                        debtList = debtList
                    )
                }
                Log.d("debt_list", debtList.toString())
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

    fun loadDebt() {
        viewModelScope.launch {
            _mainState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    paymentError = null
                )
            }
            try {
                val debt = withContext(Dispatchers.IO) { repository.getCurrentDebt() }
                _mainState.update {
                    it.copy(
                        isLoading = false,
//                        isDebtExist = debt != null,
//                        debt = debt
                    )
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

    fun createDebt(initialAmount: Long, name: String, date: Long?) {
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                withContext(Dispatchers.IO) { repository.createOrUpdateDebt(initialAmount, name, date) }
                loadDebtList()
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

    fun recordPayment(debtId: Long, amount: Long) {
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true, paymentError = null) }
            try {
                withContext(Dispatchers.IO) { repository.recordPayment(debtId, amount) }
                loadDebtList()
            } catch (e: Exception) {
                _mainState.update {
                    it.copy(
                        isLoading = false,
                        paymentError = e.message ?: "Failed to add payment"
                    )
                }
            }
        }
    }

    fun onPaymentClick(debtId: Long?) {
        //TODO только сигнал в UI «открыть диалог платежа» (событие/флаг).
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true) }
            debtId?.let { recordPayment(it, 200000) }
            _mainState.update { it.copy(isLoading = false) }
        }

    }

    fun showPaymentDialog() = _mainState.update { it.copy(showPaymentDialog = true) }

    fun showDebtDialog() = _mainState.update { it.copy(showDebtDialog = true) }

    fun confirmAddDebt(amount: Long, name: String, date: Long?) {
        createDebt(amount, name, date)
        _mainState.update { it.copy(showDebtDialog = false) }
    }

    fun clearError() {
        _mainState.update { it.copy(errorMessage = null) }
    }

    fun clearPaymentError() {
        _mainState.update { it.copy(paymentError = null) }
    }

    fun dismissDialogs() = _mainState.update { it.copy(showPaymentDialog = false, showDebtDialog = false) }
}

class MainViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                repository = repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}