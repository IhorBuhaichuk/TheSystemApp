package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import javax.inject.Inject

class ToggleQuestTaskUseCase @Inject constructor(
    private val repo: QuestRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase
) {
    suspend operator fun invoke(taskId: Int, questId: Int, currentCompletedState: Boolean) {
        repo.toggleTaskCompletion(
            taskId      = taskId,
            questId     = questId,
            isCompleted = !currentCompletedState
        )

        // Перевіряємо, чи цей квест є PROMOTION і чи він завершений
        val quest = repo.getQuestById(questId)
        if (quest != null && quest.type == DomainQuestType.PROMOTION && quest.status == DomainQuestStatus.COMPLETED) {
            val exerciseId = quest.targetExerciseId
            if (exerciseId != null) {
                // 1. Скидаємо прапорець очікування
                matrixRepo.setPromotionPending(exerciseId, false)
                // 2. Підвищуємо ранг вправи
                matrixRepo.promoteRank(exerciseId)
                // 3. Перераховуємо глобальний ранг гравця
                recalculateGlobalRank()
            }
        }
    }
}

class AddTaskToQuestUseCase @Inject constructor(
    private val repo: QuestRepository
) {
    suspend operator fun invoke(questId: Int, taskName: String) {
        if (taskName.isNotBlank()) {
            repo.addTaskToQuest(questId, taskName)
        }
    }
}

class RemoveQuestTaskUseCase @Inject constructor(
    private val repo: QuestRepository
) {
    suspend operator fun invoke(taskId: Int) {
        repo.removeTask(taskId)
    }
}
