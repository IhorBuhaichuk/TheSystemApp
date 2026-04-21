package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.StringResourceException
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.DebuffConfig
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.DatabaseStatus
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.usecase.*
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.WorkoutSetInput
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

sealed class StatusDialogState {
    object None                                                       : StatusDialogState()
    object EditName                                                   : StatusDialogState()
    object LogWeight                                                  : StatusDialogState()
    object EditHeight                                                 : StatusDialogState()
    object EditDebuffs                                                : StatusDialogState()
    object EditSystemConfig                                           : StatusDialogState()
    data class QuestChecklist(val questId: Int, val isDaily: Boolean) : StatusDialogState()
    data class AddTask(val questId: Int)                             : StatusDialogState()
    object MainQuestWorkout                                          : StatusDialogState()
    data class SetupMatrix(val entry: MatrixEntryUiModel, val startWeight: String, val targetWeight: String, val showWorkoutAfter: Boolean = false) : StatusDialogState()
    data class LogWorkoutSets(val entry: MatrixEntryUiModel, val sets: List<WorkoutSetInput>, val existingLog: com.ihor.thesystem.domain.model.ExerciseSet? = null, val showWorkoutAfter: Boolean = false) : StatusDialogState()
}

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val useCases:              StatusUseCases,
    private val databaseReadinessRepo: DatabaseReadinessRepository,
    private val playerRepo: PlayerRepository,
    private val scheduleRepo: ScheduleRepository,
    private val getStatsUseCase: GetStatisticsDataUseCase,
    private val matrixRepo: com.ihor.thesystem.domain.repository.ProgressionMatrixRepository,
    private val saveExerciseSetsUseCase: com.ihor.thesystem.domain.usecase.SaveExerciseSetsUseCase,
    private val viewingDateRepo: com.ihor.thesystem.domain.repository.ViewingDateRepository,
    private val calculateRecommendation: com.ihor.thesystem.domain.usecase.CalculateRecommendedSetUseCase
) : ViewModel() {

    val databaseStatus: StateFlow<DatabaseStatus> = databaseReadinessRepo.status

    val uiState: StateFlow<UiState<StatusUiData>> = useCases.getStatusData()
        .map<StatusUiData, UiState<StatusUiData>> { UiState.Content(it) }
        .catch { 
            it.printStackTrace()
            emit(UiState.Error(UiText.StringResource(R.string.system_loading)))
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeDayWorkout: StateFlow<ActiveDayUiModel?> = playerRepo.getPlayer()
        .filterNotNull()
        .flatMapLatest { player ->
            combine(
                scheduleRepo.getSchedulesForDays(listOf(player.currentCycleDay)),
                getStatsUseCase()
            ) { schedules, stats ->
                val schedule = schedules.firstOrNull() ?: return@combine null
                
                val exercisesWithRecs = mutableListOf<ExerciseWorkoutUiModel>()
                for (ex in schedule.exercises) {
                    val rec = calculateRecommendation(ex.id, ex.name)
                    exercisesWithRecs.add(
                        ExerciseWorkoutUiModel(
                            exerciseId = ex.id,
                            name = ex.name,
                            recommendedWeight = rec.weight,
                            recommendedReps = rec.reps,
                            recommendedSets = rec.sets,
                            recommendation = "${rec.sets}x${rec.reps} @ ${rec.weight}kg"
                        )
                    )
                }

                ActiveDayUiModel(
                    dayNumber = schedule.cycleDay,
                    debuffName = null,
                    dailyTasks = persistentListOf(),
                    workoutName = schedule.workoutTemplateName,
                    exercises = exercisesWithRecs.toImmutableList(),
                    matrixEntries = stats.matrixEntries
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _dialogState = MutableStateFlow<StatusDialogState>(StatusDialogState.None)
    val dialogState: StateFlow<StatusDialogState> = _dialogState.asStateFlow()

    val allDebuffs: StateFlow<List<DebuffConfig>> = useCases.getAllDebuffs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val systemConfig: StateFlow<SystemConfig?> = useCases.getSystemConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = MutableSharedFlow<StatusOneOffEvent>()
    val events = _events.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _currentSetInputs = MutableStateFlow<List<WorkoutSetInput>>(emptyList())

    init {
        viewModelScope.launch {
            // 1. Чекаємо, поки DatabasePopulator завершить роботу
            databaseReadinessRepo.status.first { it is DatabaseStatus.Ready }
            
            // 2. Очікуємо появу гравця
            playerRepo.getPlayer().filterNotNull().first()
            
            // 3. Тепер розклад точно в базі, можна генерувати
            useCases.generateDailyQuests()
            useCases.calculateAttributes()
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
                    if (!prev.isPenaltyActive && current.isPenaltyActive) {
                        _events.emit(StatusOneOffEvent.ShowPenaltyActivated)
                    }
                    if (prev.isPenaltyActive && !current.isPenaltyActive) {
                        _events.emit(StatusOneOffEvent.ShowPenaltyDeactivated)
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

    fun onMainQuestWorkoutTap() {
        _dialogState.value = StatusDialogState.MainQuestWorkout
    }

    fun onOpenLogSets(entry: MatrixEntryUiModel, fromWorkout: Boolean = false) {
        val initialSets = listOf(WorkoutSetInput())
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
        _currentSetInputs.update { it + WorkoutSetInput() }
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

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<WorkoutSetInput>, feedback: String) {
        val currentDialog = _dialogState.value
        viewModelScope.launch {
            try {
                saveExerciseSetsUseCase(
                    exerciseId = exerciseId,
                    sets = sets,
                    date = viewingDateRepo.selectedDate.value,
                    userFeedback = feedback
                )
                // Use the status screen data use case to trigger a refresh of attributes and rank if needed
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

    fun onDebuffToggled(debuff: DebuffConfig) = launchCatching {
        useCases.updateDebuff(debuff.copy(isActive = !debuff.isActive))
    }

    fun onSystemConfigConfirmed(config: SystemConfig) = launchCatching {
        useCases.updateSystemConfig(config)
        onDismissDialog()
    }

    suspend fun getCurrentPlayer(): Player? = useCases.getPlayerFlow().firstOrNull()
}
