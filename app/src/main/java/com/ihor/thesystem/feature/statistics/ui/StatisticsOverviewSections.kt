package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemColorTokens
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.domain.model.NutritionFloorStatus
import com.ihor.thesystem.domain.model.NutritionFloorTargetStatus
import com.ihor.thesystem.domain.model.NutritionGoalMode
import com.ihor.thesystem.domain.model.ProgressProofType
import com.ihor.thesystem.domain.model.WeightTrend
import com.ihor.thesystem.feature.statistics.viewmodel.ProgressProofUiModel
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData
import com.ihor.thesystem.feature.statistics.viewmodel.WeeklySystemReportUiModel
import java.util.Locale

@Composable
fun AnalyticsSummaryBlock(data: StatisticsUiData) {
    val colors = SystemTheme.colors
    DarkGlassCard(active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SystemSectionHeader(
                title = "Поточний період",
                subtitle = "Тиждень ${data.currentWeek}"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricCell(
                    label = "Тренування",
                    value = data.weeklySummary.workoutCount.toString(),
                    subtitle = "за тиждень",
                    accent = colors.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCell(
                    label = "Серія",
                    value = data.currentStreak.toString(),
                    subtitle = "днів",
                    accent = colors.accentSuccess,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCell(
                    label = "XP",
                    value = data.xpThisWeek.toString(),
                    subtitle = "за період",
                    accent = colors.accentWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NutritionFloorBlock(status: NutritionFloorStatus) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Нутриціологічна база",
                subtitle = "Без калорій і бази продуктів"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricCell(
                    label = "Білок",
                    value = status.proteinStatus.shortLabel(),
                    subtitle = "денний floor",
                    accent = status.proteinStatus.statusAccent(colors),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCell(
                    label = "Вода",
                    value = status.hydrationStatus.shortLabel(),
                    subtitle = "гідрація",
                    accent = status.hydrationStatus.statusAccent(colors),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricCell(
                    label = "7-day avg",
                    value = status.weeklyWeightAverage?.formatWeightAverage() ?: "-",
                    subtitle = status.trend.label(),
                    accent = status.trend.trendAccent(colors),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricCell(
                    label = "Режим",
                    value = status.goalMode.shortLabel(),
                    subtitle = "харчування",
                    accent = colors.accentAi,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = status.recommendation,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SummaryMetricCell(
    label: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Column(
        modifier = modifier
            .height(92.dp)
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, accent.copy(alpha = 0.16f), shape)
            .padding(11.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                color = accent,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProgressProofsBlock(
    proofs: List<ProgressProofUiModel>
) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Доказ прогресу",
                subtitle = "Короткі факти з логів"
            )

            if (proofs.isEmpty()) {
                EmptyAnalyticsMessage(text = "Поки недостатньо логів для порівняння.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    proofs.take(3).forEach { proof ->
                        ProgressProofRow(
                            proof = proof,
                            accent = proof.proofAccent(colors)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressProofRow(
    proof: ProgressProofUiModel,
    accent: Color
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, accent.copy(alpha = 0.16f), shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.small))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (proof.proofType) {
                    ProgressProofType.CONSISTENCY -> Icons.Filled.LocalFireDepartment
                    else -> Icons.AutoMirrored.Filled.TrendingUp
                },
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = proof.exerciseName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = proof.previousLabel,
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = proof.currentLabel,
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = proof.deltaText,
            style = MaterialTheme.typography.labelLarge.copy(
                color = accent,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1
        )
    }
}

@Composable
fun WeeklySystemReportBlock(
    report: WeeklySystemReportUiModel
) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Тижневий звіт системи",
                subtitle = "Детермінований підсумок"
            )
            ReportLine("Найкращий день", report.bestTrainingDay, colors.accentSuccess)
            ReportLine("Слабке місце", report.weakestPattern, colors.accentWarning)
            ReportLine("Найбільший прогрес", report.biggestProgress, colors.accentPrimary)
            ReportLine("Відновлення", report.recoveryIssue, colors.accentAi)
            ReportLine("Наступний тиждень", report.nextWeekDecision, colors.textPrimary)
        }
    }
}

@Composable
private fun ReportLine(
    title: String,
    text: String,
    accent: Color
) {
    val colors = SystemTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = accent,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = text.ifBlank { "Даних поки недостатньо." },
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun ProgressProofUiModel.proofAccent(colors: SystemColorTokens): Color =
    when (proofType) {
        ProgressProofType.STRENGTH -> colors.accentPrimary
        ProgressProofType.REPS -> colors.accentSuccess
        ProgressProofType.CONSISTENCY -> colors.accentWarning
        ProgressProofType.BODY_WEIGHT -> colors.accentAi
    }

private fun NutritionFloorTargetStatus.shortLabel(): String =
    when (this) {
        NutritionFloorTargetStatus.HIT -> "OK"
        NutritionFloorTargetStatus.MISSED -> "MISS"
        NutritionFloorTargetStatus.UNKNOWN -> "-"
    }

private fun NutritionFloorTargetStatus.statusAccent(colors: SystemColorTokens): Color =
    when (this) {
        NutritionFloorTargetStatus.HIT -> colors.accentSuccess
        NutritionFloorTargetStatus.MISSED -> colors.accentWarning
        NutritionFloorTargetStatus.UNKNOWN -> colors.textMuted
    }

private fun NutritionGoalMode.shortLabel(): String =
    when (this) {
        NutritionGoalMode.DEFICIT -> "Дефіцит"
        NutritionGoalMode.MAINTENANCE -> "Підтримка"
        NutritionGoalMode.GAIN -> "Набір"
    }

private fun WeightTrend.label(): String =
    when (this) {
        WeightTrend.DOWN -> "trend down"
        WeightTrend.STABLE -> "stable"
        WeightTrend.UP -> "trend up"
    }

private fun WeightTrend.trendAccent(colors: SystemColorTokens): Color =
    when (this) {
        WeightTrend.DOWN -> colors.accentAi
        WeightTrend.STABLE -> colors.accentPrimary
        WeightTrend.UP -> colors.accentSuccess
    }

private fun Float.formatWeightAverage(): String =
    String.format(Locale.US, "%.1f кг", this)
