package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class LogWorkoutSetsUseCaseTest {

    private val playerRepository: PlayerRepository = mockk()
    private val matrixRepository: ProgressionMatrixRepository = mockk(relaxed = true)
    private val analyticsRepository: WorkoutAnalyticsRepository = mockk(relaxed = true)
    private val clock = FixedClock(
        nowMillis = LocalDate.of(2026, 5, 2)
            .atStartOfDay(TEST_ZONE)
            .toInstant()
            .toEpochMilli(),
        zoneId = TEST_ZONE
    )
    private val useCase = LogWorkoutSetsUseCase(
        playerRepo = playerRepository,
        matrixRepo = matrixRepository,
        analyticsRepo = analyticsRepository,
        clock = clock
    )

    @Test
    fun `new manual log uses selected timestamp instead of current clock time`() = runTest {
        val selectedTimestamp = LocalDate.of(2026, 4, 20)
            .atStartOfDay(TEST_ZONE)
            .toInstant()
            .toEpochMilli()
        val sessionSlot = slot<WorkoutSession>()
        every { playerRepository.getPlayer() } returns flowOf(player(currentCycleDay = 3))
        coEvery { analyticsRepository.getLogsForExerciseOnDate(7, any(), any()) } returns emptyList()
        coEvery { analyticsRepository.saveFullSessionLog(capture(sessionSlot), any()) } returns 100L

        useCase(
            exerciseId = 7,
            sets = listOf(ActiveSetInput(weight = "80", reps = "5")),
            timestamp = selectedTimestamp,
            userFeedback = "solid"
        )

        assertEquals(selectedTimestamp, sessionSlot.captured.timestamp)
        assertEquals(3, sessionSlot.captured.cycleDay)
        coVerify { matrixRepository.updateCurrentWeight(7, 80f) }
    }

    @Test
    fun `existing manual log keeps selected timestamp when replacing same-day sets`() = runTest {
        val selectedTimestamp = LocalDate.of(2026, 4, 20)
            .atStartOfDay(TEST_ZONE)
            .toInstant()
            .toEpochMilli()
        val sessionSlot = slot<WorkoutSession>()
        every { playerRepository.getPlayer() } returns flowOf(player(currentCycleDay = 4))
        coEvery { analyticsRepository.getLogsForExerciseOnDate(7, any(), any()) } returns listOf(
            ExerciseSet(
                setId = 1L,
                sessionId = 77L,
                exerciseId = 7,
                weight = 75.0,
                reps = 5,
                isCompleted = true
            )
        )
        coEvery { analyticsRepository.updateSessionLog(capture(sessionSlot)) } returns Unit

        useCase(
            exerciseId = 7,
            sets = listOf(ActiveSetInput(weight = "82.5", reps = "3")),
            timestamp = selectedTimestamp,
            userFeedback = null
        )

        assertEquals(77L, sessionSlot.captured.sessionId)
        assertEquals(selectedTimestamp, sessionSlot.captured.timestamp)
        assertEquals(4, sessionSlot.captured.cycleDay)
        coVerify { analyticsRepository.deleteSetsBySession(77L) }
        coVerify { matrixRepository.updateCurrentWeight(7, 82.5f) }
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

    private class FixedClock(
        private val nowMillis: Long,
        private val zoneId: ZoneId
    ) : AppClock {
        override fun now(): Long = nowMillis
        override fun zoneId(): ZoneId = zoneId
    }

    private companion object {
        val TEST_ZONE: ZoneId = ZoneId.of("Europe/Kyiv")
    }
}
