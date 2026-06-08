package com.ihor.thesystem.domain.model

import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BossFightTest {

    @Test
    fun `bodyweight exercise uses reps target`() {
        val bossFight = progressionEntry(
            name = "Pull-up",
            trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS.name,
            nextRecommendedReps = "10"
        ).toBossFight()

        assertEquals(BossFightTargetMetric.REPS, bossFight.targetMetric)
        assertEquals(10.0, bossFight.targetValue, 0.001)
        assertTrue(bossFight.rulesText.contains("10"))
    }

    @Test
    fun `time exercise uses seconds target`() {
        val bossFight = progressionEntry(
            name = "Plank",
            trackingMode = ExerciseTrackingMode.TIME_SECONDS.name,
            nextRecommendedReps = "45"
        ).toBossFight()

        assertEquals(BossFightTargetMetric.TIME_SECONDS, bossFight.targetMetric)
        assertEquals(45.0, bossFight.targetValue, 0.001)
        assertTrue(bossFight.rulesText.contains("45"))
    }

    private fun progressionEntry(
        name: String,
        trackingMode: String,
        nextRecommendedReps: String?
    ): ProgressionMatrixEntry =
        ProgressionMatrixEntry(
            id = 1,
            exerciseId = 77,
            exerciseName = name,
            exerciseTrackingMode = trackingMode,
            startWeight = 0f,
            targetWeight = 0f,
            currentWeight = 0f,
            targetWeightNote = null,
            weeklyStep = 0f,
            progressPercent = 100f,
            currentRank = Rank.E,
            isPromotionPending = true,
            nextRecommendedReps = nextRecommendedReps
        )
}
