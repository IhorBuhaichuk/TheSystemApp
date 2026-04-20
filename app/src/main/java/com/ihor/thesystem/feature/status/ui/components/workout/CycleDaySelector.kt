package com.ihor.thesystem.feature.status.ui.components.workout

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.feature.status.viewmodel.CycleDayUiModel

@Composable
fun CycleDaySelector(
    days: List<CycleDayUiModel>,
    onTap: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            CycleDayButton(
                day      = day,
                onTap    = { onTap(day.dayNumber) },
                onLongPress = { onLongPress(day.dayNumber) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CycleDayButton(
    day: CycleDayUiModel,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor, glowColor) = when (day.dayNumber) {
        1 -> Triple(Icons.Filled.WbSunny, Color(0xFFFFD600), Color(0xFFFFD600))
        2 -> Triple(Icons.Filled.NightsStay, Color(0xFF81D4FA), Color(0xFF0288D1))
        3 -> Triple(Icons.Filled.Bedtime, Color(0xFFA5D6A7), Color(0xFF4CAF50))
        4 -> Triple(Icons.Filled.Favorite, Color(0xFFF06292), Color(0xFFE91E63))
        else -> Triple(Icons.Filled.Circle, Color.White, Color.White)
    }

    val label = when (day.dayNumber) {
        1 -> stringResource(R.string.cycle_day_1)
        2 -> stringResource(R.string.cycle_day_2)
        3 -> stringResource(R.string.cycle_day_3)
        4 -> stringResource(R.string.cycle_day_4)
        else -> stringResource(R.string.cycle_day_default, day.dayNumber)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val pulseValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(56.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = { onLongPress() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (day.isActive || day.isSelected) {
                    val factor = pulseValue * 0.3f
                    Color(
                        red = iconColor.red + (1f - iconColor.red) * factor,
                        green = iconColor.green + (1f - iconColor.green) * factor,
                        blue = iconColor.blue + (1f - iconColor.blue) * factor,
                        alpha = 1f
                    )
                } else iconColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer {
                        if (day.isActive || day.isSelected) {
                            val scale = 1f + (0.05f * pulseValue)
                            scaleX = scale
                            scaleY = scale
                        }
                    }
                    .drawBehind {
                        if (day.isActive || day.isSelected) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        glowColor.copy(alpha = 0.6f * pulseValue),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension * 0.7f
                                ),
                                radius = size.minDimension * 0.7f
                            )
                        }
                    }
            )
        }

        Text(
            text       = label.uppercase(),
            color      = if (day.isActive) iconColor else if (day.isSelected) Color.White else Color.White.copy(alpha = 0.2f),
            fontSize   = 10.sp,
            fontFamily = RajdhaniFamily,
            fontWeight = if (day.isActive || day.isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}
