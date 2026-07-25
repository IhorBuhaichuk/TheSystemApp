package com.ihor.thesystem.feature.statistics.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.core.theme.SystemColorTokens
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.domain.model.AnnualProgressionDetailStatus
import com.ihor.thesystem.domain.model.AnnualProgressionExerciseDetails
import com.ihor.thesystem.domain.model.AnnualProgressionMonthlyProgress
import com.ihor.thesystem.domain.model.formatPrimaryValue
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionDetailsUiMapper
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionDetailsUiState
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionDetailsViewModel
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionManualEditorUiState
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionManualExerciseUiModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import java.time.LocalDate
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
    val manualState by viewModel.manualEditorState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = SystemTheme.colors

    LaunchedEffect(manualState.message) {
        val message = manualState.message ?: return@LaunchedEffect
        Toast.makeText(context, message.asString(context), Toast.LENGTH_SHORT).show()
        viewModel.onManualMessageShown()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        when {
            manualState.isOpen -> AnnualProgressionManualEditorScreen(
                state = manualState,
                onBack = viewModel::onCloseManualEditor,
                onTargetChanged = viewModel::onManualTargetChanged,
                onSave = viewModel::onSaveManualPlan
            )
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accentAi)
            }
            uiState.data.exercises.isEmpty() -> AnnualProgressionEmptyState(
                onBack = onBack,
                onCreateInAi = onCreateInAi,
                onCreateManually = viewModel::onCreateManually
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
internal fun AnnualDetailsHeader(onBack: () -> Unit) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RoundIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Назад",
            onClick = onBack
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(
                        text = "Річний прогрес",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                        text = "Результат проти плану",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.pill)
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) colors.accentAiSoft else colors.overlayLight)
            .border(1.dp, if (selected) colors.borderActive else colors.borderSubtle, shape)
            .systemClickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (selected) colors.accentAi else colors.textMuted)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (selected) colors.textPrimary else colors.textSecondary,
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
                subtitle = "Старт ${exercise.startDate.formatDate()}"
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
    val colors = SystemTheme.colors
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
                    .clip(RoundedCornerShape(SystemTheme.shapes.medium))
                    .background(colors.surfaceGlassSoft)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(SystemTheme.shapes.medium))
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
                        color = colors.overlayMedium,
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

                drawSeries(planPoints, colors.accentAi.copy(alpha = 0.36f), 2.dp.toPx())
                drawSeries(factPoints, colors.accentPrimary, 2.6.dp.toPx())
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
                        text = "Міс. $month",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun AxisLabel(value: Double) {
    val colors = SystemTheme.colors
    Text(
        text = "${value.roundToInt()}",
        style = MaterialTheme.typography.labelSmall.copy(
            color = colors.textMuted,
            fontWeight = FontWeight.Bold
        ),
        maxLines = 1
    )
}

@Composable
private fun ChartLegend() {
    val colors = SystemTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(label = "План", color = colors.accentAi)
            LegendItem(label = "Результат", color = colors.accentPrimary)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    val colors = SystemTheme.colors
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
                color = colors.textSecondary,
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
                title = "Місячні цілі",
                subtitle = "12 місяців плану"
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
    val colors = SystemTheme.colors
    val accent = month.status.statusColor(colors)
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, accent.copy(alpha = 0.16f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.medium))
                .background(accent.copy(alpha = 0.10f))
                .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(SystemTheme.shapes.medium)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                    text = "Міс. ${month.monthIndex}",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = accent,
                    fontWeight = FontWeight.Black
                )
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "План ${month.planWeight.formatWeight()} кг",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                    text = "Результат ${month.actualWeight?.let { "${it.formatWeight()} кг" } ?: "-"}",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
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
    val colors = SystemTheme.colors
    val accent = exercise.currentStatus.statusColor(colors)
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    DarkGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Поточний висновок",
                subtitle = "Останній результат проти плану",
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
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
            exercise.recommendation?.let { recommendation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(colors.accentAi.copy(alpha = 0.075f))
                        .border(1.dp, colors.accentAi.copy(alpha = 0.18f), shape)
                        .padding(SystemItemSpacing),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = colors.accentAi,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = recommendation,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
internal fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textSecondary
        )
    }
}

private fun AnnualProgressionDetailStatus.statusColor(colors: SystemColorTokens): Color =
    when (this) {
        AnnualProgressionDetailStatus.OnPlan -> colors.accentSuccess
        AnnualProgressionDetailStatus.SlightlyBelow -> colors.accentWarning
        AnnualProgressionDetailStatus.AbovePlan -> colors.accentPrimary
        AnnualProgressionDetailStatus.NoFact -> colors.textMuted
    }

private fun LocalDate.formatDate(): String =
    format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }
