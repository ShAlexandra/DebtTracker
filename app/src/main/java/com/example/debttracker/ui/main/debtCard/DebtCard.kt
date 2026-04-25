package com.example.debttracker.ui.main.debtCard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debttracker.data.local.entity.Debt
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DebtCard(
    debt: Debt,
    onRecordPayment: () -> Unit
) {

    val progress = 1f - (debt.currentAmount / debt.initialAmount).toFloat()

    if (progress != 1f) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 6.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Column(
                modifier = Modifier.padding(24.dp)
            ) {

                Text(
                    text = debt.name,
                    fontSize = 24.sp,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Осталось: ${formatAmount(debt.currentAmount)} ₽",
                    fontSize = 20.sp,
                )

                Text(
                    text = "Всего: ${formatAmount(debt.initialAmount)} ₽",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                RoundedLinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = Color.Gray,
                    height = 10.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Отметить платеж",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RoundedLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    trackColor: Color = Color.Gray,
    height: Dp = 4.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val cornerPx = (height / 2).toPx()  // Полностью скруглённые края

        // Трек (фон) - весь со скруглениями
        drawRoundRect(
            color = trackColor,
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )

        // Прогресс - тоже со скруглениями, но без разрыва
        val progressWidth = size.width * progress.coerceIn(0f, 1f)
        if (progressWidth > 0) {
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(progressWidth, size.height),
                cornerRadius = CornerRadius(cornerPx, cornerPx)
            )
        }
    }
}

private fun formatAmount(amount: Long): String =
    NumberFormat.getNumberInstance(Locale.Builder().setLanguage("ru").setRegion("RU").build()).format(amount)
