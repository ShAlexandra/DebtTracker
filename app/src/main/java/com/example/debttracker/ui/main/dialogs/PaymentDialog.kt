package com.example.debttracker.ui.main.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
fun BindPaymentDialog(
    debt: Debt,
    onDismiss: () -> Unit,
    onConfirm: (debtId: Long, amount: Long, date: Long?) -> Unit
) {

    var amount by remember { mutableStateOf("") }

    val rawAmount = amount.filter { it.isDigit() }
    val parsedAmount = rawAmount.toLongOrNull()

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val displayDate = selectedDateMillis?.let {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
    } ?: ""


    val isValid = parsedAmount != null && parsedAmount > 0 && parsedAmount <= debt.currentAmount
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
                    text = "Добавить платеж",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { input ->
                        amount = input.filter { it.isDigit() && !(input.length == 1 && it == '0') }
                    },
                    label = { Text("Сумма") },
                    singleLine = true,
                    isError = rawAmount.isNotEmpty() && parsedAmount == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    visualTransformation = AmountVisualTransformation,
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors
                )

                if (!isValid && amount.isNotEmpty()) {
                    Text(
                        text = "Введите корректную сумму",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    val compactLabelStyle = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 18.sp,
                        letterSpacing = (-0.01).sp
                    )
                    Button(
                        onClick = { amount = debt.currentAmount.toString() },
                        modifier = Modifier
                            .wrapContentWidth(Alignment.Start)
                            .defaultMinSize(minWidth = 0.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row() {
                            Text("Ввести весь остаток: ", style = compactLabelStyle)
                            Text(
                                debt.currentAmount.toString(),
                                style = compactLabelStyle
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = displayDate,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Дата возврата") },
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
            }
        },

        confirmButton = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-10).dp),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    parsedAmount?.let { onConfirm(debt.id!!, parsedAmount, selectedDateMillis) }
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