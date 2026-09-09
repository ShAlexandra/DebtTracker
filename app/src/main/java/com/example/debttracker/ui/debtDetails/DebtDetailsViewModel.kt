package com.example.debttracker.ui.debtDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.debttracker.data.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DebtDetailsViewModel(
    private val repository: Repository,
    private val debtId: Long
) : ViewModel() {

    private val _state = MutableStateFlow<DebtDetailsState?>(null)
    val state: StateFlow<DebtDetailsState?> = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    companion object {
        private const val TAG = "DebtDetailsViewModel"
    }

    init {
        load()
    }

    fun load() {
        Log.d(TAG, "load() called with debtId=$debtId")
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val debt = repository.getDebtById(debtId)
                if (debt == null) {
                    _state.value = null
                    _errorMessage.value = "Долг не найден"
                    return@launch
                }
                val payments = repository.getPaymentsForDebt(debtId)
                _state.value = DebtDetailsState(debt = debt, paymentList = payments)
            } catch (e: Exception) {
                Log.e(TAG, "load() failed: ${e.message}")
                _errorMessage.value = Repository.parseErrorMessage(e)
            }
        }
    }

    fun recordPayment(amount: Long, date: Long?) {
        Log.d(TAG, "recordPayment() called with debtId=$debtId, amount=$amount, date=$date")
        viewModelScope.launch {
            try {
                repository.recordPayment(debtId, amount, date)
                load()
            } catch (e: Exception) {
                Log.e(TAG, "recordPayment() failed: ${e.message}")
                _errorMessage.value = Repository.parseErrorMessage(e)
            }
        }
    }

    fun deleteDebt(onDeleted: () -> Unit) {
        Log.d(TAG, "deleteDebt() called with debtId=$debtId")
        viewModelScope.launch {
            try {
                repository.deleteDebt(debtId)
                onDeleted()
            } catch (e: Exception) {
                Log.e(TAG, "deleteDebt() failed: ${e.message}")
                _errorMessage.value = Repository.parseErrorMessage(e)
            }
        }
    }

    fun updateDebt(name: String, initialAmount: Long, createdAt: Long, reminderIntervalDays: Int?) {
        Log.d(TAG, "updateDebt() called with id=$debtId, name='$name', initialAmount=$initialAmount, reminderIntervalDays=$reminderIntervalDays")
        viewModelScope.launch {
            try {
                repository.updateDebt(debtId, name, initialAmount, createdAt, reminderIntervalDays)
                load()
            } catch (e: Exception) {
                Log.e(TAG, "updateDebt() failed: ${e.message}")
                _errorMessage.value = Repository.parseErrorMessage(e)
            }
        }
    }
}

class DebtDetailsViewModelFactory(
    private val repository: Repository,
    private val debtId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DebtDetailsViewModel::class.java)) {
            return DebtDetailsViewModel(
                repository = repository,
                debtId = debtId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
