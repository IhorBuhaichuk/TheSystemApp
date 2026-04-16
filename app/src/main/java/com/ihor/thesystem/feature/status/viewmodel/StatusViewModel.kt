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
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.DatabaseStatus
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
    private val useCases:              StatusUseCases,
    private val databaseReadinessRepo: DatabaseReadinessRepository
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

    init {
        viewModelScope.launch {
            val isReady = withTimeoutOrNull(10_000L) {
                databaseReadinessRepo.isDbReady
                    .filter { it }
                    .first()
            }
            if (isReady == null) {
                return@launch
            }

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

    fun onAddTask(questId: Int, taskName: String) = launchCatching {
        useCases.addTaskToQuest(questId, taskName)
    }

    fun onReorderTask(taskId: Int, from: Int, to: Int) = launchCatching {
        // useCases.reorderQuestTask(taskId, from, to)
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
