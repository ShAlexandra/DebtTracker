package com.example.debttracker.ui.utils

import android.content.Context
import android.content.Intent
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.DebtType
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
                "Вежливое напоминание",
                "Привет! Напоминаю про долг в размере $amount ₽. Буду благодарен, если сможешь вернуть при возможности 🙏"
            ),
            MessageTemplate(
                "Прямое напоминание",
                "Привет! Хотел уточнить насчёт долга ($amount ₽) — когда планируешь вернуть?"
            ),
            MessageTemplate(
                "Настойчивое",
                "Привет! Долг $amount ₽ всё ещё актуален. Давай решим вопрос на этой неделе, пожалуйста."
            ),
            MessageTemplate(
                "Короткое",
                "Привет! Не забудь про долг $amount ₽ 🙂"
            )
        )
    } else {
        val paymentAmount = formatter.format(lastPaymentAmount ?: debt.currentAmount)
        listOf(
            MessageTemplate(
                "Вежливое уведомление",
                "Привет! Сообщаю, что перевёл $paymentAmount ₽. Спасибо за терпение! 🙏"
            ),
            MessageTemplate(
                "С деталями",
                "Привет! Я выплатил платёж в размере $paymentAmount ₽. Проверь, пожалуйста, поступление."
            ),
            MessageTemplate(
                "Короткое",
                "Привет! Перевёл $paymentAmount ₽ ✌️"
            )
        )
    }
}

fun shareMessage(context: Context, message: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Отправить через"))
}