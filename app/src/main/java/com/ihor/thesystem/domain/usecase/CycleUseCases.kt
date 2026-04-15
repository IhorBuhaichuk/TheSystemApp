package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.data.local.room.entity.QuestType
import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Оновлює статуси активних квестів перед завершенням дня.
 * Сама зміна дня та генерація нових квестів тепер у FinalizeDayUseCase.
 */
class AdvanceCycleDayUseCase @Inject constructor(
    private val playerRepo:       PlayerRepository,
    private val questRepo:        QuestRepository
) {
    suspend operator fun invoke(forceComplete: Boolean = false) {
        val daily = questRepo.getActiveDailyQuest().firstOrNull()
        val main  = questRepo.getActiveMainQuest().firstOrNull()

        // ── Завершуємо денний квест ───────────────────────────────────
        daily?.let {
            val allDone = it.tasks.isNotEmpty() && it.tasks.all { t -> t.isCompleted }
            val isSuccess = allDone || forceComplete
            questRepo.updateQuestStatus(
                it.id,
                if (isSuccess) DomainQuestStatus.COMPLETED
                else DomainQuestStatus.FAILED
            )
            questRepo.logQuestResult(
                questId = it.id,
                questType = QuestType.DAILY,
                wasSuccessful = isSuccess
            )
        }

        // ── Завершуємо основний квест ─────────────────────────────────
        main?.let {
            val allDone = it.tasks.isNotEmpty() && it.tasks.all { t -> t.isCompleted }
            val isSuccess = allDone || forceComplete
            val status  = if (isSuccess) DomainQuestStatus.COMPLETED
            else DomainQuestStatus.FAILED
            questRepo.updateQuestStatus(it.id, status)
            questRepo.logQuestResult(
                questId = it.id,
                questType = QuestType.MAIN,
                wasSuccessful = isSuccess
            )
        }
    }
}

class GetFullScheduleUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepository
) {
    operator fun invoke(day: Int) = scheduleRepo.getScheduleForDay(day)
}
