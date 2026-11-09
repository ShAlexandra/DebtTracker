package com.example.debttracker.data.repository

import com.example.debttracker.BuildConfig
import com.example.debttracker.data.local.DebtCache
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.data.local.entity.Payment
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
import com.example.debttracker.data.remote.TokenStore
import com.example.debttracker.data.remote.VkLoginRequest
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/** Список долгов; [isOffline] — сервер недоступен, показана сохранённая копия от [savedAt]. */
data class DebtsSnapshot(val debts: List<Debt>, val isOffline: Boolean, val savedAt: Long?)

/** Долг с платежами; [payments] == null — история платежей без связи недоступна. */
data class DebtDetails(val debt: Debt, val payments: List<Payment>?, val isOffline: Boolean)

class Repository(
    private val api: DebtTrackerApi,
    private val tokenStore: TokenStore,
    private val cache: DebtCache? = null
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

    suspend fun loginWithVk(code: String, codeVerifier: String, state: String, deviceId: String) {
        val response = api.loginWithVk(
            VkLoginRequest(
                code = code,
                redirectUri = BuildConfig.VK_ID_REDIRECT_URI,
                codeVerifier = codeVerifier,
                deviceId = deviceId,
                state = state
            )
        )
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
        cache?.clear()
    }

    suspend fun deleteAccount() {
        api.deleteAccount()
        tokenStore.clear()
        cache?.clear()
    }

    // ── Debts ──

    suspend fun getDebts(): List<Debt> =
        api.getDebts().map { it.toDebt() }

    /**
     * Список долгов с синхронизацией кэша: при успехе сохраняет свежие долги и платежи,
     * при недоступном сервере отдаёт последнюю сохранённую копию (если она есть).
     */
    suspend fun loadDebts(): DebtsSnapshot {
        try {
            val debts = api.getDebts()
            val payments = try {
                api.getAllPayments()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            val now = System.currentTimeMillis()
            withContext(Dispatchers.IO) { cache?.save(debts, payments, now) }
            return DebtsSnapshot(debts.map { it.toDebt() }, isOffline = false, savedAt = now)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val snapshot = if (isServerUnavailable(e)) {
                withContext(Dispatchers.IO) { cache?.load() }
            } else {
                null
            }
            if (snapshot == null) throw e
            return DebtsSnapshot(
                snapshot.debts.map { it.toDebt() },
                isOffline = true,
                savedAt = snapshot.savedAt
            )
        }
    }

    /** Долг и его платежи; без связи берёт данные из кэша. null — долг не найден. */
    suspend fun loadDebtDetails(id: Long): DebtDetails? {
        try {
            val debt = api.getDebt(id).toDebt()
            val payments = api.getPayments(id).map { it.toPayment() }
            return DebtDetails(debt, payments, isOffline = false)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (isServerUnavailable(e)) {
                return cachedDetails(id) ?: throw e
            }
            if (e is HttpException) return null
            throw e
        }
    }

    private suspend fun cachedDetails(id: Long): DebtDetails? {
        val snapshot = withContext(Dispatchers.IO) { cache?.load() } ?: return null
        val debt = snapshot.debts.firstOrNull { it.id == id } ?: return null
        val payments = snapshot.payments?.let { byDebt ->
            byDebt[id].orEmpty().sortedByDescending { it.dateMillis }.map { it.toPayment() }
        }
        return DebtDetails(debt.toDebt(), payments, isOffline = true)
    }

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
        /** Нет связи, таймаут или сбой на стороне сервера (5xx) — можно показать сохранённую копию. */
        fun isServerUnavailable(t: Throwable): Boolean =
            t is IOException || (t is HttpException && t.code() in 500..599)

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
