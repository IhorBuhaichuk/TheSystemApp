package com.ihor.thesystem.feature.exercise_search.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseFilterState
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExercisePickerItemUiModel
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop

@Composable
fun ExerciseSearchScreen(
    viewModel: ExerciseSearchViewModel,
    onExerciseClick: (ExerciseDetails) -> Unit
) {
    ExercisePickerScreen(
        viewModel = viewModel,
        onBack = {},
        onSelectExercise = onExerciseClick,
        actionLabel = "Вибрати"
    )
}

@Composable
fun ExercisePickerScreen(
    onBack: () -> Unit,
    onSelectExercise: (ExerciseDetails) -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "Вибрати",
    createExerciseLabel: String? = null,
    onCreateExercise: ((String) -> Unit)? = null,
    viewModel: ExerciseSearchViewModel = hiltViewModel()
) {
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val pickerItems by viewModel.pickerItems.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors
    var newExerciseName by remember { mutableStateOf("") }

    Box(
        modifier = modifier
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
            ExercisePickerHeader(onBack = onBack)
            ExercisePickerSearchField(
                query = filterState.query,
                onQueryChange = { viewModel.onEvent(ExerciseSearchEvent.UpdateQuery(it)) },
                onClear = { viewModel.onEvent(ExerciseSearchEvent.UpdateQuery("")) }
            )
            ExercisePickerFilters(
                state = filterState,
                onEvent = viewModel::onEvent
            )
            ExercisePickerList(
                items = pickerItems,
                actionLabel = actionLabel,
                onSelectExercise = onSelectExercise,
                modifier = Modifier.weight(1f)
            )
            if (onCreateExercise != null && createExerciseLabel != null) {
                ExercisePickerCreateBar(
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it },
                    placeholder = createExerciseLabel,
                    onCreate = {
                        val name = newExerciseName.trim()
                        if (name.isNotEmpty()) {
                            onCreateExercise(name)
                            newExerciseName = ""
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ExercisePickerHeader(onBack: () -> Unit) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Вибір вправи",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                )
            )
            Text(
                text = "Швидкий пошук у базі вправ",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
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
                imageVector = Icons.Filled.Close,
                contentDescription = "Закрити",
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun ExercisePickerSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val colors = SystemTheme.colors
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Пошук вправи",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textMuted)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = colors.accentPrimary
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Очистити",
                        tint = colors.textMuted
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(SystemTheme.shapes.medium),
        colors = systemOutlinedTextFieldColors()
    )
}

@Composable
private fun ExercisePickerFilters(
    state: ExerciseFilterState,
    onEvent: (ExerciseSearchEvent) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = SystemCardPadding) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
            PickerFilterChip(
                text = "System Core",
                selected = state.systemCoreOnly,
                onClick = { onEvent(ExerciseSearchEvent.ToggleSystemCore) }
            )

            FilterGroupTitle(text = "М'язова група")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(muscleFilters, key = { it.label }) { filter ->
                    val selected = filter.muscles.any { it in state.selectedMuscles }
                    PickerFilterChip(
                        text = filter.label,
                        selected = selected,
                        onClick = {
                            onEvent(ExerciseSearchEvent.ToggleMuscleGroup(filter.muscles))
                        }
                    )
                }
            }

            FilterGroupTitle(text = "Обладнання")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(equipmentFilters, key = { it }) { equipment ->
                    val context = LocalContext.current
                    PickerFilterChip(
                        text = equipment.toEquipmentUiText().asString(context),
                        selected = equipment in state.selectedEquipment,
                        onClick = {
                            onEvent(ExerciseSearchEvent.ToggleEquipment(equipment))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterGroupTitle(text: String) {
    val colors = SystemTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            color = colors.textSecondary,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun PickerFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.pill)
    Surface(
        modifier = Modifier
            .clip(shape)
            .systemClickable(onClick = onClick),
        shape = shape,
        color = if (selected) colors.accentPrimarySoft else colors.overlayLight,
        border = BorderStroke(1.dp, if (selected) colors.borderActive else colors.borderSubtle)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
private fun ExercisePickerList(
    items: List<ExercisePickerItemUiModel>,
    actionLabel: String,
    onSelectExercise: (ExerciseDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    DarkGlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 0.dp
    ) {
        if (items.isEmpty()) {
            ExercisePickerEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SystemItemSpacing),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.exercise.id }) { item ->
                    ExercisePickerRow(
                        item = item,
                        actionLabel = actionLabel,
                        onSelectExercise = onSelectExercise
                    )
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerEmptyState() {
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SystemScreenPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.text_no_exercises_found),
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textMuted)
        )
    }
}

@Composable
private fun ExercisePickerRow(
    item: ExercisePickerItemUiModel,
    actionLabel: String,
    onSelectExercise: (ExerciseDetails) -> Unit
) {
    val colors = SystemTheme.colors
    val context = LocalContext.current
    val exercise = item.exercise
    val primaryMuscle = exercise.muscleGroups.firstOrNull()
        ?.toUiText()
        ?.asString(context)
        ?: "М'язи не вказано"
    val equipment = exercise.equipment
        ?.toEquipmentUiText()
        ?.asString(context)
        ?: "Без обладнання"
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.overlayMedium,
                        colors.overlayLight
                    )
                )
            )
            .border(1.dp, colors.borderSubtle, shape)
            .systemClickable { onSelectExercise(exercise) }
            .padding(SystemItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconShape = RoundedCornerShape(SystemTheme.shapes.medium)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(iconShape)
                .background(colors.accentPrimarySoft)
                .border(
                    1.dp,
                    colors.accentPrimary.copy(alpha = 0.24f),
                    iconShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = colors.accentPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

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
                text = "$primaryMuscle · $equipment",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.lastResultText?.let { lastResult ->
                Text(
                    text = "Останній: $lastResult",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        SystemButton(
            text = actionLabel,
            onClick = { onSelectExercise(exercise) },
            modifier = Modifier.width(104.dp)
        )
    }
}

@Composable
private fun ExercisePickerCreateBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onCreate: () -> Unit
) {
    val colors = SystemTheme.colors
    val canCreate = value.trim().isNotEmpty()

    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = SystemItemSpacing) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textMuted)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(SystemTheme.shapes.medium),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canCreate) onCreate() }),
                colors = systemOutlinedTextFieldColors()
            )
            SystemButton(
                text = "Створити",
                icon = Icons.Filled.Add,
                onClick = onCreate,
                enabled = canCreate,
                modifier = Modifier.width(118.dp)
            )
        }
    }
}

private data class PickerMuscleFilter(
    val label: String,
    val muscles: Set<String>
)

private val muscleFilters = listOf(
    PickerMuscleFilter(label = "Груди", muscles = setOf("CHEST")),
    PickerMuscleFilter(label = "Спина", muscles = setOf("BACK")),
    PickerMuscleFilter(label = "Ноги", muscles = setOf("QUADS", "HAMSTRINGS_GLUTES", "LEGS")),
    PickerMuscleFilter(label = "Плечі", muscles = setOf("SHOULDERS")),
    PickerMuscleFilter(label = "Руки", muscles = setOf("ARMS")),
    PickerMuscleFilter(label = "Кор", muscles = setOf("ABS", "CORE"))
)

private val equipmentFilters = listOf(
    "body only",
    "machine",
    "dumbbell",
    "barbell",
    "cable",
    "bands",
    "kettlebell",
    "medicine ball",
    "exercise ball",
    "e-z curl bar",
    "foam roll",
    "other"
)
