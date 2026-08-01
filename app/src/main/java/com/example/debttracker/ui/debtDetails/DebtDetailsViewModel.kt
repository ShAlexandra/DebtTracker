package com.example.debttracker.ui.debtDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.debttracker.data.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DebtDetailsViewModel(
    private val repository: Repository,
    private val debtId: Long
) : ViewModel() {

    private val _state = MutableStateFlow<DebtDetailsState?>(null)
    val state: StateFlow<DebtDetailsState?> = _state.asStateFlow()

    companion object {
        private const val TAG = "DebtDetailsViewModel"
    }

    init {
        observeDebtAndPayments()
    }

    private fun observeDebtAndPayments() {
        Log.d(TAG, "observeDebtAndPayments() called with debtId=$debtId")
        viewModelScope.launch {
            repository.getDebtByIdFlow(debtId).combine(
                repository.getPaymentsForDebt(debtId)
            ) { debt, payments ->
                Log.d(TAG, "combine emitted: debt=${debt?.name}, payments=${payments.size}")
                if (debt != null) {
                    DebtDetailsState(
                        debt = debt,
                        paymentList = payments
                    )
                } else {
                    null
                }
            }.collect { state ->
                _state.value = state
            }
        }
    }

    fun recordPayment(amount: Long, date: Long?) {
        Log.d(TAG, "recordPayment() called with debtId=$debtId, amount=$amount, date=$date")
        viewModelScope.launch {
            try {
                repository.recordPayment(debtId, amount, date)
            } catch (e: Exception) {
                Log.e(TAG, "recordPayment() failed: ${e.message}")
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
            }
        }
    }

    fun updateDebt(name: String, initialAmount: Long, createdAt: Long, reminderIntervalDays: Int?) {
        Log.d(TAG, "updateDebt() called with id=$debtId, name='$name', initialAmount=$initialAmount, reminderIntervalDays=$reminderIntervalDays")
        viewModelScope.launch {
            try {
                val currentDebt = repository.getCurrentDebt(debtId) ?: return@launch
                val paid = currentDebt.initialAmount - currentDebt.currentAmount
                val newCurrentAmount = (initialAmount - paid).coerceAtLeast(0L)
                repository.updateDebt(
                    id = debtId,
                    name = name,
                    initialAmount = initialAmount,
                    currentAmount = newCurrentAmount,
                    createdAt = createdAt,
                    reminderIntervalDays = reminderIntervalDays
                )
            } catch (e: Exception) {
                Log.e(TAG, "updateDebt() failed: ${e.message}")
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