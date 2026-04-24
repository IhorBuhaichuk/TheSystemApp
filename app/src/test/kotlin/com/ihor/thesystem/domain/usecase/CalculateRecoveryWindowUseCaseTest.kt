package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours

class CalculateRecoveryWindowUseCaseTest {

    private val useCase = CalculateRecoveryWindowUseCase()

    @Test
    fun `tonnage=0, nightShift=false returns 24 hours`() {
        val result = useCase(tonnage = 0.0, isNightShift = false)
        assertEquals(24.hours, (result as Result.Success).data)
    }

    @Test
    fun `tonnage=0, nightShift=true returns 36 hours`() {
        val result = useCase(tonnage = 0.0, isNightShift = true)
        assertEquals(36.hours, (result as Result.Success).data)
    }

    @Test
    fun `tonnage=5000, nightShift=false returns 34 hours`() {
        // 24 + (5000 / 1000 * 2) = 24 + 10 = 34
        val result = useCase(tonnage = 5000.0, isNightShift = false)
        assertEquals(34.hours, (result as Result.Success).data)
    }

    @Test
    fun `tonnage=100000, nightShift=false returns 72 hours`() {
        // 24 + (100000 / 1000 * 2) = 24 + 200 = 224
        // Capped at 72
        val result = useCase(tonnage = 100000.0, isNightShift = false)
        assertEquals(72.hours, (result as Result.Success).data)
    }
}
