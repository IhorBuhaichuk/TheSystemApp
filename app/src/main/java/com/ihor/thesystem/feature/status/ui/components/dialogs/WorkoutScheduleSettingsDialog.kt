package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.feature.exercise_search.ui.ExercisePickerScreen
import com.ihor.thesystem.feature.exercise_search.ui.toEquipmentUiText
import com.ihor.thesystem.feature.exercise_search.ui.toUiText
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop
import com.ihor.thesystem.feature.status.viewmodel.BackupUiState
import com.ihor.thesystem.feature.status.viewmodel.HealthConnectUiState
import com.ihor.thesystem.feature.status.viewmodel.WorkoutScheduleSettingsUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutScheduleSettingsScreen(
    uiState: WorkoutScheduleSettingsUiState,
    onDismiss: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onWorkoutNameChange: (String) -> Unit,
    onSaveWorkoutName: () -> Unit,
    onAddExercise: (String) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onDeleteAllExercises: () -> Unit,
    onCreateNewExercise: (String) -> Unit,
    onDeleteExercise: (Int) -> Unit,
    onTrackingModeChanged: (Int, ExerciseTrackingMode) -> Unit,
    onEquipmentLocationChanged: (Boolean) -> Unit,
    onEquipmentAvailabilityChanged: (EquipmentType, Boolean) -> Unit,
    onDumbbellMaxKgChanged: (String) -> Unit,
    onConnectHealthConnect: () -> Unit = {},
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onConfirmBackupImport: () -> Unit = {},
    onCancelBackupImport: () -> Unit = {},
    exerciseSearchViewModel: ExerciseSearchViewModel = hiltViewModel()
) {
    val colors = SystemTheme.colors
    var selectedPane by remember { mutableStateOf(ScheduleSettingsPane.Day) }
    var showExercisePicker by remember { mutableStateOf(false) }

    uiState.backup.pendingImport?.let { preview ->
        BackupImportConfirmationDialog(
            preview = preview,
            isBusy = uiState.backup.isBusy,
            onConfirm = onConfirmBackupImport,
            onDismiss = onCancelBackupImport
        )
    }

    if (showExercisePicker) {
        ExercisePickerScreen(
            viewModel = exerciseSearchViewModel,
            onBack = { showExercisePicker = false },
            onSelectExercise = { exercise ->
                onAddExercise(exercise.id.toString())
                showExercisePicker = false
            },
            actionLabel = "Додати",
            createExerciseLabel = "Своя вправа",
            onCreateExercise = { name ->
                onCreateNewExercise(name)
                showExercisePicker = false
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SystemScreenPadding)
                .padding(top = SystemCardPadding, bottom = SystemScreenPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
        ) {
            ScheduleSettingsHeader(
                selectedDay = uiState.selectedDay,
                exerciseCount = uiState.exercisesForSelectedDay.size,
                onBack = onDismiss
            )
            ScheduleDaySelector(
                selectedDay = uiState.selectedDay,
                totalDays = uiState.totalCycleDays,
                onSelectDay = onSelectDay
            )
            WorkoutNameBlock(
                value = uiState.workoutNameDraft,
                selectedDay = uiState.selectedDay,
                onValueChange = onWorkoutNameChange,
                onSave = onSaveWorkoutName
            )
            ScheduleSettingsTabs(
                selectedPane = selectedPane,
                onSelectPane = { selectedPane = it }
            )

            when (selectedPane) {
                ScheduleSettingsPane.Day -> DayExercisesPanel(
                    exercises = uiState.exercisesForSelectedDay,
                    onAddExercise = { showExercisePicker = true },
                    onRemoveExercise = onRemoveExercise,
                    onTrackingModeChanged = onTrackingModeChanged,
                    modifier = Modifier.weight(1f)
                )
                ScheduleSettingsPane.Library -> ExerciseLibraryPanel(
                    exercises = uiState.allExercises,
                    onAddExercise = { showExercisePicker = true },
                    onDeleteExercise = onDeleteExercise,
                    onTrackingModeChanged = onTrackingModeChanged,
                    modifier = Modifier.weight(1f)
                )
                ScheduleSettingsPane.Equipment -> EquipmentSettingsPanel(
                    profile = uiState.equipmentProfile,
                    dumbbellMaxKgDraft = uiState.dumbbellMaxKgDraft,
                    healthConnect = uiState.healthConnect,
                    backup = uiState.backup,
                    onLocationChanged = onEquipmentLocationChanged,
                    onEquipmentAvailabilityChanged = onEquipmentAvailabilityChanged,
                    onDumbbellMaxKgChanged = onDumbbellMaxKgChanged,
                    onConnectHealthConnect = onConnectHealthConnect,
                    onExportBackup = onExportBackup,
                    onImportBackup = onImportBackup,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WorkoutScheduleSettingsDialog(
    uiState: WorkoutScheduleSettingsUiState,
    onDismiss: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onWorkoutNameChange: (String) -> Unit,
    onSaveWorkoutName: () -> Unit,
    onAddExercise: (String) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onDeleteAllExercises: () -> Unit,
    onCreateNewExercise: (String) -> Unit,
    onDeleteExercise: (Int) -> Unit,
    onTrackingModeChanged: (Int, ExerciseTrackingMode) -> Unit,
    onEquipmentLocationChanged: (Boolean) -> Unit,
    onEquipmentAvailabilityChanged: (EquipmentType, Boolean) -> Unit,
    onDumbbellMaxKgChanged: (String) -> Unit,
    onConnectHealthConnect: () -> Unit = {},
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onConfirmBackupImport: () -> Unit = {},
    onCancelBackupImport: () -> Unit = {},
    exerciseSearchViewModel: ExerciseSearchViewModel = hiltViewModel()
) {
    WorkoutScheduleSettingsScreen(
        uiState = uiState,
        onDismiss = onDismiss,
        onSelectDay = onSelectDay,
        onWorkoutNameChange = onWorkoutNameChange,
        onSaveWorkoutName = onSaveWorkoutName,
        onAddExercise = onAddExercise,
        onRemoveExercise = onRemoveExercise,
        onDeleteAllExercises = onDeleteAllExercises,
        onCreateNewExercise = onCreateNewExercise,
        onDeleteExercise = onDeleteExercise,
        onTrackingModeChanged = onTrackingModeChanged,
        onEquipmentLocationChanged = onEquipmentLocationChanged,
        onEquipmentAvailabilityChanged = onEquipmentAvailabilityChanged,
        onDumbbellMaxKgChanged = onDumbbellMaxKgChanged,
        onConnectHealthConnect = onConnectHealthConnect,
        onExportBackup = onExportBackup,
        onImportBackup = onImportBackup,
        onConfirmBackupImport = onConfirmBackupImport,
        onCancelBackupImport = onCancelBackupImport,
        exerciseSearchViewModel = exerciseSearchViewModel
    )
}

@Composable
private fun ScheduleSettingsHeader(
    selectedDay: Int,
    exerciseCount: Int,
    onBack: () -> Unit
) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Налаштування розкладу",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "День $selectedDay · ${exerciseCount.exerciseCountText()}",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.overlayLight)
                .border(1.dp, colors.borderSubtle, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun ScheduleDaySelector(
    selectedDay: Int,
    totalDays: Int,
    onSelectDay: (Int) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = SystemItemSpacing) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items((1..totalDays).toList(), key = { it }) { day ->
                ScheduleDayChip(
                    day = day,
                    selected = selectedDay == day,
                    onClick = { onSelectDay(day) }
                )
            }
        }
    }
}

@Composable
private fun ScheduleDayChip(
    day: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.pill)
    Surface(
        modifier = Modifier
            .widthIn(min = 82.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) colors.accentPrimarySoft else colors.surfaceGlassSoft,
        border = BorderStroke(1.dp, if (selected) colors.borderActive else colors.borderSubtle)
    ) {
        Text(
            text = "День $day",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (selected) colors.accentPrimary else colors.textSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WorkoutNameBlock(
    value: String,
    selectedDay: Int,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "День $selectedDay",
                subtitle = "Назва тренування"
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Назва тренування",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textMuted)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(SystemTheme.shapes.medium),
                colors = systemOutlinedTextFieldColors(accent = colors.accentPrimary)
            )
            SystemButton(
                text = "Зберегти назву",
                icon = Icons.Filled.Save,
                onClick = onSave,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScheduleSettingsTabs(
    selectedPane: ScheduleSettingsPane,
    onSelectPane: (ScheduleSettingsPane) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScheduleSettingsTab(
            text = "День",
            selected = selectedPane == ScheduleSettingsPane.Day,
            onClick = { onSelectPane(ScheduleSettingsPane.Day) },
            modifier = Modifier.weight(1f)
        )
        ScheduleSettingsTab(
            text = "База",
            selected = selectedPane == ScheduleSettingsPane.Library,
            onClick = { onSelectPane(ScheduleSettingsPane.Library) },
            modifier = Modifier.weight(1f)
        )
        ScheduleSettingsTab(
            text = "Обладнання",
            selected = selectedPane == ScheduleSettingsPane.Equipment,
            onClick = { onSelectPane(ScheduleSettingsPane.Equipment) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun ScheduleSettingsTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.accentPrimarySoft else colors.surfaceGlassSoft)
            .border(1.dp, if (selected) colors.borderActive else colors.borderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (selected) colors.accentPrimary else colors.textSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private enum class ScheduleSettingsPane {
    Day,
    Library,
    Equipment
}
