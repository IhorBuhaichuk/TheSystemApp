package com.ihor.thesystem.feature.status.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.glassCard
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CurrentDateBlock(modifier: Modifier = Modifier) {
    val colors = SystemTheme.colors
    val today = LocalDate.now()
    val day = today.dayOfMonth
    val monthName = today.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dateDisplay = "$day $monthName"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(radius = SystemTheme.shapes.pill)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateDisplay.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                color = colors.accentPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
