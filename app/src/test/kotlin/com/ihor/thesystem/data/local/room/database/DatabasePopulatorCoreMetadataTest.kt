package com.ihor.thesystem.data.local.room.database

import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(DatabasePopulator.CORE_METADATA_VERSION, result.coreMetadataVersion)
        assertEquals("squat", result.movementPattern)
        assertEquals(listOf("Brace", "Knees track toes"), result.techniqueTips)
        assertEquals(listOf("Knees cave"), result.commonMistakes)
        assertEquals(listOf("Bodyweight_Squat", "Goblet_Squat"), result.substitutionExternalIds)
    }

    @Test
    fun `current core metadata version skips unchanged startup rewrite`() {
        val state = DatabasePopulator.PopulationState(
            exerciseCount = 759,
            currentCoreMetadataCount = DatabasePopulator.CORE_METADATA_EXERCISE_COUNT
        )

        assertFalse(DatabasePopulator.shouldApplyCoreMetadata(state))
    }

    @Test
    fun `stale or restored core metadata is reapplied once`() {
        val state = DatabasePopulator.PopulationState(
            exerciseCount = 759,
            currentCoreMetadataCount = 0
        )

        assertTrue(DatabasePopulator.shouldApplyCoreMetadata(state))
    }

    @Test
    fun `version gate count matches shipped core metadata asset`() {
        val asset = File(
            requireNotNull(System.getProperty("user.dir")),
            "src/main/assets/system_core_exercises.json"
        ).readText()
        val exerciseCount = Json.parseToJsonElement(asset)
            .jsonObject
            .getValue("exercises")
            .jsonArray
            .size

        assertEquals(DatabasePopulator.CORE_METADATA_EXERCISE_COUNT, exerciseCount)
    }
}
