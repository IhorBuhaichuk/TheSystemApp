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
import com.ihor.thesystem.core.theme.Primary
import com.ihor.thesystem.core.ui.components.glassCard
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun CurrentDateBlock(modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    
    val day = today.dayOfMonth
    val monthName = today.month.getDisplayName(TextStyle.FULL, Locale("uk"))
    
    val dateDisplay = "$day $monthName"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(radius = 100.dp)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateDisplay.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                color = Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
    }
}
