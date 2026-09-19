package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LogWorkoutSetsUseCaseTest {

    private val playerRepository: PlayerRepository = mockk()
    private val matrixRepository: ProgressionMatrixRepository = mockk(relaxed = true)
    private val analyticsRepository: WorkoutAnalyticsRepository = mockk(relaxed = true)
    private val transactionProvider = RecordingTransactionProvider()
    private val useCase = LogWorkoutSetsUseCase(
        playerRepo = playerRepository,
        matrixRepo = matrixRepository,
        analyticsRepo = analyticsRepository,
        transactionProvider = transactionProvider
    )

    @Test
    fun `new manual log uses selected timestamp instead of current clock time`() = runTest {
        val selectedTimestamp = LocalDate.of(2026, 4, 20)
            .atStartOfDay(TEST_ZONE)
            .toInstant()
            .toEpochMilli()
        val sessionSlot = slot<WorkoutSession>()
        every { playerRepository.getPlayer() } returns flowOf(player(currentCycleDay = 3))
        coEvery { analyticsRepository.saveFullSessionLog(capture(sessionSlot), any()) } coAnswers {
            assertTrue(transactionProvider.inTransaction)
            100L
        }
        coEvery { matrixRepository.updateCurrentWeight(7, 80f) } coAnswers {
            assertTrue(transactionProvider.inTransaction)
        }

        useCase(
            sessionId = null,
            exerciseId = 7,
            sets = listOf(ActiveSetInput(weight = "80", reps = "5", isCompleted = true)),
            timestamp = selectedTimestamp,
            userFeedback = "solid"
        )

        assertEquals(selectedTimestamp, sessionSlot.captured.timestamp)
        assertEquals(3, sessionSlot.captured.cycleDay)
        assertEquals(400.0, sessionSlot.captured.totalTonnage, 0.0)
        assertEquals(1, transactionProvider.calls)
        coVerify(exactly = 0) { analyticsRepository.getLogsForExerciseOnDate(any(), any(), any()) }
        coVerify { matrixRepository.updateCurrentWeight(7, 80f) }
    }

    @Test
    fun `edit replaces only the requested exercise in the explicit session`() = runTest {
        val selectedTimestamp = LocalDate.of(2026, 4, 20)
            .atStartOfDay(TEST_ZONE)
            .toInstant()
            .toEpochMilli()
        val setsSlot = slot<List<ExerciseSet>>()
        every { playerRepository.getPlayer() } returns flowOf(player(currentCycleDay = 4))
        coEvery {
            analyticsRepository.replaceExerciseSets(77L, 7, capture(setsSlot))
        } coAnswers {
            assertTrue(transactionProvider.inTransaction)
        }
        coEvery { matrixRepository.updateCurrentWeight(7, 82.5f) } coAnswers {
            assertTrue(transactionProvider.inTransaction)
        }

        useCase(
            sessionId = 77L,
            exerciseId = 7,
            sets = listOf(
                ActiveSetInput(weight = "82.5", reps = "3", isCompleted = true),
                ActiveSetInput(weight = "75", reps = "5", isCompleted = false)
            ),
            timestamp = selectedTimestamp,
            userFeedback = null
        )

        assertEquals(listOf(77L, 77L), setsSlot.captured.map { it.sessionId })
        assertEquals(listOf(7, 7), setsSlot.captured.map { it.exerciseId })
        assertEquals(listOf(true, false), setsSlot.captured.map { it.isCompleted })
        assertEquals(1, transactionProvider.calls)
        coVerify(exactly = 1) { analyticsRepository.replaceExerciseSets(77L, 7, any()) }
        coVerify(exactly = 0) { analyticsRepository.getLogsForExerciseOnDate(any(), any(), any()) }
        coVerify(exactly = 0) { analyticsRepository.saveFullSessionLog(any(), any()) }
        coVerify { matrixRepository.updateCurrentWeight(7, 82.5f) }
    }

    @Test
    fun `incomplete edited sets are persisted but do not advance matrix weight`() = runTest {
        every { playerRepository.getPlayer() } returns flowOf(player(currentCycleDay = 4))
        coEvery { analyticsRepository.replaceExerciseSets(91L, 7, any()) } coAnswers {
            assertTrue(transactionProvider.inTransaction)
        }

        useCase(
            sessionId = 91L,
            exerciseId = 7,
            sets = listOf(ActiveSetInput(weight = "100", reps = "2", isCompleted = false)),
            timestamp = 123L
        )

        coVerify(exactly = 1) { analyticsRepository.replaceExerciseSets(91L, 7, any()) }
        coVerify(exactly = 0) { matrixRepository.updateCurrentWeight(any(), any()) }
    }

    private fun player(currentCycleDay: Int): Player =
        Player(
            id = 1,
            name = "Player",
            level = 1,
            playerClass = PlayerRank.NOVICE,
            height = 180f,
            currentMonth = 1,
            currentWeek = 1,
            currentCycleDay = currentCycleDay
        )

    private class RecordingTransactionProvider : TransactionProvider {
        var calls: Int = 0
            private set
        var inTransaction: Boolean = false
            private set

        override suspend fun <R> runInTransaction(block: suspend () -> R): R {
            check(!inTransaction)
            calls++
            inTransaction = true
            return try {
                block()
            } finally {
                inTransaction = false
            }
        }
    }

    private companion object {
        val TEST_ZONE = java.time.ZoneId.of("Europe/Kyiv")
    }
}
