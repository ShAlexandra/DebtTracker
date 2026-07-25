package com.example.debttracker.ui.debtDetails

import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.Payment

data class DebtDetailsState(
    val debt: Debt,
    val paymentList: List<Payment> = listOf(),
)
