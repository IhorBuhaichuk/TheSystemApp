package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.repository.QuestRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetActiveWorkoutQuestUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    operator fun invoke(scheduleId: Int?): Flow<Quest?> {
        if (scheduleId == null) return flowOf(null)

        return questRepository.getActiveQuests()
            .map { quests ->
                quests.firstOrNull { quest ->
                    quest.type == DomainQuestType.MAIN && quest.scheduleId == scheduleId
                }
            }
            .distinctUntilChanged()
    }
}
