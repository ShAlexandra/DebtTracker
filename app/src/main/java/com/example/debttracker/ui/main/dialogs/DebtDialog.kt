package com.example.debttracker.ui.main.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.ui.theme.AppColors
import com.example.debttracker.ui.theme.AppStrings
import com.example.debttracker.ui.utils.AmountVisualTransformation
import kotlinx.coroutines.android.awaitFrame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun BindDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Long, debtType: DebtType, date: Long?, reminderIntervalDays: Int?) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var debtType by remember {
        mutableStateOf(DebtType.I_OWE)
    }

    val rawAmount = amount.filter { it.isDigit() }
    val parsedAmount = rawAmount.toLongOrNull()
    val isValid = name.isNotBlank() && parsedAmount != null && parsedAmount > 0

    var selectedDateMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var reminderIntervalDays by remember { mutableStateOf<Int?>(null) }
    var showReminderDropdown by remember { mutableStateOf(false) }

    val reminderOptions = listOf(
        null to AppStrings.reminderNone,
        1 to AppStrings.reminderEveryDay,
        3 to AppStrings.reminderEvery3Days,
        7 to AppStrings.reminderEveryWeek,
        30 to AppStrings.reminderEveryMonth
    )
    val displayDate = selectedDateMillis?.let {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
    } ?: ""

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outline,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        awaitFrame()
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AppStrings.backContentDescription
                    )
                }
                Text(
                    text = AppStrings.dialogTitleNewDebt,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        text = {
            Column {

                DebtTypeSelector(
                    selectedType = debtType,
                    onTypeSelected = {
                        debtType = it
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.labelName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { input ->
                        amount = input.filter { it.isDigit() && !(input.length == 1 && it == '0') }
                    },
                    label = { Text(AppStrings.labelAmount) },
                    singleLine = true,
                    isError = rawAmount.isNotEmpty() && parsedAmount == null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = AmountVisualTransformation
                )

                if (rawAmount.isNotEmpty() && parsedAmount == null) {
                    Text(
                        text = AppStrings.invalidAmountError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = displayDate,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(AppStrings.labelDate) },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                        .clickable { showDatePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = reminderOptions.firstOrNull { it.first == reminderIntervalDays }?.second
                            ?: AppStrings.reminderNone,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(AppStrings.labelReminders) },
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(8.dp),
                        colors = textFieldColors
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showReminderDropdown = true }
                    )

                    DropdownMenu(
                        expanded = showReminderDropdown,
                        onDismissRequest = { showReminderDropdown = false }
                    ) {
                        reminderOptions.forEach { (days, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    reminderIntervalDays = days
                                    showReminderDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    if (isValid) {
                        onConfirm(name.trim(), parsedAmount, debtType, selectedDateMillis, reminderIntervalDays)
                    }
                },
                enabled = isValid
            ) {
                Text(
                    text = AppStrings.buttonSave,
                    color = AppColors.white,
                    fontSize = 16.sp
                )
            }
        },

        shape = RoundedCornerShape(20.dp)
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text(AppStrings.buttonOk)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(AppStrings.buttonCancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}