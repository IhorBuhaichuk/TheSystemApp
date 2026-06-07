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
import com.ihor.thesystem.feature.status.viewmodel.WorkoutScheduleSettingsUiState

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
    exerciseSearchViewModel: ExerciseSearchViewModel = hiltViewModel()
) {
    val colors = SystemTheme.colors
    var selectedPane by remember { mutableStateOf(ScheduleSettingsPane.Day) }
    var showExercisePicker by remember { mutableStateOf(false) }

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
                    onLocationChanged = onEquipmentLocationChanged,
                    onEquipmentAvailabilityChanged = onEquipmentAvailabilityChanged,
                    onDumbbellMaxKgChanged = onDumbbellMaxKgChanged,
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
private fun ScheduleSettingsTab(
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

@Composable
private fun DayExercisesPanel(
    exercises: List<ExerciseDetails>,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onTrackingModeChanged: (Int, ExerciseTrackingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column(modifier = Modifier.fillMaxSize()) {
            PanelHeader(
                title = "Вправи дня",
                subtitle = exercises.size.exerciseCountText(),
                actionIcon = Icons.Filled.Add,
                actionTint = colors.accentPrimary,
                onAction = onAddExercise
            )
            if (exercises.isEmpty()) {
                EmptyPanelText(
                    text = "Вправи ще не додані",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = SystemCardPadding, end = SystemCardPadding, bottom = SystemCardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(exercises, key = { _, exercise -> exercise.id }) { index, exercise ->
                        ExerciseSettingsRow(
                            exercise = exercise,
                            leadingNumber = index + 1,
                            actionIcon = Icons.Filled.Delete,
                            actionTint = colors.accentError,
                            onAction = { onRemoveExercise(exercise.id) },
                            onTrackingModeChanged = onTrackingModeChanged
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseLibraryPanel(
    exercises: List<ExerciseDetails>,
    onAddExercise: () -> Unit,
    onDeleteExercise: (Int) -> Unit,
    onTrackingModeChanged: (Int, ExerciseTrackingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column(modifier = Modifier.fillMaxSize()) {
            PanelHeader(
                title = "База вправ",
                subtitle = exercises.size.exerciseCountText(),
                actionIcon = Icons.Filled.Add,
                actionTint = colors.accentPrimary,
                onAction = onAddExercise
            )
            if (exercises.isEmpty()) {
                EmptyPanelText(
                    text = "База вправ порожня",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = SystemCardPadding, end = SystemCardPadding, bottom = SystemCardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseSettingsRow(
                            exercise = exercise,
                            leadingNumber = null,
                            actionIcon = Icons.Filled.Delete,
                            actionTint = colors.accentError,
                            onAction = { onDeleteExercise(exercise.id) },
                            onTrackingModeChanged = onTrackingModeChanged
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipmentSettingsPanel(
    profile: EquipmentProfile,
    dumbbellMaxKgDraft: String,
    onLocationChanged: (Boolean) -> Unit,
    onEquipmentAvailabilityChanged: (EquipmentType, Boolean) -> Unit,
    onDumbbellMaxKgChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = modifier.fillMaxWidth(), contentPadding = 0.dp) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(SystemCardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SystemSectionHeader(
                    title = "Обладнання",
                    subtitle = if (profile.trainsAtGym) "Зал або повний доступ" else "Домашній профіль"
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ScheduleSettingsTab(
                        text = "Дім",
                        selected = !profile.trainsAtGym,
                        onClick = { onLocationChanged(false) },
                        modifier = Modifier.weight(1f)
                    )
                    ScheduleSettingsTab(
                        text = "Зал",
                        selected = profile.trainsAtGym,
                        onClick = { onLocationChanged(true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = dumbbellMaxKgDraft,
                    onValueChange = onDumbbellMaxKgChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Макс. гантель, кг",
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(SystemTheme.shapes.medium),
                    colors = systemOutlinedTextFieldColors(accent = colors.accentPrimary)
                )
            }
            items(EQUIPMENT_OPTIONS, key = { it.type.name }) { option ->
                EquipmentToggleRow(
                    label = option.label,
                    checked = profile.isEquipmentEnabled(option.type),
                    onCheckedChange = { checked -> onEquipmentAvailabilityChanged(option.type, checked) }
                )
            }
        }
    }
}

@Composable
private fun EquipmentToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PanelHeader(
    title: String,
    subtitle: String,
    actionIcon: ImageVector,
    actionTint: Color,
    onAction: () -> Unit
) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SystemCardPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        CompactIconButton(
            icon = actionIcon,
            tint = actionTint,
            onClick = onAction
        )
    }
}

@Composable
private fun ExerciseSettingsRow(
    exercise: ExerciseDetails,
    leadingNumber: Int?,
    actionIcon: ImageVector,
    actionTint: Color,
    onAction: () -> Unit,
    onTrackingModeChanged: (Int, ExerciseTrackingMode) -> Unit
) {
    val context = LocalContext.current
    val colors = SystemTheme.colors
    val trackingMode = ExerciseTrackingModeResolver.resolve(exercise)
    val canConfigureTracking = exercise.externalId == null
    val primaryMuscle = exercise.muscleGroups.firstOrNull()
        ?.toUiText()
        ?.asString(context)
        ?: "М'язи не вказано"
    val equipment = exercise.equipment
        ?.toEquipmentUiText()
        ?.asString(context)
        ?: "Без обладнання"
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseRowLead(number = leadingNumber)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exercise.nameUk ?: exercise.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$primaryMuscle / $equipment / ${trackingMode.settingsLabel()}",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            CompactIconButton(
                icon = actionIcon,
                tint = actionTint,
                onClick = onAction
            )
        }

        if (canConfigureTracking) {
            TrackingModeSelector(
                selected = trackingMode,
                onSelect = { selectedMode -> onTrackingModeChanged(exercise.id, selectedMode) },
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun TrackingModeSelector(
    selected: ExerciseTrackingMode,
    onSelect: (ExerciseTrackingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items(USER_CONFIGURABLE_TRACKING_MODES, key = { it.name }) { mode ->
            TrackingModeOptionChip(
                text = mode.settingsShortLabel(),
                selected = mode == selected,
                onClick = { onSelect(mode) }
            )
        }
    }
}

@Composable
private fun TrackingModeOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.pill)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) colors.accentPrimarySoft else colors.surfaceGlassSoft)
            .border(1.dp, if (selected) colors.borderActive else colors.borderSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (selected) colors.accentPrimary else colors.textSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExerciseRowLead(number: Int?) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.small)
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(shape)
            .background(colors.accentPrimarySoft)
            .border(1.dp, colors.accentPrimary.copy(alpha = 0.24f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (number != null) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Black
                )
            )
        } else {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = colors.accentPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CompactIconButton(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(shape)
            .background(tint.copy(alpha = 0.075f))
            .border(1.dp, tint.copy(alpha = 0.2f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EmptyPanelText(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(SystemScreenPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textMuted)
        )
    }
}

private enum class ScheduleSettingsPane {
    Day,
    Library,
    Equipment
}

private data class EquipmentOption(
    val type: EquipmentType,
    val label: String
)

private val EQUIPMENT_OPTIONS = listOf(
    EquipmentOption(EquipmentType.DUMBBELL, "Гантелі"),
    EquipmentOption(EquipmentType.BARBELL, "Штанга"),
    EquipmentOption(EquipmentType.BENCH, "Лава"),
    EquipmentOption(EquipmentType.PULL_UP_BAR, "Турнік"),
    EquipmentOption(EquipmentType.DIP_BARS, "Бруси"),
    EquipmentOption(EquipmentType.BANDS, "Резинки"),
    EquipmentOption(EquipmentType.MACHINE, "Тренажери / кабелі"),
    EquipmentOption(EquipmentType.KETTLEBELL, "Гиря"),
    EquipmentOption(EquipmentType.EXERCISE_BALL, "Фітбол"),
    EquipmentOption(EquipmentType.FOAM_ROLL, "Ролер")
)

private fun EquipmentProfile.isEquipmentEnabled(type: EquipmentType): Boolean =
    when (type) {
        EquipmentType.BODY_ONLY -> true
        EquipmentType.BARBELL -> barbellAvailable || type in availableEquipment
        EquipmentType.BENCH -> benchAvailable || type in availableEquipment
        EquipmentType.PULL_UP_BAR -> pullUpBarAvailable || type in availableEquipment
        EquipmentType.DIP_BARS -> dipBarsAvailable || type in availableEquipment
        EquipmentType.BANDS -> bandsAvailable || type in availableEquipment
        EquipmentType.MACHINE,
        EquipmentType.CABLE -> machinesAvailable || type in availableEquipment
        else -> type in availableEquipment
    }

private val USER_CONFIGURABLE_TRACKING_MODES = listOf(
    ExerciseTrackingMode.WEIGHT_REPS,
    ExerciseTrackingMode.BODYWEIGHT_REPS,
    ExerciseTrackingMode.TIME_SECONDS,
    ExerciseTrackingMode.TIME_MINUTES
)

private fun ExerciseTrackingMode.settingsLabel(): String =
    when (this) {
        ExerciseTrackingMode.WEIGHT_REPS -> "кг + повтори"
        ExerciseTrackingMode.BODYWEIGHT_REPS -> "повтори"
        ExerciseTrackingMode.TIME_SECONDS -> "секунди"
        ExerciseTrackingMode.TIME_MINUTES -> "хвилини"
    }

private fun ExerciseTrackingMode.settingsShortLabel(): String =
    when (this) {
        ExerciseTrackingMode.WEIGHT_REPS -> "кг+повт"
        ExerciseTrackingMode.BODYWEIGHT_REPS -> "повт"
        ExerciseTrackingMode.TIME_SECONDS -> "сек"
        ExerciseTrackingMode.TIME_MINUTES -> "хв"
    }

private fun Int.exerciseCountText(): String =
    when {
        this == 1 -> "1 вправа"
        this in 2..4 -> "$this вправи"
        else -> "$this вправ"
    }
