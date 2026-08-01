package com.example.debttracker.ui.theme

/**
 * Centralized string resources for the DebtTracker app.
 * All hardcoded UI text lives here.
 */
object AppStrings {

    // ── Main screen ──
    const val mainScreenTitle = "Раздам долги"
    const val addDebtContentDescription = "Добавить долг"
    const val emptyTitle = "Нет долгов"
    const val emptyHint = "Нажми + чтобы добавить"
    const val errorTitle = "Ошибка"

    // ── Debt dialog ──
    const val dialogTitleNewDebt = "Добавить долг"
    const val dialogTitleEditDebt = "Редактировать долг"
    const val labelName = "Имя"
    const val labelAmount = "Сумма"
    const val labelDebtAmount = "Сумма долга"
    const val labelDate = "Дата выдачи долга"
    const val labelReminders = "Напоминания"
    const val invalidAmountError = "Введите корректную сумму"
    const val buttonSave = "Сохранить"
    const val buttonOk = "ОК"
    const val buttonCancel = "Отмена"
    const val backContentDescription = "Назад"

    // ── Reminder options ──
    const val reminderNone = "Без напоминаний"
    const val reminderEveryDay = "Каждый день"
    const val reminderEvery3Days = "Раз в 3 дня"
    const val reminderEveryWeek = "Раз в неделю"
    const val reminderEveryMonth = "Раз в месяц"

    // ── Debt type selector ──
    const val typeOweMe = "Мне должны"
    const val typeIOwe = "Я должен"

    // ── Debt card ──
    fun remainingLabel(amount: String) = "Осталось: $amount ₽"
    fun totalLabel(amount: String) = "Всего: $amount ₽"

    // ── Debt details screen ──
    const val detailsRecordPayment = "Отметить платеж"
    const val detailsRemindPayment = "Напомнить о платеже"
    const val detailsReportPayment = "Сообщить об оплате"
    const val detailsPaymentHistory = "История платежей"
    const val detailsDeleteDebtTitle = "Удалить долг"
    fun detailsDeleteDebtMessage(name: String) = "Вы уверены, что хотите удалить долг «$name»? Все связанные платежи также будут удалены."
    const val detailsDeleteButton = "Удалить"
    const val detailsEditOption = "Редактировать"
    const val detailsDeleteOption = "Удалить долг"

    // ── Debt progress card ──
    const val progressRemaining = "Осталось"
    const val progressTotal = "Всего"

    // ── Share dialog ──
    const val shareDialogTitleRemind = "Напомнить о платеже"
    const val shareDialogTitleReport = "Сообщить об оплате"
    const val shareDialogChooseText = "Выберите текст сообщения:"
    const val shareChooserTitle = "Отправить через"

    // ── Message templates ──
    const val msgPoliteReminderLabel = "Вежливое напоминание"
    fun msgPoliteReminder(amount: String) = "Привет! Напоминаю про долг в размере $amount ₽. Буду благодарен, если сможешь вернуть при возможности 🙏"

    const val msgDirectReminderLabel = "Прямое напоминание"
    fun msgDirectReminder(amount: String) = "Привет! Хотел уточнить насчёт долга ($amount ₽) — когда планируешь вернуть?"

    const val msgPersistentLabel = "Настойчивое"
    fun msgPersistent(amount: String) = "Привет! Долг $amount ₽ всё ещё актуален. Давай решим вопрос на этой неделе, пожалуйста."

    const val msgShortRemindLabel = "Короткое"
    fun msgShortRemind(amount: String) = "Привет! Не забудь про долг $amount ₽ 🙂"

    const val msgPoliteNotificationLabel = "Вежливое уведомление"
    fun msgPoliteNotification(amount: String) = "Привет! Сообщаю, что перевёл $amount ₽. Спасибо за терпение! 🙏"

    const val msgDetailedLabel = "С деталями"
    fun msgDetailed(amount: String) = "Привет! Я выплатил платёж в размере $amount ₽. Проверь, пожалуйста, поступление."

    const val msgShortReportLabel = "Короткое"
    fun msgShortReport(amount: String) = "Привет! Перевёл $amount ₽ ✌️"
}