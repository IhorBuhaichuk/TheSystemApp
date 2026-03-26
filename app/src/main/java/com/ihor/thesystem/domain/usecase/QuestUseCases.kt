package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel
import javax.inject.Inject

class ToggleQuestTaskUseCase @Inject constructor(
    private val repo: QuestRepository
) {
    suspend operator fun invoke(task: TaskUiModel, questId: Int) {
        repo.toggleTaskCompletion(
            taskId      = task.id,
            questId     = questId,
            isCompleted = !task.isCompleted
        )
    }
}