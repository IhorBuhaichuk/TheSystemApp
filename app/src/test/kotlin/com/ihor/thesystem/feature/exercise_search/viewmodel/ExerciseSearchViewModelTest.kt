package com.ihor.thesystem.feature.exercise_search.viewmodel

import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseSearchViewModelTest {

    @Test
    fun `weighted picker item shows last result in kilograms`() {
        val text = entry(
            exerciseName = "Bench press",
            trackingMode = ExerciseTrackingMode.WEIGHT_REPS,
            currentWeight = 60f
        ).toExercisePickerLastResultText()

        assertEquals("60 кг", text)
    }

    @Test
    fun `bodyweight picker item does not show technical kilogram load`() {
        val text = entry(
            exerciseName = "Push-up",
            trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS,
            currentWeight = 1f
        ).toExercisePickerLastResultText()

        assertNull(text)
    }

    @Test
    fun `timed picker item does not show technical kilogram load`() {
        val text = entry(
            exerciseName = "Plank",
            trackingMode = ExerciseTrackingMode.TIME_SECONDS,
            currentWeight = 1f
        ).toExercisePickerLastResultText()

        assertNull(text)
    }

    private fun entry(
        exerciseName: String,
        trackingMode: ExerciseTrackingMode,
        currentWeight: Float
    ): ProgressionMatrixEntry =
        ProgressionMatrixEntry(
            id = 1,
            exerciseId = 1,
            exerciseName = exerciseName,
            exerciseTrackingMode = trackingMode.name,
            startWeight = currentWeight,
            targetWeight = currentWeight,
            currentWeight = currentWeight,
            targetWeightNote = null,
            weeklyStep = 0f,
            progressPercent = 0f
        )
}
