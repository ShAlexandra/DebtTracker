package com.example.debttracker.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DebtTrackerApi {

    // ── Auth ──
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest)

    // ── Debts ──
    @GET("debts")
    suspend fun getDebts(@Query("active") active: Boolean? = null): List<DebtResponse>

    @GET("debts/with-reminders")
    suspend fun getDebtsWithReminders(): List<DebtResponse>

    @GET("debts/{id}")
    suspend fun getDebt(@Path("id") id: Long): DebtResponse

    @POST("debts")
    suspend fun createDebt(@Body body: DebtCreateRequest): DebtResponse

    @PUT("debts/{id}")
    suspend fun updateDebt(@Path("id") id: Long, @Body body: DebtUpdateRequest): DebtResponse

    @PATCH("debts/{id}/current-amount")
    suspend fun updateCurrentAmount(
        @Path("id") id: Long,
        @Body body: CurrentAmountRequest
    ): DebtResponse

    @PATCH("debts/{id}/reminder-timestamp")
    suspend fun updateReminderTimestamp(
        @Path("id") id: Long,
        @Body body: ReminderTimestampRequest
    ): DebtResponse

    @DELETE("debts/{id}")
    suspend fun deleteDebt(@Path("id") id: Long)

    // ── Payments ──
    @GET("debts/{id}/payments")
    suspend fun getPayments(@Path("id") id: Long): List<PaymentResponse>

    @POST("debts/{id}/payments")
    suspend fun recordPayment(
        @Path("id") id: Long,
        @Body body: PaymentCreateRequest
    ): PaymentResponse

    @DELETE("debts/{id}/payments/{paymentId}")
    suspend fun deletePayment(
        @Path("id") id: Long,
        @Path("paymentId") paymentId: Long
    ): PaymentDeleteResponse
}
