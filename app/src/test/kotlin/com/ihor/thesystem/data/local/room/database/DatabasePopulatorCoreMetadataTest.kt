package com.ihor.thesystem.data.local.room.database

import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabasePopulatorCoreMetadataTest {

    @Test
    fun `core metadata is applied over seed exercise`() {
        val entity = ExerciseEntity(
            externalId = "Barbell_Squat",
            name = "Barbell Squat"
        )
        val metadata = DatabasePopulator.SystemCoreExerciseMetadataDto(
            externalId = "Barbell_Squat",
            movementPattern = "squat",
            substitutionExternalIds = listOf("Bodyweight_Squat", "Goblet_Squat")
        )
        val defaults = mapOf(
            "squat" to DatabasePopulator.SystemCoreExercisePatternDto(
                techniqueTips = listOf("Brace", "Knees track toes"),
                commonMistakes = listOf("Knees cave")
            )
        )

        val result = with(DatabasePopulator) {
            entity.withCoreMetadata(metadata, defaults)
        }

        assertTrue(result.isCoreSystemExercise)
        assertEquals("squat", result.movementPattern)
        assertEquals(listOf("Brace", "Knees track toes"), result.techniqueTips)
        assertEquals(listOf("Knees cave"), result.commonMistakes)
        assertEquals(listOf("Bodyweight_Squat", "Goblet_Squat"), result.substitutionExternalIds)
    }
}
