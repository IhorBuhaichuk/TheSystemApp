package com.ihor.thesystem.feature.status.ui.components.workout

import com.ihor.thesystem.feature.status.viewmodel.ExerciseWorkoutUiModel
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TechniqueCheckTextTest {

    @Test
    fun `technique check is hidden when tips are empty`() {
        val exercise = ExerciseWorkoutUiModel(
            exerciseId = 1,
            name = "Squat",
            isCoreSystemExercise = true,
            techniqueTips = persistentListOf()
        )

        assertNull(exercise.techniqueCheckText())
    }

    @Test
    fun `technique check is hidden for non core exercise`() {
        val exercise = ExerciseWorkoutUiModel(
            exerciseId = 1,
            name = "Custom movement",
            isCoreSystemExercise = false,
            techniqueTips = persistentListOf("Brace")
        )

        assertNull(exercise.techniqueCheckText())
    }

    @Test
    fun `technique check uses compact first three tips`() {
        val exercise = ExerciseWorkoutUiModel(
            exerciseId = 1,
            name = "Squat",
            isCoreSystemExercise = true,
            techniqueTips = persistentListOf("Brace", "Knees stable", "Control down", "Extra")
        )

        assertEquals(
            "Перевірте техніку: Brace · Knees stable · Control down",
            exercise.techniqueCheckText()
        )
    }
}
