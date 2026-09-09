package com.example.debttracker.data.local.entity

/**
 * Долг. Доменная модель, приходящая с сервера (см. DebtResponse).
 */
data class Debt(
    val id: Long? = null,
    val name: String,
    val type: DebtType,
    val initialAmount: Long,
    val currentAmount: Long,
    val createdAt: Long,
    val reminderIntervalDays: Int? = null,
    val lastReminderTimestamp: Long? = null
)
