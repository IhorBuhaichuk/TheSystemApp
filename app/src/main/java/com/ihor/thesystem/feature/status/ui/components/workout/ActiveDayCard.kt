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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.onFocusChanged
import com.ihor.thesystem.core.ui.components.glassCard
import com.ihor.thesystem.domain.model.ActiveSetInput
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import com.ihor.thesystem.R

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        currentRank = com.ihor.thesystem.domain.model.Rank.E
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
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name.uppercase(),
                        color = OnBackground,
                        fontSize = 18.sp,
                        fontFamily = RajdhaniFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (exercise.recommendation != null) {
                        Text(
                            text = exercise.recommendation,
                            color = OnSurfaceVariant,
                            fontSize = 13.sp,
                            fontFamily = RajdhaniFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = OnSurfaceVariant
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
                    if (exercise.gifUrl != null) {
                        com.ihor.thesystem.presentation.common.components.HologramExerciseImage(
                            gifUrl = exercise.gifUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Sets Input Area
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        exercise.sets.forEachIndexed { index, set ->
                            SetInputRow(
                                index = index + 1,
                                set = set,
                                onWeightChange = { onSetWeightChanged(set.id, it) },
                                onRepsChange = { onSetRepsChanged(set.id, it) },
                                onFocusLost = { onSetFocusLost(set.id) },
                                onComplete = { onSetCompleted(set.id) }
                            )
                        }
                    }

                    if (matrixEntry != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Progress Bar
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.text_exercise_progress),
                                    color = OnSurfaceVariant,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${(matrixEntry.progressPercent * 100).toInt()}%",
                                    color = Primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(OnBackground.copy(alpha = 0.05f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(matrixEntry.progressPercent.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(Brush.horizontalGradient(listOf(Primary, Color(0xFFB257FF))))
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSetup,
                            modifier = Modifier
                                .background(OnBackground.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.Settings, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
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
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    onComplete: () -> Unit
) {
    var weightText by remember(set.id) { mutableStateOf(set.weight) }
    var repsText by remember(set.id) { mutableStateOf(set.reps) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(radius = 12.dp)
            .background(if (set.isCompleted) StatusSuccess.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            color = if (set.isCompleted) StatusSuccess else OnSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = weightText,
            onValueChange = { 
                weightText = it
                onWeightChange(it) 
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        onFocusLost()
                    }
                },
            placeholder = { Text("кг", fontSize = 12.sp, color = OnSurfaceVariant) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = OnBackground.copy(alpha = 0.1f),
                focusedBorderColor = Primary,
                unfocusedTextColor = OnBackground,
                focusedTextColor = OnBackground
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = repsText,
            onValueChange = { 
                repsText = it
                onRepsChange(it)
            },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        onFocusLost()
                    }
                },
            placeholder = { Text("reps", fontSize = 12.sp, color = OnSurfaceVariant) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = OnBackground.copy(alpha = 0.1f),
                focusedBorderColor = Primary,
                unfocusedTextColor = OnBackground,
                focusedTextColor = OnBackground
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onComplete,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (set.isCompleted) StatusSuccess else OnBackground.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (set.isCompleted) Color.Black else OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, color = OnSurfaceVariant, fontSize = 10.sp)
        Text(value, color = OnBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
