package com.example.debttracker.data.local.entity

/**
 * Платёж. Доменная модель, приходящая с сервера (см. PaymentResponse).
 */
data class Payment(
    val id: Long = 0L,
    val amount: Long,
    val dateMillis: Long,
    val debtId: Long
)
