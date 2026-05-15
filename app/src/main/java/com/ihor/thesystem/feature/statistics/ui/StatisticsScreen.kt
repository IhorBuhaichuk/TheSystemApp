package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemColorTokens
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsViewModel
import com.ihor.thesystem.feature.statistics.viewmodel.SystemInsightUiModel
import com.ihor.thesystem.feature.statistics.viewmodel.WeeklyTrainingDayUiModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors

    RefreshOnResume(viewModel::refreshForCurrentDay)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        when (val state = uiState) {
            UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.accentPrimary)
                }
            }

            is UiState.Content -> {
                AnalyticsDashboard(
                    data = state.data,
                    onOpenAnnualProgression = { navController.navigate(Routes.AnnualProgressionDetails) }
                )
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SystemScreenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    DarkGlassCard(active = true) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Аналітика недоступна",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = colors.accentWarning,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Text(
                                text = state.message.asString(context),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsDashboard(
    data: StatisticsUiData,
    onOpenAnnualProgression: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 8.dp),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        AnalyticsHeader()
        AnalyticsSummaryBlock(data)
        WeeklySummaryBlock(days = data.weeklySummary.days, totalTonnage = data.weeklySummary.totalTonnage)
        AnnualProgressionBlock(
            data = data,
            onOpenAnnualProgression = onOpenAnnualProgression
        )
        DeterministicSystemInsightBlock(insight = data.systemInsight)
    }
}

@Composable
private fun AnalyticsHeader() {
    val colors = SystemTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = "Аналітика",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black
            )
        )
        Text(
            text = "Підсумок і прогрес",
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
        )
    }
}

@Composable
private fun AnalyticsSummaryBlock(data: StatisticsUiData) {
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
private fun WeeklySummaryBlock(
    days: List<WeeklyTrainingDayUiModel>,
    totalTonnage: Double
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

@Composable
private fun AnnualProgressionBlock(
    data: StatisticsUiData,
    onOpenAnnualProgression: () -> Unit
) {
    val colors = SystemTheme.colors
    val plannedEntries = data.matrixEntries.filter { it.usesExternalLoad && it.targetWeight > 0f }
    val keyEntries = plannedEntries.take(4)

    DarkGlassCard(active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SystemSectionHeader(
                title = "Річна прогресія",
                subtitle = "План / факт · місяць ${data.currentMonth}/${data.totalMonths}",
                trailing = {
                    AiLabel(text = "${plannedEntries.size} вправ")
                }
            )

            if (plannedEntries.isEmpty()) {
                EmptyAnalyticsMessage(text = "Річна прогресія ще порожня")
            } else {
                AnnualPlanFactChart(
                    entries = plannedEntries.take(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
                )
                ChartLegend()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    keyEntries.forEach { entry ->
                        KeyExerciseRow(entry = entry)
                    }
                }
            }

            SystemButton(
                text = "Відкрити графік",
                onClick = onOpenAnnualProgression,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                accent = colors.accentAi,
                glow = plannedEntries.isNotEmpty()
            )
        }
    }
}

@Composable
private fun AnnualPlanFactChart(
    entries: List<MatrixEntryUiModel>,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Canvas(modifier = modifier) {
        val targetValues = entries.map { it.targetWeight.coerceAtLeast(0f) }
        val currentValues = entries.map { it.currentWeight.coerceAtLeast(0f) }
        val allValues = targetValues + currentValues
        val maxValue = (allValues.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val horizontalPadding = 12.dp.toPx()
        val verticalPadding = 12.dp.toPx()
        val graphWidth = size.width - horizontalPadding * 2f
        val graphHeight = size.height - verticalPadding * 2f

        repeat(4) { index ->
            val y = verticalPadding + graphHeight * ((index + 1) / 5f)
            drawLine(
                color = colors.overlayMedium,
                start = Offset(horizontalPadding, y),
                end = Offset(size.width - horizontalPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        fun chartPoint(index: Int, value: Float): Offset {
            val x = if (entries.size == 1) {
                size.width / 2f
            } else {
                horizontalPadding + graphWidth * (index.toFloat() / entries.lastIndex.toFloat())
            }
            val y = verticalPadding + graphHeight * (1f - (value / maxValue).coerceIn(0f, 1f))
            return Offset(x, y)
        }

        fun drawSeries(values: List<Float>, color: Color, strokeWidth: Float) {
            val points = values.mapIndexed { index, value -> chartPoint(index, value) }
            if (points.size > 1) {
                val path = Path().apply {
                    points.forEachIndexed { index, point ->
                        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                    }
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            points.forEach { point ->
                drawCircle(color = color.copy(alpha = 0.2f), radius = 6.dp.toPx(), center = point)
                drawCircle(color = color, radius = 2.6.dp.toPx(), center = point)
            }
        }

        drawSeries(targetValues, colors.accentAi.copy(alpha = 0.36f), 2.dp.toPx())
        drawSeries(currentValues, colors.accentPrimary, 2.5.dp.toPx())
    }
}

@Composable
private fun ChartLegend() {
    val colors = SystemTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(label = "План", color = colors.accentAi)
        LegendItem(label = "Факт", color = colors.accentPrimary)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    val colors = SystemTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun KeyExerciseRow(entry: MatrixEntryUiModel) {
    val colors = SystemTheme.colors
    val status = planStatus(entry, colors)
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, status.color.copy(alpha = 0.16f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            tint = status.color,
            modifier = Modifier.size(18.dp)
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = entry.exerciseName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Факт ${formatKg(entry.currentWeight)} · план ${formatKg(entry.targetWeight)}",
                style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = status.color,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun DeterministicSystemInsightBlock(insight: SystemInsightUiModel) {
    val colors = SystemTheme.colors
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Висновок системи",
                subtitle = "На основі існуючих метрик"
            )
            InsightLine(
                title = "Покращилось",
                text = insight.improved,
                accent = colors.accentSuccess,
                icon = Icons.AutoMirrored.Filled.TrendingUp
            )
            InsightLine(
                title = "Слабке місце",
                text = insight.weakPoint,
                accent = colors.accentWarning,
                icon = Icons.Filled.LocalFireDepartment
            )
            InsightLine(
                title = "Рекомендація",
                text = insight.recommendation,
                accent = colors.accentAi,
                icon = Icons.Filled.FitnessCenter
            )
        }
    }
}

@Composable
private fun InsightLine(
    title: String,
    text: String,
    accent: Color,
    icon: ImageVector
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(accent.copy(alpha = 0.075f))
            .border(1.dp, accent.copy(alpha = 0.16f), shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.small))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = text.ifBlank { "Даних поки недостатньо." },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun AiLabel(text: String) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.pill)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(colors.accentAiSoft)
            .border(1.dp, colors.accentAi.copy(alpha = 0.24f), shape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.accentAi,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyAnalyticsMessage(text: String) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(SystemCardPadding),
        style = MaterialTheme.typography.bodySmall.copy(
            color = colors.textMuted,
            fontWeight = FontWeight.Medium
        )
    )
}

private data class PlanStatus(
    val label: String,
    val color: Color
)

private fun planStatus(entry: MatrixEntryUiModel, colors: SystemColorTokens): PlanStatus {
    val ratio = if (entry.targetWeight > 0f) {
        entry.currentWeight / entry.targetWeight
    } else {
        0f
    }

    return when {
        ratio > 1.02f -> PlanStatus("Вище плану", colors.accentPrimary)
        ratio >= 0.9f -> PlanStatus("За планом", colors.accentSuccess)
        else -> PlanStatus("Трохи нижче", colors.accentWarning)
    }
}

private fun formatKg(value: Float): String {
    if (value <= 0f) return "-"
    return if (value % 1f == 0f) {
        "${value.toInt()} кг"
    } else {
        String.format(Locale.US, "%.1f кг", value)
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
