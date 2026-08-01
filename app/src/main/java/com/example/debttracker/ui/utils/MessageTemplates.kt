package com.example.debttracker.ui.utils

import android.content.Context
import android.content.Intent
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.ui.theme.AppStrings
import java.text.NumberFormat
import java.util.Locale

data class MessageTemplate(
    val label: String,
    val text: String
)

fun getMessageTemplates(debt: Debt, lastPaymentAmount: Long? = null): List<MessageTemplate> {
    val formatter = NumberFormat.getNumberInstance(
        Locale.Builder().setLanguage("ru").setRegion("RU").build()
    )

    return if (debt.type == DebtType.OWE_ME) {
        val amount = formatter.format(debt.currentAmount)
        listOf(
            MessageTemplate(
                AppStrings.msgPoliteReminderLabel,
                AppStrings.msgPoliteReminder(amount)
            ),
            MessageTemplate(
                AppStrings.msgDirectReminderLabel,
                AppStrings.msgDirectReminder(amount)
            ),
            MessageTemplate(
                AppStrings.msgPersistentLabel,
                AppStrings.msgPersistent(amount)
            ),
            MessageTemplate(
                AppStrings.msgShortRemindLabel,
                AppStrings.msgShortRemind(amount)
            )
        )
    } else {
        val paymentAmount = formatter.format(lastPaymentAmount ?: debt.currentAmount)
        listOf(
            MessageTemplate(
                AppStrings.msgPoliteNotificationLabel,
                AppStrings.msgPoliteNotification(paymentAmount)
            ),
            MessageTemplate(
                AppStrings.msgDetailedLabel,
                AppStrings.msgDetailed(paymentAmount)
            ),
            MessageTemplate(
                AppStrings.msgShortReportLabel,
                AppStrings.msgShortReport(paymentAmount)
            )
        )
    }
}

fun shareMessage(context: Context, message: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, AppStrings.shareChooserTitle))
}