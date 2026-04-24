package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.usecase.GetSystemConfigUseCase
import com.ihor.thesystem.domain.usecase.WorkoutUseCases
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class SetSavePayload(
    val exerciseId: Int,
    val setId: Long,
    val weight: String,
    val reps: String
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val useCases: WorkoutUseCases,
    getSystemConfig: GetSystemConfigUseCase
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _dialogState = MutableStateFlow<StatusDialogState>(StatusDialogState.None)
    val dialogState: StateFlow<StatusDialogState> = _dialogState.asStateFlow()

    private val _currentLogSets = MutableStateFlow<List<ActiveSetInput>>(emptyList())
    private val _userEdits = MutableStateFlow<Map<Int, List<ActiveSetInput>>>(emptyMap())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val _saveEvents = MutableSharedFlow<SetSavePayload>(replay = 0)

    private val cycleDay: Flow<Int> = combine(
        useCases.viewingDateRepo.selectedDate,
        getSystemConfig().filterNotNull(),
        useCases.getPlayerFlow()
    ) { date, config, _ ->
        if (config.cycleAnchorDateTimestamp > 0) {
            useCases.calculateCycleDay(
                targetDate = date,
                anchorEpochDay = Instant.ofEpochMilli(config.cycleAnchorDateTimestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay(),
                anchorCycleDay = config.cycleAnchorDay,
                cycleDaysPerMicrocycle = config.cycleDaysPerMicrocycle
            )
        } else {
            1
        }
    }.distinctUntilChanged()
    .onEach { _userEdits.value = emptyMap() }

    private val activeWorkoutFlow: Flow<ActiveDayUiModel?> = cycleDay
        .flatMapLatest { day ->
            useCases.scheduleRepo.getSchedulesForDays(listOf(day))
        }
        .map { schedules ->
            val schedule = schedules.firstOrNull() ?: return@map null
            
            val exercisesWithRecs = schedule.exercises.map { ex ->
                val rec = useCases.calculateRecommendation(ex.id, ex.name)
                ExerciseWorkoutUiModel(
                    exerciseId = ex.id,
                    name = ex.name,
                    recommendedWeight = rec.weight,
                    recommendedReps = rec.reps,
                    recommendedSets = rec.sets,
                    recommendation = "${rec.sets}x${rec.reps} @ ${rec.weight}kg",
                    sets = (1..(rec.sets ?: 1)).map { ActiveSetInput() }.toImmutableList()
                )
            }.toImmutableList()

            ActiveDayUiModel(
                dayNumber = schedule.cycleDay,
                dailyTasks = persistentListOf(),
                workoutName = schedule.workoutTemplateName,
                exercises = exercisesWithRecs,
                matrixEntries = persistentListOf()
            )
        }

    private val statsFlow = useCases.getStatisticsData()

    val activeWorkoutState: StateFlow<ActiveDayUiModel?> = combine(
        activeWorkoutFlow,
        statsFlow,
        _userEdits
    ) { workout, stats, edits ->
        workout?.copy(
            exercises = workout.exercises.map { ex ->
                edits[ex.exerciseId]?.let { editedSets ->
                    ex.copy(sets = editedSets.toImmutableList())
                } ?: ex
            }.toImmutableList(),
            matrixEntries = stats.matrixEntries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            _saveEvents
                .debounce(1500L)
                .collectLatest { payload ->
                    performAutoSave(payload)
                }
        }
    }

    private suspend fun performAutoSave(payload: SetSavePayload) {
        try {
            useCases.saveExerciseSets(
                exerciseId = payload.exerciseId,
                sets = listOf(
                    ActiveSetInput(
                        id = payload.setId,
                        weight = payload.weight,
                        reps = payload.reps
                    )
                ),
                date = useCases.viewingDateRepo.selectedDate.value,
                userFeedback = ""
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
        }
    }

    fun onSetWeightChanged(exerciseId: Int, setId: Long, weight: String) {
        updateSetEdit(exerciseId, setId) { it.copy(weight = weight) }
    }

    fun onSetRepsChanged(exerciseId: Int, setId: Long, reps: String) {
        updateSetEdit(exerciseId, setId) { it.copy(reps = reps) }
    }

    private fun updateSetEdit(exerciseId: Int, setId: Long, transform: (ActiveSetInput) -> ActiveSetInput) {
        val currentWorkout = activeWorkoutState.value ?: return
        val exercise = currentWorkout.exercises.find { it.exerciseId == exerciseId } ?: return
        val currentSets = _userEdits.value[exerciseId] ?: exercise.sets
        
        val newSets = currentSets.map { 
            if (it.id == setId) transform(it) else it 
        }
        _userEdits.update { it + (exerciseId to newSets) }
    }

    fun onSetFocusLost(exerciseId: Int, setId: Long) {
        viewModelScope.launch {
            val state = activeWorkoutState.value ?: return@launch
            val exercise = state.exercises.find { it.exerciseId == exerciseId } ?: return@launch
            val set = exercise.sets.find { it.id == setId } ?: return@launch
            _saveEvents.emit(SetSavePayload(exerciseId, setId, set.weight, set.reps))
        }
    }

    fun onSetCompleted(exerciseId: Int, setId: Long) {
        updateSetEdit(exerciseId, setId) { it.copy(isCompleted = !it.isCompleted) }
        viewModelScope.launch {
            val state = activeWorkoutState.value ?: return@launch
            val exercise = state.exercises.find { it.exerciseId == exerciseId } ?: return@launch
            val set = exercise.sets.find { it.id == setId } ?: return@launch
            _saveEvents.emit(SetSavePayload(exerciseId, setId, set.weight, set.reps))
        }
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<ActiveSetInput>, feedback: String) {
        viewModelScope.launch {
            try {
                useCases.saveExerciseSets(
                    exerciseId = exerciseId,
                    sets = sets,
                    date = useCases.viewingDateRepo.selectedDate.value,
                    userFeedback = feedback
                )
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
            }
        }
    }

    fun onConfirmSetup(exerciseId: Int, start: String, target: String) {
        viewModelScope.launch {
            try {
                useCases.updateMatrixGoals(
                    exerciseId = exerciseId,
                    startWeight = start.toFloatOrNull() ?: 0f,
                    targetWeight = target.toFloatOrNull() ?: 0f
                )
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
            }
        }
    }

    fun onOpenMainWorkout() {
        _dialogState.value = StatusDialogState.MainQuestWorkout
    }

    fun onOpenSetup(entry: MatrixEntryUiModel, fromWorkout: Boolean = false) {
        _dialogState.value = StatusDialogState.SetupMatrix(
            entry = entry,
            startWeight = entry.startWeight.toString(),
            targetWeight = entry.targetWeight.toString(),
            showWorkoutAfter = fromWorkout
        )
    }

    fun onOpenLogSets(entry: MatrixEntryUiModel, fromWorkout: Boolean = false) {
        viewModelScope.launch {
            val lastSets = useCases.analyticsRepo.getLastSetsForExercise(entry.exerciseId)
            val initialSets = if (lastSets.isNotEmpty()) {
                lastSets.map { ActiveSetInput(weight = it.weight.toString(), reps = it.reps.toString()) }
            } else {
                listOf(ActiveSetInput())
            }
            _currentLogSets.value = initialSets
            _dialogState.value = StatusDialogState.LogWorkoutSets(
                entry = entry,
                sets = initialSets,
                existingLogs = lastSets,
                showWorkoutAfter = fromWorkout
            )
        }
    }

    fun updateLogSetInput(id: Long, weight: String, reps: String) {
        _currentLogSets.update { list ->
            list.map { if (it.id == id) it.copy(weight = weight, reps = reps) else it }
        }
        updateLogDialogState()
    }

    fun addLogSet() {
        _currentLogSets.update { it + ActiveSetInput() }
        updateLogDialogState()
    }

    fun removeLogSet() {
        _currentLogSets.update { if (it.size > 1) it.dropLast(1) else it }
        updateLogDialogState()
    }

    private fun updateLogDialogState() {
        val current = _dialogState.value
        if (current is StatusDialogState.LogWorkoutSets) {
            _dialogState.value = current.copy(sets = _currentLogSets.value)
        }
    }

    fun onDismissDialog() {
        _dialogState.value = StatusDialogState.None
    }
}
