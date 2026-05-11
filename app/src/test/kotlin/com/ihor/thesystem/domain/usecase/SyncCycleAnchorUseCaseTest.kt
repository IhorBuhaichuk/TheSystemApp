package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SyncCycleAnchorUseCaseTest {

    private val configRepo: SystemConfigRepository = mockk()
    private val playerRepo: PlayerRepository = mockk()
    private val questRepo: QuestRepository = mockk()
    private val generateQuests: GenerateDailyQuestsUseCase = mockk()

    @Test
    fun `cycle anchor uses injected AppClock date`() = runTest {
        val configSlot = slot<SystemConfig>()
        every { configRepo.getConfigFlow() } returns flowOf(SystemConfig())
        coEvery { configRepo.updateConfig(capture(configSlot)) } just runs
        coEvery { playerRepo.updateCurrentCycleDay(3) } returns Result.Success(Unit)
        coEvery { generateQuests() } just runs

        SyncCycleAnchorUseCase(
            configRepo = configRepo,
            playerRepo = playerRepo,
            questRepo = questRepo,
            generateQuests = generateQuests,
            clock = FixedClock(LocalDate.of(2030, 1, 2))
        )(selectedDay = 3)

        assertEquals(LocalDate.of(2030, 1, 2).toEpochDay(), configSlot.captured.cycleAnchorDateTimestamp)
        assertEquals(3, configSlot.captured.cycleAnchorDay)
        coVerify { generateQuests() }
    }

    private class FixedClock(private val today: LocalDate) : AppClock {
        override fun now(): Long =
            today.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = TEST_ZONE
    }

    private companion object {
        val TEST_ZONE: ZoneId = ZoneId.of("UTC")
    }
}
