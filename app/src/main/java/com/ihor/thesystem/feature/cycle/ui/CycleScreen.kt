package com.ihor.thesystem.feature.cycle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.feature.status.ui.WorkoutDialogHost
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.DayType
import com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel

@Composable
fun CycleScreen(
    navController: NavHostController,
    workoutViewModel: WorkoutViewModel = hiltViewModel()
) {
    val activeWorkout by workoutViewModel.activeWorkoutState.collectAsStateWithLifecycle()
    val cycleDays by workoutViewModel.cycleDaysState.collectAsStateWithLifecycle()
    val dialogState by workoutViewModel.dialogState.collectAsStateWithLifecycle()
    val settingsUiState by workoutViewModel.settingsUiState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors
    val selectedCycleDayModel = cycleDays.firstOrNull { it.isSelected }
    val activeCycleDay = cycleDays.firstOrNull { it.isActive }?.dayNumber
    val selectedCycleDay = selectedCycleDayModel?.dayNumber
        ?: activeWorkout?.dayNumber
        ?: settingsUiState.selectedDay
    val openExercisePicker = {
        navController.navigate(
            Routes.ExercisePicker(
                source = Routes.PICKER_SOURCE_CYCLE,
                cycleDay = selectedCycleDay
            )
        )
    }

    RefreshOnResume(workoutViewModel::refreshForCurrentDay)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SystemScreenPadding)
                .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            CycleHeader()
            TrainingDaySwitcher(
                days = cycleDays,
                onSelectDay = workoutViewModel::onCycleDaySelected
            )
            if (activeCycleDay != null && selectedCycleDay != activeCycleDay) {
                TodayCycleActivationCard(
                    selectedDay = selectedCycleDay,
                    activeDay = activeCycleDay,
                    onActivateToday = workoutViewModel::onActivateSelectedCycleDayToday
                )
            }

            if (activeWorkout == null || activeWorkout?.exercises.isNullOrEmpty()) {
                EmptyCycleBlock(
                    selectedDay = cycleDays.firstOrNull { it.isSelected }?.dayNumber,
                    onEditDay = workoutViewModel::onOpenWorkoutSettings,
                    onAddExercise = openExercisePicker
                )
            } else {
                val workout = requireNotNull(activeWorkout)
                DayOverviewCard(
                    workout = workout,
                    onEditDay = workoutViewModel::onOpenWorkoutSettings
                )
                TrainingStackCard(
                    workout = workout,
                    onOpenExercise = { exercise ->
                        workoutViewModel.onOpenSetup(exercise.toMatrixEntry(workout))
                    }
                )
                CycleActions(
                    onStartWorkout = workoutViewModel::onOpenMainWorkout,
                    onEditDay = workoutViewModel::onOpenWorkoutSettings,
                    onAddExercise = openExercisePicker
                )
            }
        }

        WorkoutDialogHost(
            dialogState = dialogState,
            activeDayWorkout = activeWorkout,
            settingsUiState = settingsUiState,
            workoutViewModel = workoutViewModel,
            onOpenWorkoutAnalysis = { sessionId ->
                navController.navigate(Routes.WorkoutAnalysis(sessionId = sessionId))
            }
        )
    }
}

@Composable
private fun CycleHeader() {
    val colors = SystemTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Цикл",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black
            )
        )
        Text(
            text = "Поточний тренувальний день",
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TrainingDaySwitcher(
    days: List<CycleDayUiModel>,
    onSelectDay: (Int) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = SystemItemSpacing) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.chunked(CYCLE_DAY_COLUMNS).forEach { rowDays ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowDays.forEach { day ->
                        CycleDayChip(
                            day = day,
                            onClick = { onSelectDay(day.dayNumber) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(CYCLE_DAY_COLUMNS - rowDays.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleDayChip(
    day: CycleDayUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    val subtitle = when {
        day.isActive -> "Сьогодні"
        day.workoutName != null -> day.workoutName
        day.type == DayType.WORKOUT -> "Тренування"
        else -> "Відновлення"
    }
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                when {
                    day.isSelected -> colors.accentPrimarySoft
                    day.isActive -> colors.accentPrimary.copy(alpha = 0.085f)
                    else -> colors.surfaceGlassSoft
                }
            )
            .border(
                1.dp,
                when {
                    day.isSelected || day.isActive -> colors.borderActive
                    else -> colors.borderSubtle
                },
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "День ${day.dayNumber}",
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (day.isSelected || day.isActive) colors.accentPrimary else colors.textPrimary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TodayCycleActivationCard(
    selectedDay: Int,
    activeDay: Int,
    onActivateToday: () -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Зробити День $selectedDay поточним?",
                subtitle = "Зараз сьогодні позначено як День $activeDay"
            )
            SystemButton(
                text = "Зробити сьогодні Днем $selectedDay",
                icon = Icons.Filled.Today,
                onClick = onActivateToday,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DayOverviewCard(
    workout: ActiveDayUiModel,
    onEditDay: () -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SystemSectionHeader(
                title = "День ${workout.dayNumber} · ${workout.workoutName ?: "Тренування"}",
                subtitle = "Основна картка дня",
                trailing = {
                    CompactActionButton(
                        icon = Icons.Filled.Edit,
                        onClick = onEditDay
                    )
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetric(
                    label = "Вправи",
                    value = workout.exercises.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    label = "Підходи",
                    value = workout.totalSetCount().toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textMuted,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black
            )
        )
    }
}

@Composable
private fun TrainingStackCard(
    workout: ActiveDayUiModel,
    onOpenExercise: (ExerciseWorkoutUiModel) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Тренувальний стек",
                subtitle = "Цільові ваги, підходи та повторення"
            )
            workout.exercises.forEachIndexed { index, exercise ->
                ExerciseStackRow(
                    index = index + 1,
                    exercise = exercise,
                    onClick = { onOpenExercise(exercise) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseStackRow(
    index: Int,
    exercise: ExerciseWorkoutUiModel,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.small))
                .background(colors.accentPrimarySoft)
                .border(
                    1.dp,
                    colors.accentPrimary.copy(alpha = 0.28f),
                    RoundedCornerShape(SystemTheme.shapes.small)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Black
                )
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = exercise.nameUk ?: exercise.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Ціль: ${exercise.targetWeightText()}",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = exercise.setSchemeText(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CycleActions(
    onStartWorkout: () -> Unit,
    onEditDay: () -> Unit,
    onAddExercise: () -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SystemButton(
                text = "Почати тренування",
                icon = Icons.Filled.PlayArrow,
                onClick = onStartWorkout,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "Редагувати день",
                    icon = Icons.Filled.Edit,
                    onClick = onEditDay,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Додати вправу",
                    icon = Icons.Filled.Add,
                    onClick = onAddExercise,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyCycleBlock(
    selectedDay: Int?,
    onEditDay: () -> Unit,
    onAddExercise: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = selectedDay?.let { "День $it без вправ" } ?: "Тренування не заплановано",
                subtitle = "Для цього тренувального дня ще немає вправ"
            )
            Text(
                text = "Додай вправу або відкрий редагування дня. Календарний цикл при цьому не змінюється.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
            )
            SystemButton(
                text = "Почати тренування",
                icon = Icons.Filled.PlayArrow,
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "Редагувати день",
                    icon = Icons.Filled.Settings,
                    onClick = onEditDay,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Додати вправу",
                    icon = Icons.Filled.Add,
                    onClick = onAddExercise,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactActionButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(shape)
            .background(colors.overlayLight)
            .border(1.dp, colors.borderSubtle, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accentPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun ActiveDayUiModel.totalSetCount(): Int =
    exercises.sumOf { exercise -> exercise.recommendedSets ?: exercise.sets.size }

private fun ExerciseWorkoutUiModel.targetWeightText(): String =
    recommendedWeight
        ?.takeIf { it > 0.0 }
        ?.let { "${it.formatWeight()} кг" }
        ?: "не задано"

private fun ExerciseWorkoutUiModel.setSchemeText(): String {
    val sets = recommendedSets ?: this.sets.size.takeIf { it > 0 } ?: 1
    val reps = recommendedReps ?: 0
    return if (reps > 0) "$sets x $reps" else "$sets підх."
}

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)

private fun ExerciseWorkoutUiModel.toMatrixEntry(workout: ActiveDayUiModel): MatrixEntryUiModel =
    workout.matrixEntries.find { it.exerciseId == exerciseId }
        ?: MatrixEntryUiModel(
            exerciseId = exerciseId,
            exerciseName = nameUk ?: name,
            startWeight = 0f,
            targetWeight = recommendedWeight?.toFloat() ?: 0f,
            currentWeight = recommendedWeight?.toFloat() ?: 0f,
            targetWeightNote = null,
            weeklyStep = 0f,
            progressPercent = 0f,
            currentRank = Rank.E,
            usesExternalLoad = trackingMode.usesWeightInput
        )

private const val CYCLE_DAY_COLUMNS = 4
