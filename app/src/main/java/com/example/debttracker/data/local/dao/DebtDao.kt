package com.example.debttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.debttracker.data.local.entity.Debt

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    fun getDebtById(id: Long): Debt?

    @Query("SELECT * FROM debts")
    fun getDebtList(): List<Debt>?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDebt(debt: Debt): Long

    @Query("UPDATE debts SET currentAmount = :amount WHERE id = :id")
    suspend fun updateCurrentAmount(id: Long, amount: Long)
}