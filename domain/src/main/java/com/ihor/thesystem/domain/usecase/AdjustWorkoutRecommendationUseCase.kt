package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AdjustedExerciseRecommendation
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import kotlin.math.roundToInt
import javax.inject.Inject

class AdjustWorkoutRecommendationUseCase @Inject constructor() {

    operator fun invoke(
        recommendation: SetRecommendation,
        trackingMode: ExerciseTrackingMode,
        decision: TodayTrainingDecision
    ): AdjustedExerciseRecommendation {
        val baseWeight = recommendation.weight.coerceAtLeast(0.0)
        val baseSets = recommendation.sets.coerceAtLeast(1)
        val baseReps = recommendation.reps.coerceAtLeast(1)

        val adjusted = when (decision.decisionType) {
            TodayTrainingDecisionType.PROGRESS_ALLOWED ->
                Adjustment(
                    weight = baseWeight,
                    sets = baseSets,
                    reps = baseReps,
                    reason = if (recommendation.isProgression) {
                        "Progression kept; base recommendation is already capped by matrix target."
                    } else {
                        "Progression allowed; no extra load added without a matrix-capped progression."
                    }
                )
            TodayTrainingDecisionType.STANDARD_TRAINING ->
                Adjustment(
                    weight = baseWeight,
                    sets = baseSets,
                    reps = baseReps,
                    reason = "Standard training: recommendation unchanged."
                )
            TodayTrainingDecisionType.REDUCED_LOAD ->
                Adjustment(
                    weight = adjustedLoad(baseWeight, trackingMode, decision.loadMultiplier),
                    sets = adjustedSets(baseSets, decision.volumeMultiplier, minSets = 2),
                    reps = baseReps,
                    reason = "Reduced load from today's readiness and recovery state."
                )
            TodayTrainingDecisionType.ACTIVE_RECOVERY ->
                Adjustment(
                    weight = adjustedLoad(baseWeight, trackingMode, ACTIVE_RECOVERY_LOAD_MULTIPLIER),
                    sets = 1,
                    reps = adjustedReps(baseReps, trackingMode, ACTIVE_RECOVERY_VOLUME_MULTIPLIER),
                    reason = "Active recovery: heavy work removed or strongly reduced."
                )
            TodayTrainingDecisionType.NO_EXCUSE ->
                Adjustment(
                    weight = adjustedLoad(baseWeight, trackingMode, NO_EXCUSE_LOAD_MULTIPLIER),
                    sets = baseSets.coerceIn(1, 2),
                    reps = baseReps,
                    reason = "No-excuse mode: short simple work block."
                )
            TodayTrainingDecisionType.DELOAD ->
                Adjustment(
                    weight = adjustedLoad(baseWeight, trackingMode, decision.loadMultiplier.coerceAtMost(0.8f)),
                    sets = adjustedSets(baseSets, decision.volumeMultiplier.coerceAtMost(0.7f), minSets = 2),
                    reps = baseReps,
                    reason = "Deload: load and volume reduced to manage recovery debt."
                )
            TodayTrainingDecisionType.REST ->
                Adjustment(
                    weight = 0.0,
                    sets = 0,
                    reps = 0,
                    reason = "Rest day: no workout recommendation."
                )
        }

        return AdjustedExerciseRecommendation(
            exerciseId = recommendation.exerciseId,
            baseWeight = baseWeight,
            adjustedWeight = adjusted.weight,
            baseSets = baseSets,
            adjustedSets = adjusted.sets,
            baseReps = baseReps,
            adjustedReps = adjusted.reps,
            adjustmentReason = adjusted.reason
        )
    }

    private fun adjustedLoad(
        weight: Double,
        trackingMode: ExerciseTrackingMode,
        multiplier: Float
    ): Double =
        if (trackingMode.usesWeightInput) {
            roundToNearestLoadStep(weight * multiplier)
        } else {
            weight
        }

    private fun adjustedSets(baseSets: Int, multiplier: Float, minSets: Int): Int =
        (baseSets * multiplier)
            .roundToInt()
            .coerceAtLeast(minSets.coerceAtLeast(1))
            .coerceAtMost(baseSets)

    private fun adjustedReps(
        baseReps: Int,
        trackingMode: ExerciseTrackingMode,
        multiplier: Float
    ): Int =
        if (trackingMode.usesTimeInput) {
            (baseReps * multiplier).roundToInt().coerceAtLeast(MIN_ACTIVE_RECOVERY_DURATION_SECONDS)
        } else {
            (baseReps * multiplier).roundToInt().coerceAtLeast(MIN_ACTIVE_RECOVERY_REPS)
        }

    private fun roundToNearestLoadStep(value: Double): Double =
        ((value / LOAD_STEP_KG).roundToInt() * LOAD_STEP_KG).coerceAtLeast(0.0)

    private data class Adjustment(
        val weight: Double,
        val sets: Int,
        val reps: Int,
        val reason: String
    )

    private companion object {
        const val LOAD_STEP_KG = 2.5
        const val ACTIVE_RECOVERY_LOAD_MULTIPLIER = 0.5f
        const val ACTIVE_RECOVERY_VOLUME_MULTIPLIER = 0.5f
        const val NO_EXCUSE_LOAD_MULTIPLIER = 0.9f
        const val MIN_ACTIVE_RECOVERY_REPS = 5
        const val MIN_ACTIVE_RECOVERY_DURATION_SECONDS = 20
    }
}
