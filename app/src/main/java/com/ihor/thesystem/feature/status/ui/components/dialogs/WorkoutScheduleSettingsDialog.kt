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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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

import com.ihor.thesystem.feature.exercise_search.ui.ExerciseSearchScreen
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchViewModel
import com.ihor.thesystem.feature.exercise_search.ui.toUiText
import com.ihor.thesystem.feature.exercise_search.ui.toMuscleUiText
import com.ihor.thesystem.feature.exercise_search.ui.toEquipmentUiText
import com.ihor.thesystem.feature.exercise_search.ui.toMechanicUiText
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

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
    exerciseSearchViewModel: ExerciseSearchViewModel = hiltViewModel()
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
                                    text = exercise.nameUk ?: exercise.name,
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
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
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
                                    Text(exercise.nameUk ?: exercise.name, color = OnBackground.copy(alpha = 0.7f), fontSize = 14.sp)
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
            viewModel = exerciseSearchViewModel,
            onDismiss = { showAddExerciseDialog = false },
            onSelect = { 
                onAddExercise(it.id.toString())
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
    viewModel: ExerciseSearchViewModel,
    onDismiss: () -> Unit,
    onSelect: (com.ihor.thesystem.domain.model.ExerciseDetails) -> Unit,
    onCreateNew: (String) -> Unit
) {
    val state by viewModel.filterState.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    var newName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .border(BorderStroke(1.dp, Primary.copy(alpha = 0.2f)), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0A0A12)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "ВИБІР ВПРАВИ",
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFamily,
                    fontSize = 20.sp
                )

                // Скинути фільтри
                TextButton(
                    onClick = { viewModel.onEvent(com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent.ClearFilters) },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Скинути фільтри",
                        color = Primary.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontFamily = RajdhaniFamily
                    )
                }

                // Пошуковий рядок
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { viewModel.onEvent(com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent.UpdateQuery(it)) },
                    placeholder = { Text("Пошук за назвою...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onEvent(com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent.UpdateQuery("")) }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = OnSurfaceVariant)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground,
                        unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Панель фільтрів
                FilterChipsRow(
                    title = "М\'язи",
                    items = listOf("CHEST", "BACK", "SHOULDERS", "QUADS", "HAMSTRINGS_GLUTES", "ARMS", "ABS", "LEGS", "CORE"),
                    selected = state.selectedMuscles,
                    onToggle = { viewModel.onEvent(com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent.ToggleMuscle(it)) },
                    labelMapper = { it.toMuscleUiText().asString() }
                )
                
                FilterChipsRow(
                    title = "Обладнання",
                    items = listOf("body only", "machine", "dumbbell", "barbell", "cable", "bands", "kettlebell", "medicine ball", "exercise ball", "e-z curl bar", "foam roll", "other"),
                    selected = state.selectedEquipment,
                    onToggle = { viewModel.onEvent(com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent.ToggleEquipment(it)) },
                    labelMapper = { it.toEquipmentUiText().asString() }
                )

                FilterChipsRow(
                    title = "Механіка",
                    items = listOf("compound", "isolation"),
                    selected = state.selectedMechanics,
                    onToggle = { viewModel.onEvent(com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent.ToggleMechanic(it)) },
                    labelMapper = { it.toMechanicUiText().asString() }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                // Список результатів
                if (exercises.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.text_no_exercises_found),
                            color = OnSurfaceVariant,
                            fontFamily = RajdhaniFamily
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(exercises) { exercise ->
                            ExerciseSearchItem(exercise) { onSelect(exercise) }
                        }
                    }
                }

                // Створення нової вправи
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Своя вправа...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )
                    Button(
                        onClick = { onCreateNew(newName) },
                        enabled = newName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
                    ) {
                        Text("ДОДАТИ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    title: String,
    items: List<String>,
    selected: Set<String>,
    labelMapper: @Composable (String) -> String = { it.lowercase().replaceFirstChar { char -> char.uppercase() } },
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = Primary.copy(alpha = 0.6f))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                val isSelected = selected.contains(item)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Primary else Color.White.copy(alpha = 0.05f))
                        .clickable { onToggle(item) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = labelMapper(item),
                        color = if (isSelected) Color.Black else OnBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseSearchItem(exercise: com.ihor.thesystem.domain.model.ExerciseDetails, onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.nameUk ?: exercise.name, color = OnBackground, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFamily)
            Text(
                text = "${exercise.muscleGroups.joinToString { it.toUiText().asString(context) }} | ${exercise.equipment?.toEquipmentUiText()?.asString(context) ?: "Без обладнання"}",
                color = OnSurfaceVariant,
                fontSize = 10.sp
            )
            exercise.mechanic?.let { mechanic ->
                Text(
                    text = mechanic.toMechanicUiText().asString(context),
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }
        }
        Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
    }
}
