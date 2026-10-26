package com.example.debttracker

import com.example.debttracker.data.remote.DebtTrackerApi
import com.example.debttracker.data.repository.Repository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AccountDeletionTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenStore: InMemoryTokenStore
    private lateinit var repository: Repository

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
        repository = Repository(api, tokenStore)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun deleteAccount_sendsDeleteToAuthMeAndClearsTokens() = runBlocking<Unit> {
        server.enqueue(MockResponse().setResponseCode(204))

        repository.deleteAccount()

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/auth/me", request.path)
        assertFalse("tokens should be cleared after deletion", tokenStore.isLoggedIn())
    }

    @Test
    fun deleteAccount_keepsTokensWhenServerFails() = runBlocking<Unit> {
        server.enqueue(MockResponse().setResponseCode(500))

        try {
            repository.deleteAccount()
            fail("expected HttpException")
        } catch (e: HttpException) {
            assertEquals(500, e.code())
        }

        assertTrue("tokens must stay when deletion failed", tokenStore.isLoggedIn())
    }
}
