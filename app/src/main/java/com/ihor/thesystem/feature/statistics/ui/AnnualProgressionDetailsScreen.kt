package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.theme.AccentAi
import com.ihor.thesystem.core.theme.AccentAiSoft
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.AccentSuccess
import com.ihor.thesystem.core.theme.AccentWarning
import com.ihor.thesystem.core.theme.BorderActive
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.SystemBackground
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemSurfaceGlass
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.domain.model.AnnualProgressionDetailStatus
import com.ihor.thesystem.domain.model.AnnualProgressionExerciseDetails
import com.ihor.thesystem.domain.model.AnnualProgressionMonthlyProgress
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionDetailsUiMapper
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionDetailsUiState
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionDetailsViewModel
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AnnualProgressionDetailsScreen(
    onBack: () -> Unit,
    onCreateInAi: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnnualProgressionDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SystemBackground)
    ) {
        RpgStatusBackdrop()

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentAi)
            }
            uiState.data.exercises.isEmpty() -> AnnualProgressionEmptyState(
                onBack = onBack,
                onCreateInAi = onCreateInAi
            )
            else -> AnnualProgressionDetailsContent(
                state = uiState,
                onBack = onBack,
                onExerciseSelected = viewModel::onExerciseSelected
            )
        }
    }
}

@Composable
private fun AnnualProgressionDetailsContent(
    state: AnnualProgressionDetailsUiState,
    onBack: () -> Unit,
    onExerciseSelected: (Int) -> Unit
) {
    val selectedExercise = state.selectedExercise ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        AnnualDetailsHeader(onBack = onBack)
        ExerciseSelector(
            exercises = state.data.exercises,
            selectedExerciseId = selectedExercise.exerciseId,
            onExerciseSelected = onExerciseSelected
        )
        AnnualProgressionChartBlock(exercise = selectedExercise)
        MonthlyTargetsBlock(exercise = selectedExercise)
        CurrentConclusionBlock(exercise = selectedExercise)
    }
}

@Composable
private fun AnnualDetailsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, BorderSubtle, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "РќР°Р·Р°Рґ",
                tint = TextSecondary
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = "Р С–С‡РЅР° РїСЂРѕРіСЂРµСЃС–СЏ",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Р¤Р°РєС‚ РїСЂРѕС‚Рё РїР»Р°РЅСѓ",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExerciseSelector(
    exercises: List<AnnualProgressionExerciseDetails>,
    selectedExerciseId: Int,
    onExerciseSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        exercises.forEach { exercise ->
            ExerciseChip(
                text = exercise.exerciseName,
                selected = exercise.exerciseId == selectedExerciseId,
                onClick = { onExerciseSelected(exercise.exerciseId) }
            )
        }
    }
}

@Composable
private fun ExerciseChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) AccentAiSoft else Color.White.copy(alpha = 0.035f))
            .border(1.dp, if (selected) BorderActive else BorderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (selected) AccentAi else TextMuted)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (selected) TextPrimary else TextSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AnnualProgressionChartBlock(exercise: AnnualProgressionExerciseDetails) {
    DarkGlassCard(active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SystemSectionHeader(
                title = exercise.exerciseName,
                subtitle = "РЎС‚Р°СЂС‚ ${exercise.startDate.formatDate()}"
            )
            AnnualProgressionLineChart(
                progress = exercise.monthlyProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )
            ChartLegend()
        }
    }
}

@Composable
private fun AnnualProgressionLineChart(
    progress: List<AnnualProgressionMonthlyProgress>,
    modifier: Modifier = Modifier
) {
    val visibleProgress = progress.filter { it.monthIndex in 0..12 }
    val values = visibleProgress.map { it.planWeight } + visibleProgress.mapNotNull { it.actualWeight }
    val minValue = (values.minOrNull() ?: 0.0).coerceAtLeast(0.0)
    val maxValue = (values.maxOrNull() ?: 1.0).coerceAtLeast(minValue + 1.0)
    val midValue = (minValue + maxValue) / 2.0

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                AxisLabel(maxValue)
                AxisLabel(midValue)
                AxisLabel(minValue)
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.018f))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                val horizontalPadding = 14.dp.toPx()
                val verticalPadding = 16.dp.toPx()
                val graphWidth = size.width - horizontalPadding * 2f
                val graphHeight = size.height - verticalPadding * 2f
                val valueRange = (maxValue - minValue).coerceAtLeast(1.0)

                repeat(4) { index ->
                    val y = verticalPadding + graphHeight * (index / 3f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.055f),
                        start = Offset(horizontalPadding, y),
                        end = Offset(size.width - horizontalPadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                fun point(monthIndex: Int, value: Double): Offset {
                    val x = horizontalPadding + graphWidth * (monthIndex / 12f)
                    val normalized = ((value - minValue) / valueRange).toFloat().coerceIn(0f, 1f)
                    val y = verticalPadding + graphHeight * (1f - normalized)
                    return Offset(x, y)
                }

                fun drawSeries(points: List<Offset>, color: Color, stroke: Float) {
                    if (points.size > 1) {
                        val path = Path().apply {
                            points.forEachIndexed { index, offset ->
                                if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    points.forEach { offset ->
                        drawCircle(color.copy(alpha = 0.18f), radius = 6.dp.toPx(), center = offset)
                        drawCircle(color, radius = 2.8.dp.toPx(), center = offset)
                    }
                }

                val planPoints = visibleProgress.map { point(it.monthIndex, it.planWeight) }
                val factPoints = visibleProgress.mapNotNull { month ->
                    month.actualWeight?.let { point(month.monthIndex, it) }
                }

                drawSeries(planPoints, AccentAi.copy(alpha = 0.78f), 2.dp.toPx())
                drawSeries(factPoints, AccentPrimary, 2.6.dp.toPx())
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(0, 3, 6, 9, 12).forEach { month ->
                Text(
                    text = "M$month",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun AxisLabel(value: Double) {
    Text(
        text = "${value.roundToInt()}",
        style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontWeight = FontWeight.Bold
        ),
        maxLines = 1
    )
}

@Composable
private fun ChartLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(label = "РџР»Р°РЅ", color = AccentAi)
        LegendItem(label = "Р¤Р°РєС‚", color = AccentPrimary)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun MonthlyTargetsBlock(exercise: AnnualProgressionExerciseDetails) {
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "РњС–СЃСЏС‡РЅС– С†С–Р»С–",
                subtitle = "12 РјС–СЃСЏС†С–РІ РїР»Р°РЅСѓ"
            )
            exercise.monthlyProgress
                .filter { it.monthIndex in 1..12 }
                .forEach { month ->
                    MonthlyTargetRow(month = month)
                }
        }
    }
}

@Composable
private fun MonthlyTargetRow(month: AnnualProgressionMonthlyProgress) {
    val accent = month.status.statusColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SystemSurfaceGlass.copy(alpha = 0.58f))
            .border(1.dp, accent.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(accent.copy(alpha = 0.1f))
                .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "M${month.monthIndex}",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = accent,
                    fontWeight = FontWeight.Black
                )
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "РџР»Р°РЅ ${month.planWeight.formatWeight()} РєРі",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Р¤Р°РєС‚ ${month.actualWeight?.let { "${it.formatWeight()} РєРі" } ?: "вЂ”"}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SystemStatusChip(
            text = AnnualProgressionDetailsUiMapper.statusLabel(month.status),
            accent = accent,
            active = month.status != AnnualProgressionDetailStatus.NoFact
        )
    }
}

@Composable
private fun CurrentConclusionBlock(exercise: AnnualProgressionExerciseDetails) {
    val accent = exercise.currentStatus.statusColor()
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "РџРѕС‚РѕС‡РЅРёР№ РІРёСЃРЅРѕРІРѕРє",
                subtitle = "РћСЃС‚Р°РЅРЅС–Р№ РґРѕСЃС‚СѓРїРЅРёР№ С„Р°РєС‚ РїСЂРѕС‚Рё РїР»Р°РЅСѓ",
                trailing = {
                    SystemStatusChip(
                        text = AnnualProgressionDetailsUiMapper.statusLabel(exercise.currentStatus),
                        accent = accent,
                        active = exercise.currentStatus != AnnualProgressionDetailStatus.NoFact
                    )
                }
            )
            Text(
                text = AnnualProgressionDetailsUiMapper.conclusionText(exercise.currentStatus),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
            exercise.recommendation?.let { recommendation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AccentAi.copy(alpha = 0.075f))
                        .border(1.dp, AccentAi.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                        .padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = AccentAi,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = recommendation,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnualProgressionEmptyState(
    onBack: () -> Unit,
    onCreateInAi: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        AnnualDetailsHeader(onBack = onBack)
        Spacer(modifier = Modifier.height(24.dp))
        DarkGlassCard(active = true) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(AccentAiSoft)
                        .border(1.dp, AccentAi.copy(alpha = 0.24f), RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = AccentAi,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "РџР»Р°РЅ СЂС–С‡РЅРѕС— РїСЂРѕРіСЂРµСЃС–С— С‰Рµ РЅРµ СЃС‚РІРѕСЂРµРЅРѕ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "РЎС‚РІРѕСЂРё РїР»Р°РЅ Сѓ РјРѕРґСѓР»С– РЁР†, РїС–СЃР»СЏ Р·Р±РµСЂРµР¶РµРЅРЅСЏ РІС–РЅ Р°РІС‚РѕРјР°С‚РёС‡РЅРѕ Р·КјСЏРІРёС‚СЊСЃСЏ РІ РђРЅР°Р»С–С‚РёС†С–.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                SystemButton(
                    text = "РЎС‚РІРѕСЂРёС‚Рё РІ РЁР†",
                    icon = Icons.Filled.AutoAwesome,
                    onClick = onCreateInAi,
                    accent = AccentAi,
                    glow = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun AnnualProgressionDetailStatus.statusColor(): Color =
    when (this) {
        AnnualProgressionDetailStatus.OnPlan -> AccentSuccess
        AnnualProgressionDetailStatus.SlightlyBelow -> AccentWarning
        AnnualProgressionDetailStatus.AbovePlan -> AccentPrimary
        AnnualProgressionDetailStatus.NoFact -> TextMuted
    }

private fun java.time.LocalDate.formatDate(): String =
    format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("uk")))

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }
