package com.example.debttracker

import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.data.remote.ApiClient
import com.example.debttracker.data.remote.TokenStore
import com.example.debttracker.data.repository.Repository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

class InMemoryTokenStore : TokenStore {
    override var accessToken: String? = null
    override var refreshToken: String? = null
    override fun isLoggedIn(): Boolean = accessToken != null || refreshToken != null
    override fun saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }
    override fun clear() {
        accessToken = null
        refreshToken = null
    }
}

/**
 * Интеграционный тест против реального сервера (https://45.132.185.161:8443).
 * Проверяет полный жизненный цикл долга и платежей через тот же код,
 * который использует приложение (ApiClient + Repository).
 */
class ApiIntegrationTest {

    private lateinit var tokenStore: InMemoryTokenStore
    private lateinit var apiClient: ApiClient
    private lateinit var repository: Repository

    @Before
    fun setUp() {
        assumeTrue(
            "Integration tests disabled: set API_BASE_URL in secrets.properties",
            !BuildConfig.API_BASE_URL.contains("example.invalid")
        )
        tokenStore = InMemoryTokenStore()
        apiClient = ApiClient(tokenStore)
        repository = Repository(apiClient.api, tokenStore)
    }

    @Test
    fun fullDebtLifecycleAgainstLiveServer() = runBlocking {
        val username = "ci_${System.currentTimeMillis()}"
        val password = "ci_password_123"

        // 1. Регистрация (или вход, если такой пользователь уже есть)
        try {
            repository.register(username, password)
        } catch (e: Exception) {
            repository.login(username, password)
        }
        assertTrue("should be logged in after register/login", tokenStore.isLoggedIn())

        // 2. Создание долга
        val created = repository.createDebt(
            name = "CI test debt",
            initialAmount = 1000L,
            type = DebtType.OWE_ME,
            createdAt = System.currentTimeMillis(),
            reminderIntervalDays = 7
        )
        val debtId = requireNotNull(created.id) { "created debt id must not be null" }
        assertEquals(1000L, created.initialAmount)
        assertEquals(1000L, created.currentAmount)
        assertEquals(DebtType.OWE_ME, created.type)

        // 3. Список долгов и получение по id
        assertTrue("created debt should be in list", repository.getDebts().any { it.id == debtId })
        assertEquals("CI test debt", repository.getDebtById(debtId)!!.name)

        // 4. Запись платежа — currentAmount уменьшается
        val payment = repository.recordPayment(debtId, 400L)
        assertEquals(600L, repository.getDebtById(debtId)!!.currentAmount)

        // 5. Список платежей
        val payments = repository.getPaymentsForDebt(debtId)
        assertEquals(1, payments.size)
        assertEquals(400L, payments[0].amount)

        // 6. Редактирование долга — сервер пересчитывает currentAmount (2000 - 400)
        repository.updateDebt(debtId, "CI test debt updated", 2000L, created.createdAt, 30)
        val updated = repository.getDebtById(debtId)!!
        assertEquals("CI test debt updated", updated.name)
        assertEquals(2000L, updated.initialAmount)
        assertEquals(1600L, updated.currentAmount)

        // 7. Удаление платежа — currentAmount восстанавливается
        repository.deletePayment(debtId, payment.id)
        assertEquals(2000L, repository.getDebtById(debtId)!!.currentAmount)

        // 8. Обновление timestamp напоминания + список долгов с напоминаниями
        repository.updateReminderTimestamp(debtId, System.currentTimeMillis())
        assertTrue("debt should be in with-reminders list", repository.getDebtsWithReminders().any { it.id == debtId })

        // 9. Удаление долга
        repository.deleteDebt(debtId)
        assertNull("debt should be gone after delete", repository.getDebtById(debtId))

        // 10. Выход — токены очищаются
        repository.logout()
        assertTrue("tokens should be cleared after logout", !tokenStore.isLoggedIn())
    }
}
