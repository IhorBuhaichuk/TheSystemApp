package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseRecommendation
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.model.BossFight
import com.ihor.thesystem.domain.model.BossFightTargetMetric
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.SystemWorkoutTemplateType
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.toBossFight
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.EquipmentProfileRepository
import com.ihor.thesystem.domain.util.AppClock
import java.time.Instant
import javax.inject.Inject
import kotlin.math.abs
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
    private val equipmentProfileRepository: EquipmentProfileRepository,
    private val findExerciseSubstitutions: FindExerciseSubstitutionsUseCase,
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
        val systemTemplateType = todayDecision.systemTemplateType()
        val questTitle = systemTemplateType?.questTitle ?: schedule.workoutTemplateName?.uppercase()
        val recommendations = when (systemTemplateType) {
            SystemWorkoutTemplateType.NO_EXCUSE -> noExcuseProtocolRecommendations()
            SystemWorkoutTemplateType.ACTIVE_RECOVERY -> activeRecoveryProtocolRecommendations()
            SystemWorkoutTemplateType.DELOAD -> schedule.exercises
                .takeIf { it.isNotEmpty() }
                ?.resolveAvailableExercises()
                ?.takeIf { it.isNotEmpty() }
                ?.toAdjustedRecommendations(todayDecision)
                ?: activeRecoveryProtocolRecommendations()
            null -> {
                if (schedule.isWorkoutDay && schedule.workoutTemplateName != null && schedule.exercises.isNotEmpty()) {
                    schedule.exercises.resolveAvailableExercises().toAdjustedRecommendations(todayDecision)
                } else {
                    emptyList()
                }
            }
        }
        val shouldCreateMainQuest = questTitle != null && recommendations.isNotEmpty() &&
            (schedule.isWorkoutDay || systemTemplateType != null)

        val existingMainQuest = todayQuests.find { it.type == DomainQuestType.MAIN }
        if (existingMainQuest != null) {
            when {
                !shouldCreateMainQuest -> questRepo.deleteQuestWithTasks(existingMainQuest.id)
                existingMainQuest.scheduleId != schedule.id ->
                    questRepo.deleteQuestWithTasks(existingMainQuest.id)
                existingMainQuest.title != questTitle ->
                    questRepo.deleteQuestWithTasks(existingMainQuest.id)
                !existingMainQuest.matchesRecommendations(recommendations) ->
                    questRepo.deleteQuestWithTasks(existingMainQuest.id)
            }
        } else if (!shouldCreateMainQuest) {
            todayQuests
                .filter { it.type == DomainQuestType.MAIN }
                .forEach { questRepo.deleteQuestWithTasks(it.id) }
        }

        val updatedQuests = questRepo.getDailyQuestsForDate(now).first()
        val hasMain = updatedQuests.any { it.type == DomainQuestType.MAIN }

        if (!hasMain && shouldCreateMainQuest) {
            questRepo.createMainQuest(
                title = requireNotNull(questTitle),
                exercises = recommendations,
                scheduleId = schedule.id
            )
        }

        generateBossFightQuests()
    }

    private suspend fun generateBossFightQuests() {
        val matrixEntries = matrixRepo.getAllEntries().firstOrNull() ?: emptyList()
        val active = questRepo.getActiveQuests().firstOrNull() ?: emptyList()
        matrixEntries.filter { it.isPromotionPending }.forEach { pending ->
            if (active.none { it.type == DomainQuestType.PROMOTION && it.targetExerciseId == pending.exerciseId }) {
                val bossFight = pending.toBossFight()
                questRepo.createPromotionQuest(
                    exerciseId = pending.exerciseId,
                    title = bossFight.title,
                    description = bossFight.rulesText,
                    targetWeight = bossFight.targetWeightOrNull(),
                    targetReps = bossFight.targetRepsOrNull(),
                    exerciseNameUk = null
                )
            }
        }
    }

    private suspend fun List<ExerciseDetails>.toAdjustedRecommendations(
        decision: TodayTrainingDecision
    ): List<ExerciseRecommendation> =
        map { exercise ->
            val trackingMode = ExerciseTrackingModeResolver.resolve(exercise)
            val baseRecommendation = calculateRecommendation(exercise.id, exercise.name)
            val adjustedRecommendation = adjustRecommendation(
                recommendation = baseRecommendation,
                trackingMode = trackingMode,
                decision = decision
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

    private suspend fun List<ExerciseDetails>.resolveAvailableExercises(): List<ExerciseDetails> {
        val profile = equipmentProfileRepository.getProfileSnapshot()
        val usedExerciseIds = mutableSetOf<Int>()

        return mapNotNull { exercise ->
            val resolved = if (profile.allows(exercise)) {
                exercise
            } else {
                findExerciseSubstitutions(exercise.id).firstOrNull { substitution ->
                    substitution.id !in usedExerciseIds && profile.allows(substitution)
                }
            }

            resolved?.also { usedExerciseIds += it.id }
        }
    }

    private fun TodayTrainingDecision.systemTemplateType(): SystemWorkoutTemplateType? =
        when (decisionType) {
            TodayTrainingDecisionType.NO_EXCUSE -> SystemWorkoutTemplateType.NO_EXCUSE
            TodayTrainingDecisionType.ACTIVE_RECOVERY -> SystemWorkoutTemplateType.ACTIVE_RECOVERY
            TodayTrainingDecisionType.DELOAD -> SystemWorkoutTemplateType.DELOAD
            else -> null
        }

    private fun noExcuseProtocolRecommendations(): List<ExerciseRecommendation> =
        listOf(
            ExerciseRecommendation(
                exerciseId = SYSTEM_NO_EXCUSE_PUSH_UP_ID,
                exerciseName = "Knee Push-up",
                exerciseNameUk = "Knee Push-up",
                weight = BODYWEIGHT_TARGET,
                sets = 1,
                reps = 8
            ),
            ExerciseRecommendation(
                exerciseId = SYSTEM_NO_EXCUSE_SQUAT_ID,
                exerciseName = "Bodyweight Squat",
                exerciseNameUk = "Bodyweight Squat",
                weight = BODYWEIGHT_TARGET,
                sets = 1,
                reps = 12
            ),
            ExerciseRecommendation(
                exerciseId = SYSTEM_NO_EXCUSE_PLANK_ID,
                exerciseName = "Plank Hold",
                exerciseNameUk = "Plank Hold",
                weight = BODYWEIGHT_TARGET,
                sets = 1,
                reps = 30
            ),
            ExerciseRecommendation(
                exerciseId = SYSTEM_NO_EXCUSE_MOBILITY_ID,
                exerciseName = "Mobility Hold",
                exerciseNameUk = "Mobility Hold",
                weight = BODYWEIGHT_TARGET,
                sets = 1,
                reps = 60
            )
        )

    private fun activeRecoveryProtocolRecommendations(): List<ExerciseRecommendation> =
        listOf(
            ExerciseRecommendation(
                exerciseId = SYSTEM_RECOVERY_WALK_ID,
                exerciseName = "Walking",
                exerciseNameUk = "Walking",
                weight = BODYWEIGHT_TARGET,
                sets = 1,
                reps = 420
            ),
            ExerciseRecommendation(
                exerciseId = SYSTEM_RECOVERY_MOBILITY_ID,
                exerciseName = "Mobility Hold",
                exerciseNameUk = "Mobility Hold",
                weight = BODYWEIGHT_TARGET,
                sets = 2,
                reps = 45
            ),
            ExerciseRecommendation(
                exerciseId = SYSTEM_RECOVERY_CORE_ID,
                exerciseName = "Light Core Hold",
                exerciseNameUk = "Light Core Hold",
                weight = BODYWEIGHT_TARGET,
                sets = 1,
                reps = 30
            )
        )

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

    private fun BossFight.targetWeightOrNull(): Double? =
        targetValue.takeIf { targetMetric == BossFightTargetMetric.WEIGHT }

    private fun BossFight.targetRepsOrNull(): Int? =
        targetValue.toInt().takeIf {
            targetMetric == BossFightTargetMetric.REPS ||
                targetMetric == BossFightTargetMetric.TIME_SECONDS
        }

    private companion object {
        const val TARGET_EPSILON = 0.001
        const val BODYWEIGHT_TARGET = 0.0
        const val SYSTEM_NO_EXCUSE_PUSH_UP_ID = -10_001
        const val SYSTEM_NO_EXCUSE_SQUAT_ID = -10_002
        const val SYSTEM_NO_EXCUSE_PLANK_ID = -10_003
        const val SYSTEM_NO_EXCUSE_MOBILITY_ID = -10_004
        const val SYSTEM_RECOVERY_WALK_ID = -20_001
        const val SYSTEM_RECOVERY_MOBILITY_ID = -20_002
        const val SYSTEM_RECOVERY_CORE_ID = -20_003
    }
}
