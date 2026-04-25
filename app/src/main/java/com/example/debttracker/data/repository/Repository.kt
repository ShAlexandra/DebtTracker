package com.example.debttracker.data.repository

import androidx.room.withTransaction
import com.example.debttracker.data.local.database.AppDatabase
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.Payment

class Repository(private val database: AppDatabase) {
    private val debtDao = database.debtDao()
    private val paymentDao = database.paymentDao()

    fun getCurrentDebt(debtId: Long? = null): Debt? = debtId.let { debtDao.getDebtById(it!!) }

    fun getDebtList(): List<Debt>? = debtDao.getDebtList()

    suspend fun createOrUpdateDebt(
        initialAmount: Long,
        name: String,
        date: Long? = null,
        id: Long? = null
    ) {
        if (id == null) {
            debtDao.insertDebt(
                Debt(
                    initialAmount = initialAmount,
                    currentAmount = initialAmount,
                    createdAt = date ?: System.currentTimeMillis(),
                    name = name
                )
            )
        } else {
            val currentDebt = getCurrentDebt(id)
            debtDao.updateCurrentAmount(id = currentDebt?.id!!, amount = initialAmount)
        }
    }

    suspend fun recordPayment(debtId: Long, amount: Long) {
        database.withTransaction {
            val currentDebt = debtDao.getDebtById(debtId)
                ?: throw IllegalStateException("Debt does not exist")
            if (currentDebt.currentAmount == 0L) {
                throw IllegalStateException("Debt is already fully paid")
            }
            paymentDao.insertPayment(
                Payment(
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    debtId = debtId
                )
            )
            val newRemaining = (currentDebt.currentAmount - amount).coerceAtLeast(0L)
            debtDao.updateCurrentAmount(id = debtId, amount = newRemaining)
        }
    }

    suspend fun deletePayment(debtId: Long, payment: Payment) {
        database.withTransaction {
            paymentDao.deletePayment(payment.id)
            val debt = debtDao.getDebtById(debtId) ?: return@withTransaction
            debtDao.updateCurrentAmount(
                id = debt.id!!,
                amount = debt.currentAmount + payment.amount
            )
        }
    }
}