package com.example.debttracker.ui.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.debttracker.MainActivity
import com.example.debttracker.data.local.database.AppDatabase
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.data.repository.Repository
import java.text.NumberFormat
import java.util.Locale
import android.util.Log
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ReminderWorker"
        private const val CHANNEL_ID = "debt_reminders"
        private const val CHANNEL_NAME = "Напоминания о долгах"
        private const val WORK_NAME = "debt_reminder_work"

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Напоминания о ваших долгах"
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun enqueueImmediateTest(context: Context) {
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork() started")
        val database = AppDatabase.getInstance(applicationContext)
        val repository = Repository(database)

        val debts = repository.getDebtsWithReminders()
        Log.d(TAG, "doWork() found ${debts?.size ?: 0} debts with reminders")

        if (debts == null || debts.isEmpty()) {
            Log.d(TAG, "doWork() no debts with reminders, skipping")
            return Result.success()
        }

        val now = System.currentTimeMillis()
        val formatter = NumberFormat.getNumberInstance(
            Locale.Builder().setLanguage("ru").setRegion("RU").build()
        )

        for (debt in debts) {
            val days = debt.reminderIntervalDays ?: 1
            val isTestMode = days == -1
            val intervalMs = if (isTestMode) {
                0L // всегда показывать
            } else {
                days * 24 * 60 * 60 * 1000L
            }
            val lastReminder = debt.lastReminderTimestamp ?: debt.createdAt

            val elapsed = now - lastReminder
            Log.d(TAG, "doWork() debt ${debt.id} '${debt.name}': elapsed=${elapsed}ms, interval=${intervalMs}ms, currentAmount=${debt.currentAmount}, testMode=$isTestMode")

            if ((isTestMode || elapsed >= intervalMs) && debt.currentAmount > 0) {
                val amount = formatter.format(debt.currentAmount)
                val (title, message) = if (debt.type == DebtType.OWE_ME) {
                    "Напоминание о долге" to "${debt.name} должен вам $amount ₽. Не забудьте напомнить!"
                } else {
                    "Не забудьте оплатить долг" to "Вы должны ${debt.name} $amount ₽. Погасите задолженность!"
                }

                Log.d(TAG, "doWork() showing notification for debt ${debt.id} '${debt.name}': $title")
                showNotification(debt.id!!, title, message)
                repository.updateReminderTimestamp(debt.id, now)
            } else {
                Log.d(TAG, "doWork() skipping debt ${debt.id} (elapsed < interval or fully paid)")
            }
        }

        Log.d(TAG, "doWork() finished")
        return Result.success()
    }

    private fun showNotification(debtId: Long, title: String, message: String) {
        Log.d(TAG, "showNotification() debtId=$debtId, title=$title")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_debt_id", debtId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            debtId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(debtId.toInt(), notification)
    }
}