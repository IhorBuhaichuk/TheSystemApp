package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.QuestCompletionPolicy
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AdvanceCycleDayUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val completeQuest: CompleteQuestUseCase
) {
    suspend operator fun invoke(forceComplete: Boolean = false): Result<Unit, DomainError> {
        val activeQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()

        activeQuests.forEach { quest ->
            QuestCompletionPolicy.resolveForDayFinalization(quest, forceComplete)
            when (
                val result = completeQuest(
                    questId = quest.id,
                    mode = QuestCompletionMode.DayFinalization,
                    forceComplete = forceComplete
                )
            ) {
                is Result.Success -> Unit
                is Result.Error -> return result
            }
        }

        return Result.Success(Unit)
    }
}

class GetFullScheduleUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepository
) {
    operator fun invoke(day: Int) = scheduleRepo.getScheduleForDay(day)
}
