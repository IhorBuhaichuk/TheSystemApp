package com.ihor.thesystem.feature.exercise_search.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseFilterState
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchEvent
import com.ihor.thesystem.feature.exercise_search.viewmodel.ExerciseSearchViewModel

@Composable
fun ExerciseSearchScreen(
    viewModel: ExerciseSearchViewModel,
    onExerciseClick: (ExerciseDetails) -> Unit
) {
    val state by viewModel.filterState.collectAsState()
    val exercises by viewModel.exercises.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A12))
    ) {
        SearchBar(
            query = state.query,
            onQueryChange = { viewModel.onEvent(ExerciseSearchEvent.UpdateQuery(it)) },
            onClearFilters = { viewModel.onEvent(ExerciseSearchEvent.ClearFilters) }
        )

        FilterPanel(
            state = state,
            onEvent = viewModel::onEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exercises, key = { it.id }) { exercise ->
                ExerciseItem(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) }
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Пошук вправи...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00FFFF)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Очистити", tint = Color.Gray)
                }
            } else {
                TextButton(onClick = onClearFilters) {
                    Text("Скинути", color = Color(0xFF00FFFF), fontSize = 12.sp)
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00FFFF),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun FilterPanel(
    state: ExerciseFilterState,
    onEvent: (ExerciseSearchEvent) -> Unit
) {
    val categories = ExerciseCategory.entries.filter { it != ExerciseCategory.UNKNOWN }
    val muscleGroups = listOf("CHEST", "BACK", "SHOULDERS", "QUADS", "HAMSTRINGS_GLUTES", "ARMS", "ABS", "LEGS", "CORE")
    val equipment = listOf("body only", "dumbbell", "barbell", "cable", "machine", "kettlebells", "bands")
    val levels = listOf("beginner", "intermediate", "expert")

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        FilterGroup(
            title = "Категорія", 
            options = categories.map { it.name }, 
            selectedOptions = state.selectedCategories.map { it.name }.toSet(),
            onToggle = { onEvent(ExerciseSearchEvent.ToggleCategory(ExerciseCategory.valueOf(it))) },
            labelMapper = { ExerciseCategory.valueOf(it).toUiText().asString() }
        )
        FilterGroup(
            title = "М'язи", 
            options = muscleGroups, 
            selectedOptions = state.selectedMuscles, 
            onToggle = { onEvent(ExerciseSearchEvent.ToggleMuscle(it)) },
            labelMapper = { it.toMuscleUiText().asString() }
        )
        FilterGroup(
            title = "Обладнання", 
            options = equipment, 
            selectedOptions = state.selectedEquipment, 
            onToggle = { onEvent(ExerciseSearchEvent.ToggleEquipment(it)) },
            labelMapper = { it.toEquipmentUiText().asString() }
        )
        FilterGroup(
            title = "Складність", 
            options = levels, 
            selectedOptions = state.selectedLevels, 
            onToggle = { onEvent(ExerciseSearchEvent.ToggleLevel(it)) },
            labelMapper = { it.toLevelUiText().asString() }
        )
    }
}

@Composable
fun FilterGroup(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onToggle: (String) -> Unit,
    labelMapper: @Composable (String) -> String = { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title, 
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF00FFFF).copy(alpha = 0.6f)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(options) { option ->
                val isSelected = selectedOptions.contains(option)
                Surface(
                    modifier = Modifier.clickable { onToggle(option) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF00FFFF) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(
                        1.dp, 
                        if (isSelected) Color(0xFF00FFFF) else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = labelMapper(option),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: ExerciseDetails,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name, 
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = exercise.category.toUiText().asString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00FFFF).copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${exercise.muscleGroups.joinToString { it.name.toMuscleUiText().asString(context) }} | ${exercise.equipment?.toEquipmentUiText()?.asString(context) ?: "Без обладнання"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            if (exercise.level != null) {
                Text(
                    text = "Складність: ${exercise.level?.toLevelUiText()?.asString(context)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}
