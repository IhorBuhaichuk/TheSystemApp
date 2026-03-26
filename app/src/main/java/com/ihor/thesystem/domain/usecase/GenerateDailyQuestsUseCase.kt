package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GenerateDailyQuestsUseCase @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase
) {
    suspend operator fun invoke() {
        // Отримуємо поточну дату
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        // Перевіряємо, чи вже є активні квести на сьогодні
        if (questRepo.hasActiveQuests()) return

        val config = configRepo.getConfig().firstOrNull() ?: return
        val cycleDay = calculateCycleDay(today, config.cycleAnchorDateTimestamp)
        val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull() ?: return

        // Створюємо "Рутину" (Daily Quest) порожньою за замовчуванням (To-Do List)
        questRepo.createDailyQuest(
            title = "РУТИНА | ДЕНЬ $cycleDay",
            tasks = emptyList(), // Порожній список за замовчуванням
            scheduleId = schedule.id
        )

        // Основний квест (тренування) створюємо за розкладом
        schedule.workoutTemplateName?.let { templateName ->
            questRepo.createMainQuest(
                title = templateName.uppercase(),
                exercises = schedule.exerciseNames,
                scheduleId = schedule.id
            )
        }
    }
}
