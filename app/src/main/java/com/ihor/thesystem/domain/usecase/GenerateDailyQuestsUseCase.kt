package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.round
import java.util.Locale

class GenerateDailyQuestsUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val calculateRecommendation: CalculateRecommendedSetUseCase
) {
    suspend operator fun invoke() {
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        val cycleDay = player.currentCycleDay
        
        // ЗАВДАННЯ 2: Міграція "застряглих" квестів
        val todayQuests = questRepo.getDailyQuestsForDate(System.currentTimeMillis()).first()
        if (todayQuests.isNotEmpty()) {
            // Примусове розблокування старих квестів (Fix)
            todayQuests.filter { it.status == DomainQuestStatus.LOCKED }.forEach { lockedQuest ->
                questRepo.updateQuestStatus(lockedQuest.id, DomainQuestStatus.ACTIVE)
            }
            
            // Перевіряємо PROMOTION навіть якщо денні квести вже є
            generatePromotionQuests()
            return
        }

        // БЛОК 1: ГЕНЕРАЦІЯ DAILY ТА MAIN
        val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull()

        if (schedule != null) {
            // ЗАВДАННЯ 1: Виправлення генерації (Завжди ACTIVE)
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

            if (schedule.workoutTemplateName != null) {
                val recommendedExercises = schedule.exercises.map { exercise ->
                    val rec = calculateRecommendation(exercise.id, exercise.name)
                    ExerciseRecommendation(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        weight = rec.weight,
                        sets = rec.sets,
                        reps = rec.reps
                    )
                }

                // ФІКС: Квест створюється як ACTIVE (імплементовано в репозиторії)
                questRepo.createMainQuest(
                    title = schedule.workoutTemplateName.uppercase(),
                    exercises = recommendedExercises,
                    scheduleId = schedule.id
                )
            }
        }

        // БЛОК 2: ГЕНЕРАЦІЯ PROMOTION (НЕЗАЛЕЖНО)
        generatePromotionQuests()
    }

    private suspend fun generatePromotionQuests() {
        val matrixEntries = matrixRepo.getAllEntries().firstOrNull() ?: emptyList()
        val pendingPromotions = matrixEntries.filter { it.isPromotionPending }
        
        pendingPromotions.forEach { pending ->
            val allActiveQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()
            val alreadyExists = allActiveQuests.any { q -> 
                q.type == DomainQuestType.PROMOTION && q.scheduleId == pending.exerciseId 
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
