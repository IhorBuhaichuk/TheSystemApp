package com.ihor.thesystem.domain.usecase

import javax.inject.Inject

data class StatusUseCases @Inject constructor(
    val getStatusData: GetStatusScreenDataUseCase,
    val updatePlayerName: UpdatePlayerNameUseCase,
    val logWeight: LogWeightUseCase,
    val updateHeight: UpdatePlayerHeightUseCase,
    val toggleQuestTask: ToggleQuestTaskUseCase,
    val generateDailyQuests: GenerateDailyQuestsUseCase,
    val getSystemConfig: GetSystemConfigUseCase,
    val updateSystemConfig: UpdateSystemConfigUseCase,
    val getPlayerFlow: GetPlayerFlowUseCase,
    val addTaskToQuest: AddTaskToQuestUseCase,
    val removeQuestTask: RemoveQuestTaskUseCase,
    val calculateAttributes: CalculateAttributesUseCase
)
