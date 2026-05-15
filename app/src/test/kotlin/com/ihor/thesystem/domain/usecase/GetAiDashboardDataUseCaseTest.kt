package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.MatrixEntryData
import com.ihor.thesystem.domain.model.StatisticsData
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test

class GetAiDashboardDataUseCaseTest {

    private val getStatisticsData: GetStatisticsDataUseCase = mockk()
    private val useCase = GetAiDashboardDataUseCase(getStatisticsData)

    @Test
    fun `last recommendation ignores stale bodyweight matrix recommendation`() = runTest {
        every { getStatisticsData.invoke() } returns flowOf(
            StatisticsData(
                matrixEntries = listOf(
                    MatrixEntryData(
                        entry = ProgressionMatrixEntry(
                            id = 10,
                            exerciseId = 10,
                            exerciseName = "Push-up",
                            exerciseTrackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS.name,
                            startWeight = 0f,
                            targetWeight = 0f,
                            currentWeight = 0f,
                            targetWeightNote = null,
                            weeklyStep = 0f,
                            progressPercent = 0f,
                            nextRecommendedWeight = 1.0,
                            nextRecommendedSets = 3,
                            nextRecommendedReps = "15",
                            lastAiFeedback = "Add kilograms next time",
                            lastAnalyzedTimestamp = 1L
                        ),
                        isActive = true,
                        orderIndex = 0,
                        weightHistory = emptyList()
                    )
                )
            )
        )

        val data = useCase().first()

        assertNull(data.lastRecommendation)
    }
}
