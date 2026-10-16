package com.ihor.thesystem.data.repository_impl

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.data.local.room.database.DatabaseMigrations
import com.ihor.thesystem.domain.model.WorkoutDraft
import com.ihor.thesystem.domain.model.WorkoutDraftExercise
import com.ihor.thesystem.domain.model.WorkoutDraftSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDraftRepositoryImplTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "workout-draft-${System.nanoTime()}.db"
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = openDatabase()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun draftSurvivesDatabaseRecreationWithInputsAndCompletionFlags() = runBlocking {
        val original = draft()
        WorkoutDraftRepositoryImpl(database.workoutDraftDao()).saveDraft(original)

        database.close()
        database = openDatabase()

        val restored = WorkoutDraftRepositoryImpl(database.workoutDraftDao())
            .observeActiveDraft()
            .first()

        assertEquals(original, restored)
    }

    @Test
    fun saveReplacesSnapshotAtomicallyAndDiscardCascadesSets() = runBlocking {
        val repository = WorkoutDraftRepositoryImpl(database.workoutDraftDao())
        val changed = draft().copy(
            updatedAtMillis = 300L,
            exercises = listOf(
                WorkoutDraftExercise(
                    exerciseId = 11,
                    sets = listOf(WorkoutDraftSet(1L, "22.5", "9", true))
                )
            )
        )

        repository.saveDraft(draft())
        repository.saveDraft(changed)

        assertEquals(changed, repository.observeActiveDraft().first())

        repository.discardDraft(changed.id)

        assertNull(repository.observeActiveDraft().first())
        database.openHelper.readableDatabase
            .query("SELECT COUNT(*) FROM workout_draft_sets")
            .use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
    }

    private fun openDatabase(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .build()

    private fun draft() = WorkoutDraft(
        id = "draft-1",
        dateEpochDay = 20_000L,
        scheduleId = 3,
        cycleDay = 2,
        workoutTemplateId = 7,
        workoutName = "Workout A",
        questId = 42L,
        exercises = listOf(
            WorkoutDraftExercise(
                exerciseId = 11,
                sets = listOf(
                    WorkoutDraftSet(1L, "20", "10", true),
                    WorkoutDraftSet(2L, "20", "8", false)
                )
            ),
            WorkoutDraftExercise(
                exerciseId = 22,
                sets = listOf(WorkoutDraftSet(3L, "", "12", false))
            )
        ),
        createdAtMillis = 100L,
        updatedAtMillis = 200L
    )
}

