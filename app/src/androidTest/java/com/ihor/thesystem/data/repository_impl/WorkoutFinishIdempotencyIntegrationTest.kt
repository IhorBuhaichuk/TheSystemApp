package com.ihor.thesystem.data.repository_impl

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutDraft
import com.ihor.thesystem.domain.model.WorkoutDraftExercise
import com.ihor.thesystem.domain.model.WorkoutDraftSet
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.util.AppClock
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutFinishIdempotencyIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var analyticsRepository: WorkoutAnalyticsRepositoryImpl
    private lateinit var draftRepository: WorkoutDraftRepositoryImpl

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        analyticsRepository = WorkoutAnalyticsRepositoryImpl(
            dao = database.workoutAnalyticsDao(),
            workoutDao = database.workoutDao(),
            clock = FixedClock
        )
        draftRepository = WorkoutDraftRepositoryImpl(database.workoutDraftDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun concurrentRetryCreatesOneSessionPreservesSetSemanticsAndClosesDraft() = runBlocking {
        draftRepository.saveDraft(draft(COMPLETION_ID))

        val commits = listOf(1, 2).map {
            async(Dispatchers.IO) {
                database.withTransaction {
                    analyticsRepository.commitSessionOnce(COMPLETION_ID, session(), sets()).also {
                        draftRepository.discardDraft(COMPLETION_ID)
                    }
                }
            }
        }.awaitAll()

        assertEquals(1, commits.count { it.wasCreated })
        assertEquals(1, commits.map { it.sessionId }.distinct().size)
        assertEquals(1, scalarCount("SELECT COUNT(*) FROM workout_session_logs"))
        assertEquals(2, scalarCount("SELECT COUNT(*) FROM exercise_set_logs"))
        assertEquals(1, scalarCount("SELECT COUNT(*) FROM exercise_set_logs WHERE isCompleted = 1"))
        assertEquals(1, scalarCount("SELECT COUNT(*) FROM exercise_set_logs WHERE isCompleted = 0"))
        assertNull(draftRepository.observeActiveDraft().firstOrNull())

        val nextCommit = analyticsRepository.commitSessionOnce(
            completionId = "draft-next-workout",
            session = session().copy(timestamp = 2_000L),
            sets = sets()
        )

        assertNotEquals(commits.first().sessionId, nextCommit.sessionId)
        assertEquals(2, scalarCount("SELECT COUNT(*) FROM workout_session_logs"))
    }

    private fun scalarCount(query: String): Int =
        database.openHelper.readableDatabase.query(query).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun session() = WorkoutSession(
        questId = 0L,
        timestamp = 1_000L,
        totalTonnage = 800.0,
        cycleDay = 2,
        durationMinutes = 45
    )

    private fun sets() = listOf(
        ExerciseSet(
            sessionId = 0L,
            exerciseId = 11,
            weight = 50.0,
            reps = 10,
            isCompleted = true
        ),
        ExerciseSet(
            sessionId = 0L,
            exerciseId = 11,
            weight = 50.0,
            reps = 8,
            isCompleted = false
        )
    )

    private fun draft(id: String) = WorkoutDraft(
        id = id,
        dateEpochDay = 20_000L,
        scheduleId = 3,
        cycleDay = 2,
        workoutTemplateId = 7,
        workoutName = "Workout A",
        questId = 0L,
        exercises = listOf(
            WorkoutDraftExercise(
                exerciseId = 11,
                sets = listOf(
                    WorkoutDraftSet(1L, "50", "10", true),
                    WorkoutDraftSet(2L, "50", "8", false)
                )
            )
        ),
        createdAtMillis = 100L,
        updatedAtMillis = 200L
    )

    private object FixedClock : AppClock {
        override fun now(): Long = 1_000L
        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    }

    private companion object {
        const val COMPLETION_ID = "draft-completion-1"
    }
}
