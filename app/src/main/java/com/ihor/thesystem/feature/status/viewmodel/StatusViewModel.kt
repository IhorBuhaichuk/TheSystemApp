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
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.DatabaseStatus
import com.ihor.thesystem.domain.usecase.*
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList

@HiltViewModel
class StatusViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val useCases: StatusUseCases,
    private val databaseReadinessRepo: DatabaseReadinessRepository
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

    val currentPlayer: StateFlow<Player?> = useCases.getPlayerFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Initialization Group 1: Database Readiness and Initial Calculations
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                kotlinx.coroutines.withTimeout(10_000) {
                    databaseReadinessRepo.status.first { it is DatabaseStatus.Ready }
                    useCases.getPlayerFlow().filterNotNull().first()
                }
                useCases.generateDailyQuests()
                useCases.calculateAttributes()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
            }
        }

        // Initialization Group 2: Level-up Event Monitoring
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

    fun onDismissDialog()   { _dialogState.value = StatusDialogState.None }

    fun onEditNameTap() {
        _dialogState.value = StatusDialogState.EditName
    }

    fun onNameConfirmed(newName: String) = launchCatching {
        val player = currentPlayer.value ?: return@launchCatching
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
        val player = currentPlayer.value ?: return@launchCatching
        
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
        useCases.generateDailyQuests()
        onDismissDialog()
    }
}
