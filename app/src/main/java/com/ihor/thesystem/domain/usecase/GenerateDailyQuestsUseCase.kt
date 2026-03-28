package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject

class GenerateDailyQuestsUseCase @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase,
    private val calculateRecommendation: CalculateRecommendedSetUseCase
) {
    suspend operator fun invoke() {
        if (questRepo.hasActiveQuests()) return

        val config = configRepo.getConfig().firstOrNull() ?: return
        val today = LocalDate.now()
        val cycleDay = calculateCycleDay(today, config.cycleAnchorDateTimestamp)
        val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull() ?: return

        // 1. Створюємо "РУТИНУ" (To-Do List)
        questRepo.createDailyQuest(
            title = "РУТИНА | ДЕНЬ $cycleDay",
            tasks = emptyList(), 
            scheduleId = schedule.id
        )

        // 2. Створюємо "ОСНОВНИЙ КВЕСТ" (Тренування)
        schedule.workoutTemplateName?.let { templateName ->
            // Генеруємо список вправ з рекомендаціями
            val recommendedExercises = schedule.exercises.map { exercise ->
                val rec = calculateRecommendation(exercise.id, exercise.name)
                // Форматуємо назву вправи з вагою та повтореннями
                "${exercise.name.uppercase()} | ${rec.weight}кг | ${rec.sets}x${rec.reps}"
            }

            questRepo.createMainQuest(
                title = templateName.uppercase(),
                exercises = recommendedExercises,
                scheduleId = schedule.id
            )
        }
    }
}
