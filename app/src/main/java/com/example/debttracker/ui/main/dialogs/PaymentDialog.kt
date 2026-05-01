package com.example.debttracker.ui.main.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debttracker.data.local.entity.Debt
import com.example.debttracker.ui.utils.AmountVisualTransformation

@Composable
fun BindPaymentDialog(
    debt: Debt,
    onDismiss: () -> Unit,
    onConfirm: (debtId: Long, amount: Long, date: Long?) -> Unit
) {

    var amount by remember { mutableStateOf("") }

    val rawAmount = amount.filter { it.isDigit() }
    val parsedAmount = rawAmount.toLongOrNull()

    var date by remember { mutableStateOf(null) }

    val isValid = parsedAmount != null && parsedAmount > 0 && parsedAmount <= debt.currentAmount

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
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма") },
                    singleLine = true,
                    isError = !isValid && amount.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = AmountVisualTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
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
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Ввести весь остаток:", style = compactLabelStyle)
                            Text(
                                debt.currentAmount.toString(),
                                style = compactLabelStyle
                            )
                        }
                    }
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
                    parsedAmount?.let { onConfirm(debt.id!!, parsedAmount, date) }
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
}