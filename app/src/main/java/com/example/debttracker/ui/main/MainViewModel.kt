package com.example.debttracker.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.DebtType
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

    companion object {
        private const val TAG = "MainViewModel"
    }

    init {
        loadDebtList()
    }

    fun loadDebtList() {
        Log.d(TAG, "loadDebtList() called")
        viewModelScope.launch {
            _mainState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    paymentError = null
                )
            }
            try {
                val debtList = withContext(Dispatchers.IO) {
                    repository.getDebtList()?.filter { it.currentAmount != 0L }
                }
                _mainState.update {
                    it.copy(
                        isLoading = false,
                        debtList = debtList
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

    fun createDebt(initialAmount: Long, name: String, debtType: DebtType, date: Long?) {
        Log.d(TAG, "createDebt() called with initialAmount=$initialAmount, name='$name', date=$date")
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                withContext(Dispatchers.IO) {
                    repository.createOrUpdateDebt(
                        initialAmount,
                        name,
                        debtType,
                        date,
                    )
                }
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

    fun recordPayment(debtId: Long, amount: Long, date: Long?) {
        Log.d(TAG, "recordPayment() called with debtId=$debtId, amount=$amount, date=$date")
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true, paymentError = null) }
            try {
                withContext(Dispatchers.IO) { repository.recordPayment(debtId, amount, date) }
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

    fun confirmAddPayment(debtId: Long, amount: Long, date: Long?) {
        Log.d(TAG, "confirmAddPayment() called with debtId=$debtId, amount=$amount, date=$date")
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true) }
            recordPayment(debtId, amount, date)
            _mainState.update { it.copy(isLoading = false, showPaymentDialog = false) }
        }

    }

    fun showPaymentDialog(debt: Debt) {
        Log.d(TAG, "showPaymentDialog() called for debtId=${debt.id}, name='${debt.name}', currentAmount=${debt.currentAmount}")
        _mainState.update { it.copy(currentDebt = debt, showPaymentDialog = true) }
    }

    fun showDebtDialog() {
        Log.d(TAG, "showDebtDialog() called")
        _mainState.update { it.copy(showDebtDialog = true) }
    }

    fun confirmAddDebt(amount: Long, name: String, debtType: DebtType, date: Long?) {
        Log.d(TAG, "confirmAddDebt() called with amount=$amount, name='$name', date=$date")
        _mainState.update { it.copy(isLoading = true) }
        createDebt(amount, name, debtType, date)
        _mainState.update { it.copy(isLoading = false, showDebtDialog = false) }
    }

    fun dismissDialogs() {
        Log.d(TAG, "dismissDialogs() called")
        _mainState.update { it.copy(showPaymentDialog = false, showDebtDialog = false) }
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