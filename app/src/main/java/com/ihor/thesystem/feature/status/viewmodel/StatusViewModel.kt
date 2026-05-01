package com.ihor.thesystem.feature.status.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.core.ui.StringResourceException
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.CalendarDayCompletionStatus
import com.ihor.thesystem.domain.model.CalendarWeekDay
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.StatusData
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.model.TodoItem
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.DatabaseStatus
import com.ihor.thesystem.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatusViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val useCases: StatusUseCases,
    private val databaseReadinessRepo: DatabaseReadinessRepository,
    private val clock: AppClock
) : ViewModel() {

    val databaseStatus: StateFlow<DatabaseStatus> = databaseReadinessRepo.status

    private val _questsReady = MutableStateFlow(false)

    val systemConfig: StateFlow<SystemConfig?> = databaseStatus
        .flatMapLatest { status ->
            if (status is DatabaseStatus.Ready) {
                useCases.getSystemConfig()
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    val uiState: StateFlow<UiState<StatusUiData>> = databaseStatus
        .flatMapLatest { status ->
            when (status) {
                is DatabaseStatus.Ready -> {
                    _questsReady
                        .filter { it } // чекаємо поки квести готові
                        .flatMapLatest {
                            combine(
                                useCases.getStatusData(),
                                useCases.getCalendarWeekPreview()
                            ) { data, weekPreview ->
                                data to weekPreview
                            }
                                .map<Pair<StatusData, List<CalendarWeekDay>>, UiState<StatusUiData>> { (data, weekPreview) ->
                                    UiState.Content(data.toUiData(weekPreview))
                                }
                                .catch { e ->
                                    Timber.e(e, "Error loading status data")
                                    emit(UiState.Error(UiText.StringResource(R.string.system_loading)))
                                }
                        }
                }
                is DatabaseStatus.Failed -> {
                    flowOf(UiState.Error(UiText.DynamicString(status.reason)))
                }
                else -> {
                    flowOf(UiState.Loading)
                }
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.Eagerly,
            initialValue = UiState.Loading
        )

    private val _dialogState = MutableStateFlow<StatusDialogState>(StatusDialogState.None)
    val dialogState: StateFlow<StatusDialogState> = _dialogState.asStateFlow()

    private val _events = MutableSharedFlow<StatusOneOffEvent>()
    val events = _events.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    val currentPlayer: StateFlow<Player?> = databaseStatus
        .flatMapLatest { status ->
            if (status is DatabaseStatus.Ready) {
                useCases.getPlayerFlow()
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // Initialization Group 1: Database Readiness and Initial Calculations
        viewModelScope.launch {
            try {
                withTimeout(15_000) {
                    val readiness = databaseReadinessRepo.status.first { 
                        it is DatabaseStatus.Ready || it is DatabaseStatus.Failed 
                    }

                    if (readiness is DatabaseStatus.Failed) {
                        Timber.e("Database initialization failed: ${(readiness as DatabaseStatus.Failed).reason}")
                        return@withTimeout
                    }
                    
                    // Очікуємо гравця, але обробляємо null як нормальний кейс для нового користувача
                    try {
                        withTimeout(3000) {
                            useCases.getPlayerFlow().first()
                        }
                    } catch (e: TimeoutCancellationException) {
                        Timber.i("New user detected or player data not yet initialized (timeout)")
                    }
                }
                
                // Виконуємо розрахунки тільки якщо база готова
                val config = useCases.getSystemConfig().first()
                val statusData = useCases.getStatusData().firstOrNull() ?: run {
                    _questsReady.value = true
                    return@launch
                }
                val hasNoQuests = statusData.dailyQuest == null && statusData.mainQuest == null && statusData.promotionQuests.isEmpty()

                val today = java.time.Instant.ofEpochMilli(clock.now())
                    .atZone(clock.zoneId())
                    .toLocalDate()
                    .toEpochDay()
                val lastDate = config?.lastInitEpochDay ?: 0L
                val dateChanged = lastDate < today

                if (config?.needsDailyInit == true || hasNoQuests || dateChanged) {
                    useCases.finalizeDay(forceComplete = false)
                }
                
                _questsReady.value = true   // ← ТІЛЬКИ тут відкриваємо доступ до UI Flow
                
            } catch (e: TimeoutCancellationException) {
                Timber.w("Database initialization timeout")
                _questsReady.value = true  // ← знімаємо блок навіть при таймауті, щоб UI не завис
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Error during StatusViewModel initialization")
                _questsReady.value = true  // ← завжди знімаємо блок при будь-якій помилці
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

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: StringResourceException) {
                Timber.e(e, "Domain error in launchCatching")
                _uiEvents.emit(UiEvent.ShowError(e.uiText))
            } catch (e: SecurityException) {
                Timber.e(e, "Security error in StatusViewModel")
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_unknown)))
            } catch (e: IllegalStateException) {
                Timber.e(e, "Invalid state in StatusViewModel")
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Unexpected error in StatusViewModel action")
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
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

        val localUri = withContext(Dispatchers.IO) {
            try {
                val avatarDir = File(context.filesDir, "avatars")
                if (!avatarDir.exists()) avatarDir.mkdirs()

                // Delete existing files to avoid accumulation
                avatarDir.listFiles()?.forEach { it.delete() }

                val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                val destFile = File(avatarDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.toUri().toString()
            } catch (e: IOException) {
                Timber.e(e, "Failed to copy avatar to internal storage")
                null
            }
        }

        if (localUri == null) {
            _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
            return@launchCatching
        }

        useCases.updatePlayerAvatar(player, localUri).onFailure { e ->
            handleError(e)
        }
    }

    private suspend fun handleError(e: Throwable) {
        if (e is StringResourceException) {
            _uiEvents.emit(UiEvent.ShowError(e.uiText))
        } else {
            Timber.e(e, "Handled unknown error")
            _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
        }
    }

    fun onTaskToggled(task: TaskUiModel, questId: Int) = launchCatching {
        useCases.toggleQuestTask(task.id, questId, task.isCompleted)
    }

    fun onTodoToggled(todo: TodoUiModel) = launchCatching {
        useCases.toggleTodo(todo.id, todo.isCompleted)
    }

    fun onAddTaskTap(questId: Int) {
        _dialogState.value = StatusDialogState.AddTask(questId)
    }

    fun onAddTaskConfirmed(questId: Int, taskName: String) = launchCatching {
        useCases.addTodayTodo(taskName)
        onDismissDialog()
    }

    fun onRemoveTask(taskId: Int) = launchCatching {
        useCases.removeQuestTask(taskId)
    }

    fun onRemoveTodo(todoId: Int) = launchCatching {
        useCases.removeTodo(todoId)
    }

    fun onForceEndDay() = launchCatching {
        _questsReady.value = false
        useCases.finalizeDay(forceComplete = true)
        _questsReady.value = true
    }

    fun onSystemConfigConfirmed(config: SystemConfig) = launchCatching {
        useCases.updateSystemConfig(config)
        useCases.generateDailyQuests()
        onDismissDialog()
    }

    fun onWeekDaySelected(date: LocalDate) {
        useCases.selectViewingDate(date)
    }

    private fun StatusData.toUiData(weekPreview: List<CalendarWeekDay> = emptyList()) = StatusUiData(
        playerName = playerName,
        playerClass = playerClass,
        level = level,
        xpTotal = xpTotal,
        xpMax = xpMax,
        currentMonth = currentMonth,
        totalMonths = totalMonths,
        currentWeight = currentWeight,
        height = height,
        cycleDay = cycleDay,
        monthWorkoutsCompleted = monthWorkoutsCompleted,
        monthWorkoutsTotal = monthWorkoutsTotal,
        todos = todos.map { it.toUiModel() }.toImmutableList(),
        dailyQuest = dailyQuest?.toUiModel(),
        mainQuest = mainQuest?.toUiModel(),
        promotionQuests = promotionQuests.map { it.toUiModel() }.toImmutableList(),
        globalRank = globalRank,
        characterAttributes = characterAttributes,
        currentStreak = currentStreak,
        maxStreak = maxStreak,
        xpThisWeek = xpThisWeek,
        avatarUri = avatarUri,
        weekPreview = weekPreview.map { it.toUiModel() }.toImmutableList()
    )

    private fun TodoItem.toUiModel() = TodoUiModel(
        id = id,
        title = title,
        isCompleted = isCompleted
    )

    private fun Quest.toUiModel() = QuestUiModel(
        id = id,
        title = title,
        subtitle = when (type) {
            DomainQuestType.DAILY ->
                UiText.StringResource(R.string.quest_progress, listOf(tasks.count { it.isCompleted }, tasks.size))
            DomainQuestType.MAIN ->
                if (status == DomainQuestStatus.COMPLETED) {
                    UiText.StringResource(R.string.quest_completed_capital)
                } else {
                    UiText.StringResource(R.string.quest_reward_week)
                }
            DomainQuestType.PROMOTION ->
                UiText.StringResource(R.string.quest_reward_promotion)
        },
        tasks = tasks.map { task ->
            TaskUiModel(task.id, task.name, task.nameUk, task.isCompleted)
        }.toImmutableList(),
        isCompleted = status == DomainQuestStatus.COMPLETED
    )

    private fun CalendarWeekDay.toUiModel(): StatusWeekDayUiModel {
        val visualType = when {
            calendarDay.type.isWorkDay && hasTraining -> StatusWeekDayVisualType.MIXED
            hasTraining -> StatusWeekDayVisualType.TRAINING
            calendarDay.type.isWorkDay -> StatusWeekDayVisualType.WORK
            else -> StatusWeekDayVisualType.REST
        }
        val locale = Locale("uk")
        return StatusWeekDayUiModel(
            date = date,
            weekDayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
                .replace(".", "")
                .take(2)
                .uppercase(locale),
            dayNumber = date.dayOfMonth.toString(),
            visualType = visualType,
            status = when (completionStatus) {
                CalendarDayCompletionStatus.COMPLETED -> StatusWeekDayStatus.COMPLETED
                CalendarDayCompletionStatus.PARTIAL -> StatusWeekDayStatus.PARTIAL
                CalendarDayCompletionStatus.MISSED -> StatusWeekDayStatus.MISSED
                CalendarDayCompletionStatus.PLANNED -> StatusWeekDayStatus.PLANNED
                CalendarDayCompletionStatus.NO_DATA -> StatusWeekDayStatus.NO_DATA
            },
            isToday = isToday
        )
    }
}
