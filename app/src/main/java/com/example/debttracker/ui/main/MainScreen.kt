package com.example.debttracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.ui.main.debtCard.DebtCard
import com.example.debttracker.ui.main.dialogs.BindDebtDialog
import com.example.debttracker.ui.theme.AppColors
import com.example.debttracker.ui.theme.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BindMainScreen(
    viewModel: MainViewModel,
    onDebtClick: (Long) -> Unit,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit
) {

    val state = viewModel.mainState.collectAsState().value
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDebts()
    }

    Scaffold(
        floatingActionButton = {
            if (!state.isOffline) {
                FloatingActionButton(
                    onClick = { viewModel.showDebtDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = AppStrings.addDebtContentDescription)
                }
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


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.mainScreenTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = AppStrings.menuContentDescription
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(AppStrings.logoutButton) },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                }
                            )
                            if (!state.isOffline) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            AppStrings.deleteAccountOption,
                                            color = AppColors.remainingRed
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.requestDeleteAccount()
                                    }
                                )
                            }
                        }
                    }
                }

                if (state.isOffline) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OfflineBanner(
                        lastSyncMillis = state.lastSyncMillis,
                        onRetry = { viewModel.loadDebts(showSpinner = true) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when {

                    state.errorMessage != null -> {
                        ErrorView(
                            message = state.errorMessage,
                            onRetry = { viewModel.loadDebts(showSpinner = true) }
                        )
                    }

                    state.debtList.isNullOrEmpty() && !state.isLoading -> {
                        EmptyView(isOffline = state.isOffline)
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

    if (state.showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeletingAccount) viewModel.dismissDeleteAccountDialog()
            },
            title = { Text(AppStrings.deleteAccountTitle) },
            text = {
                Column {
                    Text(AppStrings.deleteAccountMessage)
                    if (state.deleteAccountError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.deleteAccountError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAccount(onAccountDeleted) },
                    enabled = !state.isDeletingAccount
                ) {
                    if (state.isDeletingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(AppStrings.deleteAccountConfirm, color = AppColors.remainingRed)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissDeleteAccountDialog() },
                    enabled = !state.isDeletingAccount
                ) {
                    Text(AppStrings.buttonCancel)
                }
            }
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
fun ErrorView(message: String, onRetry: () -> Unit) {

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

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(AppStrings.retryButton)
        }
    }
}

@Composable
fun OfflineBanner(lastSyncMillis: Long?, onRetry: () -> Unit) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppStrings.offlineBannerTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (lastSyncMillis != null) {
                    Text(
                        text = AppStrings.offlineBannerText(formatSyncTime(lastSyncMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onRetry) {
                Text(AppStrings.retryButton)
            }
        }
    }
}

private fun formatSyncTime(millis: Long): String =
    SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(millis))

@Composable
fun EmptyView(isOffline: Boolean = false) {

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
            text = if (isOffline) AppStrings.emptyHintOffline else AppStrings.emptyHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}