package com.ihor.thesystem.feature.mode.viewmodel

import app.cash.turbine.test
import com.ihor.thesystem.core.util.AppLogger
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModeViewModelTest {

    private val playerRepo: PlayerRepository = mockk()
    private val questRepo: QuestRepository = mockk()
    private val scheduleRepo: ScheduleRepository = mockk()
    private val configRepo: SystemConfigRepository = mockk()
    private val debuffRepo: DebuffRepository = mockk()
    private val generateQuests: GenerateDailyQuestsUseCase = mockk()
    private val finalizeDayTransaction: FinalizeDayTransactionUseCase = mockk()
    private val logger: AppLogger = mockk(relaxed = true)

    private lateinit var viewModel: ModeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Default mocks to avoid initialization crashes
        val mockPlayer = Player(
            id = 1, name = "Test", level = 1, playerClass = "Novice",
            height = 180f, currentMonth = 1, currentWeek = 1, currentCycleDay = 1,
            consecutiveMainQuestFailures = 0, isPenaltyActive = false
        )
        
        every { playerRepo.getPlayer() } returns flowOf(mockPlayer)
        every { scheduleRepo.getSchedulesForDays(any()) } returns flowOf(emptyList())
        every { questRepo.getActiveDailyQuest() } returns flowOf(null)
        every { questRepo.getActiveMainQuest() } returns flowOf(null)
        every { debuffRepo.getDebuffsForCycleDay(any()) } returns flowOf(emptyList())
        coEvery { generateQuests() } returns Result.Success(Unit)

        viewModel = ModeViewModel(
            playerRepo, questRepo, scheduleRepo, configRepo, debuffRepo,
            generateQuests, finalizeDayTransaction, logger
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onConfirmAdvance triggers LevelUp event when transaction succeeds with LevelUp`() = runTest {
        // GIVEN
        coEvery { finalizeDayTransaction(forceComplete = false) } returns Result.Success(DayFinalizationResult.LevelUp)

        // WHEN
        viewModel.events.test {
            viewModel.onConfirmAdvance()
            
            // THEN
            assertEquals(ModeEvent.LevelUp, awaitItem())
        }
    }

    @Test
    fun `initialization calls generateQuests`() = runTest {
        advanceUntilIdle()
        coVerify { generateQuests() }
    }
}
