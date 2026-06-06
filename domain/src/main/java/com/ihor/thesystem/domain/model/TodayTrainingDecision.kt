package com.ihor.thesystem.domain.model

enum class TodayTrainingDecisionType {
    PROGRESS_ALLOWED,
    STANDARD_TRAINING,
    REDUCED_LOAD,
    ACTIVE_RECOVERY,
    NO_EXCUSE,
    DELOAD,
    REST
}

data class TodayTrainingDecision(
    val dateEpochDay: Long,
    val cycleDay: Int,
    val workoutName: String?,
    val readinessScore: Int,
    val readinessLevel: ReadinessLevel,
    val recoveryDebt: RecoveryDebt,
    val decisionType: TodayTrainingDecisionType,
    val loadMultiplier: Float,
    val volumeMultiplier: Float,
    val reason: String,
    val warnings: List<String>,
    val selectedWorkoutTemplateId: Int?,
    val isTrainingAllowed: Boolean
)
