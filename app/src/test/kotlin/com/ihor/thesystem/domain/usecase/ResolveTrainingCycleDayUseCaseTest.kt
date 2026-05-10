package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.model.SystemConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ResolveTrainingCycleDayUseCaseTest {

    private val today = LocalDate.of(2024, 1, 10)
    private val clock = FakeClock(today)
    private val useCase = ResolveTrainingCycleDayUseCase(
        calculateCycleDay = CalculateCycleDayForDateUseCase(),
        clock = clock
    )

    @Test
    fun `stored anchor is source of truth when configured`() {
        val config = SystemConfig(
            cycleAnchorDateTimestamp = LocalDate.of(2024, 1, 1).toEpochDay(),
            cycleAnchorDay = 2,
            cycleDaysPerMicrocycle = 4
        )

        val result = useCase(
            targetDate = LocalDate.of(2024, 1, 2),
            config = config,
            fallbackCurrentCycleDay = 4
        )

        assertEquals(3, result)
    }

    @Test
    fun `missing anchor uses today as fallback anchor with current player cycle day`() {
        val config = SystemConfig(
            cycleAnchorDateTimestamp = 0L,
            cycleAnchorDay = 1,
            cycleDaysPerMicrocycle = 4
        )

        assertEquals(3, useCase(today, config, fallbackCurrentCycleDay = 3))
        assertEquals(4, useCase(today.plusDays(1), config, fallbackCurrentCycleDay = 3))
        assertEquals(1, useCase(today.plusDays(2), config, fallbackCurrentCycleDay = 3))
    }

    @Test
    fun `fallback cycle day is normalized into configured cycle length`() {
        val config = SystemConfig(
            cycleAnchorDateTimestamp = 0L,
            cycleAnchorDay = 1,
            cycleDaysPerMicrocycle = 4
        )

        assertEquals(2, useCase(today, config, fallbackCurrentCycleDay = 6))
    }

    private class FakeClock(private val today: LocalDate) : AppClock {
        private val zone = ZoneId.of("UTC")

        override fun now(): Long =
            today.atStartOfDay(zone).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = zone
    }
}
