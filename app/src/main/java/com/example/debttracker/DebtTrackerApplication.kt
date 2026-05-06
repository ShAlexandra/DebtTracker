package com.example.debttracker

import android.app.Application
import com.example.debttracker.data.local.database.AppDatabase
import com.example.debttracker.data.repository.Repository

class DebtTrackerApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: Repository by lazy { Repository(database) }
}
