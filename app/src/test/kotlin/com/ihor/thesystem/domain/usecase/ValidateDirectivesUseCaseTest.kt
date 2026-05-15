package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateDirectivesUseCaseTest {

    private val useCase = ValidateDirectivesUseCase()

    private val matrix = listOf(
        ProgressionMatrixEntry(
            id = 1,
            exerciseId = 10,
            exerciseName = "Exercise 10",
            startWeight = 50f,
            targetWeight = 100f,
            currentWeight = 60f,
            targetWeightNote = null,
            weeklyStep = 2.5f,
            progressPercent = 0f
        )
    )

    @Test
    fun `targetWeight within currentWeight and targetWeight unchanged`() {
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 70.0, targetSets = 3, targetReps = "8-12")
        val result = useCase(listOf(directive), matrix)
        
        assertTrue(result is Result.Success)
        assertEquals(70.0, (result as Result.Success).data[0].targetWeight, 0.01)
    }

    @Test
    fun `targetWeight below currentWeight clamped to currentWeight`() {
        // currentWeight is 60.0
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 55.0, targetSets = 3, targetReps = "8-12")
        val result = useCase(listOf(directive), matrix)
        
        assertEquals(60.0, (result as Result.Success).data[0].targetWeight, 0.01)
    }

    @Test
    fun `targetWeight above targetWeight clamped to targetWeight`() {
        // targetWeight is 100.0
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 110.0, targetSets = 3, targetReps = "8-12")
        val result = useCase(listOf(directive), matrix)
        
        assertEquals(100.0, (result as Result.Success).data[0].targetWeight, 0.01)
    }

    @Test
    fun `corrupt data currentWeight gt targetWeight clamps to valid range`() {
        val corruptMatrix = listOf(
            ProgressionMatrixEntry(
                id = 1, exerciseId = 10, exerciseName = "Ex",
                startWeight = 50f, targetWeight = 60f, currentWeight = 80f,
                targetWeightNote = null, weeklyStep = 0f, progressPercent = 0f
            )
        )
        // Range is [60, 80]
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 90.0, targetSets = 3, targetReps = "8-12")
        val result = useCase(listOf(directive), corruptMatrix)
        
        assertEquals(80.0, (result as Result.Success).data[0].targetWeight, 0.01)

        val directiveLow = WorkoutDirective(exerciseId = 10, targetWeight = 50.0, targetSets = 3, targetReps = "8-12")
        val resultLow = useCase(listOf(directiveLow), corruptMatrix)
        assertEquals(60.0, (resultLow as Result.Success).data[0].targetWeight, 0.01)
    }

    @Test
    fun `exerciseId not in matrix only targetSets clamped`() {
        val directive = WorkoutDirective(exerciseId = 999, targetWeight = 500.0, targetSets = 0, targetReps = "8-12")
        val result = useCase(listOf(directive), matrix)
        
        val validated = (result as Result.Success).data[0]
        assertEquals(500.0, validated.targetWeight, 0.01) // Not clamped
        assertEquals(1, validated.targetSets) // Clamped to 1
    }

    @Test
    fun `bodyweight tracking mode directive is rejected`() {
        val bodyweightMatrix = listOf(
            matrix[0].copy(
                exerciseName = "Push-up",
                exerciseTrackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS.name,
                startWeight = 0f,
                targetWeight = 0f,
                currentWeight = 0f
            )
        )
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 25.0, targetSets = 3, targetReps = "12")

        val result = useCase(listOf(directive), bodyweightMatrix)

        assertTrue(result is Result.Success)
        assertEquals(emptyList<WorkoutDirective>(), (result as Result.Success).data)
    }

    @Test
    fun `non weight exercise name fallback directive is rejected when tracking mode is missing`() {
        val timedMatrix = listOf(
            matrix[0].copy(
                exerciseName = "Plank",
                exerciseTrackingMode = null,
                startWeight = 0f,
                targetWeight = 0f,
                currentWeight = 0f
            )
        )
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 10.0, targetSets = 3, targetReps = "60")

        val result = useCase(listOf(directive), timedMatrix)

        assertTrue(result is Result.Success)
        assertEquals(emptyList<WorkoutDirective>(), (result as Result.Success).data)
    }
}
