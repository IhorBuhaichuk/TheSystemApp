package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.feature.statistics.viewmodel.WeeklyTrainingDayUiModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeeklySummaryBlock(
    days: List<WeeklyTrainingDayUiModel>,
    totalTonnage: Double,
    onLogWorkout: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SystemSectionHeader(
                title = "Тижневий підсумок",
                subtitle = if (totalTonnage > 0.0) "${formatTonnage(totalTonnage)} тоннажу" else "Ритм виконання"
            )

            if (days.isEmpty()) {
                EmptyAnalyticsMessage(text = "Поки немає даних для тижневого підсумку")
            } else {
                WeeklyRhythmBars(days = days)
                if (days.sumOf { it.workoutCount } == 0) {
                    Text(
                        text = "Немає зафіксованих тренувань цього тижня",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
                    )
                    SystemButton(
                        text = "Записати тренування",
                        icon = Icons.Filled.FitnessCenter,
                        onClick = onLogWorkout,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyRhythmBars(days: List<WeeklyTrainingDayUiModel>) {
    val colors = SystemTheme.colors
    val maxTonnage = days.maxOfOrNull { it.totalTonnage }?.coerceAtLeast(1.0) ?: 1.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val hasWorkout = day.workoutCount > 0
            val ratio = if (hasWorkout) (day.totalTonnage / maxTonnage).toFloat().coerceIn(0.14f, 1f) else 0.05f
            val barHeight = (12f + 70f * ratio).dp

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(SystemTheme.shapes.extraSmall))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        if (hasWorkout) colors.accentPrimary.copy(alpha = 0.34f) else colors.overlayMedium,
                                        if (hasWorkout) colors.accentPrimary.copy(alpha = 0.13f) else colors.overlayLight
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                if (hasWorkout) colors.accentPrimary.copy(alpha = 0.3f) else colors.borderSubtle,
                                RoundedCornerShape(SystemTheme.shapes.extraSmall)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (hasWorkout) colors.textPrimary else colors.textMuted,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

private fun formatTonnage(value: Double): String {
    if (value <= 0.0) return "0 кг"
    return if (value >= 1000.0) {
        String.format(Locale.US, "%.1f т", value / 1000.0)
    } else {
        "${value.roundToInt()} кг"
    }
}
