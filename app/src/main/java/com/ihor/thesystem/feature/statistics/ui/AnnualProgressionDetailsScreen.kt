package com.ihor.thesystem.feature.statistics.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
private fun AnnualDetailsHeader(onBack: () -> Unit) {
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
                text = "Річна прогресія",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Факт проти плану",
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
            .clickable(onClick = onClick)
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
                    text = "M$month",
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
        LegendItem(label = "Факт", color = colors.accentPrimary)
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
                text = "M${month.monthIndex}",
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
                text = "Факт ${month.actualWeight?.let { "${it.formatWeight()} кг" } ?: "-"}",
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
                subtitle = "Останній доступний факт проти плану",
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
private fun AnnualProgressionManualEditorScreen(
    state: AnnualProgressionManualEditorUiState,
    onBack: () -> Unit,
    onTargetChanged: (Int, Int, String) -> Unit,
    onSave: () -> Unit
) {
    val colors = SystemTheme.colors
    val monthLabels = remember(state.currentDate) {
        buildManualMonthLabels(state.currentDate)
    }
    var page by remember(state.currentDate) { mutableStateOf(0) }
    val pageCount = (monthLabels.size + MANUAL_MONTHS_PER_PAGE - 1) / MANUAL_MONTHS_PER_PAGE
    val safePage = page.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
    val firstMonth = safePage * MANUAL_MONTHS_PER_PAGE
    val visibleMonthIndexes = (firstMonth until (firstMonth + MANUAL_MONTHS_PER_PAGE).coerceAtMost(monthLabels.size))
        .toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnnualManualHeader(onBack = onBack)

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.accentAi)
                }
            }
            state.exercises.isEmpty() -> {
                EmptyManualScheduleBlock(
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                val requiredTargetCount = state.exercises.sumOf { it.monthTargets.size }
                val filledTargetCount = state.exercises.sumOf { exercise ->
                    exercise.monthTargets.count { target -> target.isNotBlank() }
                }
                val hasEnteredTargets = filledTargetCount > 0
                val targetsReady = requiredTargetCount > 0 && filledTargetCount == requiredTargetCount
                val saveAccent = when {
                    targetsReady -> colors.accentAi
                    hasEnteredTargets -> colors.accentWarning
                    else -> colors.textSecondary
                }
                val saveButtonText = when {
                    state.isSaving -> "Зберігаю..."
                    targetsReady -> "Зберегти графік"
                    else -> "Перевірити цілі"
                }
                val tableModifier = when {
                    state.exercises.size <= 1 -> Modifier
                        .fillMaxWidth()
                        .heightIn(max = 230.dp)
                    state.exercises.size == 2 -> Modifier
                        .fillMaxWidth()
                        .heightIn(max = 430.dp)
                    else -> Modifier.weight(1f)
                }

                ManualEditorIntroBlock(exerciseCount = state.exercises.size)
                ManualMonthPager(
                    monthLabels = monthLabels,
                    visibleMonthIndexes = visibleMonthIndexes,
                    page = safePage,
                    pageCount = pageCount,
                    onPageChanged = { page = it.coerceIn(0, (pageCount - 1).coerceAtLeast(0)) }
                )
                ManualTargetsTable(
                    exercises = state.exercises,
                    monthLabels = monthLabels,
                    visibleMonthIndexes = visibleMonthIndexes,
                    onTargetChanged = onTargetChanged,
                    modifier = tableModifier
                )
                ManualSaveProgressIndicator(
                    filledTargetCount = filledTargetCount,
                    requiredTargetCount = requiredTargetCount,
                    ready = targetsReady,
                    accent = saveAccent
                )
                SystemButton(
                    text = saveButtonText,
                    icon = Icons.Filled.Save,
                    onClick = onSave,
                    accent = saveAccent,
                    enabled = state.canSave,
                    glow = targetsReady,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AnnualManualHeader(onBack: () -> Unit) {
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
                text = "Створити самостійно",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Річна прогресія · ручні цілі",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ManualEditorIntroBlock(exerciseCount: Int) {
    val colors = SystemTheme.colors
    DarkGlassCard(active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SystemSectionHeader(
                title = "Таблиця цілей",
                subtitle = "$exerciseCount вправ з розкладу"
            )
            Text(
                text = "Щоб таблиця залишалась читабельною, показано 3 місяці за раз. Перемикай сторінки місяців, а вправи переглядай вертикально.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun ManualMonthPager(
    monthLabels: List<String>,
    visibleMonthIndexes: List<Int>,
    page: Int,
    pageCount: Int,
    onPageChanged: (Int) -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    val visibleRange = visibleMonthIndexes
        .takeIf { it.isNotEmpty() }
        ?.let { indexes ->
            "${monthLabels[indexes.first()]} - ${monthLabels[indexes.last()]}"
        }
        .orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { onPageChanged(page - 1) },
            enabled = page > 0,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Попередні місяці",
                tint = if (page > 0) colors.accentAi else colors.textMuted
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = visibleRange,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Сторінка ${page + 1}/$pageCount",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textMuted,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        IconButton(
            onClick = { onPageChanged(page + 1) },
            enabled = page < pageCount - 1,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Наступні місяці",
                tint = if (page < pageCount - 1) colors.accentAi else colors.textMuted
            )
        }
    }
}

@Composable
private fun ManualSaveProgressIndicator(
    filledTargetCount: Int,
    requiredTargetCount: Int,
    ready: Boolean,
    accent: Color
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.pill)
    val progress = if (requiredTargetCount > 0) {
        (filledTargetCount.toFloat() / requiredTargetCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val statusText = when {
        ready -> "Готово"
        filledTargetCount > 0 -> "Частково"
        else -> "Порожньо"
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Заповнено $filledTargetCount/$requiredTargetCount",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SystemStatusChip(
                text = statusText,
                accent = accent,
                active = filledTargetCount > 0
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(shape)
                .background(colors.overlayLight)
                .border(1.dp, colors.borderMuted, shape)
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(5.dp)
                        .clip(shape)
                        .background(accent.copy(alpha = if (ready) 0.9f else 0.64f))
                )
            }
        }
    }
}

@Composable
private fun ManualTargetsTable(
    exercises: List<AnnualProgressionManualExerciseUiModel>,
    monthLabels: List<String>,
    visibleMonthIndexes: List<Int>,
    onTargetChanged: (Int, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(exercises, key = { it.exerciseId }) { exercise ->
            ManualExerciseTargetRow(
                exercise = exercise,
                monthLabels = monthLabels,
                visibleMonthIndexes = visibleMonthIndexes,
                onTargetChanged = onTargetChanged
            )
        }
    }
}

@Composable
private fun ManualExerciseTargetRow(
    exercise: AnnualProgressionManualExerciseUiModel,
    monthLabels: List<String>,
    visibleMonthIndexes: List<Int>,
    onTargetChanged: (Int, Int, String) -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                text = exercise.exerciseName,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ManualMetaChip(text = exercise.manualMetricLabel())
                ManualMetaChip(text = "Дні ${exercise.cycleDays.joinToString(", ")}")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visibleMonthIndexes.forEach { monthIndex ->
                ManualTargetCell(
                    label = monthLabels[monthIndex],
                    hint = exercise.trackingMode.metricUnit,
                    value = exercise.monthTargets.getOrNull(monthIndex).orEmpty(),
                    onValueChange = { value ->
                        onTargetChanged(exercise.exerciseId, monthIndex, value)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ManualMetaChip(text: String) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.pill)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(colors.accentAiSoft)
            .border(1.dp, colors.accentAi.copy(alpha = 0.18f), shape)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ManualTargetCell(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.accentAi,
                fontWeight = FontWeight.Black
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            ),
            colors = systemOutlinedTextFieldColors(accent = colors.accentAi),
            shape = RoundedCornerShape(SystemTheme.shapes.medium),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmptyManualScheduleBlock(modifier: Modifier = Modifier) {
    val colors = SystemTheme.colors
    val iconShape = RoundedCornerShape(SystemTheme.shapes.medium)
    DarkGlassCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(iconShape)
                    .background(colors.accentAiSoft)
                    .border(1.dp, colors.accentAi.copy(alpha = 0.22f), iconShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = colors.accentAi,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "У розкладі поки немає вправ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Додай вправи в налаштування розкладу, і вони автоматично з'являться тут.",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AnnualProgressionEmptyState(
    onBack: () -> Unit,
    onCreateInAi: () -> Unit,
    onCreateManually: () -> Unit
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
        Spacer(modifier = Modifier.height(8.dp))
        AnnualCreateOptionCard(
            icon = Icons.Filled.AutoAwesome,
            title = "Створити за допомогою AI",
            text = "AI сформує місячні цілі за вибраними вправами і збереже їх у річний графік.",
            buttonText = "Створити в AI",
            onClick = onCreateInAi,
            active = true
        )
        AnnualCreateOptionCard(
            icon = Icons.Filled.Edit,
            title = "Створити самостійно",
            text = "Відкриється таблиця з вправами з усіх днів циклу. Значення для кожного місяця можна ввести вручну.",
            buttonText = "Відкрити таблицю",
            onClick = onCreateManually,
            active = false
        )
    }
}

@Composable
private fun AnnualCreateOptionCard(
    icon: ImageVector,
    title: String,
    text: String,
    buttonText: String,
    onClick: () -> Unit,
    active: Boolean
) {
    val colors = SystemTheme.colors
    val iconShape = RoundedCornerShape(SystemTheme.shapes.medium)
    val iconBackground = if (active) colors.accentAiSoft else colors.overlayLight
    val iconBorder = if (active) colors.accentAi.copy(alpha = 0.24f) else colors.borderSubtle
    val iconTint = if (active) colors.accentAi else colors.textSecondary
    DarkGlassCard(active = active) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(iconShape)
                    .background(iconBackground)
                    .border(1.dp, iconBorder, iconShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (active) {
                SystemButton(
                    text = buttonText,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onClick,
                    accent = colors.accentAi,
                    glow = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                SystemGhostButton(
                    text = buttonText,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onClick,
                    accent = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RoundIconButton(
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

private fun buildManualMonthLabels(currentDate: LocalDate): List<String> {
    val formatter = DateTimeFormatter.ofPattern("LLL yyyy", Locale.getDefault())
    return (0 until 12).map { index ->
        currentDate.plusMonths(index.toLong()).format(formatter)
    }
}

private fun LocalDate.formatDate(): String =
    format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))

private fun AnnualProgressionManualExerciseUiModel.manualMetricLabel(): String =
    if (trackingMode.usesWeightInput) {
        "Старт ${currentWorkingWeight?.let { trackingMode.formatPrimaryValue(it) } ?: "-"}"
    } else {
        "Метрика ${trackingMode.metricUnit}"
    }

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }

private const val MANUAL_MONTHS_PER_PAGE = 3
