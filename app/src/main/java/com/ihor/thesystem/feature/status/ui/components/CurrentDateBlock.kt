package com.ihor.thesystem.feature.status.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.NeonCyan
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun CurrentDateBlock(modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    
    // Форматування: "Чт."
    val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("uk"))
        .replaceFirstChar { it.uppercase() }
    
    // Форматування: "27/03"
    val day = String.format("%02d", today.dayOfMonth)
    val month = String.format("%02d", today.monthValue)
    
    val dateDisplay = "$dayOfWeek. $day/$month"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateDisplay.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
    }
}
