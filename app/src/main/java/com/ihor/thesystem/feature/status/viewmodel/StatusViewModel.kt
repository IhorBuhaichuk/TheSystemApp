package com.ihor.thesystem.feature.status.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.domain.model.DebuffConfig
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
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
    private val databaseReadinessRepo: DatabaseReadinessRepository
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
                _uiEvents.emit(UiEvent.ShowError("Помилка ініціалізації бази даних. Перезапустіть додаток."))
                return@launch
            }

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
            try {
                val player = playerRepo.getPlayer().firstOrNull() ?: return@launch
                updatePlayerName(player, newName)
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    fun onWeightConfirmed(weight: Float) {
        viewModelScope.launch {
            try {
                logWeight(weight)
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    fun onHeightConfirmed(height: Float) {
        viewModelScope.launch {
            try {
                updateHeight(height)
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    fun onTaskToggled(task: TaskUiModel, questId: Int) {
        viewModelScope.launch {
            try {
                toggleQuestTask(task, questId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    fun onAddTask(questId: Int, taskName: String) {
        if (taskName.isBlank()) return
        viewModelScope.launch {
            try {
                questRepo.addTaskToQuest(questId, taskName)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    fun onRemoveTask(taskId: Int) {
        viewModelScope.launch {
            try {
                questRepo.removeTask(taskId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    fun onDebuffToggled(debuff: DebuffConfig) {
        viewModelScope.launch {
            try {
                updateDebuff(debuff.copy(isActive = !debuff.isActive))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    fun onSystemConfigConfirmed(config: SystemConfig) {
        viewModelScope.launch {
            try {
                updateSystemConfig(config)
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            }
        }
    }

    suspend fun getCurrentPlayer(): Player? = playerRepo.getPlayer().firstOrNull()
}
