package com.example.debttracker.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.debttracker.data.local.database.AppDatabase
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.Payment

class Repository(private val database: AppDatabase) {
    private val debtDao = database.debtDao()
    private val paymentDao = database.paymentDao()

    fun getCurrentDebt(debtId: Long? = null): Debt? {
        Log.d(TAG, "getCurrentDebt() called with debtId=$debtId")
        val result = debtId.let { debtDao.getDebtById(it!!) }
        Log.d(TAG, "getCurrentDebt() result: id=${result?.id}, name=${result?.name}, currentAmount=${result?.currentAmount}")
        return result
    }

    fun getDebtList(): List<Debt>? {
        Log.d(TAG, "getDebtList() called")
        val result = debtDao.getDebtList()
        Log.d(TAG, "getDebtList() result: ${result?.size ?: 0} debts; ids=${result?.map { it.id }}")
        return result
    }

    suspend fun createOrUpdateDebt(
        initialAmount: Long,
        name: String,
        date: Long? = null,
        id: Long? = null
    ) {
        Log.d(TAG, "createOrUpdateDebt() called with initialAmount=$initialAmount, name='$name', date=$date, id=$id")
        if (id == null) {
            val debt = Debt(
                initialAmount = initialAmount,
                currentAmount = initialAmount,
                createdAt = date ?: System.currentTimeMillis(),
                name = name
            )
            Log.d(TAG, "createOrUpdateDebt() inserting new debt: $debt")
            debtDao.insertDebt(debt)
        } else {
            val currentDebt = getCurrentDebt(id)
            Log.d(TAG, "createOrUpdateDebt() updating existing debt: id=$id, oldCurrentAmount=${currentDebt?.currentAmount}, newAmount=$initialAmount")
            debtDao.updateCurrentAmount(id = currentDebt?.id!!, amount = initialAmount)
        }
    }

    suspend fun recordPayment(debtId: Long, amount: Long, date: Long? = null) {
        Log.d(TAG, "recordPayment() called with debtId=$debtId, amount=$amount, date=$date")
        database.withTransaction {
            val currentDebt = debtDao.getDebtById(debtId)
                ?: throw IllegalStateException("Debt does not exist")
            Log.d(TAG, "recordPayment() currentDebt: id=${currentDebt.id}, currentAmount=${currentDebt.currentAmount}, initialAmount=${currentDebt.initialAmount}")
            if (currentDebt.currentAmount == 0L) {
                Log.w(TAG, "recordPayment() debt $debtId is already fully paid")
                throw IllegalStateException("Debt is already fully paid")
            }
            val payment = Payment(
                amount = amount,
                dateMillis = date ?: System.currentTimeMillis(),
                debtId = debtId
            )
            Log.d(TAG, "recordPayment() inserting payment: $payment")
            paymentDao.insertPayment(payment)
            val newRemaining = (currentDebt.currentAmount - amount).coerceAtLeast(0L)
            Log.d(TAG, "recordPayment() updating currentAmount: ${currentDebt.currentAmount} -> $newRemaining")
            debtDao.updateCurrentAmount(id = debtId, amount = newRemaining)
        }
    }

    suspend fun deletePayment(debtId: Long, payment: Payment) {
        Log.d(TAG, "deletePayment() called with debtId=$debtId, paymentId=${payment.id}, paymentAmount=${payment.amount}, paymentDate=${payment.dateMillis}")
        database.withTransaction {
            paymentDao.deletePayment(payment.id)
            Log.d(TAG, "deletePayment() payment ${payment.id} deleted")
            val debt = debtDao.getDebtById(debtId)
            if (debt == null) {
                Log.w(TAG, "deletePayment() debt $debtId not found, skipping currentAmount update")
                return@withTransaction
            }
            val newAmount = debt.currentAmount + payment.amount
            Log.d(TAG, "deletePayment() restoring currentAmount: ${debt.currentAmount} -> $newAmount")
            debtDao.updateCurrentAmount(
                id = debt.id!!,
                amount = newAmount
            )
        }
    }

    companion object {
        private const val TAG = "Repository"
    }
}
