package com.ihor.thesystem.feature.status.ui.components.workout

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.feature.status.viewmodel.CycleDayUiModel

@Composable
fun CycleDaySelector(
    days: List<CycleDayUiModel>,
    onTap: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            CycleDayButton(
                day = day,
                onTap = { onTap(day.dayNumber) },
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
    val colors = SystemTheme.colors
    val motion = SystemTheme.motion
    val (icon, iconColor, glowColor) = cycleDayVisual(day.dayNumber)

    val label = when (day.dayNumber) {
        1 -> stringResource(R.string.cycle_day_1)
        2 -> stringResource(R.string.cycle_day_2)
        3 -> stringResource(R.string.cycle_day_3)
        4 -> stringResource(R.string.cycle_day_4)
        else -> stringResource(R.string.cycle_day_default, day.dayNumber)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cycle-day-pulse")
    val pulseValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.breathingMillis / 3, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cycle-day-glow"
    )

    val isEmphasized = day.isActive || day.isSelected

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
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
                tint = when {
                    day.isActive -> lerp(iconColor, colors.textPrimary, pulseValue * 0.18f)
                    day.isSelected -> iconColor
                    else -> iconColor.copy(alpha = 0.24f)
                },
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer {
                        if (isEmphasized) {
                            val scale = 1f + ((motion.activeScale - 1f) * pulseValue)
                            scaleX = scale
                            scaleY = scale
                        }
                    }
                    .drawBehind {
                        if (isEmphasized) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        glowColor.copy(alpha = 0.28f * pulseValue),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension * 0.72f
                                ),
                                radius = size.minDimension * 0.72f
                            )
                        }
                    }
            )
        }

        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = when {
                    day.isActive -> iconColor
                    day.isSelected -> colors.textPrimary
                    else -> colors.textMuted
                },
                fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun cycleDayVisual(dayNumber: Int): Triple<ImageVector, Color, Color> {
    val colors = SystemTheme.colors
    return when (dayNumber) {
        1 -> Triple(Icons.Filled.WbSunny, colors.accentWarning, colors.accentWarning)
        2 -> Triple(Icons.Filled.NightsStay, colors.accentInfo, colors.accentPrimary)
        3 -> Triple(Icons.Filled.Bedtime, colors.accentSuccess, colors.accentSuccess)
        4 -> Triple(Icons.Filled.Favorite, colors.accentAi, colors.accentAi)
        else -> Triple(Icons.Filled.Circle, colors.textSecondary, colors.textSecondary)
    }
}
