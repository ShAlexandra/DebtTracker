package com.example.debttracker.ui.dialogs

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BindDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Long) -> Unit
) {

    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    val parsedAmount = amount.toLongOrNull()
    val isValid = name.isNotBlank() && parsedAmount != null && parsedAmount > 0

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Добавить долг",
                style = MaterialTheme.typography.titleLarge
            )
        },

        text = {
            Column {

                // Имя
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Кому должен") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Сумма
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма (€)") },
                    singleLine = true,
                    isError = amount.isNotEmpty() && parsedAmount == null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (amount.isNotEmpty() && parsedAmount == null) {
                    Text(
                        text = "Введите корректную сумму",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(name.trim(), parsedAmount!!)
                    }
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

        shape = RoundedCornerShape(20.dp)
    )
}