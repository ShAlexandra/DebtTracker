package com.example.debttracker.ui.main

import com.example.debttracker.data.local.entity.Debt

data class MainScreenState(
    val isLoading: Boolean = false,
    val name: String = "",
    val isDebtExist: Boolean = false,
    val debt: Debt? = null,
    val defaultPaymentAmount: Double? = null
)
