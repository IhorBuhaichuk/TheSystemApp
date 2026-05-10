package com.ihor.thesystem.domain.model

import java.time.LocalDate

data class AnnualProgressionExerciseSnapshot(
    val exercise: ExerciseDetails,
    val currentWorkingWeight: Double?,
    val reps: Int?,
    val lastTrainingTimestamp: Long?,
    val estimatedOneRepMax: Double?,
    val defaultTargetWeight: Double?,
    val inventoryStep: Double
)

data class AnnualProgressionPlanInput(
    val exerciseId: Int,
    val exerciseName: String,
    val currentWeight: Double,
    val targetWeight: Double,
    val inventoryStep: Double
)

data class AnnualProgressionPlan(
    val startDate: LocalDate,
    val adaptationEndsOn: LocalDate,
    val exercises: List<AnnualProgressionExercisePlan>
)

data class AnnualProgressionExercisePlan(
    val exerciseId: Int,
    val exerciseName: String,
    val currentWeight: Double,
    val targetWeight: Double,
    val inventoryStep: Double,
    val idealMonthlyStep: Double,
    val monthlyTargets: List<AnnualProgressionMonthlyTarget>,
    val status: AnnualProgressionPlanStatus
)

data class AnnualProgressionMonthlyTarget(
    val monthIndex: Int,
    val weight: Double,
    val adjustment: AnnualProgressionAdjustment
)

enum class AnnualProgressionAdjustment {
    Baseline,
    StandardStep,
    Plateau,
    ForcedJump
}

enum class AnnualProgressionPlanStatus {
    Ready,
    NeedsCurrentWeight,
    NeedsTargetWeight
}
