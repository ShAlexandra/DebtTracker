package com.example.debttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.debttracker.data.local.entity.Debt
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    fun getDebtById(id: Long): Debt?

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    fun getDebtByIdFlow(id: Long): Flow<Debt?>

    @Query("SELECT * FROM debts")
    fun getDebtList(): List<Debt>?

    @Query("SELECT * FROM debts")
    fun getDebtListFlow(): Flow<List<Debt>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDebt(debt: Debt): Long

    @Query("UPDATE debts SET currentAmount = :amount WHERE id = :id")
    suspend fun updateCurrentAmount(id: Long, amount: Long)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebt(id: Long)

    @Query("SELECT * FROM debts WHERE reminderIntervalDays IS NOT NULL")
    fun getDebtsWithReminders(): List<Debt>?

    @Query("UPDATE debts SET lastReminderTimestamp = :timestamp WHERE id = :id")
    suspend fun updateReminderTimestamp(id: Long, timestamp: Long)

    @Query("UPDATE debts SET name = :name, initialAmount = :initialAmount, currentAmount = :currentAmount, createdAt = :createdAt, reminderIntervalDays = :reminderIntervalDays WHERE id = :id")
    suspend fun updateDebt(id: Long, name: String, initialAmount: Long, currentAmount: Long, createdAt: Long, reminderIntervalDays: Int?)
}
