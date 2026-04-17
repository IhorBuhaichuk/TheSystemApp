package com.ihor.thesystem.feature.mode.viewmodel

import app.cash.turbine.test
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.util.AppLogger
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    private val mockPlayer = Player(
        id = 1, name = "Test", level = 1, playerClass = "Novice",
        height = 180f, currentMonth = 1, currentWeek = 1, currentCycleDay = 1,
        consecutiveMainQuestFailures = 0, isPenaltyActive = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Default mocks
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
    fun `uiState initially maps repository data to Content`() = runTest {
        // Оскільки uiState використовує WhileSubscribed, нам потрібно почати збір
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Content)
            val content = (state as UiState.Content).data
            assertEquals(1, content.currentCycleDay)
            assertEquals(1, content.selectedDay)
        }
    }

    @Test
    fun `onCycleDayTap updates selectedDay in uiState`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem() as UiState.Content
            assertEquals(1, initialState.data.selectedDay)

            // WHEN
            viewModel.onCycleDayTap(3)

            // THEN
            val updatedState = awaitItem() as UiState.Content
            assertEquals(3, updatedState.data.selectedDay)
        }
    }

    @Test
    fun `onConfirmAdvance triggers LevelUp event when transaction succeeds with LevelUp`() = runTest {
        coEvery { finalizeDayTransaction(forceComplete = false) } returns Result.Success(DayFinalizationResult.LevelUp)

        viewModel.events.test {
            viewModel.onConfirmAdvance()
            assertEquals(ModeEvent.LevelUp, awaitItem())
        }
    }

    @Test
    fun `initialization calls generateQuests`() = runTest {
        advanceUntilIdle()
        coVerify { generateQuests() }
    }
}
