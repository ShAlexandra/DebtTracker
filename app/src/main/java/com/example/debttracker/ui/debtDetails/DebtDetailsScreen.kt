package com.example.debttracker.ui.debtDetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.data.local.entity.Payment
import com.example.debttracker.ui.main.debtCard.RoundedLinearProgressIndicator
import com.example.debttracker.ui.main.dialogs.BindPaymentDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BindDebtDetailsScreen(
    viewModel: DebtDetailsViewModel,
    onBack: () -> Unit,
    onPaymentClick: (Payment) -> Unit
) {
    val state = viewModel.state.collectAsState().value

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (state == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        DebtDetailsScreen(
            state = state,
            onBack = onBack,
            onRecordPayment = { showPaymentDialog = true },
            onPaymentClick = onPaymentClick,
            onDeleteDebtClick = { showDeleteConfirmDialog = true }
        )

        if (showPaymentDialog) {
            BindPaymentDialog(
                debt = state.debt,
                onDismiss = { showPaymentDialog = false },
                onConfirm = { debtId, amount, date ->
                    viewModel.recordPayment(amount, date)
                    showPaymentDialog = false
                }
            )
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Удалить долг") },
                text = { Text("Вы уверены, что хотите удалить долг «${state.debt.name}»? Все связанные платежи также будут удалены.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            viewModel.deleteDebt { onBack() }
                        }
                    ) {
                        Text("Удалить", color = Color(0xFFE4564F))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun DebtDetailsScreen(
    state: DebtDetailsState,
    onBack: () -> Unit,
    onRecordPayment: () -> Unit,
    onPaymentClick: (Payment) -> Unit,
    onDeleteDebtClick: () -> Unit
) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                DebtDetailsHeader(
                    debt = state.debt!!,
                    onBack = onBack,
                    onDeleteDebtClick = onDeleteDebtClick
                )
            }

            item {

                DebtProgressCard(state.debt)
            }

            item {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRecordPayment
                ) {

                    Icon(Icons.Default.Add, null)

                    Spacer(Modifier.width(8.dp))

                    Text("Отметить платеж")
                }
            }

            item {

                Text(
                    "История платежей",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                PaymentHistoryCard(
                    paymentList = state.paymentList,
                    onPaymentClick = onPaymentClick
                )
            }
        }
    }
}

@Composable
private fun DebtDetailsHeader(
    debt: Debt,
    onBack: () -> Unit,
    onDeleteDebtClick: () -> Unit
) {

    var showMenu by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = onBack) {

            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                null
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                debt.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            AssistChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        if (debt.type == DebtType.OWE_ME)
                            "Мне должны"
                        else
                            "Я должен"
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor =
                        if (debt.type == DebtType.OWE_ME)
                            Color(0xFF1F3B20)
                        else
                            Color(0xFF442323)
                )
            )
        }

        Box {
            IconButton(
                onClick = { showMenu = true }
            ) {

                Icon(
                    Icons.Default.MoreVert,
                    null
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Удалить долг", color = Color(0xFFE4564F)) },
                    onClick = {
                        showMenu = false
                        onDeleteDebtClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun DebtProgressCard(
    debt: Debt
) {

    val progress =
        1f - debt.currentAmount.toFloat() /
                debt.initialAmount.toFloat()

    Card(
        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            Modifier.padding(20.dp)
        ) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column {

                    Text("Осталось")

                    Text(
                        formatAmount(debt.currentAmount),
                        color = when (debt.type) {
                            DebtType.OWE_ME ->
                                Color(0xFF59C65A)

                            DebtType.I_OWE ->
                                Color(0xFFE4564F)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {

                    Text("Всего")

                    Text(
                        formatAmount(debt.initialAmount),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            RoundedLinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth(),
                color = when (debt.type) {
                    DebtType.OWE_ME ->
                        Color(0xFF59C65A)

                    DebtType.I_OWE ->
                        Color(0xFFE4564F)
                },
                trackColor = Color.Gray,
                height = 8.dp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "${(progress * 100).toInt()}%",
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun PaymentHistoryCard(
    paymentList: List<Payment>,
    onPaymentClick: (Payment) -> Unit
) {

    Card(
        shape = RoundedCornerShape(20.dp)
    ) {

        Column {

            paymentList.forEachIndexed { index, payment ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPaymentClick(payment)
                        }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        formatDate(payment.dateMillis),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        "+${formatAmount(payment.amount)}",
                        color = Color(0xFF59C65A),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (index != paymentList.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun formatAmount(amount: Long): String =
    NumberFormat.getNumberInstance(Locale.Builder().setLanguage("ru").setRegion("RU").build())
        .format(amount)

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(millis))