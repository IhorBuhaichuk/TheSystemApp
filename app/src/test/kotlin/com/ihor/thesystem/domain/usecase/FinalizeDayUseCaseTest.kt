package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.QuestTask
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.AppLogger
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
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException

class FinalizeDayUseCaseTest {

    private val playerRepo: PlayerRepository = mockk()
    private val questRepo: QuestRepository = mockk()
    private val configRepo: SystemConfigRepository = mockk()
    private val scheduleRepo: ScheduleRepository = mockk()
    private val generateDailyQuests: GenerateDailyQuestsUseCase = mockk()
    private val calculateAttributes: CalculateAttributesUseCase = mockk()
    private val resolveTrainingCycleDay = ResolveTrainingCycleDayUseCase(
        calculateCycleDay = CalculateCycleDayForDateUseCase(),
        clock = FixedClock(TODAY)
    )
    private val advanceCycleDayStatus = AdvanceCycleDayUseCase(questRepo)
    private val logger = NoOpLogger()

    @Test
    fun `missed scheduled workout day resets streak and catches up cycle day`() = runTest {
        val playerSlot = slot<Player>()
        val config = SystemConfig(
            cycleAnchorDateTimestamp = LAST_INIT.toEpochDay(),
            cycleAnchorDay = 1,
            cycleDaysPerMicrocycle = 4,
            microCyclesPerMonth = 4,
            lastInitEpochDay = LAST_INIT.toEpochDay()
        )
        val activeQuest = completedMainQuest()

        every { playerRepo.getPlayer() } returns flowOf(player(currentCycleDay = 1, currentStreak = 3))
        every { configRepo.getConfigFlow() } returns flowOf(config)
        every { questRepo.getActiveQuests() } returns flowOf(listOf(activeQuest))
        every { scheduleRepo.getScheduleForDay(2) } returns flowOf(workoutDay(cycleDay = 2))
        every { scheduleRepo.getScheduleForDay(3) } returns flowOf(restDay(cycleDay = 3))
        coEvery { questRepo.updateQuestStatus(any(), any()) } just runs
        coEvery { questRepo.logQuestResult(any(), any(), any()) } just runs
        coEvery { playerRepo.updatePlayer(capture(playerSlot)) } returns Result.Success(Unit)
        coEvery { questRepo.archiveActiveQuests() } just runs
        coEvery { configRepo.saveLastInitDate(TODAY.toEpochDay()) } just runs
        coEvery { configRepo.setNeedsDailyInit(any()) } just runs
        coEvery { generateDailyQuests() } just runs
        coEvery { calculateAttributes() } returns Result.Success(CalculatedAttributes(emptyMap()))

        val result = useCase(transactionProvider = RecordingTransactionProvider())()

        assertTrue(result is Result.Success)
        assertEquals(0, playerSlot.captured.currentStreak)
        assertEquals(4, playerSlot.captured.maxStreak)
        assertEquals(4, playerSlot.captured.currentCycleDay)
        assertEquals(100, playerSlot.captured.xpTotal)
        coVerify { questRepo.updateQuestStatus(activeQuest.id, DomainQuestStatus.COMPLETED) }
    }

    @Test
    fun `finalize day rethrows coroutine cancellation`() = runTest {
        every { playerRepo.getPlayer() } returns flowOf(player())
        every { configRepo.getConfigFlow() } returns flowOf(
            SystemConfig(lastInitEpochDay = TODAY.toEpochDay())
        )

        try {
            useCase(transactionProvider = CancellingTransactionProvider())()
            fail("FinalizeDayUseCase must rethrow CancellationException.")
        } catch (_: CancellationException) {
            // Expected: structured concurrency must own cancellation.
        }
    }

    private fun useCase(
        transactionProvider: TransactionProvider
    ): FinalizeDayUseCase =
        FinalizeDayUseCase(
            transactionProvider = transactionProvider,
            playerRepo = playerRepo,
            questRepo = questRepo,
            configRepo = configRepo,
            scheduleRepo = scheduleRepo,
            generateDailyQuests = generateDailyQuests,
            calculateAttributes = calculateAttributes,
            advanceCycleDayStatus = advanceCycleDayStatus,
            resolveTrainingCycleDay = resolveTrainingCycleDay,
            clock = FixedClock(TODAY),
            logger = logger
        )

    private fun player(
        currentCycleDay: Int = 1,
        currentStreak: Int = 0
    ): Player =
        Player(
            id = 1,
            name = "Player",
            level = 1,
            playerClass = PlayerRank.NOVICE,
            height = 180f,
            currentMonth = 1,
            currentWeek = 1,
            currentCycleDay = currentCycleDay,
            currentStreak = currentStreak,
            maxStreak = currentStreak
        )

    private fun completedMainQuest(): Quest =
        Quest(
            id = 42,
            title = "Workout",
            type = DomainQuestType.MAIN,
            date = LAST_INIT.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli(),
            status = DomainQuestStatus.ACTIVE,
            tasks = listOf(
                QuestTask(
                    id = 7,
                    questId = 42,
                    name = "Bench press",
                    isCompleted = true,
                    exerciseId = 10
                )
            ),
            scheduleId = 2
        )

    private fun workoutDay(cycleDay: Int): ScheduleDay =
        ScheduleDay(
            id = cycleDay,
            cycleDay = cycleDay,
            workoutTemplateId = 100 + cycleDay,
            workoutTemplateName = "Workout",
            dailyTaskNames = emptyList(),
            exercises = listOf(ExerciseDetails(id = 10, name = "Bench press"))
        )

    private fun restDay(cycleDay: Int): ScheduleDay =
        ScheduleDay(
            id = cycleDay,
            cycleDay = cycleDay,
            workoutTemplateId = null,
            workoutTemplateName = null,
            dailyTaskNames = emptyList(),
            exercises = emptyList()
        )

    private class RecordingTransactionProvider : TransactionProvider {
        override suspend fun <R> runInTransaction(block: suspend () -> R): R = block()
    }

    private class CancellingTransactionProvider : TransactionProvider {
        override suspend fun <R> runInTransaction(block: suspend () -> R): R {
            throw CancellationException("cancelled")
        }
    }

    private class FixedClock(private val today: LocalDate) : AppClock {
        override fun now(): Long =
            today.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = TEST_ZONE
    }

    private class NoOpLogger : AppLogger {
        override fun d(message: String, tag: String?) = Unit
        override fun i(message: String, tag: String?) = Unit
        override fun w(message: String, tag: String?) = Unit
        override fun e(throwable: Throwable?, message: String, tag: String?) = Unit
    }

    private companion object {
        val TEST_ZONE: ZoneId = ZoneId.of("UTC")
        val LAST_INIT: LocalDate = LocalDate.of(2026, 5, 1)
        val TODAY: LocalDate = LocalDate.of(2026, 5, 4)
    }
}
