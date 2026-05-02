package com.ihor.thesystem.feature.cycle.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.AccentPrimarySoft
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
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop
import com.ihor.thesystem.feature.status.ui.WorkoutDialogHost
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.DayType
import com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel

@Composable
fun CycleScreen(
    navController: NavHostController,
    workoutViewModel: WorkoutViewModel = hiltViewModel()
) {
    val activeWorkout by workoutViewModel.activeWorkoutState.collectAsStateWithLifecycle()
    val cycleDays by workoutViewModel.cycleDaysState.collectAsStateWithLifecycle()
    val dialogState by workoutViewModel.dialogState.collectAsStateWithLifecycle()
    val settingsUiState by workoutViewModel.settingsUiState.collectAsStateWithLifecycle()
    val selectedCycleDay = cycleDays.firstOrNull { it.isSelected }?.dayNumber
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
    ) {
        RpgStatusBackdrop()

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
            CycleHeader()
            TrainingDaySwitcher(
                days = cycleDays,
                onSelectDay = workoutViewModel::onCycleDaySelected
            )

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
            onOpenWorkoutAnalysis = {
                navController.navigate(Routes.WorkoutAnalysis)
            }
        )
    }
}

@Composable
private fun CycleHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Р¦РёРєР»",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
        )
        Text(
            text = "РџРѕС‚РѕС‡РЅРёР№ С‚СЂРµРЅСѓРІР°Р»СЊРЅРёР№ РґРµРЅСЊ",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
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
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { day ->
                CycleDayChip(
                    day = day,
                    onClick = { onSelectDay(day.dayNumber) }
                )
            }
        }
    }
}

@Composable
private fun CycleDayChip(
    day: CycleDayUiModel,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .width(92.dp)
            .clip(shape)
            .background(
                when {
                    day.isSelected -> AccentPrimarySoft
                    day.isActive -> AccentPrimary.copy(alpha = 0.085f)
                    else -> Color.White.copy(alpha = 0.026f)
                }
            )
            .border(
                1.dp,
                when {
                    day.isSelected || day.isActive -> BorderActive
                    else -> BorderSubtle
                },
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "Р”РµРЅСЊ ${day.dayNumber}",
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (day.isSelected || day.isActive) AccentPrimary else TextPrimary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = day.workoutName ?: if (day.type == DayType.WORKOUT) "РўСЂРµРЅСѓРІР°РЅРЅСЏ" else "Р’С–РґРЅРѕРІР»РµРЅРЅСЏ",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
                title = "Р”РµРЅСЊ ${workout.dayNumber} В· ${workout.workoutName ?: "РўСЂРµРЅСѓРІР°РЅРЅСЏ"}",
                subtitle = "РћСЃРЅРѕРІРЅР° РєР°СЂС‚РєР° РґРЅСЏ",
                trailing = {
                    CompactActionButton(
                        icon = Icons.Filled.Edit,
                        onClick = onEditDay
                    )
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetric(
                    label = "Р’РїСЂР°РІРё",
                    value = workout.exercises.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    label = "РџС–РґС…РѕРґРё",
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
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
                title = "Training stack",
                subtitle = "Р¦С–Р»СЊРѕРІС– РІР°РіРё, РїС–РґС…РѕРґРё С‚Р° РїРѕРІС‚РѕСЂРµРЅРЅСЏ"
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
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SystemSurfaceGlass.copy(alpha = 0.62f))
            .border(1.dp, BorderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(AccentPrimarySoft)
                .border(1.dp, AccentPrimary.copy(alpha = 0.28f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AccentPrimary,
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
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Р¦С–Р»СЊ: ${exercise.targetWeightText()}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = exercise.setSchemeText(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMuted,
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
                text = "РџРѕС‡Р°С‚Рё С‚СЂРµРЅСѓРІР°РЅРЅСЏ",
                icon = Icons.Filled.PlayArrow,
                onClick = onStartWorkout,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "Р РµРґР°РіСѓРІР°С‚Рё РґРµРЅСЊ",
                    icon = Icons.Filled.Edit,
                    onClick = onEditDay,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Р”РѕРґР°С‚Рё РІРїСЂР°РІСѓ",
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
    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = selectedDay?.let { "Р”РµРЅСЊ $it Р±РµР· РІРїСЂР°РІ" } ?: "РўСЂРµРЅСѓРІР°РЅРЅСЏ РЅРµ Р·Р°РїР»Р°РЅРѕРІР°РЅРѕ",
                subtitle = "Р”Р»СЏ С†СЊРѕРіРѕ С‚СЂРµРЅСѓРІР°Р»СЊРЅРѕРіРѕ РґРЅСЏ С‰Рµ РЅРµРјР°С” stack"
            )
            Text(
                text = "Р”РѕРґР°Р№ РІРїСЂР°РІСѓ Р°Р±Рѕ РІС–РґРєСЂРёР№ СЂРµРґР°РіСѓРІР°РЅРЅСЏ РґРЅСЏ. РљР°Р»РµРЅРґР°СЂРЅРёР№ С†РёРєР» РїСЂРё С†СЊРѕРјСѓ РЅРµ Р·РјС–РЅСЋС”С‚СЊСЃСЏ.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
            SystemButton(
                text = "РџРѕС‡Р°С‚Рё С‚СЂРµРЅСѓРІР°РЅРЅСЏ",
                icon = Icons.Filled.PlayArrow,
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemButton(
                    text = "Р РµРґР°РіСѓРІР°С‚Рё РґРµРЅСЊ",
                    icon = Icons.Filled.Settings,
                    onClick = onEditDay,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = "Р”РѕРґР°С‚Рё РІРїСЂР°РІСѓ",
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
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun ActiveDayUiModel.totalSetCount(): Int =
    exercises.sumOf { exercise -> exercise.recommendedSets ?: exercise.sets.size }

private fun ExerciseWorkoutUiModel.targetWeightText(): String =
    recommendedWeight
        ?.takeIf { it > 0.0 }
        ?.let { "${it.formatWeight()} РєРі" }
        ?: "РЅРµ Р·Р°РґР°РЅРѕ"

private fun ExerciseWorkoutUiModel.setSchemeText(): String {
    val sets = recommendedSets ?: this.sets.size.takeIf { it > 0 } ?: 1
    val reps = recommendedReps ?: 0
    return if (reps > 0) "$sets x $reps" else "$sets РїС–РґС…."
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
            currentRank = Rank.E
        )
