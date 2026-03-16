package com.example.debttracker.ui.main

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
        loadDebt()
    }

    fun loadDebt() {
        viewModelScope.launch {
            _mainState.update { it.copy(isLoading = true) }
            val debt = withContext(Dispatchers.IO) { repository.getCurrentDebt() }
            _mainState.update {
                it.copy(
                    isLoading = false,
                    isDebtExist = debt != null,
                    debt = debt
                )
            }
        }
    }

    fun onCreateDebt(initialAmount: Double, name: String) {
        viewModelScope.launch {
            repository.createOrUpdateDebt(initialAmount, name)
            loadDebt()
        }
    }

    fun onPaymentClick() {
        //TODO только сигнал в UI «открыть диалог платежа» (событие/флаг).
    }

    fun onAddDebtClick() {
        //TODO только сигнал в UI «открыть диалог добавления долга» (событие/флаг).
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