package com.ihor.thesystem.feature.exercise_search.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ihor.thesystem.R
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
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseFilterState
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExercisePickerItemUiModel
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchViewModel
import com.ihor.thesystem.feature.status.ui.RpgStatusBackdrop

@Composable
fun ExerciseSearchScreen(
    viewModel: ExerciseSearchViewModel,
    onExerciseClick: (ExerciseDetails) -> Unit
) {
    ExercisePickerScreen(
        viewModel = viewModel,
        onBack = {},
        onSelectExercise = onExerciseClick,
        actionLabel = "Р’РёР±СЂР°С‚Рё"
    )
}

@Composable
fun ExercisePickerScreen(
    onBack: () -> Unit,
    onSelectExercise: (ExerciseDetails) -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "Р’РёР±СЂР°С‚Рё",
    viewModel: ExerciseSearchViewModel = hiltViewModel()
) {
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val pickerItems by viewModel.pickerItems.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SystemBackground)
    ) {
        RpgStatusBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SystemScreenPadding)
                .padding(top = 16.dp, bottom = 24.dp),
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
        }
    }
}

@Composable
private fun ExercisePickerHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Р’РёР±С–СЂ РІРїСЂР°РІРё",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
            )
            Text(
                text = "РЁРІРёРґРєРёР№ РїРѕС€СѓРє Сѓ Р±Р°Р·С– РІРїСЂР°РІ",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, BorderSubtle, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Р—Р°РєСЂРёС‚Рё",
                tint = TextSecondary
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
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "РџРѕС€СѓРє РІРїСЂР°РІРё",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = AccentPrimary
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "РћС‡РёСЃС‚РёС‚Рё",
                        tint = TextMuted
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderActive,
            unfocusedBorderColor = BorderSubtle,
            focusedContainerColor = SystemSurfaceGlass.copy(alpha = 0.58f),
            unfocusedContainerColor = SystemSurfaceGlass.copy(alpha = 0.44f),
            cursorColor = AccentPrimary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun ExercisePickerFilters(
    state: ExerciseFilterState,
    onEvent: (ExerciseSearchEvent) -> Unit
) {
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterGroupTitle(text = "Рњ'СЏР·РѕРІР° РіСЂСѓРїР°")
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

            FilterGroupTitle(text = "РћР±Р»Р°РґРЅР°РЅРЅСЏ")
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
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            color = TextSecondary,
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
    val shape = RoundedCornerShape(999.dp)
    Surface(
        modifier = Modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) AccentPrimarySoft else Color.White.copy(alpha = 0.035f),
        border = BorderStroke(1.dp, if (selected) BorderActive else BorderSubtle)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
                contentPadding = PaddingValues(12.dp),
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.text_no_exercises_found),
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
        )
    }
}

@Composable
private fun ExercisePickerRow(
    item: ExercisePickerItemUiModel,
    actionLabel: String,
    onSelectExercise: (ExerciseDetails) -> Unit
) {
    val context = LocalContext.current
    val exercise = item.exercise
    val primaryMuscle = exercise.muscleGroups.firstOrNull()
        ?.toUiText()
        ?.asString(context)
        ?: "Рњ'СЏР·Рё РЅРµ РІРєР°Р·Р°РЅРѕ"
    val equipment = exercise.equipment
        ?.toEquipmentUiText()
        ?.asString(context)
        ?: "Р‘РµР· РѕР±Р»Р°РґРЅР°РЅРЅСЏ"
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.045f),
                        Color.White.copy(alpha = 0.022f)
                    )
                )
            )
            .border(1.dp, BorderSubtle, shape)
            .clickable { onSelectExercise(exercise) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(AccentPrimarySoft.copy(alpha = 0.72f))
                .border(1.dp, AccentPrimary.copy(alpha = 0.24f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = AccentPrimary,
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
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$primaryMuscle В· $equipment",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.lastResultText?.let { lastResult ->
                Text(
                    text = "РћСЃС‚Р°РЅРЅС–Р№: $lastResult",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
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

private data class PickerMuscleFilter(
    val label: String,
    val muscles: Set<String>
)

private val muscleFilters = listOf(
    PickerMuscleFilter(label = "Р“СЂСѓРґРё", muscles = setOf("CHEST")),
    PickerMuscleFilter(label = "РЎРїРёРЅР°", muscles = setOf("BACK")),
    PickerMuscleFilter(label = "РќРѕРіРё", muscles = setOf("QUADS", "HAMSTRINGS_GLUTES", "LEGS")),
    PickerMuscleFilter(label = "РџР»РµС‡С–", muscles = setOf("SHOULDERS")),
    PickerMuscleFilter(label = "Р СѓРєРё", muscles = setOf("ARMS")),
    PickerMuscleFilter(label = "РљРѕСЂ", muscles = setOf("ABS", "CORE"))
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
