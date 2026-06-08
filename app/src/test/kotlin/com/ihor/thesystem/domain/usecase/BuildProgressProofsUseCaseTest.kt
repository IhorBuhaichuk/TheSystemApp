package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ProgressProofType
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.util.AppClock
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildProgressProofsUseCaseTest {

    private val clock = FixedClock(TODAY)
    private val useCase = BuildProgressProofsUseCase(clock)

    @Test
    fun `weighted exercise proof compares estimated one rep max`() {
        val proofs = useCase(
            workoutLogs = listOf(
                workoutLog(
                    timestamp = dateMillis(LocalDate.of(2026, 4, 20)),
                    set = set(exerciseId = BENCH_ID, weight = 50.0, reps = 8)
                ),
                workoutLog(
                    timestamp = dateMillis(LocalDate.of(2026, 6, 1)),
                    set = set(exerciseId = BENCH_ID, weight = 60.0, reps = 8)
                )
            ),
            matrixEntries = listOf(
                matrixEntry(
                    exerciseId = BENCH_ID,
                    name = "Bench Press",
                    trackingMode = ExerciseTrackingMode.WEIGHT_REPS
                )
            )
        )

        val proof = proofs.single { it.exerciseName == "Bench Press" }
        assertEquals(ProgressProofType.STRENGTH, proof.proofType)
        assertEquals("50 кг x 8", proof.previousLabel)
        assertEquals("60 кг x 8", proof.currentLabel)
        assertEquals(20f, proof.percentageChange, 0.1f)
        assertTrue(proof.deltaText.startsWith("+20"))
    }

    @Test
    fun `bodyweight exercise proof compares reps`() {
        val proofs = useCase(
            workoutLogs = listOf(
                workoutLog(
                    timestamp = dateMillis(LocalDate.of(2026, 4, 20)),
                    set = set(exerciseId = PULL_UP_ID, weight = 1.0, reps = 8)
                ),
                workoutLog(
                    timestamp = dateMillis(LocalDate.of(2026, 6, 1)),
                    set = set(exerciseId = PULL_UP_ID, weight = 1.0, reps = 10)
                )
            ),
            matrixEntries = listOf(
                matrixEntry(
                    exerciseId = PULL_UP_ID,
                    name = "Pull-up",
                    trackingMode = ExerciseTrackingMode.BODYWEIGHT_REPS
                )
            )
        )

        val proof = proofs.single { it.exerciseName == "Pull-up" }
        assertEquals(ProgressProofType.REPS, proof.proofType)
        assertEquals("8 повт.", proof.previousLabel)
        assertEquals("10 повт.", proof.currentLabel)
        assertEquals(25f, proof.percentageChange, 0.1f)
    }

    @Test
    fun `no data returns empty proofs`() {
        val proofs = useCase(
            workoutLogs = emptyList(),
            matrixEntries = emptyList()
        )

        assertTrue(proofs.isEmpty())
    }

    private fun workoutLog(
        timestamp: Long,
        set: ExerciseSet
    ): WorkoutLog =
        WorkoutLog(
            session = WorkoutSession(
                sessionId = timestamp,
                questId = 1L,
                timestamp = timestamp,
                totalTonnage = set.weight * set.reps,
                cycleDay = 1
            ),
            sets = listOf(set)
        )

    private fun set(
        exerciseId: Int,
        weight: Double,
        reps: Int
    ): ExerciseSet =
        ExerciseSet(
            sessionId = 1L,
            exerciseId = exerciseId,
            weight = weight,
            reps = reps,
            isCompleted = true
        )

    private fun matrixEntry(
        exerciseId: Int,
        name: String,
        trackingMode: ExerciseTrackingMode
    ): ProgressionMatrixEntry =
        ProgressionMatrixEntry(
            id = exerciseId,
            exerciseId = exerciseId,
            exerciseName = name,
            exerciseTrackingMode = trackingMode.name,
            startWeight = 0f,
            targetWeight = 100f,
            currentWeight = 0f,
            targetWeightNote = null,
            weeklyStep = 0f,
            progressPercent = 0f
        )

    private fun dateMillis(date: LocalDate): Long =
        date.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()

    private class FixedClock(private val today: LocalDate) : AppClock {
        override fun now(): Long =
            today.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = TEST_ZONE
    }

    private companion object {
        const val BENCH_ID = 10
        const val PULL_UP_ID = 20
        val TODAY: LocalDate = LocalDate.of(2026, 6, 7)
        val TEST_ZONE: ZoneId = ZoneId.of("UTC")
    }
}
