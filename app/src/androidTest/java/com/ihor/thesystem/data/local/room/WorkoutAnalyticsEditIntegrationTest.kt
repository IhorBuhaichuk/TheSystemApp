package com.ihor.thesystem.data.local.room

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.data.local.room.entity.ExerciseSetLogEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutSessionLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutAnalyticsEditIntegrationTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exerciseScopedEditPreservesSessionSiblingsMetadataAndOtherSameDaySession() = runBlocking {
        val dao = database.workoutAnalyticsDao()
        val timestamp = 1_767_225_600_000L
        val firstSessionId = dao.saveFullSessionLog(
            session = session(timestamp = timestamp, questId = 41L, cycleDay = 5),
            sets = listOf(
                set(exerciseId = EXERCISE_A, weight = 100.0, reps = 5),
                set(exerciseId = EXERCISE_B, weight = 80.0, reps = 5),
                set(exerciseId = EXERCISE_C, weight = 1.0, reps = 30)
            )
        )
        val secondSessionId = dao.saveFullSessionLog(
            session = session(timestamp = timestamp + 3_600_000L, questId = 99L, cycleDay = 6),
            sets = listOf(set(exerciseId = EXERCISE_B, weight = 70.0, reps = 8))
        )

        dao.replaceExerciseSets(
            sessionId = firstSessionId,
            exerciseId = EXERCISE_B,
            sets = listOf(
                set(exerciseId = EXERCISE_B, weight = 120.0, reps = 3, isCompleted = true),
                set(exerciseId = EXERCISE_B, weight = 200.0, reps = 1, isCompleted = false)
            )
        )

        val edited = requireNotNull(dao.getSessionLogById(firstSessionId).first())
        assertEquals(41L, edited.session.questId)
        assertEquals(timestamp, edited.session.timestamp)
        assertEquals(5, edited.session.cycleDay)
        assertEquals(42, edited.session.durationMinutes)
        assertEquals(860.0, edited.session.totalTonnage, 0.0)
        assertEquals(listOf(EXERCISE_A, EXERCISE_B, EXERCISE_B, EXERCISE_C), edited.sets.map { it.exerciseId }.sorted())
        assertEquals(listOf(true, false), edited.sets.filter { it.exerciseId == EXERCISE_B }.map { it.isCompleted })

        val untouched = requireNotNull(dao.getSessionLogById(secondSessionId).first())
        assertEquals(99L, untouched.session.questId)
        assertEquals(listOf(70.0), untouched.sets.map { it.weight })

        dao.replaceExerciseSets(
            sessionId = firstSessionId,
            exerciseId = EXERCISE_B,
            sets = listOf(set(exerciseId = EXERCISE_B, weight = 130.0, reps = 2))
        )

        val repeatedEdit = requireNotNull(dao.getSessionLogById(firstSessionId).first())
        assertEquals(1, repeatedEdit.sets.count { it.exerciseId == EXERCISE_B })
        assertEquals(760.0, repeatedEdit.session.totalTonnage, 0.0)

        try {
            database.withTransaction {
                dao.deleteSetsBySessionAndExercise(firstSessionId, EXERCISE_B)
                error("force rollback")
            }
            fail("The forced transaction failure must escape")
        } catch (expected: IllegalStateException) {
            assertEquals("force rollback", expected.message)
        }

        val afterRollback = requireNotNull(dao.getSessionLogById(firstSessionId).first())
        assertEquals(listOf(130.0), afterRollback.sets.filter { it.exerciseId == EXERCISE_B }.map { it.weight })
    }

    private fun session(
        timestamp: Long,
        questId: Long,
        cycleDay: Int
    ) = WorkoutSessionLogEntity(
        questId = questId,
        timestamp = timestamp,
        totalTonnage = 0.0,
        cycleDay = cycleDay,
        durationMinutes = 42
    )

    private fun set(
        exerciseId: Int,
        weight: Double,
        reps: Int,
        isCompleted: Boolean = true
    ) = ExerciseSetLogEntity(
        sessionId = 0L,
        exerciseId = exerciseId,
        weight = weight,
        reps = reps,
        isCompleted = isCompleted
    )

    private companion object {
        const val EXERCISE_A = 101
        const val EXERCISE_B = 202
        const val EXERCISE_C = 303
    }
}
