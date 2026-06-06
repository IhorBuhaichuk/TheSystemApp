package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseRecommendation
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.util.AppClock
import java.time.Instant
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.round
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class GenerateDailyQuestsUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val configRepo: SystemConfigRepository,
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val calculateRecommendation: CalculateRecommendedSetUseCase,
    private val adjustRecommendation: AdjustWorkoutRecommendationUseCase,
    private val decideTodayWorkout: DecideTodayWorkoutUseCase,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val clock: AppClock
) {
    suspend operator fun invoke() {
        val config = configRepo.getConfigFlow().firstOrNull() ?: return
        val now = clock.now()
        val todayDate = Instant.ofEpochMilli(now)
            .atZone(clock.zoneId())
            .toLocalDate()

        val currentDay = resolveTrainingCycleDay(
            targetDate = todayDate,
            config = config,
            fallbackCurrentCycleDay = playerRepo.getPlayer().firstOrNull()?.currentCycleDay
        )
        val schedule = scheduleRepo.getScheduleForDay(currentDay)
            .firstOrNull() ?: return

        val todayQuests = questRepo.getDailyQuestsForDate(now).first()
        val todayDecision = decideTodayWorkout(todayDate)
        val workoutTemplateName = schedule.workoutTemplateName
        val recommendations = if (schedule.isWorkoutDay && workoutTemplateName != null && schedule.exercises.isNotEmpty()) {
            schedule.exercises
                .selectForDecision(todayDecision)
                .map { exercise ->
                    val trackingMode = ExerciseTrackingModeResolver.resolve(exercise)
                    val baseRecommendation = calculateRecommendation(exercise.id, exercise.name)
                    val adjustedRecommendation = adjustRecommendation(
                        recommendation = baseRecommendation,
                        trackingMode = trackingMode,
                        decision = todayDecision
                    )
                    ExerciseRecommendation(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        exerciseNameUk = exercise.nameUk,
                        weight = adjustedRecommendation.adjustedWeight,
                        sets = adjustedRecommendation.adjustedSets,
                        reps = adjustedRecommendation.adjustedReps
                    )
                }
        } else {
            emptyList()
        }

        val existingMainQuest = todayQuests.find { it.type == DomainQuestType.MAIN }
        if (existingMainQuest != null) {
            when {
                schedule.workoutTemplateId == null ->
                    questRepo.deleteQuestWithTasks(existingMainQuest.id)
                existingMainQuest.scheduleId != schedule.id ->
                    questRepo.deleteQuestWithTasks(existingMainQuest.id)
                !existingMainQuest.matchesRecommendations(recommendations) ->
                    questRepo.deleteQuestWithTasks(existingMainQuest.id)
            }
        } else if (schedule.workoutTemplateId == null) {
            todayQuests
                .filter { it.type == DomainQuestType.MAIN }
                .forEach { questRepo.deleteQuestWithTasks(it.id) }
        }

        val updatedQuests = questRepo.getDailyQuestsForDate(now).first()
        val hasMain = updatedQuests.any { it.type == DomainQuestType.MAIN }

        if (!hasMain && schedule.isWorkoutDay && workoutTemplateName != null && recommendations.isNotEmpty()) {
            questRepo.createMainQuest(
                title = workoutTemplateName.uppercase(),
                exercises = recommendations,
                scheduleId = schedule.id
            )
        }

        generatePromotionQuests()
    }

    private suspend fun generatePromotionQuests() {
        val matrixEntries = matrixRepo.getAllEntries().firstOrNull() ?: emptyList()
        matrixEntries.filter { it.isPromotionPending }.forEach { pending ->
            val active = questRepo.getActiveQuests().firstOrNull() ?: emptyList()
            if (active.none { it.type == DomainQuestType.PROMOTION && it.targetExerciseId == pending.exerciseId }) {
                val examWeight = (round((pending.targetWeight * 1.025f) / 2.5f) * 2.5f).toDouble()
                questRepo.createPromotionQuest(
                    exerciseId = pending.exerciseId,
                    title = "ЕКЗАМЕН: ${(pending.exerciseNameUk ?: pending.exerciseName).uppercase()}",
                    description = "Тест 1RM",
                    targetWeight = examWeight,
                    targetReps = 1,
                    exerciseNameUk = pending.exerciseNameUk
                )
            }
        }
    }

    private fun List<ExerciseDetails>.selectForDecision(decision: TodayTrainingDecision): List<ExerciseDetails> =
        when (decision.decisionType) {
            TodayTrainingDecisionType.NO_EXCUSE -> {
                val simpleExercises = filter { it.hasSimpleEquipment() }
                simpleExercises.ifEmpty { this }.take(NO_EXCUSE_EXERCISE_LIMIT)
            }
            TodayTrainingDecisionType.ACTIVE_RECOVERY -> {
                val lightExercises = filter { !ExerciseTrackingModeResolver.resolve(it).usesWeightInput }
                lightExercises.ifEmpty { this }
            }
            else -> this
        }

    private fun ExerciseDetails.hasSimpleEquipment(): Boolean {
        val normalizedEquipment = equipment.orEmpty().lowercase()
        val normalizedName = listOfNotNull(name, nameUk).joinToString(separator = " ").lowercase()

        if (normalizedEquipment.isBlank()) return true
        if (normalizedEquipment.contains("body") || normalizedEquipment.contains("none")) return true
        if (normalizedName.contains("push") || normalizedName.contains("pull")) return true
        return COMPLEX_EQUIPMENT.none { normalizedEquipment.contains(it) }
    }

    private fun Quest.matchesRecommendations(recommendations: List<ExerciseRecommendation>): Boolean {
        if (tasks.size != recommendations.size) return false

        val tasksByExercise = tasks.mapNotNull { task ->
            task.exerciseId?.let { exerciseId -> exerciseId to task }
        }.toMap()

        return recommendations.all { recommendation ->
            val task = tasksByExercise[recommendation.exerciseId] ?: return@all false
            task.recommendedWeight.isSameTarget(recommendation.weight) &&
                task.recommendedSets == recommendation.sets &&
                task.recommendedReps == recommendation.reps
        }
    }

    private fun Double?.isSameTarget(other: Double): Boolean =
        this != null && abs(this - other) < TARGET_EPSILON

    private companion object {
        const val NO_EXCUSE_EXERCISE_LIMIT = 2
        const val TARGET_EPSILON = 0.001
        val COMPLEX_EQUIPMENT = listOf("barbell", "machine", "cable", "smith")
    }
}
