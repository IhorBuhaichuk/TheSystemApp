package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import javax.inject.Inject

class GetOrCreateDailyTaskContainerIdUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val configRepo: SystemConfigRepository,
    private val playerRepo: PlayerRepository,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val clock: AppClock
) {
    suspend operator fun invoke(): Int {
        val now = clock.now()
        val dailyQuests = questRepo.getDailyQuestsForDate(now).first()
        
        // 1. Перевіряємо, чи вже є DAILY квест на сьогодні
        val existingDaily = dailyQuests.find { it.type == DomainQuestType.DAILY }
        if (existingDaily != null) {
            return existingDaily.id
        }

        // 2. Якщо немає — створюємо порожній контейнер
        val config = configRepo.getConfigFlow().firstOrNull() ?: return 0
        val todayDate = Instant.ofEpochMilli(now)
            .atZone(clock.zoneId())
            .toLocalDate()

        val currentDay = resolveTrainingCycleDay(
            targetDate = todayDate,
            config = config,
            fallbackCurrentCycleDay = playerRepo.getPlayer().firstOrNull()?.currentCycleDay
        )

        val schedule = scheduleRepo.getScheduleForDay(currentDay).firstOrNull()
        
        questRepo.createDailyQuest(
            title = "РУТИНА | ДЕНЬ $currentDay",
            tasks = emptyList(), // Порожній список, бо ми лише створюємо контейнер
            scheduleId = schedule?.id
        )

        // Повторно зчитуємо, щоб отримати ID
        return questRepo.getDailyQuestsForDate(now).first()
            .find { it.type == DomainQuestType.DAILY }?.id ?: 0
    }
}
