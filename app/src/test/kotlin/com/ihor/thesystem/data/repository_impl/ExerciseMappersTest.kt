package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseMappersTest {

    @Test
    fun `exercise mapper keeps core metadata lists`() {
        val entity = ExerciseEntity(
            id = 7,
            externalId = "Barbell_Squat",
            name = "Barbell Squat",
            category = ExerciseCategory.STRENGTH,
            muscleGroups = listOf(MuscleGroup.QUADS),
            isCoreSystemExercise = true,
            movementPattern = "squat",
            techniqueTips = listOf("Brace", "Control down"),
            commonMistakes = listOf("Knees cave"),
            substitutionExternalIds = listOf("Bodyweight_Squat")
        )

        val result = entity.toExerciseDetails()

        assertTrue(result.isCoreSystemExercise)
        assertEquals("squat", result.movementPattern)
        assertEquals(listOf("Brace", "Control down"), result.techniqueTips)
        assertEquals(listOf("Knees cave"), result.commonMistakes)
        assertEquals(listOf("Bodyweight_Squat"), result.substitutionExternalIds)
    }
}
