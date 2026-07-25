package com.ihor.thesystem.domain.model

data class HealthSignals(
    val sleepDurationMinutes: Int? = null,
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
    SLEEP;

    companion object {
        val ReadinessDefaults: Set<HealthSignalPermission> = setOf(SLEEP)
        val All: Set<HealthSignalPermission> = entries.toSet()
    }
}

data class HealthPermissionRequest(
    val permissions: Set<HealthSignalPermission> = HealthSignalPermission.ReadinessDefaults
)
