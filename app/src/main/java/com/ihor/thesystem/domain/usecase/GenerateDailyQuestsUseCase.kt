package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseDetails
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
        val today = LocalDate.now()
        val config = configRepo.getConfig().firstOrNull() ?: return
        val cycleDay = calculateCycleDay(today, config.cycleAnchorDateTimestamp)
        val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull() ?: return

        // Перевіряємо наявність квестів окремо
        val activeDaily = questRepo.getActiveDailyQuest().firstOrNull()
        val activeMain = questRepo.getActiveMainQuest().firstOrNull()

        // 1. Створюємо "РУТИНУ", якщо її ще немає
        if (activeDaily == null) {
            questRepo.createDailyQuest(
                title = "РУТИНА | ДЕНЬ $cycleDay",
                tasks = emptyList(), 
                scheduleId = schedule.id
            )
        }

        // 2. Створюємо "ОСНОВНИЙ КВЕСТ", якщо він передбачений графіком і його ще немає
        if (activeMain == null && schedule.workoutTemplateName != null) {
            val recommendedExercises = schedule.exercises.map { exercise ->
                val rec = calculateRecommendation(exercise.id, exercise.name)
                // Форматуємо назву вправи з вагою та повтореннями для збереження в БД
                val formattedName = "${exercise.name.uppercase()} | ${rec.weight}кг | ${rec.sets}x${rec.reps}"
                ExerciseDetails(id = exercise.id, name = formattedName)
            }

            questRepo.createMainQuest(
                title = schedule.workoutTemplateName.uppercase(),
                exercises = recommendedExercises,
                scheduleId = schedule.id
            )
        }
    }
}
