package com.ihor.thesystem.feature.status.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
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
            .sciPanel(
                borderColor = NeonCyan.copy(alpha = 0.25f),
                backgroundColor = PanelSurface.copy(alpha = 0.5f),
                cornerCut = 8.dp
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateDisplay,
            color = NeonCyan,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}
