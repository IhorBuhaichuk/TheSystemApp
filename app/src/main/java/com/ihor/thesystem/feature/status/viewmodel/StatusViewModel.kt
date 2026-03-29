package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.DebuffConfig
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.DebuffRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val systemConfigRepo:      SystemConfigRepository
) : ViewModel() {

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

    init {
        viewModelScope.launch {
            // ПЕРЕВІРКА: Чекаємо трохи, поки DatabasePopulator завершить роботу
            delay(500)
            generateDailyQuests()
        }

        viewModelScope.launch {
            var prevClass: String?   = null
            var prevPenalty: Boolean? = null
            playerRepo.getPlayer()
                .filterNotNull()
                .collect { player ->
                    if (prevClass != null && prevClass != player.playerClass) {
                        _events.emit(StatusOneOffEvent.ShowLevelUp(player.playerClass, player.currentMonth))
                    }
                    if (prevPenalty == false && player.isPenaltyActive) {
                        _events.emit(StatusOneOffEvent.ShowPenaltyActivated)
                    }
                    prevClass   = player.playerClass
                    prevPenalty = player.isPenaltyActive
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

    fun onNameConfirmed(newName: String) {
        viewModelScope.launch {
            val player = playerRepo.getPlayer().firstOrNull() ?: return@launch
            updatePlayerName(player, newName)
            onDismissDialog()
        }
    }

    fun onWeightConfirmed(weight: Float) {
        viewModelScope.launch {
            logWeight(weight)
            onDismissDialog()
        }
    }

    fun onHeightConfirmed(height: Float) {
        viewModelScope.launch {
            updateHeight(height)
            onDismissDialog()
        }
    }

    fun onTaskToggled(task: TaskUiModel, questId: Int) {
        viewModelScope.launch { toggleQuestTask(task, questId) }
    }

    fun onAddTask(questId: Int, taskName: String) {
        if (taskName.isBlank()) return
        viewModelScope.launch {
            questRepo.addTaskToQuest(questId, taskName)
        }
    }

    fun onRemoveTask(taskId: Int) {
        viewModelScope.launch {
            questRepo.removeTask(taskId)
        }
    }

    fun onDebuffToggled(debuff: DebuffConfig) {
        viewModelScope.launch { updateDebuff(debuff.copy(isActive = !debuff.isActive)) }
    }

    fun onSystemConfigConfirmed(config: SystemConfig) {
        viewModelScope.launch {
            updateSystemConfig(config)
            onDismissDialog()
        }
    }

    suspend fun getCurrentPlayer(): Player? = playerRepo.getPlayer().firstOrNull()
}
