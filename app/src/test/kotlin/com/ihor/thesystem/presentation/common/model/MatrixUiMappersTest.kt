package com.ihor.thesystem.presentation.common.model

import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.MatrixEntryData
import com.ihor.thesystem.domain.model.WeightHistoryEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatrixUiMappersTest {

    @Test
    fun `non weight matrix entry hides stale ai recommendation fields`() {
        val uiModel = MatrixEntryData(
            entry = entry(
                exerciseName = "Push-up",
                trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS,
                nextRecommendedWeight = 1.0,
                lastAiFeedback = "Add kilograms next time"
            ),
            isActive = true,
            orderIndex = 0,
            weightHistory = listOf(WeightHistoryEntry(weight = 1.0, timestamp = 1L))
        ).toMatrixEntryUiModel()

        assertFalse(uiModel.usesExternalLoad)
        assertTrue(uiModel.weightHistory.isEmpty())
        assertEquals("—", uiModel.displayCurrent)
        assertEquals("—", uiModel.displayStart)
        assertNull(uiModel.nextRecommendedWeight)
        assertNull(uiModel.nextRecommendedSets)
        assertNull(uiModel.nextRecommendedReps)
        assertNull(uiModel.lastAiFeedback)
    }

    @Test
    fun `weighted matrix entry keeps ai recommendation fields`() {
        val uiModel = MatrixEntryData(
            entry = entry(
                exerciseName = "Bench press",
                trackingMode = ExerciseTrackingMode.WEIGHT_REPS,
                nextRecommendedWeight = 62.5,
                lastAiFeedback = "Keep the bar speed steady"
            ),
            isActive = true,
            orderIndex = 0,
            weightHistory = listOf(WeightHistoryEntry(weight = 60.0, timestamp = 1L))
        ).toMatrixEntryUiModel()

        assertTrue(uiModel.usesExternalLoad)
        assertTrue(uiModel.weightHistory.isNotEmpty())
        assertTrue(uiModel.displayCurrent.contains("кг") || uiModel.displayCurrent.contains("РєРі"))
        assertEquals(62.5, uiModel.nextRecommendedWeight ?: 0.0, 0.001)
        assertEquals(3, uiModel.nextRecommendedSets)
        assertEquals("8-10", uiModel.nextRecommendedReps)
        assertEquals("Keep the bar speed steady", uiModel.lastAiFeedback)
    }

    private fun entry(
        exerciseName: String,
        trackingMode: ExerciseTrackingMode,
        nextRecommendedWeight: Double?,
        lastAiFeedback: String?
    ): ProgressionMatrixEntry =
        ProgressionMatrixEntry(
            id = 1,
            exerciseId = 1,
            exerciseName = exerciseName,
            exerciseTrackingMode = trackingMode.name,
            startWeight = 50f,
            targetWeight = 100f,
            currentWeight = 60f,
            targetWeightNote = null,
            weeklyStep = 2.5f,
            progressPercent = 0f,
            nextRecommendedWeight = nextRecommendedWeight,
            nextRecommendedSets = 3,
            nextRecommendedReps = "8-10",
            lastAiFeedback = lastAiFeedback,
            lastAnalyzedTimestamp = 1L
        )
}
