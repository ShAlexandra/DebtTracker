package com.example.debttracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class Debt (
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val name: String,
    val initialAmount: Long,
    val currentAmount: Long,
    val createdAt: Long
)