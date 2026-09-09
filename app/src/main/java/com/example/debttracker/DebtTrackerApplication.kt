package com.example.debttracker

import android.app.Application
import com.example.debttracker.data.remote.ApiClient
import com.example.debttracker.data.remote.AuthTokenStore
import com.example.debttracker.data.repository.Repository
import com.example.debttracker.ui.utils.ReminderWorker

class DebtTrackerApplication : Application() {

    val authTokenStore: AuthTokenStore by lazy { AuthTokenStore(this) }

    val sessionManager: SessionManager by lazy { SessionManager(authTokenStore) }

    val apiClient: ApiClient by lazy {
        ApiClient(authTokenStore) {
            sessionManager.onLoggedOut()
        }
    }

    val repository: Repository by lazy { Repository(apiClient.api, authTokenStore) }

    override fun onCreate() {
        super.onCreate()
        ReminderWorker.createChannel(this)
        ReminderWorker.schedule(this)
    }
}

