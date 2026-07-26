package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AppClock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetAnnualProgressionDetailsUseCaseTest {

    private val progressionMatrixRepository: ProgressionMatrixRepository = mockk()
    private val workoutAnalyticsRepository: WorkoutAnalyticsRepository = mockk()
    private val clock = object : AppClock {
        override fun now(): Long = LocalDate.of(2026, 5, 14)
            .atStartOfDay(zoneId())
            .toInstant()
            .toEpochMilli()

        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    }
    private val useCase = GetAnnualProgressionDetailsUseCase(
        progressionMatrixRepository = progressionMatrixRepository,
        workoutAnalyticsRepository = workoutAnalyticsRepository,
        clock = clock
    )

    @Test
    fun `annual details ignore stale plans for non weight exercises`() = runTest {
        every { progressionMatrixRepository.getAllEntries() } returns flowOf(
            listOf(
                matrixEntry(
                    exerciseId = 1,
                    exerciseName = "Push-up",
                    trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS
                ),
                matrixEntry(
                    exerciseId = 2,
                    exerciseName = "Bench press",
                    trackingMode = ExerciseTrackingMode.WEIGHT_REPS
                )
            )
        )
        every {
            workoutAnalyticsRepository.getWeightHistoriesBetween(any(), any())
        } returns flowOf(emptyList())

        val data = useCase().first()

        assertEquals(1, data.exercises.size)
        assertEquals("Bench press", data.exercises.single().exerciseName)
        verify(exactly = 1) {
            workoutAnalyticsRepository.getWeightHistoriesBetween(
                LocalDate.of(2026, 1, 1)
                    .atStartOfDay(clock.zoneId())
                    .toInstant()
                    .toEpochMilli(),
                LocalDate.of(2026, 5, 15)
                    .atStartOfDay(clock.zoneId())
                    .toInstant()
                    .toEpochMilli()
            )
        }
    }

    private fun matrixEntry(
        exerciseId: Int,
        exerciseName: String,
        trackingMode: ExerciseTrackingMode
    ): ProgressionMatrixEntry =
        ProgressionMatrixEntry(
            id = exerciseId,
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            exerciseTrackingMode = trackingMode.name,
            startWeight = if (trackingMode.usesWeightInput) 60f else 0f,
            targetWeight = if (trackingMode.usesWeightInput) 100f else 0f,
            currentWeight = if (trackingMode.usesWeightInput) 60f else 0f,
            targetWeightNote = "annual_step_loading|start=2026-01-01|adaptationEnd=2026-01-15|step=2.5|M0:60:base;M1:62.5:step",
            weeklyStep = 2.5f,
            progressPercent = 0f
        )
}
