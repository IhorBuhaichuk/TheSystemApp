package com.ihor.thesystem.feature.exercise_search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.usecase.SearchExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseFilterState(
    val query: String = "",
    val selectedMuscles: Set<String> = emptySet(),
    val selectedEquipment: Set<String> = emptySet(),
    val selectedLevels: Set<String> = emptySet(),
    val selectedMechanics: Set<String> = emptySet(),
    val selectedForces: Set<String> = emptySet(),
    val selectedCategories: Set<com.ihor.thesystem.domain.model.ExerciseCategory> = emptySet()
)

sealed interface ExerciseSearchEvent {
    data class UpdateQuery(val query: String) : ExerciseSearchEvent
    data class ToggleMuscle(val muscle: String) : ExerciseSearchEvent
    data class ToggleEquipment(val equipment: String) : ExerciseSearchEvent
    data class ToggleLevel(val level: String) : ExerciseSearchEvent
    data class ToggleMechanic(val mechanic: String) : ExerciseSearchEvent
    data class ToggleForce(val force: String) : ExerciseSearchEvent
    data class ToggleCategory(val category: com.ihor.thesystem.domain.model.ExerciseCategory) : ExerciseSearchEvent
    object ClearFilters : ExerciseSearchEvent
}

@HiltViewModel
class ExerciseSearchViewModel @Inject constructor(
    private val searchExercisesUseCase: SearchExercisesUseCase
) : ViewModel() {

    private val _filterState = MutableStateFlow(ExerciseFilterState())
    val filterState: StateFlow<ExerciseFilterState> = _filterState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: StateFlow<List<ExerciseDetails>> = _filterState
        .flatMapLatest { state ->
            searchExercisesUseCase(
                query = state.query.takeIf { it.isNotBlank() },
                muscles = state.selectedMuscles.toList(),
                equipment = state.selectedEquipment.toList(),
                levels = state.selectedLevels.toList(),
                mechanics = state.selectedMechanics.toList(),
                forces = state.selectedForces.toList()
            ).map { list ->
                if (state.selectedCategories.isEmpty()) list
                else list.filter { it.category in state.selectedCategories }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onEvent(event: ExerciseSearchEvent) {
        when (event) {
            is ExerciseSearchEvent.UpdateQuery -> {
                _filterState.update { it.copy(query = event.query) }
            }
            is ExerciseSearchEvent.ToggleMuscle -> {
                _filterState.update { state ->
                    val newSet = if (state.selectedMuscles.contains(event.muscle)) {
                        state.selectedMuscles - event.muscle
                    } else {
                        state.selectedMuscles + event.muscle
                    }
                    state.copy(selectedMuscles = newSet)
                }
            }
            is ExerciseSearchEvent.ToggleEquipment -> {
                _filterState.update { state ->
                    val newSet = if (state.selectedEquipment.contains(event.equipment)) {
                        state.selectedEquipment - event.equipment
                    } else {
                        state.selectedEquipment + event.equipment
                    }
                    state.copy(selectedEquipment = newSet)
                }
            }
            is ExerciseSearchEvent.ToggleLevel -> {
                _filterState.update { state ->
                    val newSet = if (state.selectedLevels.contains(event.level)) {
                        state.selectedLevels - event.level
                    } else {
                        state.selectedLevels + event.level
                    }
                    state.copy(selectedLevels = newSet)
                }
            }
            is ExerciseSearchEvent.ToggleMechanic -> {
                _filterState.update { state ->
                    val newSet = if (state.selectedMechanics.contains(event.mechanic)) {
                        state.selectedMechanics - event.mechanic
                    } else {
                        state.selectedMechanics + event.mechanic
                    }
                    state.copy(selectedMechanics = newSet)
                }
            }
            is ExerciseSearchEvent.ToggleForce -> {
                _filterState.update { state ->
                    val newSet = if (state.selectedForces.contains(event.force) ) {
                        state.selectedForces - event.force
                    } else {
                        state.selectedForces + event.force
                    }
                    state.copy(selectedForces = newSet)
                }
            }
            is ExerciseSearchEvent.ToggleCategory -> {
                _filterState.update { state ->
                    val newSet = if (state.selectedCategories.contains(event.category)) {
                        state.selectedCategories - event.category
                    } else {
                        state.selectedCategories + event.category
                    }
                    state.copy(selectedCategories = newSet)
                }
            }
            ExerciseSearchEvent.ClearFilters -> {
                _filterState.value = ExerciseFilterState()
            }
        }
    }
}
