package com.ihor.thesystem.feature.status.ui.components.workout

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import com.ihor.thesystem.core.ui.components.systemCombinedClickable
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

    val isEmphasized = day.isActive || day.isSelected
    val emphasis by animateFloatAsState(
        targetValue = if (isEmphasized) 1f else 0f,
        animationSpec = tween(motion.stateMillis, easing = EaseOutCubic),
        label = "cycle_day_emphasis"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .semantics { selected = day.isSelected }
                .systemCombinedClickable(
                    onClick = onTap,
                    onLongClick = onLongPress
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when {
                    day.isActive -> lerp(iconColor, colors.textPrimary, 0.12f)
                    day.isSelected -> iconColor
                    else -> iconColor.copy(alpha = 0.24f)
                },
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer {
                        val scale = 1f + ((motion.activeScale - 1f) * emphasis)
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBehind {
                        if (emphasis > 0.01f) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        glowColor.copy(alpha = 0.18f * emphasis),
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
            text = label.toSystemSentenceCase(),
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
