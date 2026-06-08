package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.HealthPermissionRequest
import com.ihor.thesystem.domain.model.HealthSignalPermission
import com.ihor.thesystem.domain.model.HealthSignals
import com.ihor.thesystem.domain.model.HealthSignalsFreshness
import com.ihor.thesystem.domain.model.ReadinessInput
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.repository.HealthSignalsRepository
import kotlinx.coroutines.test.runTest
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

    @Test
    fun `fresh health sleep improves readiness when manual sleep is missing`() {
        val result = useCase(
            input = ReadinessInput(),
            healthSignals = HealthSignals(
                sleepDurationMinutes = 8 * 60,
                sourceFreshness = HealthSignalsFreshness.TODAY
            )
        )

        assertEquals(80, result.score)
        assertEquals(ReadinessLevel.STANDARD, result.level)
        assertTrue(result.reasons.any { it == "Health sleep >= 7h: +10" })
    }

    @Test
    fun `fresh health sleep lowers readiness when sleep is very poor`() {
        val result = useCase(
            input = ReadinessInput(),
            healthSignals = HealthSignals(
                sleepDurationMinutes = 4 * 60,
                sourceFreshness = HealthSignalsFreshness.TODAY
            )
        )

        assertEquals(55, result.score)
        assertEquals(ReadinessLevel.REDUCED, result.level)
        assertTrue(result.reasons.any { it == "Health sleep < 5h: -15" })
    }

    @Test
    fun `unavailable health repository falls back to manual readiness`() = runTest {
        val repository = UnavailableHealthSignalsRepository
        val signals = if (repository.isAvailable() && repository.hasPermissions()) {
            repository.getTodaySignals()
        } else {
            null
        }

        val result = useCase(ReadinessInput(), signals)

        assertEquals(70, result.score)
        assertEquals(ReadinessLevel.STANDARD, result.level)
    }

    private object UnavailableHealthSignalsRepository : HealthSignalsRepository {
        override suspend fun isAvailable(): Boolean = false

        override suspend fun hasPermissions(required: Set<HealthSignalPermission>): Boolean = false

        override fun requestPermissions(required: Set<HealthSignalPermission>): HealthPermissionRequest =
            HealthPermissionRequest(required)

        override suspend fun getTodaySignals(): HealthSignals =
            HealthSignals(
                sleepDurationMinutes = 2 * 60,
                sourceFreshness = HealthSignalsFreshness.TODAY
            )

        override suspend fun getRecentSignals(days: Int): List<HealthSignals> = emptyList()
    }
}
