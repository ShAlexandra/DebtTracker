package com.example.debttracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.debttracker.data.local.dao.DebtDao
import com.example.debttracker.data.local.dao.PaymentDao
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.Payment

@Database(entities = [Debt::class, Payment::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun debtDao(): DebtDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var db: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return db ?: synchronized(this) {
                db ?: buildDatabase(context.applicationContext).also { db = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "debt_tracker_db"
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}