package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ReadinessInput
import com.ihor.thesystem.domain.model.ReadinessLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateReadinessUseCaseTest {

    private val useCase = CalculateReadinessUseCase()

    @Test
    fun `null values keep baseline standard readiness`() {
        val result = useCase(ReadinessInput())

        assertEquals(70, result.score)
        assertEquals(ReadinessLevel.STANDARD, result.level)
        assertTrue(result.reasons.contains("Baseline score: 70"))
    }

    @Test
    fun `good sleep and high energy produce progress readiness`() {
        val result = useCase(
            ReadinessInput(
                sleepHours = 7.5f,
                energy = 5
            )
        )

        assertEquals(90, result.score)
        assertEquals(ReadinessLevel.PROGRESS, result.level)
    }

    @Test
    fun `very poor sleep high stress soreness low energy and motivation clamp to recovery`() {
        val result = useCase(
            ReadinessInput(
                sleepHours = 4.5f,
                energy = 1,
                stress = 5,
                soreness = 5,
                motivation = 1
            )
        )

        assertEquals(0, result.score)
        assertEquals(ReadinessLevel.RECOVERY, result.level)
        assertTrue(result.reasons.any { it == "Sleep < 5h: -15" })
        assertTrue(result.reasons.any { it == "Stress 5/5: -20" })
    }

    @Test
    fun `stress and soreness at four apply moderate penalties`() {
        val result = useCase(
            ReadinessInput(
                sleepHours = 6f,
                stress = 4,
                soreness = 4
            )
        )

        assertEquals(50, result.score)
        assertEquals(ReadinessLevel.REDUCED, result.level)
    }
}
