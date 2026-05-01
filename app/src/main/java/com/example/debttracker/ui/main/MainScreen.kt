package com.example.debttracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.debttracker.ui.main.debtCard.DebtCard
import com.example.debttracker.ui.main.dialogs.BindDebtDialog
import com.example.debttracker.ui.main.dialogs.BindPaymentDialog

@Composable
fun BindMainScreen(viewModel: MainViewModel) {

    val state = viewModel.mainState.collectAsState().value

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showDebtDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить долг")
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {


                Text(
                    text = "Раздам долги",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                when {

                    state.errorMessage != null -> {
                        ErrorView(state.errorMessage)
                    }

                    state.debtList.isNullOrEmpty() && !state.isLoading -> {
                        EmptyView()
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {

                            state.debtList?.let { debts ->
                                items(debts) { debt ->
                                    DebtCard(
                                        debt = debt,
                                        onRecordPayment = { viewModel.showPaymentDialog(debt) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                LoadingView()
            }
        }
    }

    if (state.showPaymentDialog) {
        if (state.currentDebt!=null) {
            BindPaymentDialog(
                debt = state.currentDebt,
                onDismiss = { viewModel.dismissDialogs() },
                onConfirm = { debtId, amount, date -> viewModel.confirmAddPayment(debtId, amount, date) }
            )
        } else {
            viewModel.dismissDialogs()
        }

    }

    if (state.showDebtDialog) {
        BindDebtDialog(
            onDismiss = { viewModel.dismissDialogs() },
            onConfirm = { name, amount, date -> viewModel.confirmAddDebt(amount, name, date) }
        )
    }
}

@Composable
fun LoadingView() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {

        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Ошибка",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EmptyView() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Нет долгов",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Нажми + чтобы добавить",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}