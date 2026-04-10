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

    fun createDebt(initialAmount: Double, name: String) {
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                withContext(Dispatchers.IO) { repository.createOrUpdateDebt(initialAmount, name) }
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

    fun recordPayment(amount: Double) {
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true, paymentError = null) }
            try {
                withContext(Dispatchers.IO) { repository.recordPayment(amount) }
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

    fun onPaymentClick() {
        //TODO только сигнал в UI «открыть диалог платежа» (событие/флаг).
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true) }
            recordPayment(200000.0)
            _mainState.update { it.copy(isLoading = false) }
        }

    }

    fun onAddDebtClick() {
        //TODO только сигнал в UI «открыть диалог добавления долга» (событие/флаг).
        //временно хардкод добавления 3млн
        createDebt(3000000.0, "Квартира")
    }

    fun clearError() {
        _mainState.update { it.copy(errorMessage = null) }
    }

    fun clearPaymentError() {
        _mainState.update { it.copy(paymentError = null) }
    }
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