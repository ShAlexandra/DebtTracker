package com.example.debttracker.ui.main

import com.example.debttracker.data.local.entity.Debt

data class MainScreenState(
    val isLoading: Boolean = false,
    val name: String = "",
    val debtList: List<Debt>? = null,
    val defaultPaymentAmount: Long? = null,
    val errorMessage: String? = null,
    val paymentError: String? = null,
    val showPaymentDialog: Boolean = false,
    val currentDebt: Debt? = null,
    val showDebtDialog: Boolean = false,
)
