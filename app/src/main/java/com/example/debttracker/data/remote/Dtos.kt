package com.example.debttracker.data.remote

/**
 * DTO-модели, совпадающие по полям с JSON-контрактом бэкенда
 * (https://github.com/evgeny1101/DebtTrackerAPI).
 */

// ── Auth ──

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class LogoutRequest(
    val refreshToken: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "bearer"
)

// ── Debts ──

data class DebtCreateRequest(
    val name: String,
    val debtType: String,
    val initialAmount: Long,
    val createdAt: Long? = null,
    val reminderIntervalDays: Int? = null
)

data class DebtUpdateRequest(
    val name: String,
    val initialAmount: Long,
    val createdAt: Long? = null,
    val reminderIntervalDays: Int? = null
)

data class CurrentAmountRequest(
    val amount: Long
)

data class ReminderTimestampRequest(
    val timestamp: Long
)

data class DebtResponse(
    val id: Long,
    val name: String,
    val type: String,
    val initialAmount: Long,
    val currentAmount: Long,
    val createdAt: Long,
    val reminderIntervalDays: Int? = null,
    val lastReminderTimestamp: Long? = null
)

// ── Payments ──

data class PaymentCreateRequest(
    val amount: Long,
    val dateMillis: Long? = null
)

data class PaymentResponse(
    val id: Long,
    val amount: Long,
    val dateMillis: Long,
    val debtId: Long
)

data class PaymentDeleteResponse(
    val id: Long,
    val amount: Long,
    val debtId: Long,
    val currentAmount: Long
)
