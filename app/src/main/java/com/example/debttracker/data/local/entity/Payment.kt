package com.example.debttracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val dateMillis: Long,
    val debtId: Long
)
