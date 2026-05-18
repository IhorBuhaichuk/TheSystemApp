package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.model.toActiveSetInput
import com.ihor.thesystem.domain.model.toExerciseSetOrNull
import com.ihor.thesystem.domain.usecase.GetExerciseReferenceUseCase
import com.ihor.thesystem.domain.usecase.GetSystemConfigUseCase
import com.ihor.thesystem.domain.usecase.WorkoutUseCases
import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel
import com.ihor.thesystem.presentation.common.model.toMatrixEntryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val useCases: WorkoutUseCases,
    private val getSystemConfig: GetSystemConfigUseCase,
    private val getExerciseReference: GetExerciseReferenceUseCase,
    private val dispatchers: DispatcherProvider,
    private val clock: AppClock
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
    ) { date, config, player ->
        useCases.resolveTrainingCycleDay(
            targetDate = date,
            config = config,
            fallbackCurrentCycleDay = player?.currentCycleDay
        )
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private val displayedWorkoutFlow: Flow<ActiveDayUiModel?> = displayedCycleDay
        .flatMapLatest { day ->
            useCases.getSchedulesForDays(listOf(day))
                .flatMapLatest { schedules ->
                    val schedule = schedules.firstOrNull() ?: return@flatMapLatest flowOf(null)
                    useCases.getActiveWorkoutQuest(schedule.id).map { activeWorkoutQuest ->
            
                        val exercisesWithRecs = schedule.exercises.map { ex ->
                            val trackingMode = resolveTrackingMode(ex)
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
                                recommendation = rec.formatRecommendation(trackingMode),
                                gifUrl = ex.gifUrl,
                                externalId = ex.externalId,
                                trackingMode = trackingMode,
                                sets = (1..(rec.sets ?: 1)).map {
                                    ActiveSetInput(
                                        weight = if (trackingMode.usesWeightInput && rec.weight > 0.0) {
                                            rec.weight.formatInputWeight()
                                        } else {
                                            ""
                                        }
                                    )
                                }.toImmutableList()
                            )
                        }.toImmutableList()

                        ActiveDayUiModel(
                            dayNumber = schedule.cycleDay,
                            dailyTasks = listOfNotNull(activeWorkoutQuest).toImmutableList(),
                            workoutName = schedule.workoutTemplateName,
                            exercises = exercisesWithRecs,
                            matrixEntries = persistentListOf()
                        )
                    }
                }
        }
        .catch { e ->
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to build active workout state")
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

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

    fun onSetCompletionChanged(exerciseId: Int, setId: Long, completed: Boolean) {
        updateSetEdit(exerciseId, setId) { it.copy(isCompleted = completed) }
    }

    fun onAddWorkoutSet(exerciseId: Int, weight: String) {
        val currentWorkout = activeWorkoutState.value ?: return
        val exercise = currentWorkout.exercises.find { it.exerciseId == exerciseId } ?: return
        val currentSets = _userEdits.value[exerciseId] ?: exercise.sets
        _userEdits.update {
            it + (exerciseId to (currentSets + ActiveSetInput(weight = weight)))
        }
    }

    fun onFinishWorkout() {
        val currentWorkout = activeWorkoutState.value ?: return
        val questId = currentWorkout.dailyTasks.firstOrNull()?.id?.toLong() ?: 0L
        
        viewModelScope.launch {
            val allSessionSets = currentWorkout.exercises.flatMap { exercise ->
                exercise.sets.filter { it.isCompleted }.mapNotNull { setInput ->
                    setInput.toExerciseSetOrNull(
                        exerciseId = exercise.exerciseId,
                        trackingMode = exercise.trackingMode
                    )?.copy(isCompleted = true)
                }
            }
            val weightedExerciseIds = currentWorkout.exercises
                .filter { it.trackingMode.usesWeightInput }
                .map { it.exerciseId }
                .toSet()

            if (allSessionSets.isEmpty()) {
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_no_completed_exercises)))
                return@launch
            }

            val session = WorkoutSession(
                questId = questId,
                timestamp = clock.now(),
                totalTonnage = allSessionSets
                    .filter { it.exerciseId in weightedExerciseIds }
                    .sumOf { it.weight * it.reps },
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

    fun onLogSetsConfirmed(
        exerciseId: Int,
        sets: List<ActiveSetInput>,
        feedback: String,
        trackingMode: ExerciseTrackingMode
    ) {
        viewModelScope.launch {
            try {
                useCases.selectedDate.value?.let { date ->
                    useCases.saveExerciseSets(
                        exerciseId = exerciseId,
                        sets = sets,
                        date = date,
                        userFeedback = feedback,
                        trackingMode = trackingMode
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
        val setupState = _dialogState.value as? StatusDialogState.SetupMatrix
        if (setupState?.entry?.usesExternalLoad == false) {
            if (setupState.showWorkoutAfter) {
                onOpenMainWorkout()
            } else {
                onDismissDialog()
            }
            return
        }
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

    fun refreshForCurrentDay() {
        useCases.selectToday()
        _selectedCycleDayOverride.value = null
        _userEdits.value = emptyMap()
        if (_dialogState.value is StatusDialogState.WorkoutScheduleSettings) {
            loadSettingsForDay(_settingsUiState.value.selectedDay)
        }
    }

    fun onActivateSelectedCycleDayToday() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val selectedDay = displayedCycleDay.first()
                useCases.syncCycleAnchor(selectedDay)
                _selectedCycleDayOverride.value = null
                _settingsUiState.update { it.copy(selectedDay = selectedDay) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to sync selected cycle day")
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
            }
        }
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
        launchIoAction {
            useCases.saveWorkoutForDay(
                cycleDay = state.selectedDay,
                workoutName = state.workoutNameDraft,
                exerciseIds = state.exercisesForSelectedDay.map { it.id }
            )
        }
    }

    fun onAddExerciseToDay(exerciseId: Int) {
        val state = _settingsUiState.value
        launchIoAction {
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
        launchIoAction {
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
        launchIoAction {
            useCases.removeExerciseFromDay(state.selectedDay, exerciseId)
        }
    }

    fun onCreateExercise(name: String) {
        val state = _settingsUiState.value
        launchIoAction {
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
        launchIoAction {
            useCases.deleteExercise(exerciseId)
            refreshAllExercises()
        }
    }

    fun onExerciseTrackingModeChanged(exerciseId: Int, trackingMode: ExerciseTrackingMode) {
        launchIoAction {
            useCases.updateExerciseTrackingMode(exerciseId, trackingMode.name)
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

    private fun launchIoAction(block: suspend () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                block()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Workout settings action failed")
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
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

    private suspend fun resolveTrackingMode(exercise: ExerciseDetails): ExerciseTrackingMode {
        val reference = runCatching {
            getExerciseReference(exercise.id)
        }.getOrNull()
        return ExerciseTrackingModeResolver.resolve(exercise, reference)
    }

    private suspend fun resolveTrackingMode(
        exerciseId: Int,
        trackingModeOverride: String?,
        name: String,
        nameUk: String?,
        category: ExerciseCategory,
        equipment: String?,
        externalId: String?
    ): ExerciseTrackingMode {
        val reference = runCatching {
            getExerciseReference(exerciseId)
        }.getOrNull()
        return ExerciseTrackingModeResolver.resolve(
            trackingModeOverride = trackingModeOverride,
            name = name,
            nameUk = nameUk,
            externalId = externalId,
            category = category,
            equipment = equipment,
            referenceWeightType = reference?.weightType
        )
    }

    private fun com.ihor.thesystem.domain.usecase.SetRecommendation.formatRecommendation(
        trackingMode: ExerciseTrackingMode
    ): String =
        when (trackingMode) {
            ExerciseTrackingMode.WEIGHT_REPS -> "${sets}x${reps} @ ${weight}kg"
            ExerciseTrackingMode.BODYWEIGHT_REPS -> "${sets}x${reps} повт."
            ExerciseTrackingMode.TIME_SECONDS -> "${sets}x${reps} сек"
            ExerciseTrackingMode.TIME_MINUTES -> "${sets}x${(reps / 60).coerceAtLeast(1)} хв"
        }

    private fun Double.formatInputWeight(): String =
        if (this % 1.0 == 0.0) {
            toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", this)
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
            val exercise = useCases.getAllExercises().first().firstOrNull { it.id == entry.exerciseId }
            val trackingMode = exercise?.let { resolveTrackingMode(it) }
                ?: resolveTrackingMode(
                    exerciseId = entry.exerciseId,
                    trackingModeOverride = null,
                    name = entry.exerciseName,
                    nameUk = null,
                    category = ExerciseCategory.UNKNOWN,
                    equipment = null,
                    externalId = null
                )
            val initialSets = if (lastSets.isNotEmpty()) {
                lastSets.map { it.toActiveSetInput(trackingMode) }
            } else {
                listOf(ActiveSetInput())
            }
            _currentLogSets.value = initialSets
            _dialogState.value = StatusDialogState.LogWorkoutSets(
                entry = entry,
                sets = initialSets,
                existingLogs = lastSets,
                trackingMode = trackingMode,
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
