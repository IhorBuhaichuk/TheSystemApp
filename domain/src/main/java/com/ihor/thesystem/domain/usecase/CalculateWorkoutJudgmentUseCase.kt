package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.SystemWorkoutGrade
import com.ihor.thesystem.domain.model.SystemWorkoutJudgment
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.WorkoutPerformanceStatus
import com.ihor.thesystem.domain.model.WorkoutProgressionDecision
import javax.inject.Inject
import kotlin.math.roundToInt

class CalculateWorkoutJudgmentUseCase @Inject constructor() {

    operator fun invoke(
        plannedRecommendations: List<SetRecommendation>,
        actualSets: List<ExerciseSet>,
        todayDecision: TodayTrainingDecision? = null
    ): SystemWorkoutJudgment {
        val plannedByExercise = plannedRecommendations
            .filter { it.exerciseId != 0 }
            .associateBy { it.exerciseId }
        val plannedSetCount = plannedRecommendations
            .sumOf { it.sets.coerceAtLeast(0) }
            .takeIf { it > 0 }
            ?: actualSets.count { it.isCompleted }.coerceAtLeast(1)
        val completedSetCount = actualSets.count { it.isCompleted }
        val completionPercent = ((completedSetCount.toDouble() / plannedSetCount.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
        val allPlannedCompleted = completedSetCount >= plannedSetCount && actualSets.none { !it.isCompleted }
        val exceededPlan = completedSetCount > plannedSetCount ||
            actualSets.any { set -> set.isCompleted && set.exceedsPlannedTarget(plannedByExercise[set.exerciseId]) }
        val failedTopSet = actualSets.hasFailedTopSet(plannedByExercise)
        val hardFeedback = actualSets.any { it.userFeedback.isHardFeedback() }
        val painOrStressFeedback = actualSets.any { it.userFeedback.isPainOrStressFeedback() }
        val lowReadiness = todayDecision.isLowReadiness()
        val deloadContext = todayDecision.isDeloadContext()

        val performanceStatus = when {
            completionPercent < 50 || painOrStressFeedback && completionPercent < 70 -> WorkoutPerformanceStatus.FAILED
            !allPlannedCompleted -> WorkoutPerformanceStatus.PARTIAL
            failedTopSet || hardFeedback || lowReadiness -> WorkoutPerformanceStatus.COMPLETED_HARD
            exceededPlan -> WorkoutPerformanceStatus.EXCEEDED
            else -> WorkoutPerformanceStatus.COMPLETED_WITH_RESERVE
        }

        val grade = when {
            performanceStatus == WorkoutPerformanceStatus.EXCEEDED && !lowReadiness -> SystemWorkoutGrade.S
            allPlannedCompleted && !failedTopSet && !painOrStressFeedback && !lowReadiness && !hardFeedback -> SystemWorkoutGrade.S
            allPlannedCompleted -> SystemWorkoutGrade.A
            completionPercent >= 70 && !painOrStressFeedback -> SystemWorkoutGrade.B
            completionPercent >= 50 && !painOrStressFeedback -> SystemWorkoutGrade.C
            else -> SystemWorkoutGrade.D
        }

        val progressionDecision = when {
            deloadContext || lowReadiness && (hardFeedback || failedTopSet || painOrStressFeedback) ->
                WorkoutProgressionDecision.DELOAD_RECOMMENDED
            painOrStressFeedback || completionPercent < 70 ->
                WorkoutProgressionDecision.REDUCE
            failedTopSet || hardFeedback || completionPercent < 100 || lowReadiness ->
                WorkoutProgressionDecision.HOLD
            else ->
                WorkoutProgressionDecision.INCREASE_ALLOWED
        }

        return SystemWorkoutJudgment(
            grade = grade,
            completionPercent = completionPercent,
            performanceStatus = performanceStatus,
            progressionDecision = progressionDecision,
            reason = buildReason(
                grade = grade,
                completionPercent = completionPercent,
                failedTopSet = failedTopSet,
                hardFeedback = hardFeedback,
                painOrStressFeedback = painOrStressFeedback,
                lowReadiness = lowReadiness
            ),
            nextAction = progressionDecision.nextAction()
        )
    }

    private fun ExerciseSet.exceedsPlannedTarget(plan: SetRecommendation?): Boolean {
        if (plan == null) return false
        return weight > plan.weight + LOAD_EPSILON || reps > plan.reps
    }

    private fun List<ExerciseSet>.hasFailedTopSet(
        plannedByExercise: Map<Int, SetRecommendation>
    ): Boolean =
        groupBy { it.exerciseId }.any { (exerciseId, sets) ->
            val plan = plannedByExercise[exerciseId]
            val topLoad = sets.maxOfOrNull { it.weight } ?: return@any false
            val topSets = sets.filter { it.weight.isSameLoadAs(topLoad) }
            topSets.any { !it.isCompleted } ||
                topSets.lastOrNull()?.let { lastTopSet ->
                    val plannedWeight = plan?.weight ?: topLoad
                    val plannedReps = plan?.reps ?: lastTopSet.reps
                    lastTopSet.weight >= plannedWeight * TOP_SET_LOAD_TOLERANCE &&
                        lastTopSet.reps < plannedReps &&
                        !lastTopSet.isCompleted
                } == true
        }

    private fun TodayTrainingDecision?.isLowReadiness(): Boolean =
        this?.let { decision ->
            decision.readinessScore < STANDARD_READINESS_SCORE ||
                decision.readinessLevel == ReadinessLevel.REDUCED ||
                decision.readinessLevel == ReadinessLevel.RECOVERY ||
                decision.decisionType == TodayTrainingDecisionType.REDUCED_LOAD ||
                decision.decisionType == TodayTrainingDecisionType.ACTIVE_RECOVERY
        } ?: false

    private fun TodayTrainingDecision?.isDeloadContext(): Boolean =
        this?.let { decision ->
            decision.decisionType == TodayTrainingDecisionType.DELOAD ||
                decision.recoveryDebt.level == RecoveryDebtLevel.CRITICAL
        } ?: false

    private fun String?.isHardFeedback(): Boolean {
        val value = normalizedFeedback()
        if (value.isBlank()) return false
        return HARD_FEEDBACK_KEYWORDS.any(value::contains) || extractRpe(value)?.let { it >= HARD_RPE } == true
    }

    private fun String?.isPainOrStressFeedback(): Boolean {
        val value = normalizedFeedback()
        if (value.isBlank()) return false
        return PAIN_OR_STRESS_KEYWORDS.any(value::contains)
    }

    private fun String?.normalizedFeedback(): String =
        this.orEmpty().trim().lowercase()

    private fun extractRpe(value: String): Int? {
        val compact = value.replace(" ", "")
        val markerIndex = compact.indexOf("rpe")
        if (markerIndex < 0) return null
        return compact.drop(markerIndex + 3)
            .takeWhile { it.isDigit() }
            .toIntOrNull()
    }

    private fun buildReason(
        grade: SystemWorkoutGrade,
        completionPercent: Int,
        failedTopSet: Boolean,
        hardFeedback: Boolean,
        painOrStressFeedback: Boolean,
        lowReadiness: Boolean
    ): String =
        when {
            painOrStressFeedback ->
                "Є сигнал болю або стресу; система не підвищує навантаження."
            failedTopSet ->
                "Верхні підходи не закрито чисто; прогресію ставимо на паузу."
            lowReadiness && hardFeedback ->
                "Сесія була важкою на низькій готовності; потрібне розвантаження."
            completionPercent >= 100 && grade == SystemWorkoutGrade.S ->
                "План закрито повністю із нормальним запасом."
            completionPercent >= 100 ->
                "План закрито, але система не бачить достатнього запасу для агресивної прогресії."
            completionPercent >= 70 ->
                "Закрито ${completionPercent}% плану; цього достатньо для ритму, але не для підвищення."
            else ->
                "Закрито менше 70% плану; система знижує вимоги на наступний крок."
        }

    private fun WorkoutProgressionDecision.nextAction(): String =
        when (this) {
            WorkoutProgressionDecision.INCREASE_ALLOWED ->
                "Можна обережно підвищити навантаження в межах матриці."
            WorkoutProgressionDecision.HOLD ->
                "Залишити вагу без підвищення і повторити ціль чисто."
            WorkoutProgressionDecision.REDUCE ->
                "Знизити навантаження або обсяг на наступній сесії."
            WorkoutProgressionDecision.DELOAD_RECOMMENDED ->
                "Запланувати легше тренування перед наступним підвищенням навантаження."
        }

    private fun Double.isSameLoadAs(other: Double): Boolean =
        kotlin.math.abs(this - other) < LOAD_EPSILON

    private companion object {
        const val STANDARD_READINESS_SCORE = 65
        const val HARD_RPE = 9
        const val LOAD_EPSILON = 0.001
        const val TOP_SET_LOAD_TOLERANCE = 0.95

        val HARD_FEEDBACK_KEYWORDS = listOf(
            "hard",
            "grind",
            "failure",
            "fail",
            "важко",
            "відмова",
            "до відмови"
        )

        val PAIN_OR_STRESS_KEYWORDS = listOf(
            "pain",
            "injury",
            "stress",
            "бол",
            "біль",
            "травм",
            "стрес"
        )
    }
}
