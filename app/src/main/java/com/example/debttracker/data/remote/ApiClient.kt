package com.example.debttracker.data.remote

import com.example.debttracker.BuildConfig
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    val BASE_URL: String = BuildConfig.API_BASE_URL
}

/**
 * Собирает OkHttp + Retrofit, добавляет заголовок Authorization и
 * автоматическое обновление access-токена при 401.
 */
class ApiClient(
    private val tokenStore: TokenStore,
    private val onSessionExpired: () -> Unit = {}
) {

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Клиент без authenticator — используется для вызова refresh, чтобы не зациклиться.
    private val rawClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val authInterceptor = Interceptor { chain ->
        val token = tokenStore.accessToken
        val path = chain.request().url.encodedPath
        val isAuthRoute = path.endsWith("/auth/login") ||
            path.endsWith("/auth/register") ||
            path.endsWith("/auth/refresh")

        val request = if (token != null && !isAuthRoute) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val authenticator = Authenticator { _, response ->
        // Если исходный запрос не был авторизован — не пытаемся обновлять токен.
        if (response.request.header("Authorization") == null) {
            return@Authenticator null
        }
        if (responseCount(response) >= 2) {
            tokenStore.clear()
            onSessionExpired()
            return@Authenticator null
        }

        val refreshToken = tokenStore.refreshToken
        if (refreshToken == null) {
            onSessionExpired()
            return@Authenticator null
        }

        val refreshRequest = Request.Builder()
            .url(ApiConfig.BASE_URL + "auth/refresh")
            .post(gson.toJson(RefreshRequest(refreshToken)).toRequestBody(jsonMediaType))
            .build()

        val refreshResponse = rawClient.newCall(refreshRequest).execute()
        if (!refreshResponse.isSuccessful) {
            refreshResponse.close()
            tokenStore.clear()
            onSessionExpired()
            return@Authenticator null
        }

        val body = refreshResponse.body?.string().orEmpty()
        refreshResponse.close()
        val tokenResponse = gson.fromJson(body, TokenResponse::class.java)
        tokenStore.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)

        response.request.newBuilder()
            .header("Authorization", "Bearer ${tokenResponse.accessToken}")
            .build()
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .authenticator(authenticator)
        .build()

    val api: DebtTrackerApi = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(DebtTrackerApi::class.java)

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
