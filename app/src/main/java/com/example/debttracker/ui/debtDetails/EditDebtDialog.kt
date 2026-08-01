package com.example.debttracker.ui.debtDetails

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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.ui.utils.AmountVisualTransformation
import kotlinx.coroutines.android.awaitFrame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BindEditDebtDialog(
    debt: Debt,
    onDismiss: () -> Unit,
    onConfirm: (name: String, initialAmount: Long, createdAt: Long, reminderIntervalDays: Int?) -> Unit
) {
    var name by remember { mutableStateOf(debt.name) }
    var amount by remember { mutableStateOf(debt.initialAmount.toString()) }
    var selectedDateMillis by remember { mutableStateOf(debt.createdAt) }
    var showDatePicker by remember { mutableStateOf(false) }
    var reminderIntervalDays by remember { mutableStateOf(debt.reminderIntervalDays) }
    var showReminderDropdown by remember { mutableStateOf(false) }

    val rawAmount = amount.filter { it.isDigit() }
    val parsedAmount = rawAmount.toLongOrNull()
    val isValid = name.isNotBlank() && parsedAmount != null && parsedAmount > 0

    val displayDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(selectedDateMillis))

    val reminderOptions = listOf(
        null to "Без напоминаний",
        1 to "Каждый день",
        3 to "Раз в 3 дня",
        7 to "Раз в неделю",
        30 to "Раз в месяц"
    )

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
                        contentDescription = "Назад"
                    )
                }
                Text(
                    text = "Редактировать долг",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { input ->
                        amount = input.filter { it.isDigit() && !(input.length == 1 && it == '0') }
                    },
                    label = { Text("Сумма долга") },
                    singleLine = true,
                    isError = rawAmount.isNotEmpty() && parsedAmount == null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = AmountVisualTransformation
                )

                if (rawAmount.isNotEmpty() && parsedAmount == null) {
                    Text(
                        text = "Введите корректную сумму",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = displayDate,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Дата выдачи долга") },
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
                            ?: "Без напоминаний",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Напоминания") },
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
                        onConfirm(name.trim(), parsedAmount, selectedDateMillis, reminderIntervalDays)
                    }
                },
                enabled = isValid
            ) {
                Text(
                    text = "Сохранить",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis ?: selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}