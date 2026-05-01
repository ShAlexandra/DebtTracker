package com.example.debttracker.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object AmountVisualTransformation : VisualTransformation {
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