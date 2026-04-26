package com.ihor.thesystem.domain.model

data class SystemConfig(
    val id: Int = 1,
    val defaultPenalty: Int = 20,
    val targetSets: Int = 3,
    val targetReps: Int = 12,
    val matrixWeeks: Int = 48,
    val cycleAnchorDateTimestamp: Long = 0L, // Epoch Day
    val cycleAnchorDay: Int = 1,              // Який це був день циклу (1..4)
    val cycleDaysPerMicrocycle: Int = 4,
    val microCyclesPerMonth: Int = 4,
    val needsDailyInit: Boolean = false
)

object CycleConfig {
    val MICROCYCLE_DAYS = listOf(1, 2, 3, 4)
    val MICROCYCLE_DAY_RANGE = 1..4
}
