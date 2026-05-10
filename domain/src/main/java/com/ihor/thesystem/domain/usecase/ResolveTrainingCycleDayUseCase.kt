package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.model.SystemConfig
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class ResolveTrainingCycleDayUseCase @Inject constructor(
    private val calculateCycleDay: CalculateCycleDayForDateUseCase,
    private val clock: AppClock
) {
    operator fun invoke(
        targetDate: LocalDate,
        config: SystemConfig,
        fallbackCurrentCycleDay: Int? = null
    ): Int {
        val cycleLength = config.cycleDaysPerMicrocycle.coerceAtLeast(MIN_CYCLE_LENGTH)
        val hasStoredAnchor = config.cycleAnchorDateTimestamp > 0L
        val anchorEpochDay = if (hasStoredAnchor) {
            config.cycleAnchorDateTimestamp
        } else {
            today().toEpochDay()
        }
        val anchorCycleDay = normalizeCycleDay(
            if (hasStoredAnchor) config.cycleAnchorDay else fallbackCurrentCycleDay ?: config.cycleAnchorDay,
            cycleLength
        )

        return calculateCycleDay(
            targetDate = targetDate,
            anchorEpochDay = anchorEpochDay,
            anchorCycleDay = anchorCycleDay,
            cycleDaysPerMicrocycle = cycleLength
        )
    }

    private fun normalizeCycleDay(day: Int, cycleLength: Int): Int {
        val zeroBased = ((day - 1) % cycleLength + cycleLength) % cycleLength
        return zeroBased + 1
    }

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()

    private companion object {
        const val MIN_CYCLE_LENGTH = 1
    }
}
