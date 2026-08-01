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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.ui.main.debtCard.DebtCard
import com.example.debttracker.ui.main.dialogs.BindDebtDialog
import com.example.debttracker.ui.theme.AppColors
import com.example.debttracker.ui.theme.AppStrings

@Composable
fun BindMainScreen(
    viewModel: MainViewModel,
    onDebtClick: (Long) -> Unit
) {

    val state = viewModel.mainState.collectAsState().value

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showDebtDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = AppStrings.addDebtContentDescription)
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
                    text = AppStrings.mainScreenTitle,
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
                            state.debtList?.filter { it.type == DebtType.OWE_ME }?.let { debts ->
                                items(debts) { debt ->
                                    DebtCard(
                                        debt = debt,
                                        onDebtClick = { onDebtClick(it.id!!) }
                                    )
                                }
                            }

                            state.debtList?.filter { it.type == DebtType.I_OWE }?.let { debts ->
                                items(debts) { debt ->
                                    DebtCard(
                                        debt = debt,
                                        onDebtClick = { onDebtClick(it.id!!) }
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

    if (state.showDebtDialog) {
        BindDebtDialog(
            onDismiss = { viewModel.dismissDialogs() },
        onConfirm = { name, amount, debtType, date, reminderIntervalDays -> viewModel.confirmAddDebt(amount, name, debtType, date, reminderIntervalDays) }
        )
    }
}

@Composable
fun LoadingView() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.loadingOverlay),
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
            text = AppStrings.errorTitle,
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
            text = AppStrings.emptyTitle,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = AppStrings.emptyHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}