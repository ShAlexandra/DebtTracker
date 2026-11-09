package com.example.debttracker

import com.example.debttracker.data.local.DebtCache
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.data.remote.DebtTrackerApi
import com.example.debttracker.data.repository.Repository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException

class OfflineCacheTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var server: MockWebServer
    private lateinit var tokenStore: InMemoryTokenStore
    private lateinit var cacheFile: File
    private lateinit var cache: DebtCache
    private lateinit var repository: Repository

    private val debtsJson = """
        [
          {"id":1,"name":"Maria","type":"OWE_ME","initialAmount":500000,"currentAmount":120000,"createdAt":1700000000000,"reminderIntervalDays":null,"lastReminderTimestamp":null},
          {"id":2,"name":"Alex","type":"I_OWE","initialAmount":2000000,"currentAmount":2000000,"createdAt":1700000000001}
        ]
    """.trimIndent()

    private val paymentsJson = """
        [
          {"id":10,"amount":60000,"dateMillis":1000,"debtId":1},
          {"id":11,"amount":300000,"dateMillis":2000,"debtId":1}
        ]
    """.trimIndent()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/api/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DebtTrackerApi::class.java)
        tokenStore = InMemoryTokenStore().apply { saveTokens("access", "refresh") }
        cacheFile = File(tmp.root, "debt_cache.json")
        cache = DebtCache(cacheFile)
        repository = Repository(api, tokenStore, cache)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun json(body: String) =
        MockResponse().setBody(body).setHeader("Content-Type", "application/json")

    private fun enqueueOnlineSync() {
        server.enqueue(json(debtsJson))
        server.enqueue(json(paymentsJson))
    }

    @Test
    fun loadDebts_online_returnsFreshDataAndSavesCache() = runBlocking<Unit> {
        enqueueOnlineSync()

        val snapshot = repository.loadDebts()

        assertFalse(snapshot.isOffline)
        assertEquals(listOf("Maria", "Alex"), snapshot.debts.map { it.name })
        assertEquals(DebtType.OWE_ME, snapshot.debts[0].type)
        val saved = cache.load()
        assertNotNull(saved)
        assertEquals(2, saved!!.debts.size)
        assertEquals(2, saved.payments!![1L]!!.size)
        assertEquals(snapshot.savedAt, saved.savedAt)
    }

    @Test
    fun loadDebts_serverReturns503_usesCachedCopy() = runBlocking<Unit> {
        enqueueOnlineSync()
        val online = repository.loadDebts()
        server.enqueue(MockResponse().setResponseCode(503))

        val offline = repository.loadDebts()

        assertTrue(offline.isOffline)
        assertEquals(online.debts, offline.debts)
        assertEquals(online.savedAt, offline.savedAt)
    }

    @Test
    fun loadDebts_connectionRefused_usesCachedCopy() = runBlocking<Unit> {
        enqueueOnlineSync()
        repository.loadDebts()
        server.shutdown()

        val offline = repository.loadDebts()

        assertTrue(offline.isOffline)
        assertEquals(2, offline.debts.size)
    }

    @Test
    fun loadDebts_cacheSurvivesProcessRestart() = runBlocking<Unit> {
        enqueueOnlineSync()
        repository.loadDebts()
        server.enqueue(MockResponse().setResponseCode(503))

        val restarted = Repository(
            Retrofit.Builder()
                .baseUrl(server.url("/api/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DebtTrackerApi::class.java),
            tokenStore,
            DebtCache(cacheFile)
        )
        val offline = restarted.loadDebts()

        assertTrue(offline.isOffline)
        assertEquals(listOf("Maria", "Alex"), offline.debts.map { it.name })
    }

    @Test
    fun loadDebts_noCache_serverDown_throws() = runBlocking<Unit> {
        server.enqueue(MockResponse().setResponseCode(503))

        try {
            repository.loadDebts()
            fail("expected HttpException")
        } catch (e: HttpException) {
            assertEquals(503, e.code())
        }
    }

    @Test
    fun loadDebts_clientError_isNotHiddenByCache() = runBlocking<Unit> {
        enqueueOnlineSync()
        repository.loadDebts()
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"detail":"bad"}"""))

        try {
            repository.loadDebts()
            fail("a 4xx must surface as an error, not as cached data")
        } catch (e: HttpException) {
            assertEquals(400, e.code())
        }
    }

    @Test
    fun loadDebts_paymentsFailToLoad_keepsDebtsAndMarksPaymentsUnknown() = runBlocking<Unit> {
        server.enqueue(json(debtsJson))
        server.enqueue(MockResponse().setResponseCode(500))

        val online = repository.loadDebts()
        assertFalse(online.isOffline)
        assertNull(cache.load()!!.payments)

        server.enqueue(MockResponse().setResponseCode(503))
        val details = repository.loadDebtDetails(1L)

        assertNotNull(details)
        assertTrue(details!!.isOffline)
        assertNull("payments are unknown, not empty", details.payments)
    }

    @Test
    fun loadDebtDetails_offline_returnsCachedDebtWithPaymentsNewestFirst() = runBlocking<Unit> {
        enqueueOnlineSync()
        repository.loadDebts()
        server.enqueue(MockResponse().setResponseCode(503))

        val details = repository.loadDebtDetails(1L)

        assertNotNull(details)
        assertTrue(details!!.isOffline)
        assertEquals("Maria", details.debt.name)
        assertEquals(listOf(11L, 10L), details.payments!!.map { it.id })
    }

    @Test
    fun loadDebtDetails_offline_debtWithoutPaymentsHasEmptyHistory() = runBlocking<Unit> {
        enqueueOnlineSync()
        repository.loadDebts()
        server.enqueue(MockResponse().setResponseCode(503))

        val details = repository.loadDebtDetails(2L)

        assertEquals(emptyList<Any>(), details!!.payments)
    }

    @Test
    fun loadDebtDetails_offline_unknownDebt_throws() = runBlocking<Unit> {
        enqueueOnlineSync()
        repository.loadDebts()
        server.enqueue(MockResponse().setResponseCode(503))

        try {
            repository.loadDebtDetails(999L)
            fail("expected HttpException")
        } catch (e: HttpException) {
            assertEquals(503, e.code())
        }
    }

    @Test
    fun loadDebtDetails_online_notFound_returnsNull() = runBlocking<Unit> {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"detail":"not found"}"""))

        assertNull(repository.loadDebtDetails(5L))
    }

    @Test
    fun logout_clearsCache() = runBlocking<Unit> {
        enqueueOnlineSync()
        repository.loadDebts()
        server.enqueue(MockResponse().setResponseCode(204))

        repository.logout()

        assertNull(cache.load())
        assertFalse(cacheFile.exists())
    }

    @Test
    fun sessionChanges_clearCache() = runBlocking<Unit> {
        enqueueOnlineSync()
        repository.loadDebts()
        val session = SessionManager(tokenStore, cache)

        session.onLoggedOut()
        assertNull(cache.load())

        enqueueOnlineSync()
        repository.loadDebts()
        session.onLoggedIn()
        assertNull(cache.load())
    }

    @Test
    fun corruptCacheFile_isIgnored() {
        cacheFile.writeText("{not json")

        assertNull(DebtCache(cacheFile).load())
    }

    @Test
    fun isServerUnavailable_classifiesErrors() {
        assertTrue(Repository.isServerUnavailable(IOException("no route")))
        assertFalse(Repository.isServerUnavailable(IllegalStateException("bug")))
    }
}
