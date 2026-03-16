package com.example.debttracker.data.repository

import androidx.room.withTransaction
import com.example.debttracker.data.local.database.AppDatabase
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.Payment

class Repository(private val database: AppDatabase) {
    private val debtDao = database.debtDao()
    private val paymentDao = database.paymentDao()

    fun getCurrentDebt(): Debt? = debtDao.getDebt()

    suspend fun createOrUpdateDebt(initialAmount: Double, name: String) {
        val currentDebt = getCurrentDebt()
        if (currentDebt == null) {
            debtDao.insertDebt(
                Debt(
                    initialAmount = initialAmount,
                    currentAmount = initialAmount,
                    createdAt = System.currentTimeMillis(),
                    name = name
                )
            )
        } else {
            debtDao.updateCurrentAmount(id = currentDebt.id!!, amount = initialAmount)
        }
    }

    suspend fun recordPayment(amount: Double) {
        database.withTransaction {
            val currentDebt = debtDao.getDebt()
                ?: throw IllegalStateException("Debt does not exist")
            if (currentDebt.currentAmount == 0.0) {
                throw IllegalStateException("Debt is already fully paid")
            }
            val debtId = currentDebt.id!!
            paymentDao.insertPayment(
                Payment(
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    debtId = debtId
                )
            )
            val newRemaining = (currentDebt.currentAmount - amount).coerceAtLeast(0.0)
            debtDao.updateCurrentAmount(id = debtId, amount = newRemaining)
        }
    }

    suspend fun deletePayment(payment: Payment) {
        database.withTransaction {
            paymentDao.deletePayment(payment.id)
            val debt = debtDao.getDebt() ?: return@withTransaction
            debtDao.updateCurrentAmount(
                id = debt.id!!,
                amount = debt.currentAmount + payment.amount
            )
        }
    }
}