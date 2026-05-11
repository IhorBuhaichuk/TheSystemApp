package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateEffectiveWeightUseCaseTest {

    private val configRepo: SystemConfigRepository = mockk()

    @Test
    fun `effective weight applies default penalty only when active`() = runTest {
        every { configRepo.getConfigFlow() } returns flowOf(SystemConfig(defaultPenalty = 20))
        val useCase = CalculateEffectiveWeightUseCase(configRepo)

        assertEquals(100.0, useCase(baseWeight = 100.0, isPenaltyActive = false), 0.0)
        assertEquals(80.0, useCase(baseWeight = 100.0, isPenaltyActive = true), 0.0)
    }
}
