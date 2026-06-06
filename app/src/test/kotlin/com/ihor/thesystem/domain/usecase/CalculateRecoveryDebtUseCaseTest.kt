package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ReadinessInput
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.ReadinessScore
import com.ihor.thesystem.domain.model.RecoveryDebtInput
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.RecoveryDebtWorkout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateRecoveryDebtUseCaseTest {

    private val useCase = CalculateRecoveryDebtUseCase()

    @Test
    fun `empty input has low zero debt`() {
        val result = useCase(RecoveryDebtInput())

        assertEquals(0, result.value)
        assertEquals(RecoveryDebtLevel.LOW, result.level)
        assertTrue(result.reasons.isEmpty())
    }

    @Test
    fun `recent tonnage and completed workout density increase debt`() {
        val result = useCase(
            RecoveryDebtInput(
                recentWorkouts = listOf(
                    RecoveryDebtWorkout(dateEpochDay = 10, tonnage = 10_000.0),
                    RecoveryDebtWorkout(dateEpochDay = 12, tonnage = 10_000.0)
                ),
                referenceEpochDay = 12
            )
        )

        assertEquals(38, result.value)
        assertEquals(RecoveryDebtLevel.MODERATE, result.level)
        assertTrue(result.reasons.any { it.startsWith("Recent tonnage") })
    }

    @Test
    fun `missed planned workouts are counted against completed workout days`() {
        val result = useCase(
            RecoveryDebtInput(
                recentWorkouts = listOf(
                    RecoveryDebtWorkout(dateEpochDay = 100, tonnage = 2_000.0)
                ),
                plannedWorkoutEpochDays = listOf(100, 102, 104),
                referenceEpochDay = 104
            )
        )

        assertEquals(32, result.value)
        assertEquals(RecoveryDebtLevel.MODERATE, result.level)
        assertTrue(result.reasons.any { it == "Missed planned workouts 2: +24" })
    }

    @Test
    fun `low readiness high stress and soreness can push debt to critical and clamp at one hundred`() {
        val result = useCase(
            RecoveryDebtInput(
                recentWorkouts = listOf(
                    RecoveryDebtWorkout(dateEpochDay = 1, tonnage = 12_000.0),
                    RecoveryDebtWorkout(dateEpochDay = 2, tonnage = 12_000.0),
                    RecoveryDebtWorkout(dateEpochDay = 3, tonnage = 12_000.0),
                    RecoveryDebtWorkout(dateEpochDay = 4, tonnage = 12_000.0)
                ),
                plannedWorkoutEpochDays = listOf(1, 2, 3, 4, 5, 6, 7),
                readiness = ReadinessScore(
                    score = 30,
                    level = ReadinessLevel.RECOVERY,
                    reasons = emptyList()
                ),
                readinessInput = ReadinessInput(
                    stress = 5,
                    soreness = 5
                ),
                referenceEpochDay = 7
            )
        )

        assertEquals(100, result.value)
        assertEquals(RecoveryDebtLevel.CRITICAL, result.status)
        assertTrue(result.reasons.any { it == "Readiness score 30: +25" })
        assertTrue(result.reasons.any { it == "Stress 5/5: +15" })
        assertTrue(result.reasons.any { it == "Soreness 5/5: +15" })
    }

    @Test
    fun `negative tonnage does not create load debt`() {
        val result = useCase(
            RecoveryDebtInput(
                recentWorkouts = listOf(
                    RecoveryDebtWorkout(dateEpochDay = 1, tonnage = -2_000.0)
                ),
                referenceEpochDay = 1
            )
        )

        assertEquals(4, result.value)
        assertEquals(RecoveryDebtLevel.LOW, result.level)
        assertTrue(result.reasons.none { it.startsWith("Recent tonnage") })
    }
}
