package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.util.AppClock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SaveExerciseSetsUseCaseTest {

    private val matrixRepository: ProgressionMatrixRepository = mockk(relaxed = true)
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase = mockk(relaxed = true)
    private val calculateProgressRank: CalculateProgressRankUseCase = mockk(relaxed = true)
    private val useCase = SaveExerciseSetsUseCase(
        matrixRepo = matrixRepository,
        recalculateGlobalRank = recalculateGlobalRank,
        calculateProgressRank = calculateProgressRank,
        clock = FixedClock
    )

    @Test
    fun `incomplete weighted sets are stored but cannot promote rank`() = runTest {
        coEvery { matrixRepository.saveExerciseSetsWithDate(any(), any(), any(), any(), any()) } returns Unit

        useCase(
            sessionId = 73L,
            exerciseId = 9,
            sets = listOf(ActiveSetInput(weight = "120", reps = "3", isCompleted = false)),
            date = LocalDate.of(2026, 9, 19),
            userFeedback = null,
            trackingMode = ExerciseTrackingMode.WEIGHT_REPS
        )

        coVerify(exactly = 1) {
            matrixRepository.saveExerciseSetsWithDate(73L, 9, any(), any(), null)
        }
        coVerify(exactly = 0) { matrixRepository.getEntrySync(any()) }
        coVerify(exactly = 0) { recalculateGlobalRank() }
    }

    private object FixedClock : AppClock {
        override fun now(): Long = 0L
        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    }
}
