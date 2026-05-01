package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.usecase.GetSystemConfigUseCase
import com.ihor.thesystem.domain.usecase.WorkoutUseCases
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import com.ihor.thesystem.feature.statistics.viewmodel.toMatrixEntryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val useCases: WorkoutUseCases,
    private val getSystemConfig: GetSystemConfigUseCase
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _dialogState = MutableStateFlow<StatusDialogState>(StatusDialogState.None)
    val dialogState: StateFlow<StatusDialogState> = _dialogState.asStateFlow()

    private val _settingsUiState = MutableStateFlow(WorkoutScheduleSettingsUiState())
    val settingsUiState: StateFlow<WorkoutScheduleSettingsUiState> = _settingsUiState.asStateFlow()

    private var settingsDayJob: Job? = null

    private val _currentLogSets = MutableStateFlow<List<ActiveSetInput>>(emptyList())
    private val _userEdits = MutableStateFlow<Map<Int, List<ActiveSetInput>>>(emptyMap())
    private val _selectedCycleDayOverride = MutableStateFlow<Int?>(null)

    private val cycleDay: Flow<Int> = combine(
        useCases.selectedDate.filterNotNull(),
        getSystemConfig().filterNotNull(),
        useCases.getPlayerFlow()
    ) { date, config, _ ->
        if (config.cycleAnchorDateTimestamp > 0) {
            useCases.calculateCycleDay(
                targetDate = date,
                anchorEpochDay = config.cycleAnchorDateTimestamp,
                anchorCycleDay = config.cycleAnchorDay,
                cycleDaysPerMicrocycle = config.cycleDaysPerMicrocycle
            )
        } else {
            1
        }
    }.distinctUntilChanged()

    private val displayedCycleDay: Flow<Int> = combine(
        cycleDay,
        _selectedCycleDayOverride
    ) { activeDay, selectedDay ->
        selectedDay ?: activeDay
    }
        .distinctUntilChanged()
        .onEach { selectedDay ->
            _userEdits.value = emptyMap()
            _settingsUiState.update { it.copy(selectedDay = selectedDay) }
        }

    val cycleDaysState: StateFlow<List<CycleDayUiModel>> = combine(
        getSystemConfig().filterNotNull(),
        cycleDay,
        displayedCycleDay
    ) { config, activeDay, selectedDay ->
        Triple(config.cycleDaysPerMicrocycle.coerceAtLeast(1), activeDay, selectedDay)
    }.flatMapLatest { (totalDays, activeDay, selectedDay) ->
        useCases.getSchedulesForDays((1..totalDays).toList()).map { schedules ->
            val schedulesByDay = schedules.associateBy { it.cycleDay }
            (1..totalDays).map { day ->
                val schedule = schedulesByDay[day]
                val isWorkoutDay = schedule?.isWorkoutDay == true &&
                    (schedule.workoutTemplateName != null || schedule.exercises.isNotEmpty())
                CycleDayUiModel(
                    dayNumber = day,
                    type = if (isWorkoutDay) DayType.WORKOUT else DayType.REST,
                    isActive = day == activeDay,
                    isSelected = day == selectedDay,
                    workoutName = schedule?.workoutTemplateName
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val displayedWorkoutFlow: Flow<ActiveDayUiModel?> = displayedCycleDay
        .flatMapLatest { day ->
            useCases.getSchedulesForDays(listOf(day))
        }
        .map { schedules ->
            val schedule = schedules.firstOrNull() ?: return@map null
            
            val exercisesWithRecs = schedule.exercises.map { ex ->
                val rec = try {
                    useCases.calculateRecommendation(ex.id, ex.name)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    com.ihor.thesystem.domain.usecase.SetRecommendation(weight = 0.0, reps = 12, sets = 3, isProgression = false)
                }
                ExerciseWorkoutUiModel(
                    exerciseId = ex.id,
                    name = ex.name,
                    nameUk = ex.nameUk,
                    recommendedWeight = rec.weight,
                    recommendedReps = rec.reps,
                    recommendedSets = rec.sets,
                    recommendation = "${rec.sets}x${rec.reps} @ ${rec.weight}kg",
                    gifUrl = ex.gifUrl,
                    externalId = ex.externalId,
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
        .catch { e ->
            e.printStackTrace()
            emit(null)
        }

    private val statsFlow = useCases.getStatisticsData()

    val activeWorkoutState: StateFlow<ActiveDayUiModel?> = combine(
        displayedWorkoutFlow,
        statsFlow,
        _userEdits
    ) { workout, stats, edits ->
        workout?.copy(
            exercises = workout.exercises.map { ex ->
                edits[ex.exerciseId]?.let { editedSets ->
                    ex.copy(sets = editedSets.toImmutableList())
                } ?: ex
            }.toImmutableList(),
            matrixEntries = stats.matrixEntries.map { it.toMatrixEntryUiModel() }.toImmutableList()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
        // Sets are persisted when the workout is finished. This keeps workout completion
        // as the single source of truth for logs, XP, statistics, and analysis.
    }

    fun onSetCompleted(exerciseId: Int, setId: Long) {
        updateSetEdit(exerciseId, setId) { it.copy(isCompleted = !it.isCompleted) }
    }

    fun onFinishWorkout() {
        val currentWorkout = activeWorkoutState.value ?: return
        val questId = currentWorkout.dailyTasks.firstOrNull()?.id?.toLong() ?: 0L
        
        viewModelScope.launch {
            val allSessionSets = currentWorkout.exercises.flatMap { exercise ->
                exercise.sets.filter { it.isCompleted }.map { setInput ->
                    ExerciseSet(
                        sessionId = 0L,
                        exerciseId = exercise.exerciseId,
                        weight = setInput.weight.toDoubleOrNull() ?: 0.0,
                        reps = setInput.reps.toIntOrNull() ?: 0,
                        isCompleted = true
                    )
                }
            }

            if (allSessionSets.isEmpty()) {
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_no_completed_exercises)))
                return@launch
            }

            val session = WorkoutSession(
                questId = questId,
                timestamp = System.currentTimeMillis(),
                totalTonnage = allSessionSets.sumOf { it.weight * it.reps },
                cycleDay = currentWorkout.dayNumber
            )

            _dialogState.value = StatusDialogState.None // Close workout dialog show loading?
            
            when (val result = useCases.finalizeSession(session, allSessionSets)) {
                is Result.Success -> {
                    _dialogState.value = StatusDialogState.WorkoutReport(result.data)
                }
                is Result.Error -> {
                    _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
                }
            }
        }
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<ActiveSetInput>, feedback: String) {
        viewModelScope.launch {
            try {
                useCases.selectedDate.value?.let { date ->
                    useCases.saveExerciseSets(
                        exerciseId = exerciseId,
                        sets = sets,
                        date = date,
                        userFeedback = feedback
                    )
                }
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
                // Замінюємо кому на крапку для безпечного парсингу
                val startParsed = start.replace(",", ".").toFloatOrNull() ?: 0f
                val targetParsed = target.replace(",", ".").toFloatOrNull() ?: 0f

                useCases.updateMatrixGoals(
                    exerciseId = exerciseId,
                    startWeight = startParsed,
                    targetWeight = targetParsed
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

    fun onCycleDaySelected(day: Int) {
        _selectedCycleDayOverride.value = day
        _settingsUiState.update { it.copy(selectedDay = day) }
    }

    fun onOpenWorkoutSettings() {
        viewModelScope.launch {
            val selectedDay = displayedCycleDay.first()
            val config = getSystemConfig().filterNotNull().first()
            _settingsUiState.update {
                it.copy(
                    totalCycleDays = config.cycleDaysPerMicrocycle,
                    selectedDay = selectedDay
                )
            }
            _dialogState.value = StatusDialogState.WorkoutScheduleSettings
            loadSettingsForDay(selectedDay)
        }
    }

    fun onSettingsSelectDay(day: Int) {
        loadSettingsForDay(day)
    }

    fun onWorkoutNameChange(name: String) {
        _settingsUiState.update { it.copy(workoutNameDraft = name) }
    }

    fun onSaveWorkoutName() {
        val state = _settingsUiState.value
        viewModelScope.launch(Dispatchers.IO) {
            useCases.saveWorkoutForDay(
                cycleDay = state.selectedDay,
                workoutName = state.workoutNameDraft,
                exerciseIds = state.exercisesForSelectedDay.map { it.id }
            )
        }
    }

    fun onAddExerciseToDay(exerciseId: Int) {
        val state = _settingsUiState.value
        viewModelScope.launch(Dispatchers.IO) {
            val schedule = useCases.getSchedulesForDays(listOf(state.selectedDay)).first().firstOrNull()
            val currentIds = schedule?.exercises?.map { it.id }?.toMutableList()
                ?: state.exercisesForSelectedDay.map { it.id }.toMutableList()
            if (!currentIds.contains(exerciseId)) {
                currentIds.add(exerciseId)
                useCases.saveWorkoutForDay(
                    cycleDay = state.selectedDay,
                    workoutName = state.workoutNameDraft.ifBlank {
                        schedule?.workoutTemplateName ?: "День ${state.selectedDay}"
                    },
                    exerciseIds = currentIds
                )
            }
        }
    }

    fun onAddExerciseToDay(exerciseId: Int, cycleDay: Int) {
        val state = _settingsUiState.value
        viewModelScope.launch(Dispatchers.IO) {
            val schedule = useCases.getSchedulesForDays(listOf(cycleDay)).first().firstOrNull()
            val currentIds = schedule?.exercises?.map { it.id }?.toMutableList()
                ?: if (state.selectedDay == cycleDay) {
                    state.exercisesForSelectedDay.map { it.id }.toMutableList()
                } else {
                    mutableListOf()
                }
            if (!currentIds.contains(exerciseId)) {
                currentIds.add(exerciseId)
                useCases.saveWorkoutForDay(
                    cycleDay = cycleDay,
                    workoutName = state.workoutNameDraft.ifBlank {
                        schedule?.workoutTemplateName ?: "День $cycleDay"
                    },
                    exerciseIds = currentIds
                )
            }
        }
    }

    fun onRemoveExerciseFromDay(exerciseId: Int) {
        val state = _settingsUiState.value
        viewModelScope.launch(Dispatchers.IO) {
            useCases.removeExerciseFromDay(state.selectedDay, exerciseId)
        }
    }

    fun onCreateExercise(name: String) {
        val state = _settingsUiState.value
        viewModelScope.launch(Dispatchers.IO) {
            val newExerciseId = useCases.createExercise(name)
            refreshAllExercises()
            val schedule = useCases.getSchedulesForDays(listOf(state.selectedDay)).first().firstOrNull()
            val currentIds = schedule?.exercises?.map { it.id }?.toMutableList()
                ?: state.exercisesForSelectedDay.map { it.id }.toMutableList()
            currentIds.add(newExerciseId)
            useCases.saveWorkoutForDay(
                cycleDay = state.selectedDay,
                workoutName = state.workoutNameDraft.ifBlank {
                    schedule?.workoutTemplateName ?: "День ${state.selectedDay}"
                },
                exerciseIds = currentIds
            )
        }
    }

    fun onDeleteExercise(exerciseId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            useCases.deleteExercise(exerciseId)
            refreshAllExercises()
        }
    }

    private fun loadSettingsForDay(day: Int) {
        settingsDayJob?.cancel()
        settingsDayJob = viewModelScope.launch {
            _settingsUiState.update { it.copy(selectedDay = day, isLoading = true) }
            
            if (_settingsUiState.value.allExercises.isEmpty()) {
                refreshAllExercises()
            }

            useCases.getSchedulesForDays(listOf(day)).collectLatest { schedules ->
                val schedule = schedules.firstOrNull()
                _settingsUiState.update {
                    it.copy(
                        workoutNameDraft = schedule?.workoutTemplateName ?: "",
                        exercisesForSelectedDay = schedule?.exercises?.toImmutableList()
                            ?: persistentListOf(),
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun refreshAllExercises() {
        useCases.getAllExercises().first().let { exercises ->
            _settingsUiState.update { current ->
                current.copy(allExercises = exercises.toImmutableList())
            }
        }
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
            val lastSets = useCases.getLastSetsForExercise(entry.exerciseId)
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
