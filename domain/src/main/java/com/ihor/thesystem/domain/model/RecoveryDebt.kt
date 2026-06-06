package com.ihor.thesystem.domain.model

enum class RecoveryDebtLevel {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}

data class RecoveryDebt(
    val value: Int,
    val level: RecoveryDebtLevel,
    val reasons: List<String>
) {
    val status: RecoveryDebtLevel
        get() = level
}

data class RecoveryDebtWorkout(
    val dateEpochDay: Long,
    val tonnage: Double,
    val completed: Boolean = true
)

data class RecoveryDebtInput(
    val recentWorkouts: List<RecoveryDebtWorkout> = emptyList(),
    val plannedWorkoutEpochDays: List<Long> = emptyList(),
    val readiness: ReadinessScore? = null,
    val readinessInput: ReadinessInput? = null,
    val referenceEpochDay: Long? = null
)
