package com.ihor.thesystem.feature.status.ui.components.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.glassCard
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel

@Composable
fun ActiveDayCard(
    data: ActiveDayUiModel,
    onSetWeightChanged: (Int, Long, String) -> Unit,
    onSetRepsChanged: (Int, Long, String) -> Unit,
    onSetFocusLost: (Int, Long) -> Unit,
    onSetCompletionChanged: (Int, Long, Boolean) -> Unit,
    onAddSet: (Int, String) -> Unit,
    onOpenSetup: (MatrixEntryUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        data.adjustmentReason?.let { reason ->
            DecisionAdjustmentNotice(reason = reason)
        }

        data.exercises.forEach { exercise ->
            key(exercise.exerciseId) {
                val matrixEntry = data.matrixEntries.find { it.exerciseId == exercise.exerciseId }
                ActiveExerciseLoggerCard(
                    exercise = exercise,
                    matrixEntry = matrixEntry,
                    onSetWeightChanged = { setId, value -> onSetWeightChanged(exercise.exerciseId, setId, value) },
                    onSetRepsChanged = { setId, value -> onSetRepsChanged(exercise.exerciseId, setId, value) },
                    onSetFocusLost = { setId -> onSetFocusLost(exercise.exerciseId, setId) },
                    onSetCompletionChanged = { setId, completed ->
                        onSetCompletionChanged(exercise.exerciseId, setId, completed)
                    },
                    onAddSet = { weight -> onAddSet(exercise.exerciseId, weight) },
                    onSetup = {
                        if (matrixEntry != null) {
                            onOpenSetup(matrixEntry)
                        } else {
                            onOpenSetup(
                                MatrixEntryUiModel(
                                    exerciseId = exercise.exerciseId,
                                    exerciseName = exercise.name,
                                    startWeight = 0f,
                                    targetWeight = 0f,
                                    currentWeight = 0f,
                                    targetWeightNote = null,
                                    weeklyStep = 0f,
                                    progressPercent = 0f,
                                    currentRank = com.ihor.thesystem.domain.model.Rank.E,
                                    usesExternalLoad = exercise.trackingMode.usesWeightInput
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DecisionAdjustmentNotice(
    reason: String,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassCard()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = colors.accentAi,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = reason,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActiveExerciseLoggerCard(
    exercise: ExerciseWorkoutUiModel,
    matrixEntry: MatrixEntryUiModel?,
    onSetWeightChanged: (Long, String) -> Unit,
    onSetRepsChanged: (Long, String) -> Unit,
    onSetFocusLost: (Long) -> Unit,
    onSetCompletionChanged: (Long, Boolean) -> Unit,
    onAddSet: (String) -> Unit,
    onSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes
    var showInfo by remember { mutableStateOf(false) }
    val completedCount = exercise.sets.count { it.isCompleted }
    val totalCount = exercise.sets.size.coerceAtLeast(1)
    val progress = completedCount.toFloat() / totalCount.toFloat()

    if (showInfo) {
        ExerciseInfoDialog(
            exercise = exercise,
            matrixEntry = matrixEntry,
            onDismiss = { showInfo = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCard()
            .padding(SystemCardPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.accentPrimary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (exercise.trackingMode.usesTimeInput) Icons.Filled.Timer else Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = colors.accentPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = exercise.nameUk ?: exercise.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exercise.recommendation ?: exercise.trackingMode.inputSummary(exercise.sets.size),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { showInfo = true },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.overlayLight)
                    .border(1.dp, colors.borderSubtle, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Інформація про вправу",
                    tint = colors.accentAi,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        exercise.techniqueCheckText()?.let { text ->
            TechniqueCheckNotice(
                text = text,
                emphasized = exercise.techniqueCheckEmphasized
            )
        }

        WorkoutSetInputBlock(
            exercise = exercise,
            onSetWeightChanged = onSetWeightChanged,
            onSetRepsChanged = onSetRepsChanged,
            onSetFocusLost = onSetFocusLost,
            onSetCompletionChanged = onSetCompletionChanged,
            onAddSet = onAddSet
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Записано $completedCount/$totalCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
                matrixEntry?.let {
                    Text(
                        text = "Ранг ${it.currentRank.name}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.overlayLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(colors.accentPrimary, colors.accentAi)))
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onSetup,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(shapes.small))
                    .background(colors.overlayLight)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(shapes.small))
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Налаштування прогресії",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun TechniqueCheckNotice(
    text: String,
    emphasized: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val accent = if (emphasized) colors.accentWarning else colors.accentAi
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(accent.copy(alpha = if (emphasized) 0.14f else 0.08f))
            .border(1.dp, accent.copy(alpha = if (emphasized) 0.42f else 0.22f), shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WorkoutSetInputBlock(
    exercise: ExerciseWorkoutUiModel,
    onSetWeightChanged: (Long, String) -> Unit,
    onSetRepsChanged: (Long, String) -> Unit,
    onSetFocusLost: (Long) -> Unit,
    onSetCompletionChanged: (Long, Boolean) -> Unit,
    onAddSet: (String) -> Unit
) {
    val colors = SystemTheme.colors
    val groupedSets = remember(exercise.sets) { exercise.sets.groupAdjacentByWeight() }
    val lastWeight = groupedSets.lastOrNull()?.weight.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        groupedSets.forEachIndexed { groupIndex, group ->
            WorkoutSetGroupRow(
                index = groupIndex + 1,
                group = group,
                trackingMode = exercise.trackingMode,
                onSetWeightChanged = onSetWeightChanged,
                onSetRepsChanged = onSetRepsChanged,
                onSetFocusLost = onSetFocusLost,
                onSetCompletionChanged = onSetCompletionChanged
            )
        }

        SystemGhostButton(
            text = if (exercise.trackingMode.usesWeightInput) "Інша вага" else "Додати підхід",
            icon = Icons.Filled.Add,
            onClick = { onAddSet(lastWeight) },
            accent = colors.textSecondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WorkoutSetGroupRow(
    index: Int,
    group: WorkoutSetGroup,
    trackingMode: ExerciseTrackingMode,
    onSetWeightChanged: (Long, String) -> Unit,
    onSetRepsChanged: (Long, String) -> Unit,
    onSetFocusLost: (Long) -> Unit,
    onSetCompletionChanged: (Long, Boolean) -> Unit
) {
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes
    val completedCount = group.sets.count { it.isCompleted }
    val ready = completedCount == group.sets.size && group.sets.isNotEmpty()
    val hasInput = group.sets.any { it.weight.isNotBlank() || it.reps.isNotBlank() }
    val stateAccent = when {
        ready -> colors.accentSuccess
        hasInput -> colors.accentAi
        else -> colors.textMuted
    }
    val stateText = when {
        ready -> "done"
        hasInput -> "$completedCount/${group.sets.size}"
        else -> "empty"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(shapes.medium))
            .background(
                when {
                    ready -> colors.accentSuccess.copy(alpha = 0.08f)
                    hasInput -> colors.accentAi.copy(alpha = 0.055f)
                    else -> colors.surfaceGlassSoft
                }
            )
            .border(
                1.dp,
                when {
                    ready -> colors.accentSuccess.copy(alpha = 0.28f)
                    hasInput -> colors.accentAi.copy(alpha = 0.22f)
                    else -> colors.borderSubtle
                },
                RoundedCornerShape(shapes.medium)
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = stateAccent,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.width(18.dp)
        )

        if (trackingMode.usesWeightInput) {
            CompactWorkoutInput(
                value = group.weight,
                hint = "кг",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.width(88.dp),
                textAlign = TextAlign.Center,
                active = ready,
                onValueChange = { value ->
                    group.sets.forEach { set ->
                        onSetWeightChanged(set.id, value)
                        val completed = set.isReadyForCompletion(value, set.reps, trackingMode)
                        if (set.isCompleted != completed) {
                            onSetCompletionChanged(set.id, completed)
                        }
                    }
                },
                onFocusLost = {
                    group.sets.forEach { onSetFocusLost(it.id) }
                }
            )
        } else {
            StaticWorkoutMetricSlot(
                text = trackingMode.loadSlotLabel(),
                active = ready,
                modifier = Modifier.width(88.dp)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            group.sets.forEach { set ->
                CompactWorkoutInput(
                    value = set.reps,
                    hint = trackingMode.shortValueHint(),
                    keyboardType = if (trackingMode.usesTimeInput) KeyboardType.Text else KeyboardType.Number,
                    modifier = Modifier.width(54.dp),
                    textAlign = TextAlign.Center,
                    active = set.isCompleted,
                    onValueChange = { value ->
                        onSetRepsChanged(set.id, value)
                        val completed = set.isReadyForCompletion(group.weight, value, trackingMode)
                        if (set.isCompleted != completed) {
                            onSetCompletionChanged(set.id, completed)
                        }
                    },
                    onFocusLost = { onSetFocusLost(set.id) }
                )
            }
        }

        Text(
            text = stateText,
            style = MaterialTheme.typography.labelSmall.copy(
                color = stateAccent,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.End
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(38.dp)
        )
    }
}

@Composable
private fun CompactWorkoutInput(
    value: String,
    hint: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    active: Boolean = false,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.small)
    val borderColor = when {
        active -> colors.accentSuccess.copy(alpha = 0.52f)
        value.isNotBlank() -> colors.accentAi.copy(alpha = 0.42f)
        else -> colors.borderSubtle
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(colors.accentAi),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.titleSmall.copy(
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = textAlign
        ),
        modifier = modifier
            .height(42.dp)
            .clip(shape)
            .background(if (active) colors.accentSuccess.copy(alpha = 0.10f) else colors.surfaceGlassSoft)
            .border(1.dp, borderColor, shape)
            .onFocusChanged { state ->
                if (!state.isFocused) {
                    onFocusLost()
                }
            },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isBlank()) {
                    Text(
                        text = hint,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.textMuted,
                            fontWeight = FontWeight.Bold,
                            textAlign = textAlign
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun StaticWorkoutMetricSlot(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.small)
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(
                1.dp,
                if (active) colors.accentSuccess.copy(alpha = 0.28f) else colors.borderSubtle,
                shape
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = colors.textMuted,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
