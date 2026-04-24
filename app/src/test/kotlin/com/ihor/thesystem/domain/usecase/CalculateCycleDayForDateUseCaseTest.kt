package com.ihor.thesystem.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalculateCycleDayForDateUseCaseTest {

    private val useCase = CalculateCycleDayForDateUseCase()
    private val anchorDate = LocalDate.of(2024, 1, 1) // Anchor epoch day
    private val anchorEpochDay = anchorDate.toEpochDay()

    @Test
    fun `anchorDay=1, target=anchorDate returns 1`() {
        val result = useCase(
            targetDate = anchorDate,
            anchorEpochDay = anchorEpochDay,
            anchorCycleDay = 1,
            cycleDaysPerMicrocycle = 4
        )
        assertEquals(1, result)
    }

    @Test
    fun `anchorDay=1, target=anchorDate+1 returns 2`() {
        val result = useCase(
            targetDate = anchorDate.plusDays(1),
            anchorEpochDay = anchorEpochDay,
            anchorCycleDay = 1,
            cycleDaysPerMicrocycle = 4
        )
        assertEquals(2, result)
    }

    @Test
    fun `anchorDay=1, target=anchorDate+4 (full cycle) returns 1`() {
        val result = useCase(
            targetDate = anchorDate.plusDays(4),
            anchorEpochDay = anchorEpochDay,
            anchorCycleDay = 1,
            cycleDaysPerMicrocycle = 4
        )
        assertEquals(1, result)
    }

    @Test
    fun `anchorDay=3, target=anchorDate-1 (past date) returns 2`() {
        // Day before anchor (Day 3) should be Day 2
        val result = useCase(
            targetDate = anchorDate.minusDays(1),
            anchorEpochDay = anchorEpochDay,
            anchorCycleDay = 3,
            cycleDaysPerMicrocycle = 4
        )
        assertEquals(2, result)
    }

    @Test
    fun `cycleDaysPerMicrocycle=3, anchorDay=2, target=anchorDate+2 returns 1`() {
        // Cycle: 2 -> 3 -> 1
        val result = useCase(
            targetDate = anchorDate.plusDays(2),
            anchorEpochDay = anchorEpochDay,
            anchorCycleDay = 2,
            cycleDaysPerMicrocycle = 3
        )
        assertEquals(1, result)
    }
}
