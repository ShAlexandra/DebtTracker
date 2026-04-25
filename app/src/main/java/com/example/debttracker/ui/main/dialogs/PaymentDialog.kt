package com.example.debttracker.ui.main.dialogs

import android.os.Binder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BindPaymentDialog(
    defaultAmount: Long,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {

    var amount by remember { mutableStateOf(defaultAmount.toString()) }

    val parsedAmount = amount.toDoubleOrNull()
    val isValid = parsedAmount != null && parsedAmount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    parsedAmount?.let { onConfirm(it) }
                },
                enabled = isValid
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        title = {
            Text("Добавить платеж")
        },
        text = {

            Column {

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма (€)") },
                    singleLine = true,
                    isError = !isValid && amount.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isValid && amount.isNotEmpty()) {
                    Text(
                        text = "Введите корректную сумму",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}