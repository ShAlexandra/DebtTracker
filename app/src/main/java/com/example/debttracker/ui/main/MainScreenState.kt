package com.example.debttracker.ui.main

import com.example.debttracker.data.local.entity.Debt

data class MainScreenState(
    val isLoading: Boolean = false,
    val name: String = "",
    val debtList: List<Debt>? = null,
    val defaultPaymentAmount: Double? = null,
    val errorMessage: String? = null,
    val paymentError: String? = null
)
