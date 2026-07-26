package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.UkrainianLocale
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemStatusChip
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.domain.model.formatPrimaryValue
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionManualEditorUiState
import com.ihor.thesystem.feature.statistics.viewmodel.AnnualProgressionManualExerciseUiModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun AnnualProgressionManualEditorScreen(
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

private fun buildManualMonthLabels(currentDate: LocalDate): List<String> {
    val formatter = DateTimeFormatter.ofPattern("LLL yyyy", UkrainianLocale)
    return (0 until 12).map { index ->
        currentDate.plusMonths(index.toLong()).format(formatter)
    }
}

private fun AnnualProgressionManualExerciseUiModel.manualMetricLabel(): String =
    if (trackingMode.usesWeightInput) {
        "Старт ${currentWorkingWeight?.let { trackingMode.formatPrimaryValue(it) } ?: "-"}"
    } else {
        "Показник ${trackingMode.metricUnit}"
    }

private const val MANUAL_MONTHS_PER_PAGE = 3
