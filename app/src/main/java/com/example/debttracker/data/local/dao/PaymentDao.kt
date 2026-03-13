package com.example.debttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.debttracker.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert
    suspend fun insertPayment(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: Long)

    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun getAllPayments(): Flow<List<Payment>>
}