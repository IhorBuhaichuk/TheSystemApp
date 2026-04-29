package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Оновлює статуси активних квестів перед завершенням дня.
 * Сама зміна дня та генерація нових квестів тепер у FinalizeDayUseCase.
 */
class AdvanceCycleDayUseCase @Inject constructor(
    private val questRepo:        QuestRepository
) {
    suspend operator fun invoke(forceComplete: Boolean = false): Result<Unit, DomainError> {
        val activeQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()

        activeQuests.forEach { quest ->
            val hasTasks = quest.tasks.isNotEmpty()
            val allDone = hasTasks && quest.tasks.all { it.isCompleted }

            // Якщо задач немає (наприклад, день відпочинку), квест вважається успішним.
            // Інакше — або всі задачі виконані, або примусове завершення.
            val isSuccess = if (!hasTasks) true else (allDone || forceComplete)

            val finalStatus = if (isSuccess) DomainQuestStatus.COMPLETED else DomainQuestStatus.FAILED

            questRepo.updateQuestStatus(quest.id, finalStatus)
            questRepo.logQuestResult(
                questId = quest.id,
                questType = quest.type,
                wasSuccessful = isSuccess
            )
        }

        return Result.Success(Unit)
    }
}

class GetFullScheduleUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepository
) {
    operator fun invoke(day: Int) = scheduleRepo.getScheduleForDay(day)
}
