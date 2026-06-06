package com.ihor.thesystem.domain.model

enum class ReadinessLevel {
    PROGRESS,
    STANDARD,
    REDUCED,
    RECOVERY
}

data class ReadinessInput(
    val sleepHours: Float? = null,
    val energy: Int? = null,
    val stress: Int? = null,
    val soreness: Int? = null,
    val motivation: Int? = null,
    val note: String? = null
) {
    init {
        require(sleepHours == null || sleepHours >= 0f) { "Sleep hours must not be negative." }
        require(energy == null || energy in MIN_RATING..MAX_RATING) { "Energy must be in 1..5." }
        require(stress == null || stress in MIN_RATING..MAX_RATING) { "Stress must be in 1..5." }
        require(soreness == null || soreness in MIN_RATING..MAX_RATING) { "Soreness must be in 1..5." }
        require(motivation == null || motivation in MIN_RATING..MAX_RATING) { "Motivation must be in 1..5." }
    }

    private companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}

data class ReadinessScore(
    val score: Int,
    val level: ReadinessLevel,
    val reasons: List<String>
)

data class ReadinessEntry(
    val id: Long = 0L,
    val dateEpochDay: Long,
    val input: ReadinessInput,
    val score: Int,
    val level: ReadinessLevel,
    val createdAtMillis: Long
)
