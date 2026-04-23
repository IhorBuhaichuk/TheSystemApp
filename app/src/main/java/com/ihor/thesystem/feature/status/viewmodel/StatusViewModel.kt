package com.ihor.thesystem.feature.status.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.StringResourceException
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.DatabaseStatus
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.usecase.*
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.ActiveSetInput
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

sealed class StatusDialogState {
    data object None                                                       : StatusDialogState()
    data object EditName                                                   : StatusDialogState()
    data object LogWeight                                                  : StatusDialogState()
    data object EditHeight                                                 : StatusDialogState()
    data object EditSystemConfig                                           : StatusDialogState()
    data class QuestChecklist(val questId: Int, val isDaily: Boolean) : StatusDialogState()
    data class AddTask(val questId: Int)                             : StatusDialogState()
    data object MainQuestWorkout                                          : StatusDialogState()
    data class SetupMatrix(val entry: MatrixEntryUiModel, val startWeight: String, val targetWeight: String, val showWorkoutAfter: Boolean = false) : StatusDialogState()
    data class LogWorkoutSets(val entry: MatrixEntryUiModel, val sets: List<ActiveSetInput>, val existingLogs: List<com.ihor.thesystem.domain.model.ExerciseSet> = emptyList(), val showWorkoutAfter: Boolean = false) : StatusDialogState()
}

@HiltViewModel
class StatusViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val useCases:              StatusUseCases,
    private val databaseReadinessRepo: DatabaseReadinessRepository,
    private val playerRepo: PlayerRepository,
    private val scheduleRepo: ScheduleRepository,
    private val getStatsUseCase: GetStatisticsDataUseCase,
    private val matrixRepo: com.ihor.thesystem.domain.repository.ProgressionMatrixRepository,
    private val saveExerciseSetsUseCase: com.ihor.thesystem.domain.usecase.SaveExerciseSetsUseCase,
    private val viewingDateRepo: com.ihor.thesystem.domain.repository.ViewingDateRepository,
    private val calculateRecommendation: com.ihor.thesystem.domain.usecase.CalculateRecommendedSetUseCase,
    private val calculateCycleDay: com.ihor.thesystem.domain.usecase.CalculateCycleDayForDateUseCase
) : ViewModel() {

    val databaseStatus: StateFlow<DatabaseStatus> = databaseReadinessRepo.status

    val systemConfig: StateFlow<SystemConfig?> = useCases.getSystemConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    val uiState: StateFlow<UiState<StatusUiData>> = useCases.getStatusData()
        .map<StatusUiData, UiState<StatusUiData>> { UiState.Content(it) }
        .catch { 
            it.printStackTrace()
            emit(UiState.Error(UiText.StringResource(R.string.system_loading)))
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000L),
            initialValue = UiState.Loading
        )

    private val _dialogState = MutableStateFlow<StatusDialogState>(StatusDialogState.None)
    val dialogState: StateFlow<StatusDialogState> = _dialogState.asStateFlow()

    private val _events = MutableSharedFlow<StatusOneOffEvent>()
    val events = _events.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _currentSetInputs = MutableStateFlow<List<ActiveSetInput>>(emptyList())

    private val _activeWorkoutState = MutableStateFlow<ActiveDayUiModel?>(null)
    val activeWorkoutState: StateFlow<ActiveDayUiModel?> = _activeWorkoutState.asStateFlow()

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private val _saveEvents = MutableSharedFlow<Pair<Int, Long>>(replay = 0)

    init {
        // Debounced auto-save logic to prevent Race Conditions
        viewModelScope.launch {
            _saveEvents
                .debounce(1500L)
                .collectLatest { (exerciseId, setId) ->
                    performAutoSave(exerciseId, setId)
                }
        }
    }

    private suspend fun performAutoSave(exerciseId: Int, setId: Long) {
        val currentState = _activeWorkoutState.value ?: return
        val exercise = currentState.exercises.find { it.exerciseId == exerciseId } ?: return
        val set = exercise.sets.find { it.id == setId } ?: return
        
        if (set.weight.isNotEmpty() && set.reps.isNotEmpty()) {
            try {
                saveExerciseSetsUseCase(
                    exerciseId = exerciseId,
                    sets = exercise.sets.filter { it.isCompleted || it.id == setId }.map { ActiveSetInput(it.id, it.weight, it.reps) },
                    date = viewingDateRepo.selectedDate.value,
                    userFeedback = ""
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }
        }
    }

    init {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.withTimeout(10_000) {
                    databaseReadinessRepo.status.first { it is DatabaseStatus.Ready }
                    playerRepo.getPlayer().filterNotNull().first()
                }
                useCases.generateDailyQuests()
                useCases.calculateAttributes()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
            }
        }

        // Single source of truth for workout data, loaded once per day change
        viewModelScope.launch {
            combine(
                playerRepo.getPlayer().filterNotNull(),
                systemConfig.filterNotNull(),
                viewingDateRepo.selectedDate
            ) { player, config, date ->
                if (config.cycleAnchorDateTimestamp > 0) {
                    calculateCycleDay(
                        targetDate = date,
                        anchorEpochDay = java.time.Instant.ofEpochMilli(config.cycleAnchorDateTimestamp)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay(),
                        anchorCycleDay = config.cycleAnchorDay,
                        cycleDaysPerMicrocycle = config.cycleDaysPerMicrocycle
                    )
                } else {
                    player.currentCycleDay
                }
            }.distinctUntilChanged().collect { day ->
                loadDailyWorkout(day)
            }
        }

        // Update only matrix entries to avoid overwriting user input in exercises
        viewModelScope.launch {
            getStatsUseCase().collect { stats ->
                // IMPORTANT: We only update if the dialog is not currently editing specific sets
                // or if we are just updating the progress bars (matrixEntries)
                _activeWorkoutState.update { current ->
                    current?.copy(matrixEntries = stats.matrixEntries.toImmutableList())
                }
            }
        }

        viewModelScope.launch {
            useCases.getPlayerFlow()
                .filterNotNull()
                .scan(Pair<Player?, Player?>(null, null)) { (_, prev), current ->
                    Pair(prev, current)
                }
                .filter { (prev, current) -> prev != null && current != null }
                .collect { (prev, current) ->
                    requireNotNull(prev); requireNotNull(current)
                    
                    if (prev.playerClass != current.playerClass) {
                        _events.emit(
                            StatusOneOffEvent.ShowLevelUp(current.playerClass, current.currentMonth)
                        )
                    }
                }
        }
    }

    private inline fun launchCatching(crossinline block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
            }
        }
    }

    fun onSetWeightChanged(exerciseId: Int, setId: Long, weight: String) {
        _activeWorkoutState.update { state ->
            state?.copy(
                exercises = state.exercises.map { ex ->
                    if (ex.exerciseId == exerciseId) {
                        ex.copy(sets = ex.sets.map { if (it.id == setId) it.copy(weight = weight) else it }.toImmutableList())
                    } else ex
                }.toImmutableList()
            )
        }
    }

    fun onSetRepsChanged(exerciseId: Int, setId: Long, reps: String) {
        _activeWorkoutState.update { state ->
            state?.copy(
                exercises = state.exercises.map { ex ->
                    if (ex.exerciseId == exerciseId) {
                        ex.copy(sets = ex.sets.map { if (it.id == setId) it.copy(reps = reps) else it }.toImmutableList())
                    } else ex
                }.toImmutableList()
            )
        }
    }

    fun onSetFocusLost(exerciseId: Int, setId: Long) {
        autoSaveSet(exerciseId, setId)
    }

    fun onSetCompleted(exerciseId: Int, setId: Long) {
        _activeWorkoutState.update { state ->
            state?.copy(
                exercises = state.exercises.map { ex ->
                    if (ex.exerciseId == exerciseId) {
                        ex.copy(sets = ex.sets.map { if (it.id == setId) it.copy(isCompleted = !it.isCompleted) else it }.toImmutableList())
                    } else ex
                }.toImmutableList()
            )
        }
        autoSaveSet(exerciseId, setId)
    }

    private var autoSaveJob: kotlinx.coroutines.Job? = null
    private fun autoSaveSet(exerciseId: Int, setId: Long) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val currentState = _activeWorkoutState.value ?: return@launch
            val exercise = currentState.exercises.find { it.exerciseId == exerciseId } ?: return@launch
            val set = exercise.sets.find { it.id == setId } ?: return@launch
            
            if (set.weight.isNotEmpty() && set.reps.isNotEmpty()) {
                try {
                    saveExerciseSetsUseCase(
                        exerciseId = exerciseId,
                        sets = exercise.sets.filter { it.isCompleted || it.id == setId }.map { ActiveSetInput(it.id, it.weight, it.reps) },
                        date = viewingDateRepo.selectedDate.value,
                        userFeedback = ""
                    )
                    // We don't call calculateAttributes() here to avoid triggering full UI refresh
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun onOpenMainWorkout() {
        _dialogState.value = StatusDialogState.MainQuestWorkout
    }

    fun onOpenLogSets(entry: MatrixEntryUiModel, fromWorkout: Boolean = false) {
        val initialSets = listOf(ActiveSetInput())
        _currentSetInputs.value = initialSets
        _dialogState.value = StatusDialogState.LogWorkoutSets(entry, initialSets, showWorkoutAfter = fromWorkout)
    }

    fun onOpenSetup(entry: MatrixEntryUiModel, fromWorkout: Boolean = false) {
        _dialogState.value = StatusDialogState.SetupMatrix(entry, entry.startWeight.toString(), entry.targetWeight.toString(), showWorkoutAfter = fromWorkout)
    }

    fun updateSetInput(id: Long, weight: String, reps: String) {
        _currentSetInputs.update { list ->
            list.map { if (it.id == id) it.copy(weight = weight, reps = reps) else it }
        }
        updateLogDialogState()
    }

    fun addSet() {
        _currentSetInputs.update { it + ActiveSetInput() }
        updateLogDialogState()
    }

    fun removeSet() {
        _currentSetInputs.update { if (it.size > 1) it.dropLast(1) else it }
        updateLogDialogState()
    }

    private fun updateLogDialogState() {
        val current = _dialogState.value
        if (current is StatusDialogState.LogWorkoutSets) {
            _dialogState.value = current.copy(sets = _currentSetInputs.value)
        }
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<ActiveSetInput>, feedback: String) {
        val currentDialog = _dialogState.value
        viewModelScope.launch {
            try {
                saveExerciseSetsUseCase(
                    exerciseId = exerciseId,
                    sets = sets,
                    date = viewingDateRepo.selectedDate.value,
                    userFeedback = feedback
                )
                useCases.calculateAttributes()
                
                if (currentDialog is StatusDialogState.LogWorkoutSets && currentDialog.showWorkoutAfter) {
                    _dialogState.value = StatusDialogState.MainQuestWorkout
                } else {
                    onDismissDialog()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(e.localizedMessage ?: "Помилка збереження")))
            }
        }
    }

    fun onConfirmSetup(exerciseId: Int, start: String, target: String) {
        val currentDialog = _dialogState.value
        viewModelScope.launch {
            try {
                matrixRepo.updateMatrixGoals(
                    exerciseId = exerciseId,
                    startWeight = start.toFloatOrNull() ?: 0f,
                    targetWeight = target.toFloatOrNull() ?: 0f
                )
                
                if (currentDialog is StatusDialogState.SetupMatrix && currentDialog.showWorkoutAfter) {
                    _dialogState.value = StatusDialogState.MainQuestWorkout
                } else {
                    onDismissDialog()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.DynamicString(e.localizedMessage ?: "Помилка")))
            }
        }
    }

    fun onDismissDialog()   { _dialogState.value = StatusDialogState.None }

    fun onEditNameTap() {
        _dialogState.value = StatusDialogState.EditName
    }

    fun onNameConfirmed(newName: String) = launchCatching {
        val player = useCases.getPlayerFlow().firstOrNull() ?: return@launchCatching
        useCases.updatePlayerName(player, newName).onSuccess {
            onDismissDialog()
        }.onFailure { e ->
            handleError(e)
        }
    }

    fun onWeightConfirmed(weight: Float) = launchCatching {
        useCases.logWeight(weight).onSuccess {
            onDismissDialog()
        }.onFailure { e ->
            handleError(e)
        }
    }

    fun onHeightConfirmed(height: Float) = launchCatching {
        useCases.updateHeight(height).onSuccess {
            onDismissDialog()
        }.onFailure { e ->
            handleError(e)
        }
    }

    fun updateAvatarUri(uri: Uri) = launchCatching {
        val player = useCases.getPlayerFlow().firstOrNull() ?: return@launchCatching
        
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        useCases.updatePlayerAvatar(player, uri.toString()).onFailure { e ->
            handleError(e)
        }
    }

    private suspend fun handleError(e: Throwable) {
        if (e is StringResourceException) {
            _uiEvents.emit(UiEvent.ShowError(e.uiText))
        } else {
            _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
        }
    }

    fun onTaskToggled(task: TaskUiModel, questId: Int) = launchCatching {
        useCases.toggleQuestTask(task.id, questId, task.isCompleted)
    }

    fun onAddTaskTap(questId: Int) {
        _dialogState.value = StatusDialogState.AddTask(questId)
    }

    fun onAddTaskConfirmed(questId: Int, taskName: String) = launchCatching {
        useCases.addTaskToQuest(questId, taskName)
        onDismissDialog()
    }

    fun onRemoveTask(taskId: Int) = launchCatching {
        useCases.removeQuestTask(taskId)
    }

    fun onSystemConfigConfirmed(config: SystemConfig) = launchCatching {
        useCases.updateSystemConfig(config)
        onDismissDialog()
    }

    suspend fun getCurrentPlayer(): Player? = useCases.getPlayerFlow().firstOrNull()

    private fun loadDailyWorkout(day: Int) {
        viewModelScope.launch {
            val schedules = scheduleRepo.getSchedulesForDays(listOf(day)).first()
            val schedule = schedules.firstOrNull() ?: return@launch
            
            val exercisesWithRecs = schedule.exercises.map { ex ->
                val rec = calculateRecommendation(ex.id, ex.name)
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

            _activeWorkoutState.value = ActiveDayUiModel(
                dayNumber = schedule.cycleDay,
                dailyTasks = persistentListOf(),
                workoutName = schedule.workoutTemplateName,
                exercises = exercisesWithRecs,
                matrixEntries = _activeWorkoutState.value?.matrixEntries ?: persistentListOf()
            )
        }
    }
}
