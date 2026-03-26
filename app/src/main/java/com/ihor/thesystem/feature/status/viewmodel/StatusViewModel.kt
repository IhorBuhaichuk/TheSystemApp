package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.DebuffConfig
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.DebuffRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StatusDialogState {
    object None                                                       : StatusDialogState()
    object EditName                                                   : StatusDialogState()
    object LogWeight                                                  : StatusDialogState()
    object EditDebuffs                                                : StatusDialogState()
    object EditSystemConfig                                           : StatusDialogState()
    data class QuestChecklist(val questId: Int, val isDaily: Boolean) : StatusDialogState()
}

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val getStatusData:         GetStatusScreenDataUseCase,
    private val updatePlayerName:      UpdatePlayerNameUseCase,
    private val logWeight:             LogWeightUseCase,
    private val toggleQuestTask:       ToggleQuestTaskUseCase,
    private val updateDebuff:          UpdateDebuffUseCase,
    private val generateDailyQuests:   GenerateDailyQuestsUseCase,
    private val getSystemConfig:       GetSystemConfigUseCase,
    private val updateSystemConfig:    UpdateSystemConfigUseCase,
    private val playerRepo:            PlayerRepository,
    private val debuffRepo:            DebuffRepository,
    private val systemConfigRepo:      SystemConfigRepository
) : ViewModel() {

    val uiState: StateFlow<UiState<StatusUiData>> = getStatusData()
        .map<StatusUiData, UiState<StatusUiData>> { UiState.Content(it) }
        .catch { emit(UiState.Error(it.message ?: "Помилка системи")) }
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
            playerRepo.getPlayer()
                .filterNotNull()
                .first()
            generateDailyQuests()
        }

        viewModelScope.launch {
            var prevClass: String?   = null
            var prevPenalty: Boolean? = null
            playerRepo.getPlayer()
                .filterNotNull()
                .collect { player ->
                    val prev = prevClass
                    if (prev != null && prev != player.playerClass) {
                        _events.emit(
                            StatusOneOffEvent.ShowLevelUp(
                                newClass = player.playerClass,
                                newMonth = player.currentMonth
                            )
                        )
                    }
                    val wasPenalty = prevPenalty
                    if (wasPenalty == false && player.isPenaltyActive) {
                        _events.emit(StatusOneOffEvent.ShowPenaltyActivated)
                    }
                    if (wasPenalty == true && !player.isPenaltyActive) {
                        _events.emit(StatusOneOffEvent.ShowPenaltyDeactivated)
                    }
                    prevClass   = player.playerClass
                    prevPenalty = player.isPenaltyActive
                }
        }
    }

    // ── Dialog triggers ───────────────────────────────────────────────
    fun onNameTap()         { _dialogState.value = StatusDialogState.EditName }
    fun onWeightTap()       { _dialogState.value = StatusDialogState.LogWeight }
    fun onDebuffTap()       { _dialogState.value = StatusDialogState.EditDebuffs }
    fun onSystemConfigTap() { _dialogState.value = StatusDialogState.EditSystemConfig }
    fun onQuestTap(questId: Int, isDaily: Boolean) {
        _dialogState.value = StatusDialogState.QuestChecklist(questId, isDaily)
    }
    fun onDismissDialog()   { _dialogState.value = StatusDialogState.None }

    // ── Actions ───────────────────────────────────────────────────────
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

    fun onTaskToggled(task: TaskUiModel, questId: Int) {
        viewModelScope.launch { toggleQuestTask(task, questId) }
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