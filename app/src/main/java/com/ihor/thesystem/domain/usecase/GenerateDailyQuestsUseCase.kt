package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.round

class GenerateDailyQuestsUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val calculateRecommendation: CalculateRecommendedSetUseCase,
    private val clock: AppClock
) {
    suspend operator fun invoke() {
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        val cycleDay = player.currentCycleDay
        
        // 1. Отримуємо всі квести на сьогодні
        val todayQuests = questRepo.getDailyQuestsForDate(clock.now()).first()
        
        // 2. ФІКС: Розблоковуємо застряглі квести, але не перериваємо виконання
        todayQuests.filter { it.status == DomainQuestStatus.LOCKED }.forEach { lockedQuest ->
            questRepo.updateQuestStatus(lockedQuest.id, DomainQuestStatus.ACTIVE)
        }

        // 3. Ізольовані перевірки наявності кожного типу квесту
        val hasRoutine = todayQuests.any { it.title.contains("РУТИНА", ignoreCase = true) }
        val hasMainWorkout = todayQuests.any { it.type == DomainQuestType.MAIN }

        // 4. Перевіряємо розклад для поточного дня циклу
        val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull()

        if (schedule != null) {
            // ГЕНЕРУЄМО РУТИНУ, ЯКЩО ЇЇ НЕМАЄ
            if (!hasRoutine) {
                val routineTasks = listOf(
                    "Прийом вітамінів та добавок",
                    "Контроль водного балансу (2л+)",
                    "Заповнення щоденного звіту"
                )
                
                questRepo.createDailyQuest(
                    title = "РУТИНА | ДЕНЬ $cycleDay",
                    tasks = routineTasks, 
                    scheduleId = schedule.id
                )
            }

            // ГЕНЕРУЄМО ОСНОВНЕ ТРЕНУВАННЯ, ЯКЩО ЙОГО НЕМАЄ ТА ВОНО ПЕРЕДБАЧЕНЕ (не день відпочинку)
            val workoutTemplateName = schedule.workoutTemplateName
            if (!hasMainWorkout && workoutTemplateName != null) {
                val recommendedExercises = mutableListOf<ExerciseRecommendation>()
                for (exercise in schedule.exercises) {
                    val rec = calculateRecommendation(exercise.id, exercise.name)
                    recommendedExercises.add(
                        ExerciseRecommendation(
                            exerciseId = exercise.id,
                            exerciseName = exercise.name,
                            weight = rec.weight,
                            sets = rec.sets,
                            reps = rec.reps
                        )
                    )
                }

                questRepo.createMainQuest(
                    title = workoutTemplateName.uppercase(),
                    exercises = recommendedExercises,
                    scheduleId = schedule.id
                )
            }
        }

        // 5. ГЕНЕРАЦІЯ PROMOTION (НЕЗАЛЕЖНО)
        generatePromotionQuests()
    }

    private suspend fun generatePromotionQuests() {
        val matrixEntries = matrixRepo.getAllEntries().firstOrNull() ?: emptyList()
        val pendingPromotions = matrixEntries.filter { it.isPromotionPending }
        
        pendingPromotions.forEach { pending ->
            val allActiveQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()
            val alreadyExists = allActiveQuests.any { q -> 
                q.type == DomainQuestType.PROMOTION && q.targetExerciseId == pending.exerciseId
            }
            
            if (!alreadyExists) {
                // МАТЕМАТИЧНИЙ АЛГОРИТМ РОЗРАХУНКУ ДЛЯ ЕКЗАМЕНУ
                val targetWeight = pending.targetWeight
                val rawExamWeight = targetWeight * 1.025f
                val examWeight = (round(rawExamWeight / 2.5f) * 2.5f).toDouble()
                
                questRepo.createPromotionQuest(
                    exerciseId = pending.exerciseId,
                    title = "ЕКЗАМЕН: ${pending.exerciseName.uppercase()}",
                    description = "Тест 1RM: ${pending.exerciseName}",
                    targetWeight = examWeight,
                    targetReps = 1
                )
            }
        }
    }
}
