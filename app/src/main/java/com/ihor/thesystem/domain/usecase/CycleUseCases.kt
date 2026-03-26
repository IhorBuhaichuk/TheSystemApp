package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AdvanceCycleDayUseCase @Inject constructor(
    private val playerRepo:       PlayerRepository,
    private val questRepo:        QuestRepository,
    private val generateQuests:   GenerateDailyQuestsUseCase,
    private val levelUp:          LevelUpUseCase,
    private val checkPenalty:     CheckPenaltyZoneUseCase
) {
    suspend operator fun invoke(forceComplete: Boolean = false) {
        val player = playerRepo.getPlayer().firstOrNull() ?: return

        val daily = questRepo.getActiveDailyQuest().firstOrNull()
        val main  = questRepo.getActiveMainQuest().firstOrNull()

        // ── Завершуємо денний квест ───────────────────────────────────
        daily?.let {
            val allDone = it.tasks.isNotEmpty() && it.tasks.all { t -> t.isCompleted }
            questRepo.updateQuestStatus(
                it.id,
                if (allDone || forceComplete) DomainQuestStatus.COMPLETED
                else DomainQuestStatus.FAILED
            )
        }

        // ── Завершуємо основний квест ─────────────────────────────────
        main?.let {
            val allDone = it.tasks.isNotEmpty() && it.tasks.all { t -> t.isCompleted }
            val status  = if (allDone || forceComplete) DomainQuestStatus.COMPLETED
            else DomainQuestStatus.FAILED
            questRepo.updateQuestStatus(it.id, status)

            // LevelUp тільки при успішному завершенні Main Quest
            if (status == DomainQuestStatus.COMPLETED) {
                levelUp()
            }

            // Перевіряємо штрафну зону після кожного Main Quest
            checkPenalty()
        }

        // ── Перехід до наступного дня ─────────────────────────────────
        val nextDay = if (player.currentCycleDay >= 4) 1 else player.currentCycleDay + 1
        playerRepo.updatePlayer(player.copy(currentCycleDay = nextDay))

        // ── Генеруємо квести для нового дня ──────────────────────────
        generateQuests()
    }
}

class GetFullScheduleUseCase @Inject constructor(
    private val scheduleRepo: ScheduleRepository
) {
    operator fun invoke(day: Int) = scheduleRepo.getScheduleForDay(day)
}