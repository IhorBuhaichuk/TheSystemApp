package com.ihor.thesystem.feature.status.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.core.ui.asUiText
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.CalendarDayCompletionStatus
import com.ihor.thesystem.domain.model.CalendarWeekDay
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.StatusData
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.model.TodoItem
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.DatabaseStatus
import com.ihor.thesystem.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatusViewModel @Inject constructor(
    private val useCases: StatusUseCases,
    private val databaseReadinessRepo: DatabaseReadinessRepository,
    private val clock: AppClock
) : ViewModel() {

    val databaseStatus: StateFlow<DatabaseStatus> = databaseReadinessRepo.status

    private val _questsReady = MutableStateFlow(false)
    private val _refreshRequests = MutableStateFlow(0L)

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
                            _refreshRequests.flatMapLatest {
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
            started      = SharingStarted.WhileSubscribed(5_000L),
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

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
                
                refreshDailyState()
                
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

    fun refreshForCurrentDay() = launchCatching {
        if (databaseStatus.value !is DatabaseStatus.Ready) return@launchCatching
        useCases.selectViewingDate(todayDate())
        refreshDailyState()
        _refreshRequests.value = clock.now()
    }

    private suspend fun refreshDailyState() {
        syncTodayStateAndReport()
    }

    private fun todayDate(): LocalDate =
        java.time.Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
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
        when (val result = useCases.updatePlayerName(player, newName)) {
            is Result.Success -> onDismissDialog()
            is Result.Error -> handleError(result.error)
        }
    }

    fun onWeightConfirmed(weight: Float) = launchCatching {
        when (val result = useCases.logWeight(weight)) {
            is Result.Success -> onDismissDialog()
            is Result.Error -> handleError(result.error)
        }
    }

    fun onHeightConfirmed(height: Float) = launchCatching {
        when (val result = useCases.updateHeight(height)) {
            is Result.Success -> onDismissDialog()
            is Result.Error -> handleError(result.error)
        }
    }

    fun updateAvatarUri(uri: Uri) = launchCatching {
        val player = currentPlayer.value ?: return@launchCatching

        when (val avatarResult = useCases.saveAvatar(uri.toString())) {
            is Result.Success -> {
                when (val updateResult = useCases.updatePlayerAvatar(player, avatarResult.data)) {
                    is Result.Success -> Unit
                    is Result.Error -> handleError(updateResult.error)
                }
            }
            is Result.Error -> {
                _uiEvents.emit(UiEvent.ShowError(avatarResult.error.asUiText()))
            }
        }
    }

    private suspend fun handleError(error: DomainError) {
        _uiEvents.emit(UiEvent.ShowError(error.asUiText()))
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
        taskName.toTaskNames().forEach { taskName ->
            if (questId > 0) {
                useCases.addTaskToQuest(questId, taskName)
            } else {
                useCases.addTodayTodo(taskName)
            }
        }
        onDismissDialog()
    }

    fun onAddMicrotaskTap(parentTodo: TodoUiModel) {
        _dialogState.value = StatusDialogState.AddMicrotask(parentTodo.id, parentTodo.title)
    }

    fun onAddMicrotaskConfirmed(parentTodoId: Int, taskName: String) = launchCatching {
        taskName.toTaskNames().forEach { taskName ->
            useCases.addTodayMicrotask(parentTodoId, taskName)
        }
        onDismissDialog()
    }

    fun onTodosReordered(orderedTodoIds: List<Int>) = launchCatching {
        useCases.reorderTodayTodos(orderedTodoIds)
    }

    fun onRemoveTask(taskId: Int) = launchCatching {
        useCases.removeQuestTask(taskId)
    }

    fun onRemoveTodo(todoId: Int) = launchCatching {
        useCases.removeTodo(todoId)
    }

    fun onForceEndDay() = launchCatching {
        _questsReady.value = false
        try {
            finalizeDayAndReport(forceComplete = true)
        } finally {
            _questsReady.value = true
        }
    }

    private suspend fun finalizeDayAndReport(forceComplete: Boolean): Boolean {
        return when (val result = useCases.finalizeDay(forceComplete = forceComplete)) {
            is Result.Success -> true
            is Result.Error -> {
                _uiEvents.emit(UiEvent.ShowError(result.error.asUiText()))
                false
            }
        }
    }

    private suspend fun syncTodayStateAndReport(): Boolean {
        return when (val result = useCases.syncTodayState()) {
            is Result.Success -> true
            is Result.Error -> {
                _uiEvents.emit(UiEvent.ShowError(result.error.asUiText()))
                false
            }
        }
    }

    fun onSystemConfigConfirmed(config: SystemConfig) = launchCatching {
        useCases.updateSystemConfig(config)
        syncTodayStateAndReport()
        onDismissDialog()
    }

    fun onOpenCalendarTap() {
        useCases.selectViewingDate(todayDate())
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

    private fun TodoItem.toUiModel(): TodoUiModel = TodoUiModel(
        id = id,
        title = title,
        isCompleted = isCompleted,
        parentTodoId = parentTodoId,
        microtasks = microtasks.map { it.toUiModel() }.toImmutableList()
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
        val locale = Locale.getDefault()
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

internal fun String.toTaskNames(): List<String> =
    lineSequence()
        .map { line -> line.trim().removeTaskListPrefix().trim() }
        .filter { line -> line.isNotEmpty() }
        .toList()

private fun String.removeTaskListPrefix(): String =
    replace(taskListPrefixRegex, "")

private val taskListPrefixRegex = Regex("""^([-*]|\d+[.)]|\[[ xX]\])\s+""")
