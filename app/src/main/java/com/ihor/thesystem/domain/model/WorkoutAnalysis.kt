package com.ihor.thesystem.domain.model

data class WorkoutAnalysisData(
    val sessionTimestamp: Long,
    val workoutName: String?,
    val execution: WorkoutExecutionAnalysis,
    val exerciseProgress: List<ExerciseProgressAnalysis>,
    val annualProgress: List<AnnualProgressComparison>,
    val recommendations: List<NextWorkoutRecommendationAnalysis>,
    val motivationLevel: MotivationLevelResult,
    val aiFeedback: String?
)

data class WorkoutExecutionAnalysis(
    val completedSets: Int,
    val plannedSets: Int,
    val completedExercises: Int,
    val skippedExercises: Int
)

data class ExerciseProgressAnalysis(
    val exerciseId: Int,
    val exerciseName: String,
    val previousEstimatedOneRepMax: Double?,
    val currentEstimatedOneRepMax: Double,
    val difference: Double?,
    val status: ExerciseProgressStatus
)

enum class ExerciseProgressStatus {
    Improved,
    Stable,
    Decreased
}

data class AnnualProgressComparison(
    val exerciseId: Int,
    val exerciseName: String,
    val factWeight: Double,
    val plannedWeight: Double?,
    val difference: Double?,
    val status: AnnualProgressStatus
)

enum class AnnualProgressStatus {
    OnPlan,
    BelowPlan,
    AbovePlan,
    NoPlan
}

data class NextWorkoutRecommendationAnalysis(
    val exerciseId: Int,
    val exerciseName: String,
    val recommendedWeight: Double,
    val recommendedReps: String,
    val recommendedSets: Int,
    val reason: String
)
