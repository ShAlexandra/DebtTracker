package com.example.debttracker.data.repository

import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.data.local.entity.Payment
import com.example.debttracker.data.remote.AuthTokenStore
import com.example.debttracker.data.remote.CurrentAmountRequest
import com.example.debttracker.data.remote.DebtCreateRequest
import com.example.debttracker.data.remote.DebtResponse
import com.example.debttracker.data.remote.DebtTrackerApi
import com.example.debttracker.data.remote.DebtUpdateRequest
import com.example.debttracker.data.remote.LoginRequest
import com.example.debttracker.data.remote.LogoutRequest
import com.example.debttracker.data.remote.PaymentCreateRequest
import com.example.debttracker.data.remote.PaymentResponse
import com.example.debttracker.data.remote.RegisterRequest
import com.example.debttracker.data.remote.ReminderTimestampRequest
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

class Repository(
    private val api: DebtTrackerApi,
    private val tokenStore: AuthTokenStore
) {

    fun isLoggedIn(): Boolean = tokenStore.isLoggedIn()

    // ── Auth ──

    suspend fun login(username: String, password: String) {
        val response = api.login(LoginRequest(username, password))
        tokenStore.saveTokens(response.accessToken, response.refreshToken)
    }

    suspend fun register(username: String, password: String) {
        val response = api.register(RegisterRequest(username, password))
        tokenStore.saveTokens(response.accessToken, response.refreshToken)
    }

    suspend fun logout() {
        tokenStore.refreshToken?.let { refresh ->
            try {
                api.logout(LogoutRequest(refresh))
            } catch (_: Exception) {
                // Сеть может быть недоступна — токен всё равно чистим локально.
            }
        }
        tokenStore.clear()
    }

    // ── Debts ──

    suspend fun getDebts(): List<Debt> =
        api.getDebts().map { it.toDebt() }

    suspend fun getDebtById(id: Long): Debt? =
        try {
            api.getDebt(id).toDebt()
        } catch (_: HttpException) {
            null
        }

    suspend fun getDebtsWithReminders(): List<Debt> =
        api.getDebtsWithReminders().map { it.toDebt() }

    suspend fun createDebt(
        name: String,
        initialAmount: Long,
        type: DebtType,
        createdAt: Long? = null,
        reminderIntervalDays: Int? = null
    ): Debt = api.createDebt(
        DebtCreateRequest(
            name = name,
            debtType = type.name,
            initialAmount = initialAmount,
            createdAt = createdAt,
            reminderIntervalDays = reminderIntervalDays
        )
    ).toDebt()

    suspend fun updateDebt(
        id: Long,
        name: String,
        initialAmount: Long,
        createdAt: Long,
        reminderIntervalDays: Int?
    ): Debt = api.updateDebt(
        id = id,
        body = DebtUpdateRequest(
            name = name,
            initialAmount = initialAmount,
            createdAt = createdAt,
            reminderIntervalDays = reminderIntervalDays
        )
    ).toDebt()

    suspend fun updateCurrentAmount(id: Long, amount: Long): Debt =
        api.updateCurrentAmount(id, CurrentAmountRequest(amount)).toDebt()

    suspend fun updateReminderTimestamp(id: Long, timestamp: Long) {
        api.updateReminderTimestamp(id, ReminderTimestampRequest(timestamp))
    }

    suspend fun deleteDebt(id: Long) {
        api.deleteDebt(id)
    }

    // ── Payments ──

    suspend fun getPaymentsForDebt(debtId: Long): List<Payment> =
        api.getPayments(debtId).map { it.toPayment() }

    suspend fun recordPayment(debtId: Long, amount: Long, dateMillis: Long? = null): Payment =
        api.recordPayment(debtId, PaymentCreateRequest(amount, dateMillis)).toPayment()

    suspend fun deletePayment(debtId: Long, paymentId: Long) {
        api.deletePayment(debtId, paymentId)
    }

    // ── Mapping ──

    private fun DebtResponse.toDebt() = Debt(
        id = id,
        name = name,
        type = DebtType.valueOf(type),
        initialAmount = initialAmount,
        currentAmount = currentAmount,
        createdAt = createdAt,
        reminderIntervalDays = reminderIntervalDays,
        lastReminderTimestamp = lastReminderTimestamp
    )

    private fun PaymentResponse.toPayment() = Payment(
        id = id,
        amount = amount,
        dateMillis = dateMillis,
        debtId = debtId
    )

    companion object {
        /**
         * Преобразует исключение сетевого слоя в человекочитаемое сообщение.
         * Бэкенд возвращает ошибки в формате {"detail": "..."}.
         */
        fun parseErrorMessage(t: Throwable): String {
            if (t is HttpException) {
                val errorBody = try {
                    t.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }
                val detail = errorBody?.let { body ->
                    runCatching {
                        JsonParser.parseString(body).asJsonObject.get("detail")?.asString
                    }.getOrNull()
                }
                return detail ?: "Ошибка сервера (${t.code()})"
            }
            if (t is IOException) {
                return "Нет соединения с сервером"
            }
            return t.message ?: "Неизвестная ошибка"
        }
    }
}
