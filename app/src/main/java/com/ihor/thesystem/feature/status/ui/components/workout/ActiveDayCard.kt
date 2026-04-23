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
import com.ihor.thesystem.feature.status.viewmodel.ActiveSetInput

@Composable
fun ActiveDayCard(
    data: ActiveDayUiModel,
    onSetWeightChanged: (Int, Long, String) -> Unit,
    onSetRepsChanged: (Int, Long, String) -> Unit,
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
                    
                    ExerciseItem(
                        exercise = exercise,
                        matrixEntry = matrixEntry,
                        onSetWeightChanged = { setId, w -> onSetWeightChanged(exercise.exerciseId, setId, w) },
                        onSetRepsChanged = { setId, r -> onSetRepsChanged(exercise.exerciseId, setId, r) },
                        onSetCompleted = { setId -> onSetCompleted(exercise.exerciseId, setId) },
                        onSetup = { matrixEntry?.let(onOpenSetup) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    exercise: com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel,
    matrixEntry: MatrixEntryUiModel?,
    onSetWeightChanged: (Long, String) -> Unit,
    onSetRepsChanged: (Long, String) -> Unit,
    onSetCompleted: (Long) -> Unit,
    onSetup: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (exercise.recommendation != null) {
                    Text(
                        text = exercise.recommendation,
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontFamily = RajdhaniFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                // Sets Input Area
                exercise.sets.forEachIndexed { index, set ->
                    SetInputRow(
                        index = index + 1,
                        set = set,
                        onWeightChange = { onSetWeightChanged(set.id, it) },
                        onRepsChange = { onSetRepsChanged(set.id, it) },
                        onComplete = { onSetCompleted(set.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (matrixEntry != null) {
                    // Progress Bar
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ПРОГРЕС ВПРАВИ",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${(matrixEntry.progressPercent * 100).toInt()}%",
                                color = NeonCyan,
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
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(matrixEntry.progressPercent.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(NeonCyan, Color(0xFFB257FF))))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Поточна", matrixEntry.displayCurrent)
                        StatItem("Ціль", matrixEntry.displayTarget)
                        StatItem("Ранг", matrixEntry.currentRank.name)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSetup,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.Settings, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
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
    onComplete: () -> Unit
) {
    var weightText by remember(set.id) { mutableStateOf(set.weight) }
    var repsText by remember(set.id) { mutableStateOf(set.reps) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (set.isCompleted) NeonCyan.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            color = if (set.isCompleted) NeonCyan else Color.White.copy(alpha = 0.3f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && weightText != set.weight) {
                        onWeightChange(weightText)
                    }
                },
            placeholder = { Text("кг", fontSize = 12.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = NeonCyan,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = repsText,
            onValueChange = { repsText = it },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && repsText != set.reps) {
                        onRepsChange(repsText)
                    }
                },
            placeholder = { Text("reps", fontSize = 12.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedBorderColor = NeonCyan,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
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
                .background(if (set.isCompleted) NeonCyan else Color.White.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (set.isCompleted) Color.Black else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
