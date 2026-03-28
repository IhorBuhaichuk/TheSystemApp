package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseRecommendation
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Герує квести на основі ПОТОЧНОГО дня циклу гравця (SSOT).
 * Більше не використовує системний час для розрахунку дня, що забезпечує секвентальність.
 */
class GenerateDailyQuestsUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val playerRepo: PlayerRepository,
    private val calculateRecommendation: CalculateRecommendedSetUseCase
) {
    suspend operator fun invoke() {
        // ПРАВИЛЬНО: Беремо день циклу безпосередньо з профілю гравця
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        val cycleDay = player.currentCycleDay
        
        // Перевіряємо наявність квестів окремо для кожного типу
        val activeDaily = questRepo.getActiveDailyQuest().firstOrNull()
        val activeMain = questRepo.getActiveMainQuest().firstOrNull()

        val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull() ?: return

        // 1. Створюємо "РУТИНУ" (To-do List) якщо її немає
        if (activeDaily == null) {
            questRepo.createDailyQuest(
                title = "РУТИНА | ДЕНЬ $cycleDay",
                tasks = emptyList(), 
                scheduleId = schedule.id
            )
        }

        // 2. Створюємо "ОСНОВНИЙ КВЕСТ" (Тренування) якщо він передбачений графіком і його ще немає
        if (activeMain == null && schedule.workoutTemplateName != null) {
            val recommendedExercises = schedule.exercises.map { exercise ->
                val rec = calculateRecommendation(exercise.id, exercise.name)
                // Передаємо чисті дані (ID, вага, сети, репси)
                ExerciseRecommendation(
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    weight = rec.weight,
                    sets = rec.sets,
                    reps = rec.reps
                )
            }

            questRepo.createMainQuest(
                title = schedule.workoutTemplateName.uppercase(),
                exercises = recommendedExercises,
                scheduleId = schedule.id
            )
        }
    }
}
