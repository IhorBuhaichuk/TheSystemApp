package com.ihor.thesystem.domain.usecase

import javax.inject.Inject

data class StatusUseCases @Inject constructor(
    val getStatusData: GetStatusScreenDataUseCase,
    val updatePlayerName: UpdatePlayerNameUseCase,
    val logWeight: LogWeightUseCase,
    val updateHeight: UpdatePlayerHeightUseCase,
    val addTodayTodo: AddTodayTodoUseCase,
    val addTodayMicrotask: AddTodayMicrotaskUseCase,
    val toggleTodo: ToggleTodoUseCase,
    val reorderTodayTodos: ReorderTodayTodosUseCase,
    val removeTodo: RemoveTodoUseCase,
    val toggleQuestTask: ToggleQuestTaskUseCase,
    val generateDailyQuests: GenerateDailyQuestsUseCase,
    val getSystemConfig: GetSystemConfigUseCase,
    val updateSystemConfig: UpdateSystemConfigUseCase,
    val getPlayerFlow: GetPlayerFlowUseCase,
    val saveAvatar: SaveAvatarUseCase,
    val updatePlayerAvatar: UpdatePlayerAvatarUseCase,
    val addTaskToQuest: AddTaskToQuestUseCase,
    val removeQuestTask: RemoveQuestTaskUseCase,
    val getOrCreateDailyTaskContainerId: GetOrCreateDailyTaskContainerIdUseCase,
    val calculateAttributes: CalculateAttributesUseCase,
    val setNeedsDailyInit: SetNeedsDailyInitUseCase,
    val saveLastInitDate: SaveLastInitDateUseCase,
    val finalizeDay: FinalizeDayUseCase,
    val syncTodayState: SyncTodayStateUseCase,
    val getCalendarWeekPreview: GetCalendarWeekPreviewUseCase,
    val selectViewingDate: SelectViewingDateUseCase
)
