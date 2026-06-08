package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.model.DirectiveValidationStatus
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.SystemDecisionValidationContext
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
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
    fun `targetWeight above allowed step is clamped`() {
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 70.0, targetSets = 3, targetReps = "8-12")
        val result = useCase(listOf(directive), matrix)
        
        assertTrue(result is Result.Success)
        assertEquals(62.5, (result as Result.Success).data[0].targetWeight, 0.01)
    }

    @Test
    fun `targetWeight below currentWeight clamped to currentWeight`() {
        // currentWeight is 60.0
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 55.0, targetSets = 3, targetReps = "8-12")
        val result = useCase(listOf(directive), matrix)
        
        assertEquals(60.0, (result as Result.Success).data[0].targetWeight, 0.01)
    }

    @Test
    fun `targetWeight above matrix target clamped to matrix target`() {
        val cappedMatrix = listOf(matrix[0].copy(currentWeight = 95f, targetWeight = 100f, weeklyStep = 10f))
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 110.0, targetSets = 3, targetReps = "8-12")
        val result = useCase(listOf(directive), cappedMatrix)
        
        assertEquals(100.0, (result as Result.Success).data[0].targetWeight, 0.01)
    }

    @Test
    fun `AI plus five kg rejected when readiness is recovery`() {
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 65.0, targetSets = 3, targetReps = "8")
        val result = useCase(listOf(directive), matrix, systemContext(readinessLevel = ReadinessLevel.RECOVERY))

        assertTrue(result is Result.Success)
        val validation = (result as Result.Success).data
        assertEquals(emptyList<WorkoutDirective>(), validation.validatedDirectives)
        assertEquals(DirectiveValidationStatus.REJECTED, validation.audits.single().status)
    }

    @Test
    fun `AI target clamped by matrix and allowed step`() {
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 90.0, targetSets = 3, targetReps = "8")
        val result = useCase(listOf(directive), matrix, systemContext())

        assertTrue(result is Result.Success)
        val validation = (result as Result.Success).data
        assertEquals(62.5, validation.validatedDirectives.single().targetWeight, 0.01)
        assertEquals(DirectiveValidationStatus.CLAMPED, validation.audits.single().status)
    }

    @Test
    fun `AI directive over target cap is clamped with audit`() {
        val cappedMatrix = listOf(matrix[0].copy(currentWeight = 98f, targetWeight = 100f, weeklyStep = 10f))
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 120.0, targetSets = 3, targetReps = "8")

        val result = useCase(listOf(directive), cappedMatrix, systemContext())

        assertTrue(result is Result.Success)
        val validation = (result as Result.Success).data
        assertEquals(100.0, validation.validatedDirectives.single().targetWeight, 0.01)
        assertEquals(DirectiveValidationStatus.CLAMPED, validation.audits.single().status)
    }

    @Test
    fun `AI directives are blocked below standard readiness score boundary`() {
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 62.5, targetSets = 3, targetReps = "8")
        val belowStandard = useCase(
            directives = listOf(directive),
            matrix = matrix,
            context = systemContext(readinessScore = 64)
        )
        val atStandard = useCase(
            directives = listOf(directive),
            matrix = matrix,
            context = systemContext(readinessScore = 65)
        )

        assertEquals(
            DirectiveValidationStatus.REJECTED,
            (belowStandard as Result.Success).data.audits.single().status
        )
        assertEquals(
            DirectiveValidationStatus.ACCEPTED,
            (atStandard as Result.Success).data.audits.single().status
        )
    }

    @Test
    fun `AI directives are blocked by high and critical recovery debt`() {
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 62.5, targetSets = 3, targetReps = "8")

        listOf(RecoveryDebtLevel.HIGH, RecoveryDebtLevel.CRITICAL).forEach { debtLevel ->
            val result = useCase(
                directives = listOf(directive),
                matrix = matrix,
                context = systemContext(recoveryDebtLevel = debtLevel)
            )

            assertTrue(result is Result.Success)
            val validation = (result as Result.Success).data
            assertEquals("debt $debtLevel", emptyList<WorkoutDirective>(), validation.validatedDirectives)
            assertEquals("debt $debtLevel", DirectiveValidationStatus.REJECTED, validation.audits.single().status)
        }
    }

    @Test
    fun `bodyweight kg directive rejected with audit`() {
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

        val result = useCase(listOf(directive), bodyweightMatrix, systemContext())

        assertTrue(result is Result.Success)
        val validation = (result as Result.Success).data
        assertEquals(emptyList<WorkoutDirective>(), validation.validatedDirectives)
        assertEquals(DirectiveValidationStatus.REJECTED, validation.audits.single().status)
    }

    @Test
    fun `bodyweight and time exercises reject kg directives with audits`() {
        val nonKgModes = listOf(
            ExerciseTrackingMode.BODYWEIGHT_REPS to "Push-up",
            ExerciseTrackingMode.TIME_SECONDS to "Plank Hold",
            ExerciseTrackingMode.TIME_MINUTES to "Walking"
        )

        nonKgModes.forEach { (mode, exerciseName) ->
            val nonKgMatrix = listOf(
                matrix[0].copy(
                    exerciseName = exerciseName,
                    exerciseTrackingMode = mode.name,
                    startWeight = 0f,
                    targetWeight = 0f,
                    currentWeight = 0f
                )
            )
            val directive = WorkoutDirective(exerciseId = 10, targetWeight = 10.0, targetSets = 3, targetReps = "30")

            val result = useCase(listOf(directive), nonKgMatrix, systemContext())

            assertTrue(result is Result.Success)
            val validation = (result as Result.Success).data
            assertEquals("mode $mode", emptyList<WorkoutDirective>(), validation.validatedDirectives)
            assertEquals("mode $mode", DirectiveValidationStatus.REJECTED, validation.audits.single().status)
            assertTrue("mode $mode", validation.audits.single().reason.contains("does not accept kg"))
        }
    }

    @Test
    fun `valid standard recommendation accepted`() {
        val directive = WorkoutDirective(exerciseId = 10, targetWeight = 62.5, targetSets = 3, targetReps = "8-12")

        val result = useCase(listOf(directive), matrix, systemContext())

        assertTrue(result is Result.Success)
        val validation = (result as Result.Success).data
        assertEquals(directive, validation.validatedDirectives.single())
        assertEquals(DirectiveValidationStatus.ACCEPTED, validation.audits.single().status)
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

    private fun systemContext(
        readinessLevel: ReadinessLevel = ReadinessLevel.STANDARD,
        readinessScore: Int = when (readinessLevel) {
            ReadinessLevel.PROGRESS -> 90
            ReadinessLevel.STANDARD -> 75
            ReadinessLevel.REDUCED -> 55
            ReadinessLevel.RECOVERY -> 35
        },
        recoveryDebtLevel: RecoveryDebtLevel = RecoveryDebtLevel.LOW,
        decisionType: TodayTrainingDecisionType = TodayTrainingDecisionType.STANDARD_TRAINING,
        lastWorkoutFailed: Boolean = false
    ): SystemDecisionValidationContext =
        SystemDecisionValidationContext(
            todayDecision = TodayTrainingDecision(
                dateEpochDay = 0L,
                cycleDay = 1,
                workoutName = "Workout",
                readinessScore = readinessScore,
                readinessLevel = readinessLevel,
                recoveryDebt = RecoveryDebt(
                    value = when (recoveryDebtLevel) {
                        RecoveryDebtLevel.LOW -> 10
                        RecoveryDebtLevel.MODERATE -> 35
                        RecoveryDebtLevel.HIGH -> 60
                        RecoveryDebtLevel.CRITICAL -> 85
                    },
                    level = recoveryDebtLevel,
                    reasons = emptyList()
                ),
                decisionType = decisionType,
                loadMultiplier = 1f,
                volumeMultiplier = 1f,
                reason = "Test",
                warnings = emptyList(),
                selectedWorkoutTemplateId = 1,
                isTrainingAllowed = true
            ),
            lastWorkoutFailed = lastWorkoutFailed
        )
}
