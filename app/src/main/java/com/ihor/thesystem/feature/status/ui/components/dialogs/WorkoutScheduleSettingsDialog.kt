package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.OnBackground
import com.ihor.thesystem.core.theme.OnSurfaceVariant
import com.ihor.thesystem.core.theme.Primary
import com.ihor.thesystem.core.theme.RajdhaniFamily
import com.ihor.thesystem.feature.status.viewmodel.WorkoutScheduleSettingsUiState

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
    onDeleteExercise: (Int) -> Unit
) {
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showManageExercises by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(Primary.copy(alpha = 0.5f), Color.Transparent))
                    ),
                    RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1A1A2E)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "НАЛАШТУВАННЯ РОЗКЛАДУ",
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold
                )

                // 1. Horizontal LazyRow for Days
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items((1..uiState.totalCycleDays).toList()) { day ->
                        val isSelected = uiState.selectedDay == day
                        Surface(
                            modifier = Modifier.clickable { onSelectDay(day) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Primary else Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, if (isSelected) Primary else Color.White.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = "День $day",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isSelected) Color.Black else OnBackground,
                                fontFamily = RajdhaniFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. Workout Name Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.workoutNameDraft,
                        onValueChange = onWorkoutNameChange,
                        label = { Text("Назва тренування", color = OnSurfaceVariant) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnBackground,
                            unfocusedTextColor = OnBackground,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(
                        onClick = onSaveWorkoutName,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Primary)
                    }
                }

                // 3. Current Exercises List
                Text(
                    text = "Вправи дня",
                    style = MaterialTheme.typography.labelLarge,
                    color = OnSurfaceVariant,
                    fontFamily = RajdhaniFamily
                )

                Box(modifier = Modifier.heightIn(max = 200.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.exercisesForSelectedDay) { exercise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = exercise.name,
                                    color = OnBackground,
                                    fontFamily = RajdhaniFamily
                                )
                                IconButton(onClick = { onRemoveExercise(exercise.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                // 4. Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Додати вправу", fontWeight = FontWeight.Bold, fontFamily = RajdhaniFamily)
                    }

                    OutlinedButton(
                        onClick = { showManageExercises = !showManageExercises },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("Управління", color = OnBackground, fontFamily = RajdhaniFamily)
                    }
                }

                // 5. Global Exercises Management Section
                if (showManageExercises) {
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Text(
                        text = "Всі вправи системи",
                        style = MaterialTheme.typography.labelLarge,
                        color = OnSurfaceVariant,
                        fontFamily = RajdhaniFamily
                    )
                    Box(modifier = Modifier.heightIn(max = 150.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(uiState.allExercises) { exercise ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(exercise.name, color = OnBackground.copy(alpha = 0.7f), fontSize = 14.sp)
                                    IconButton(onClick = { onDeleteExercise(exercise.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExerciseDialog) {
        AddExerciseSelectionDialog(
            allExercises = uiState.allExercises,
            onDismiss = { showAddExerciseDialog = false },
            onSelect = { 
                onAddExercise(it.toString())
                showAddExerciseDialog = false
            },
            onCreateNew = {
                onCreateNewExercise(it)
                showAddExerciseDialog = false
            }
        )
    }
}

@Composable
fun AddExerciseSelectionDialog(
    allExercises: List<com.ihor.thesystem.domain.model.ExerciseDetails>,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onCreateNew: (String) -> Unit
) {
    var newName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1A1A2E),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("ДОДАТИ ВПРАВУ", color = Primary, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFamily)

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Назва нової вправи...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { if (newName.isNotBlank()) onCreateNew(newName) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newName.isNotBlank()
                ) {
                    Text("СТВОРИТИ ГЛОБАЛЬНО")
                }

                Divider(color = Color.White.copy(alpha = 0.1f))

                Text("АБО ОБЕРИ ІЗ СПИСКУ:", fontSize = 12.sp, color = OnSurfaceVariant)

                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    LazyColumn {
                        items(allExercises) { exercise ->
                            Text(
                                text = exercise.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(exercise.id) }
                                    .padding(vertical = 12.dp),
                                color = OnBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
