package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.feature.exercise_search.ui.toEquipmentUiText
import com.ihor.thesystem.feature.exercise_search.ui.toUiText

@Composable
internal fun DayExercisesPanel(
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
                    text = "Вправи ще не додані. Натисніть +.",
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
internal fun ExerciseLibraryPanel(
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
                    text = "База вправ порожня. Додайте свою вправу.",
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
            .systemClickable(onClick = onClick)
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
            .systemClickable(onClick = onClick),
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

internal fun Int.exerciseCountText(): String =
    when {
        this == 1 -> "1 вправа"
        this in 2..4 -> "$this вправи"
        else -> "$this вправ"
    }
