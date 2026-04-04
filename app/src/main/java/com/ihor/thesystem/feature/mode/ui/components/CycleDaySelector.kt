package com.ihor.thesystem.feature.mode.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import com.ihor.thesystem.feature.mode.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.mode.viewmodel.DayType

@Composable
fun CycleDaySelector(
    days: List<CycleDayUiModel>,
    onLongPress: (Int) -> Unit,
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
                onLongPress = { onLongPress(day.dayNumber) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CycleDayHex(
    day: CycleDayUiModel,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon: ImageVector, label: String) = when (day.dayNumber) {
        1    -> Icons.Filled.WbSunny to "День"
        2    -> Icons.Filled.NightsStay to "Ніч"
        3    -> Icons.Filled.Bedtime to "Відсипний"
        4    -> Icons.Filled.Weekend to "Вихідний"
        else -> Icons.Filled.Circle to "День"
    }

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
                .aspectRatio(1.155f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() }
                    )
                },
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
                text = day.dayNumber.toString(),
                color = if (day.isActive) accentColor else TextSecondary,
                fontFamily = TekoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text       = label,
            color      = if (day.isActive) accentColor else TextSecondary.copy(alpha = 0.5f),
            fontSize   = 10.sp,
            fontFamily = RajdhaniFamily,
            fontWeight = if (day.isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
