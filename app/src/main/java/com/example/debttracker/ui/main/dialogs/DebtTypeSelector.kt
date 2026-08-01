package com.example.debttracker.ui.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debttracker.data.local.entity.DebtType
import com.example.debttracker.ui.theme.AppColors
import com.example.debttracker.ui.theme.AppStrings

@Composable
fun DebtTypeSelector(
    selectedType: DebtType,
    onTypeSelected: (DebtType) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        DebtTypeCard(
            modifier = Modifier.weight(1f),
            title = AppStrings.typeOweMe,
            emoji = "↑",
            isSelected = selectedType == DebtType.OWE_ME,
            onClick = {
                onTypeSelected(DebtType.OWE_ME)
            }
        )

        DebtTypeCard(
            modifier = Modifier.weight(1f),
            title = AppStrings.typeIOwe,
            emoji = "↓",
            isSelected = selectedType == DebtType.I_OWE,
            onClick = {
                onTypeSelected(DebtType.I_OWE)
            }
        )
    }
}

@Composable
private fun DebtTypeCard(
    modifier: Modifier = Modifier,
    title: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isOweMe = title == AppStrings.typeOweMe

    val selectedColor = when {
        isSelected && isOweMe -> AppColors.oweMeGreen
        isSelected && !isOweMe -> AppColors.iOweRed
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    val variantColor = if (isOweMe) AppColors.oweMeGreen else AppColors.iOweRed

    val containerColor = if (isOweMe) AppColors.oweMeCardBackground else AppColors.iOweCardBackground

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(120.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = selectedColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = emoji,
                color = variantColor,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = variantColor,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}