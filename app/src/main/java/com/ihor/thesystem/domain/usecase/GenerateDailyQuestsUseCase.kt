package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

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
        
        // 1. ПАРАЛЕЛЬНА ГЕНЕРАЦІЯ DAILY ТА MAIN КВЕСТІВ
        val activeDaily = questRepo.getActiveDailyQuest().firstOrNull()
        val activeMain = questRepo.getActiveMainQuest().firstOrNull()
        val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull() ?: return

        if (activeDaily == null) {
            questRepo.createDailyQuest(
                title = "РУТИНА | ДЕНЬ $cycleDay",
                tasks = emptyList(), 
                scheduleId = schedule.id
            )
        }

        if (activeMain == null && schedule.workoutTemplateName != null) {
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

            questRepo.createMainQuest(
                title = schedule.workoutTemplateName.uppercase(),
                exercises = recommendedExercises,
                scheduleId = schedule.id
            )
        }

        // 2. ГЕНЕРАЦІЯ PROMOTION КВЕСТІВ (БЕЗ БЛОКУВАННЯ)
        val matrixEntries = matrixRepo.getAllEntries().firstOrNull() ?: emptyList()
        val pendingPromotions = matrixEntries.filter { it.isPromotionPending }
        
        pendingPromotions.forEach { pending ->
            val promotionQuests = questRepo.getActiveQuests().firstOrNull()?.filter { it.type == DomainQuestType.PROMOTION } ?: emptyList()
            val alreadyExists = promotionQuests.any { q -> q.tasks.any { t -> t.exerciseId == pending.exerciseId } }
            
            if (!alreadyExists) {
                questRepo.createPromotionQuest(
                    exerciseId = pending.exerciseId,
                    title = "ЕКЗАМЕН: ${pending.exerciseName.uppercase()}",
                    description = "Встанови новий максимум або підтверди статус для підвищення рангу до ${Rank.fromValue(pending.currentRank.value + 1).name}"
                )
            }
        }
    }
}
