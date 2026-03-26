package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GenerateDailyQuestsUseCase @Inject constructor(
    private val playerRepo:   PlayerRepository,
    private val questRepo:    QuestRepository,
    private val scheduleRepo: ScheduleRepository
) {
    suspend operator fun invoke() {
        if (questRepo.hasActiveQuests()) return   // вже є активні квести

        val player = playerRepo.getPlayer().firstOrNull() ?: return
        val schedule = scheduleRepo.getScheduleForDay(player.currentCycleDay)
            .firstOrNull() ?: return

        // Денний квест (якщо є завдання)
        if (schedule.dailyTaskNames.isNotEmpty()) {
            questRepo.createDailyQuest(
                title      = "ДЕННИЙ РИТУАЛ | ДЕНЬ ${player.currentCycleDay}",
                tasks      = schedule.dailyTaskNames,
                scheduleId = schedule.id
            )
        }

        // Основний квест (якщо є тренування)
        schedule.workoutTemplateName?.let { templateName ->
            questRepo.createMainQuest(
                title      = templateName.uppercase(),
                exercises  = schedule.exerciseNames,
                scheduleId = schedule.id
            )
        }
    }
}