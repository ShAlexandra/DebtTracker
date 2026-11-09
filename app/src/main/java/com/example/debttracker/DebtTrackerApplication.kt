package com.example.debttracker

import android.app.Application
import com.example.debttracker.data.CredentialsStore
import com.example.debttracker.data.local.DebtCache
import com.example.debttracker.data.remote.ApiClient
import com.example.debttracker.data.remote.AuthTokenStore
import com.example.debttracker.data.repository.Repository
import com.example.debttracker.ui.utils.ReminderWorker
import com.vk.id.VKID
import java.io.File

class DebtTrackerApplication : Application() {

    val authTokenStore: AuthTokenStore by lazy { AuthTokenStore(this) }

    val credentialsStore: CredentialsStore by lazy { CredentialsStore(this) }

    val debtCache: DebtCache by lazy { DebtCache(File(filesDir, "debt_cache.json")) }

    val sessionManager: SessionManager by lazy { SessionManager(authTokenStore, debtCache) }

    val apiClient: ApiClient by lazy {
        ApiClient(authTokenStore) {
            sessionManager.onLoggedOut()
        }
    }

    val repository: Repository by lazy { Repository(apiClient.api, authTokenStore, debtCache) }

    override fun onCreate() {
        super.onCreate()
        VKID.init(this)
        ReminderWorker.createChannel(this)
        ReminderWorker.schedule(this)
    }
}

