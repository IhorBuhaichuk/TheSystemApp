package com.ihor.thesystem.feature.mode.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.mode.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.mode.viewmodel.DayType

@Composable
fun CycleDaySelector(
    days: List<CycleDayUiModel>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            CycleDayHex(
                day      = day,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CycleDayHex(
    day: CycleDayUiModel,
    modifier: Modifier = Modifier
) {
    val accentColor = when {
        day.isActive && day.type == DayType.WORKOUT -> NeonGold
        day.isActive                                -> NeonCyan
        day.type == DayType.WORKOUT                 -> NeonGold.copy(alpha = 0.35f)
        else                                        -> NeonCyanDim.copy(alpha = 0.25f)
    }
    val borderColor = if (day.isActive) accentColor else accentColor.copy(alpha = 0.4f)

    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .aspectRatio(1.155f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = buildHexagonPath(size, rotationDegrees = 0f)
                val bg   = if (day.isActive) accentColor.copy(alpha = 0.18f)
                else PanelSurface
                drawPath(path, bg)
                drawPath(
                    path  = path,
                    color = borderColor,
                    style = Stroke(width = if (day.isActive) 2.5.dp.toPx() else 1.5.dp.toPx())
                )
            }
            Text(
                text       = "${day.dayNumber}",
                color      = if (day.isActive) accentColor else TextSecondary,
                fontSize   = if (day.isActive) 22.sp else 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text       = if (day.type == DayType.WORKOUT) "▶ КВЕСТ" else "◎ ВІДПОЧИНОК",
            color      = if (day.isActive) accentColor else TextSecondary.copy(alpha = 0.5f),
            fontSize   = 8.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}