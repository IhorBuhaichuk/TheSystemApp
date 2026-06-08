package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.EquipmentType
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.model.HealthPermissionRequest
import com.ihor.thesystem.domain.model.QuestTask
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.model.toActiveSetInput
import com.ihor.thesystem.domain.model.toExerciseSetOrNull
import com.ihor.thesystem.domain.usecase.GetExerciseReferenceUseCase
import com.ihor.thesystem.domain.usecase.GetBackupStatusUseCase
import com.ihor.thesystem.domain.usecase.GetSystemConfigUseCase
import com.ihor.thesystem.domain.usecase.ExportBackupUseCase
import com.ihor.thesystem.domain.usecase.ImportBackupUseCase
import com.ihor.thesystem.domain.usecase.SetRecommendation
import com.ihor.thesystem.domain.usecase.WorkoutUseCases
import com.ihor.thesystem.domain.repository.HealthSignalsRepository
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import com.ihor.thesystem.domain.model.BackupPayload

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val useCases: WorkoutUseCases,
    private val getSystemConfig: GetSystemConfigUseCase,
    private val getExerciseReference: GetExerciseReferenceUseCase,
    private val healthSignalsRepository: HealthSignalsRepository,
    private val exportBackup: ExportBackupUseCase,
    private val importBackup: ImportBackupUseCase,
    private val getBackupStatus: GetBackupStatusUseCase,
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
    private val backupJson = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val _currentLogSets = MutableStateFlow<List<ActiveSetInput>>(emptyList())
    private val _userEdits = MutableStateFlow<Map<Int, List<ActiveSetInput>>>(emptyMap())
    private val _selectedCycleDayOverride = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            useCases.getEquipmentProfile().collectLatest { profile ->
                _settingsUiState.update {
                    it.copy(
                        equipmentProfile = profile,
                        dumbbellMaxKgDraft = profile.dumbbellMaxKg?.formatEquipmentNumber().orEmpty()
                    )
                }
            }
        }
        refreshHealthConnectStatus()
        refreshBackupStatus()
    }

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
                        val taskByExercise = activeWorkoutQuest?.tasks.orEmpty()
                            .mapNotNull { task -> task.exerciseId?.let { exerciseId -> exerciseId to task } }
                            .toMap()
                        val scheduleExercisesById = schedule.exercises.associateBy { it.id }
                        val missingTaskExerciseIds = taskByExercise.keys - scheduleExercisesById.keys
                        val allExercisesById = if (missingTaskExerciseIds.isNotEmpty()) {
                            useCases.getAllExercises().first().associateBy { it.id }
                        } else {
                            emptyMap()
                        }
                        val displayedExercises = activeWorkoutQuest?.tasks
                            ?.takeIf { it.isNotEmpty() }
                            ?.map { task ->
                                task.exerciseId
                                    ?.let { exerciseId -> scheduleExercisesById[exerciseId] ?: allExercisesById[exerciseId] }
                                    ?: task.toSyntheticExerciseDetails()
                            }
                            ?: schedule.exercises
                        val todayDecision = runCatching { useCases.decideTodayWorkout() }.getOrNull()

                        val exercisesWithRecs = displayedExercises.map { ex ->
                            val trackingMode = resolveTrackingMode(ex)
                            val fallbackRec = try {
                                useCases.calculateRecommendation(ex.id, ex.name)
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                SetRecommendation(
                                    weight = 0.0,
                                    reps = 12,
                                    sets = 3,
                                    isProgression = false,
                                    exerciseId = ex.id
                                )
                            }
                            val questTarget = taskByExercise[ex.id]
                            val targetWeight = questTarget?.recommendedWeight ?: fallbackRec.weight
                            val targetReps = questTarget?.recommendedReps ?: fallbackRec.reps
                            val targetSets = questTarget?.recommendedSets ?: fallbackRec.sets
                            val emphasizeTechniqueCheck = ex.isCoreSystemExercise &&
                                (
                                    todayDecision?.readinessScore?.let { it < 65 } == true ||
                                        (trackingMode.usesWeightInput && targetWeight > 0.0)
                                    )

                            ExerciseWorkoutUiModel(
                                exerciseId = ex.id,
                                name = ex.name,
                                nameUk = ex.nameUk,
                                recommendedWeight = targetWeight,
                                recommendedReps = targetReps,
                                recommendedSets = targetSets,
                                recommendation = formatRecommendation(
                                    weight = targetWeight,
                                    reps = targetReps,
                                    sets = targetSets,
                                    trackingMode = trackingMode
                                ),
                                gifUrl = ex.gifUrl,
                                externalId = ex.externalId,
                                trackingMode = trackingMode,
                                isCoreSystemExercise = ex.isCoreSystemExercise,
                                movementPattern = ex.movementPattern,
                                techniqueTips = ex.techniqueTips.toImmutableList(),
                                commonMistakes = ex.commonMistakes.toImmutableList(),
                                substitutionExternalIds = ex.substitutionExternalIds.toImmutableList(),
                                techniqueCheckEmphasized = emphasizeTechniqueCheck,
                                sets = (1..targetSets.coerceAtLeast(1)).map {
                                    ActiveSetInput(
                                        weight = if (trackingMode.usesWeightInput && targetWeight > 0.0) {
                                            targetWeight.formatInputWeight()
                                        } else {
                                            ""
                                        },
                                        reps = if (ex.id < 0) trackingMode.formatTargetInput(targetReps) else ""
                                    )
                                }.toImmutableList()
                            )
                        }.toImmutableList()

                        ActiveDayUiModel(
                            dayNumber = schedule.cycleDay,
                            dailyTasks = listOfNotNull(activeWorkoutQuest).toImmutableList(),
                            workoutName = schedule.workoutTemplateName,
                            exercises = exercisesWithRecs,
                            adjustmentReason = if (activeWorkoutQuest != null) {
                                todayDecision?.toWorkoutAdjustmentReason()
                            } else {
                                null
                            },
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
                exercise.sets.mapNotNull { setInput ->
                    setInput.toExerciseSetOrNull(
                        exerciseId = exercise.exerciseId,
                        trackingMode = exercise.trackingMode
                    )?.copy(isCompleted = setInput.isCompleted)
                }
            }
            val completedSessionSets = allSessionSets.filter { it.isCompleted }
            val plannedRecommendations = currentWorkout.exercises.map { exercise ->
                SetRecommendation(
                    weight = exercise.recommendedWeight ?: 0.0,
                    reps = exercise.recommendedReps ?: 10,
                    sets = exercise.recommendedSets ?: exercise.sets.size.coerceAtLeast(1),
                    isProgression = false,
                    exerciseId = exercise.exerciseId
                )
            }
            val weightedExerciseIds = currentWorkout.exercises
                .filter { it.trackingMode.usesWeightInput }
                .map { it.exerciseId }
                .toSet()

            if (completedSessionSets.isEmpty()) {
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_no_completed_exercises)))
                return@launch
            }

            val session = WorkoutSession(
                questId = questId,
                timestamp = clock.now(),
                totalTonnage = completedSessionSets
                    .filter { it.exerciseId in weightedExerciseIds }
                    .sumOf { it.weight * it.reps },
                cycleDay = currentWorkout.dayNumber
            )

            _dialogState.value = StatusDialogState.None // Close workout dialog show loading?
            
            when (val result = useCases.finalizeSession(session, allSessionSets, plannedRecommendations)) {
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
            refreshHealthConnectStatus()
            refreshBackupStatus()
            loadSettingsForDay(selectedDay)
        }
    }

    fun healthConnectPermissionRequest(): HealthPermissionRequest =
        healthSignalsRepository.requestPermissions()

    fun onHealthConnectPermissionsChanged() {
        refreshHealthConnectStatus()
    }

    private fun refreshHealthConnectStatus() {
        _settingsUiState.update { it.copy(healthConnect = it.healthConnect.copy(isLoading = true)) }
        viewModelScope.launch(dispatchers.io) {
            val status = runCatching {
                val available = healthSignalsRepository.isAvailable()
                val hasReadinessPermission = if (available) {
                    healthSignalsRepository.hasPermissions()
                } else {
                    false
                }
                HealthConnectUiState(
                    isAvailable = available,
                    hasReadinessPermission = hasReadinessPermission,
                    isLoading = false
                )
            }.getOrElse {
                HealthConnectUiState(isLoading = false)
            }
            _settingsUiState.update { it.copy(healthConnect = status) }
        }
    }

    fun exportBackupJson(onReady: (String) -> Unit) {
        _settingsUiState.update { it.copy(backup = it.backup.copy(isBusy = true)) }
        viewModelScope.launch {
            try {
                val json = kotlinx.coroutines.withContext(dispatchers.io) {
                    backupJson.encodeToString(exportBackup())
                }
                onReady(json)
                refreshBackupStatus()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _settingsUiState.update { it.copy(backup = it.backup.copy(isBusy = false)) }
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
            }
        }
    }

    fun importBackupJson(rawJson: String) {
        _settingsUiState.update { it.copy(backup = it.backup.copy(isBusy = true)) }
        viewModelScope.launch {
            try {
                val payload = backupJson.decodeFromString<BackupPayload>(rawJson)
                kotlinx.coroutines.withContext(dispatchers.io) {
                    importBackup(payload)
                }
                refreshBackupStatus()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _settingsUiState.update { it.copy(backup = it.backup.copy(isBusy = false)) }
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
            }
        }
    }

    fun onBackupFileOperationFailed() {
        viewModelScope.launch {
            _settingsUiState.update { it.copy(backup = it.backup.copy(isBusy = false)) }
            _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
        }
    }

    private fun refreshBackupStatus() {
        viewModelScope.launch(dispatchers.io) {
            val status = runCatching { getBackupStatus() }.getOrNull()
            _settingsUiState.update {
                it.copy(
                    backup = it.backup.copy(
                        lastExportedAtMillis = status?.lastExportedAtMillis,
                        lastImportedAtMillis = status?.lastImportedAtMillis,
                        isBusy = false
                    )
                )
            }
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

    fun onEquipmentLocationChanged(trainsAtGym: Boolean) {
        val current = _settingsUiState.value.equipmentProfile
        val updated = if (trainsAtGym) {
            current.copy(
                trainsAtGym = true,
                availableEquipment = current.availableEquipment + GYM_DEFAULT_EQUIPMENT,
                barbellAvailable = true,
                benchAvailable = true,
                pullUpBarAvailable = true,
                dipBarsAvailable = true,
                bandsAvailable = true,
                machinesAvailable = true
            )
        } else {
            current.copy(trainsAtGym = false)
        }
        saveEquipmentProfile(updated)
    }

    fun onEquipmentTypeToggled(type: EquipmentType) {
        val current = _settingsUiState.value.equipmentProfile
        val updatedEquipment = if (type in current.availableEquipment) {
            current.availableEquipment - type
        } else {
            current.availableEquipment + type
        }
        saveEquipmentProfile(current.copy(availableEquipment = updatedEquipment + EquipmentType.BODY_ONLY))
    }

    fun onEquipmentAvailabilityChanged(type: EquipmentType, available: Boolean) {
        val current = _settingsUiState.value.equipmentProfile
        val updated = when (type) {
            EquipmentType.BARBELL -> current.copy(barbellAvailable = available)
            EquipmentType.BENCH -> current.copy(benchAvailable = available)
            EquipmentType.PULL_UP_BAR -> current.copy(pullUpBarAvailable = available)
            EquipmentType.DIP_BARS -> current.copy(dipBarsAvailable = available)
            EquipmentType.BANDS -> current.copy(bandsAvailable = available)
            EquipmentType.MACHINE,
            EquipmentType.CABLE -> current.copy(machinesAvailable = available)
            else -> {
                val updatedEquipment = if (available) {
                    current.availableEquipment + type
                } else {
                    current.availableEquipment - type
                }
                current.copy(availableEquipment = updatedEquipment + EquipmentType.BODY_ONLY)
            }
        }
        saveEquipmentProfile(updated)
    }

    fun onDumbbellMaxKgChanged(value: String) {
        _settingsUiState.update { it.copy(dumbbellMaxKgDraft = value) }
        val parsed = value.replace(",", ".").toFloatOrNull()
        if (value.isBlank() || parsed != null) {
            val current = _settingsUiState.value.equipmentProfile
            saveEquipmentProfile(
                current.copy(
                    dumbbellMaxKg = parsed?.takeIf { it > 0f },
                    availableEquipment = if (parsed != null && parsed > 0f) {
                        current.availableEquipment + EquipmentType.DUMBBELL + EquipmentType.BODY_ONLY
                    } else {
                        current.availableEquipment
                    }
                )
            )
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

    private fun saveEquipmentProfile(profile: EquipmentProfile) {
        _settingsUiState.update {
            it.copy(
                equipmentProfile = profile,
                dumbbellMaxKgDraft = profile.dumbbellMaxKg?.formatEquipmentNumber().orEmpty()
            )
        }
        launchIoAction {
            useCases.saveEquipmentProfile(profile)
        }
    }

    private suspend fun resolveTrackingMode(exercise: ExerciseDetails): ExerciseTrackingMode {
        val reference = runCatching {
            getExerciseReference(exercise.id)
        }.getOrNull()
        return ExerciseTrackingModeResolver.resolve(exercise, reference)
    }

    private fun QuestTask.toSyntheticExerciseDetails(): ExerciseDetails =
        ExerciseDetails(
            id = exerciseId ?: -id.coerceAtLeast(1),
            name = name,
            nameUk = nameUk,
            category = ExerciseCategory.FLEXIBILITY,
            equipment = SYSTEM_BODYWEIGHT_EQUIPMENT,
            trackingMode = inferSystemTrackingMode(name)
        )

    private fun inferSystemTrackingMode(name: String): String {
        val normalized = name.lowercase()
        return when {
            normalized.contains("walking") || normalized.contains("walk") ->
                ExerciseTrackingMode.TIME_MINUTES.name
            normalized.contains("hold") ||
                normalized.contains("plank") ||
                normalized.contains("mobility") ||
                normalized.contains("stretch") ->
                ExerciseTrackingMode.TIME_SECONDS.name
            else -> ExerciseTrackingMode.BODYWEIGHT_REPS.name
        }
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

    private fun formatRecommendation(
        weight: Double,
        reps: Int,
        sets: Int,
        trackingMode: ExerciseTrackingMode
    ): String =
        when (trackingMode) {
            ExerciseTrackingMode.WEIGHT_REPS -> "${sets}x${reps} @ ${weight}kg"
            ExerciseTrackingMode.BODYWEIGHT_REPS -> "${sets}x${reps} повт."
            ExerciseTrackingMode.TIME_SECONDS -> "${sets}x${reps} сек"
            ExerciseTrackingMode.TIME_MINUTES -> "${sets}x${(reps / 60).coerceAtLeast(1)} хв"
        }

    private fun TodayTrainingDecision.toWorkoutAdjustmentReason(): String? =
        when (decisionType) {
            TodayTrainingDecisionType.REDUCED_LOAD,
            TodayTrainingDecisionType.ACTIVE_RECOVERY,
            TodayTrainingDecisionType.DELOAD ->
                "Система знизила навантаження через readiness $readinessScore% і recovery debt ${recoveryDebt.level.name}."
            TodayTrainingDecisionType.NO_EXCUSE ->
                if (reason.contains("missed", ignoreCase = true)) {
                    "Система зафіксувала пропуск. План перераховано. Наступна оптимальна дія: коротке тренування."
                } else {
                    "Готовність нижча за планову. Наступна оптимальна дія: коротке тренування."
                }
            else -> null
        }

    private fun ExerciseTrackingMode.formatTargetInput(targetReps: Int): String =
        when (this) {
            ExerciseTrackingMode.TIME_MINUTES -> fromStoredTimeSeconds(targetReps).coerceAtLeast(1).toString()
            else -> targetReps.coerceAtLeast(1).toString()
        }

    private fun Double.formatInputWeight(): String =
        if (this % 1.0 == 0.0) {
            toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", this)
        }

    private fun Float.formatEquipmentNumber(): String =
        if (this % 1f == 0f) {
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

private const val SYSTEM_BODYWEIGHT_EQUIPMENT = "body only"

private val GYM_DEFAULT_EQUIPMENT = setOf(
    EquipmentType.BODY_ONLY,
    EquipmentType.DUMBBELL,
    EquipmentType.BARBELL,
    EquipmentType.BENCH,
    EquipmentType.PULL_UP_BAR,
    EquipmentType.DIP_BARS,
    EquipmentType.BANDS,
    EquipmentType.MACHINE,
    EquipmentType.CABLE
)
