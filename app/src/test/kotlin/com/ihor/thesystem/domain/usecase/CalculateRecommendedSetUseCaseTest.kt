package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateRecommendedSetUseCaseTest {

    private val analyticsRepo: WorkoutAnalyticsRepository = mockk()
    private val matrixRepo: ProgressionMatrixRepository = mockk()
    private val useCase = CalculateRecommendedSetUseCase(analyticsRepo, matrixRepo)

    @Test
    fun `no completed set starts from matrix start weight`() = runTest {
        coEvery { matrixRepo.getEntrySync(1) } returns entry(startWeight = 35f)
        coEvery { analyticsRepo.getLastSetsForExercise(1) } returns emptyList()

        val result = useCase(1, "Bench")

        assertEquals(35.0, result.weight, 0.001)
        assertEquals(12, result.reps)
        assertFalse(result.isProgression)
    }

    @Test
    fun `three completed top-weight sets at target reps progress weight and reset reps`() = runTest {
        coEvery { matrixRepo.getEntrySync(1) } returns entry(startWeight = 40f, targetWeight = 60f, weeklyStep = 2.5f)
        coEvery { analyticsRepo.getLastSetsForExercise(1) } returns listOf(
            set(weight = 50.0, reps = 12),
            set(weight = 50.0, reps = 12),
            set(weight = 50.0, reps = 12)
        )

        val result = useCase(1, "Bench")

        assertEquals(52.5, result.weight, 0.001)
        assertEquals(8, result.reps)
        assertEquals(3, result.sets)
        assertTrue(result.isProgression)
    }

    @Test
    fun `progression never exceeds positive matrix target`() = runTest {
        coEvery { matrixRepo.getEntrySync(1) } returns entry(startWeight = 40f, targetWeight = 51f, weeklyStep = 2.5f)
        coEvery { analyticsRepo.getLastSetsForExercise(1) } returns listOf(
            set(weight = 50.0, reps = 12),
            set(weight = 50.0, reps = 12),
            set(weight = 50.0, reps = 12)
        )

        val result = useCase(1, "Bench")

        assertEquals(51.0, result.weight, 0.001)
        assertTrue(result.isProgression)
    }

    @Test
    fun `warmups and incomplete sets do not satisfy progression`() = runTest {
        coEvery { matrixRepo.getEntrySync(1) } returns entry(startWeight = 40f, targetWeight = 60f, weeklyStep = 2.5f)
        coEvery { analyticsRepo.getLastSetsForExercise(1) } returns listOf(
            set(weight = 20.0, reps = 12),
            set(weight = 50.0, reps = 12),
            set(weight = 50.0, reps = 11),
            set(weight = 50.0, reps = 12, isCompleted = false)
        )

        val result = useCase(1, "Bench")

        assertEquals(50.0, result.weight, 0.001)
        assertEquals(12, result.reps)
        assertFalse(result.isProgression)
    }

    @Test
    fun `provided session sets drive recommendation without reading latest repository sets`() = runTest {
        coEvery { matrixRepo.getEntrySync(1) } returns entry(startWeight = 40f, targetWeight = 70f, weeklyStep = 2.5f)

        val result = useCase.fromSets(
            exerciseId = 1,
            exerciseName = "Bench",
            sets = listOf(
                set(weight = 55.0, reps = 12, sessionId = 42L),
                set(weight = 55.0, reps = 12, sessionId = 42L),
                set(weight = 55.0, reps = 12, sessionId = 42L)
            )
        )

        assertEquals(57.5, result.weight, 0.001)
        assertEquals(8, result.reps)
        assertTrue(result.isProgression)
        coVerify(exactly = 0) { analyticsRepo.getLastSetsForExercise(1) }
    }

    private fun entry(
        startWeight: Float = 40f,
        targetWeight: Float = 60f,
        weeklyStep: Float = 2.5f
    ) = ProgressionMatrixEntry(
        id = 1,
        exerciseId = 1,
        exerciseName = "Bench",
        startWeight = startWeight,
        targetWeight = targetWeight,
        currentWeight = startWeight,
        targetWeightNote = null,
        weeklyStep = weeklyStep,
        progressPercent = 0f
    )

    private fun set(
        weight: Double,
        reps: Int,
        isCompleted: Boolean = true,
        sessionId: Long = 1L
    ) = ExerciseSet(
        sessionId = sessionId,
        exerciseId = 1,
        weight = weight,
        reps = reps,
        isCompleted = isCompleted
    )
}
