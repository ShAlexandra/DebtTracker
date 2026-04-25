package com.example.debttracker.ui.main.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun BindDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Long, date: Long?) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    //TODO добавь ввод даты
    var date by remember { mutableStateOf(null) }

    val rawAmount = amount.filter { it.isDigit() }
    val parsedAmount = rawAmount.toLongOrNull()
    val isValid = name.isNotBlank() && parsedAmount != null && parsedAmount > 0

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
                    text = "Добавить долг",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
                    value = rawAmount,
                    onValueChange = { input ->
                        amount = input.filter { it.isDigit() }
                    },
                    label = { Text("Сумма") },
                    singleLine = true,
                    isError = rawAmount.isNotEmpty() && parsedAmount == null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    visualTransformation = AmountVisualTransformation
                )

                if (rawAmount.isNotEmpty() && parsedAmount == null) {
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
                        onConfirm(name.trim(), parsedAmount!!, date)
                    }
                },
                enabled = isValid
            ) {
                Text("Сохранить")
            }
        },

        shape = RoundedCornerShape(20.dp)
    )
}

private object AmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = formatWithSpaces(raw)
        val originalToTransformed = IntArray(raw.length + 1)
        val transformedToOriginal = IntArray(formatted.length + 1)
        var rawIndex = 0

        formatted.forEachIndexed { transformedIndex, ch ->
            transformedToOriginal[transformedIndex] = rawIndex
            if (ch != ' ') {
                originalToTransformed[rawIndex] = transformedIndex
                rawIndex++
            }
        }

        originalToTransformed[raw.length] = formatted.length
        transformedToOriginal[formatted.length] = raw.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return originalToTransformed[offset.coerceIn(0, raw.length)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transformedToOriginal[offset.coerceIn(0, formatted.length)]
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

private fun formatWithSpaces(rawDigits: String): String {
    if (rawDigits.isEmpty()) return rawDigits
    return rawDigits.reversed().chunked(3).joinToString(" ").reversed()
}