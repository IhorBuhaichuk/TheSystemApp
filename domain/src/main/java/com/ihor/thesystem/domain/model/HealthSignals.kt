package com.ihor.thesystem.domain.model

data class HealthSignals(
    val sleepDurationMinutes: Int? = null,
    val stepsToday: Int? = null,
    val restingHeartRate: Int? = null,
    val workoutSessions: Int? = null,
    val sourceFreshness: HealthSignalsFreshness = HealthSignalsFreshness.UNAVAILABLE
) {
    companion object {
        val Unavailable = HealthSignals()
    }
}

enum class HealthSignalsFreshness {
    TODAY,
    STALE,
    UNAVAILABLE
}

enum class HealthSignalPermission {
    SLEEP,
    STEPS,
    HEART_RATE,
    EXERCISE_SESSIONS;

    companion object {
        val ReadinessDefaults: Set<HealthSignalPermission> = setOf(SLEEP)
        val WorkoutContext: Set<HealthSignalPermission> = setOf(STEPS, HEART_RATE, EXERCISE_SESSIONS)
        val All: Set<HealthSignalPermission> = entries.toSet()
    }
}

data class HealthPermissionRequest(
    val permissions: Set<HealthSignalPermission> = HealthSignalPermission.ReadinessDefaults
)
