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
}
