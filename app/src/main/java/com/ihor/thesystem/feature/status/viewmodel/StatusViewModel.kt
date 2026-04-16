package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.DebuffConfig
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.DatabaseStatus
import com.ihor.thesystem.domain.repository.DebuffRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

sealed class StatusDialogState {
    object None                                                       : StatusDialogState()
    object EditName                                                   : StatusDialogState()
    object LogWeight                                                  : StatusDialogState()
    object EditHeight                                                 : StatusDialogState()
    object EditDebuffs                                                : StatusDialogState()
    object EditSystemConfig                                           : StatusDialogState()
    data class QuestChecklist(val questId: Int, val isDaily: Boolean) : StatusDialogState()
}

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val getStatusData:         GetStatusScreenDataUseCase,
    private val updatePlayerName:      UpdatePlayerNameUseCase,
    private val logWeight:             LogWeightUseCase,
    private val updateHeight:          UpdatePlayerHeightUseCase,
    private val toggleQuestTask:       ToggleQuestTaskUseCase,
    private val updateDebuff:          UpdateDebuffUseCase,
    private val generateDailyQuests:   GenerateDailyQuestsUseCase,
    private val getSystemConfig:       GetSystemConfigUseCase,
    private val updateSystemConfig:    UpdateSystemConfigUseCase,
    private val playerRepo:            PlayerRepository,
    private val questRepo:             QuestRepository,
    private val debuffRepo:            DebuffRepository,
    private val systemConfigRepo:      SystemConfigRepository,
    private val databaseReadinessRepo: DatabaseReadinessRepository,
    private val calculateAttributes:   CalculateAttributesUseCase
) : ViewModel() {

    val databaseStatus: StateFlow<DatabaseStatus> = databaseReadinessRepo.status

    // Додаємо невелику затримку або фільтрацію, щоб дати базі прокинутись
    val uiState: StateFlow<UiState<StatusUiData>> = getStatusData()
        .map<StatusUiData, UiState<StatusUiData>> { UiState.Content(it) }
        .catch { 
            it.printStackTrace()
            emit(UiState.Error("Завантаження системи...")) 
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    private val _dialogState = MutableStateFlow<StatusDialogState>(StatusDialogState.None)
    val dialogState: StateFlow<StatusDialogState> = _dialogState.asStateFlow()

    val allDebuffs: StateFlow<List<DebuffConfig>> = debuffRepo.getAllDebuffs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val systemConfig: StateFlow<SystemConfig?> = getSystemConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = MutableSharedFlow<StatusOneOffEvent>()
    val events = _events.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            // Чекаємо сигналізу про готовність БД замість delay(500)
            val isReady = withTimeoutOrNull(10_000L) {
                databaseReadinessRepo.isDbReady
                    .filter { it }
                    .first()
            }
            if (isReady == null) {
                // If not ready within timeout, we might still be loading or failed.
                // The UI will handle the Failed state via databaseStatus.
                return@launch
            }

            generateDailyQuests()
            calculateAttributes()
        }

        viewModelScope.launch {
            playerRepo.getPlayer()
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
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    fun onNameTap()         { _dialogState.value = StatusDialogState.EditName }
    fun onWeightTap()       { _dialogState.value = StatusDialogState.LogWeight }
    fun onHeightTap()       { _dialogState.value = StatusDialogState.EditHeight }
    fun onDebuffTap()       { _dialogState.value = StatusDialogState.EditDebuffs }
    fun onSystemConfigTap() { _dialogState.value = StatusDialogState.EditSystemConfig }
    fun onQuestTap(questId: Int, isDaily: Boolean) {
        _dialogState.value = StatusDialogState.QuestChecklist(questId, isDaily)
    }
    fun onDismissDialog()   { _dialogState.value = StatusDialogState.None }

    fun onNameConfirmed(newName: String) = launchCatching {
        if (newName.isBlank() || newName.length > 50) {
            _uiEvents.emit(UiEvent.ShowError("Некоректне значення: ім'я має бути від 1 до 50 символів"))
            return@launchCatching
        }
        val player = playerRepo.getPlayer().firstOrNull() ?: return@launchCatching
        updatePlayerName(player, newName)
        onDismissDialog()
    }

    fun onWeightConfirmed(weight: Float) = launchCatching {
        if (weight < 20f || weight > 500f) {
            _uiEvents.emit(UiEvent.ShowError("Некоректне значення: допустима вага від 20 до 500 кг"))
            return@launchCatching
        }
        logWeight(weight)
        onDismissDialog()
    }

    fun onHeightConfirmed(height: Float) = launchCatching {
        if (height < 50f || height > 300f) {
            _uiEvents.emit(UiEvent.ShowError("Некоректне значення: допустимий зріст від 50 до 300 см"))
            return@launchCatching
        }
        updateHeight(height)
        onDismissDialog()
    }

    fun onTaskToggled(task: TaskUiModel, questId: Int) = launchCatching {
        toggleQuestTask(task, questId)
    }

    fun onAddTask(questId: Int, taskName: String) = launchCatching {
        if (taskName.isBlank()) return@launchCatching
        questRepo.addTaskToQuest(questId, taskName)
    }

    fun onRemoveTask(taskId: Int) = launchCatching {
        questRepo.removeTask(taskId)
    }

    fun onDebuffToggled(debuff: DebuffConfig) = launchCatching {
        updateDebuff(debuff.copy(isActive = !debuff.isActive))
    }

    fun onSystemConfigConfirmed(config: SystemConfig) = launchCatching {
        updateSystemConfig(config)
        onDismissDialog()
    }

    suspend fun getCurrentPlayer(): Player? = playerRepo.getPlayer().firstOrNull()
}
