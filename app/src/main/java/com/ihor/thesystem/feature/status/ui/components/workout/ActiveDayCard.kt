package com.ihor.thesystem.feature.status.ui.components.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemControlHeight
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.onFocusChanged
import com.ihor.thesystem.core.ui.components.glassCard
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors

@Composable
fun ActiveDayCard(
    data: ActiveDayUiModel,
    onSetWeightChanged: (Int, Long, String) -> Unit,
    onSetRepsChanged: (Int, Long, String) -> Unit,
    onSetFocusLost: (Int, Long) -> Unit,
    onSetCompleted: (Int, Long) -> Unit,
    onOpenSetup: (MatrixEntryUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        if (data.exercises.isNotEmpty()) {
            data.exercises.forEach { exercise ->
                key(exercise.exerciseId) {
                    val matrixEntry = data.matrixEntries.find { it.exerciseId == exercise.exerciseId }
                    
                    ActiveDayCard(
                        exercise = exercise,
                        matrixEntry = matrixEntry,
                        onSetWeightChanged = { setId, w -> onSetWeightChanged(exercise.exerciseId, setId, w) },
                        onSetRepsChanged = { setId, r -> onSetRepsChanged(exercise.exerciseId, setId, r) },
                        onSetFocusLost = { setId -> onSetFocusLost(exercise.exerciseId, setId) },
                        onSetCompleted = { setId -> onSetCompleted(exercise.exerciseId, setId) },
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
}

@Composable
fun ActiveDayCard(
    exercise: com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel,
    matrixEntry: MatrixEntryUiModel?,
    onSetWeightChanged: (Long, String) -> Unit,
    onSetRepsChanged: (Long, String) -> Unit,
    onSetFocusLost: (Long) -> Unit,
    onSetCompleted: (Long) -> Unit,
    onSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard()
                .clickable { isExpanded = !isExpanded }
                .padding(SystemCardPadding),
            verticalArrangement = Arrangement.spacedBy(SystemCardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.accentPrimary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = colors.accentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (exercise.nameUk ?: exercise.name).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (exercise.recommendation != null) {
                        Text(
                            text = exercise.recommendation,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (exercise.externalId != null) {
                        com.ihor.thesystem.presentation.common.components.ExerciseAnimationPlayer(
                            exerciseExternalId = exercise.externalId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(shapes.medium))
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(shapes.medium))
                        )
                        Spacer(modifier = Modifier.height(SystemCardPadding))
                    } else if (exercise.gifUrl != null) {
                        com.ihor.thesystem.presentation.common.components.HologramExerciseImage(
                            gifUrl = exercise.gifUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(shapes.medium))
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(shapes.medium))
                        )
                        Spacer(modifier = Modifier.height(SystemCardPadding))
                    }

                    // Sets Input Area
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        exercise.sets.forEachIndexed { index, set ->
                            SetInputRow(
                                index = index + 1,
                                set = set,
                                trackingMode = exercise.trackingMode,
                                onWeightChange = { onSetWeightChanged(set.id, it) },
                                onRepsChange = { onSetRepsChanged(set.id, it) },
                                onFocusLost = { onSetFocusLost(set.id) },
                                onComplete = { onSetCompleted(set.id) }
                            )
                        }
                    }

                    if (matrixEntry != null) {
                        Spacer(modifier = Modifier.height(SystemCardPadding))
                        
                        // Progress Bar
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = SystemCardPadding)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.text_exercise_progress),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.textSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${(matrixEntry.progressPercent * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = colors.accentPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(colors.overlayLight)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(matrixEntry.progressPercent.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(Brush.horizontalGradient(listOf(colors.accentPrimary, colors.accentAi)))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(stringResource(R.string.text_current), matrixEntry.displayCurrent)
                            StatItem(stringResource(R.string.text_target), matrixEntry.displayTarget)
                            StatItem(stringResource(R.string.text_rank_label), matrixEntry.currentRank.name)
                        }
                        
                        Spacer(modifier = Modifier.height(SystemItemSpacing))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSetup,
                            modifier = Modifier
                                .background(colors.overlayLight, RoundedCornerShape(shapes.small))
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.Settings, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetInputRow(
    index: Int,
    set: ActiveSetInput,
    trackingMode: ExerciseTrackingMode,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    onComplete: () -> Unit
) {
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes
    var weightText by remember(set.id) { mutableStateOf(set.weight) }
    var repsText by remember(set.id) { mutableStateOf(set.reps) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(radius = shapes.small)
            .background(if (set.isCompleted) colors.accentSuccess.copy(alpha = 0.10f) else colors.surfaceGlassSoft)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            color = if (set.isCompleted) colors.accentSuccess else colors.textSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (trackingMode.usesWeightInput) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { 
                    weightText = it
                    onWeightChange(it) 
                },
                modifier = Modifier
                    .weight(1f)
                    .height(SystemControlHeight)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            onFocusLost()
                        }
                },
                placeholder = {
                    Text(
                        text = "кг",
                        style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = systemOutlinedTextFieldColors(),
                shape = RoundedCornerShape(shapes.extraSmall),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        OutlinedTextField(
            value = repsText,
            onValueChange = { 
                repsText = it
                onRepsChange(it)
            },
            modifier = Modifier
                .weight(1f)
                .height(SystemControlHeight)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        onFocusLost()
                    }
                },
            placeholder = {
                Text(
                    text = trackingMode.valueHint,
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (trackingMode.usesTimeInput) KeyboardType.Text else KeyboardType.Number
            ),
            colors = systemOutlinedTextFieldColors(),
            shape = RoundedCornerShape(shapes.extraSmall),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onComplete,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (set.isCompleted) colors.accentSuccess else colors.overlayMedium)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (set.isCompleted) colors.background else colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val colors = SystemTheme.colors
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
