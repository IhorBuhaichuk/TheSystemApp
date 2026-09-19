package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ProgressionMatrixDao
import com.ihor.thesystem.data.local.room.dao.WorkoutAnalyticsDao
import com.ihor.thesystem.data.local.room.entity.ExerciseSetLogEntity
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.util.AppClock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class ProgressionMatrixRepositoryImplTest {

    private val matrixDao: ProgressionMatrixDao = mockk(relaxed = true)
    private val analyticsDao: WorkoutAnalyticsDao = mockk(relaxed = true)
    private val transactionProvider = RecordingTransactionProvider()
    private val repository = ProgressionMatrixRepositoryImpl(
        matrixDao = matrixDao,
        analyticsDao = analyticsDao,
        transactionProvider = transactionProvider,
        clock = FixedClock
    )

    @Test
    fun `explicit session edit delegates to exercise scoped Room transaction`() = runTest {
        val setsSlot = slot<List<ExerciseSetLogEntity>>()
        coEvery { analyticsDao.replaceExerciseSets(55L, 9, capture(setsSlot)) } coAnswers {
            assertTrue(transactionProvider.inTransaction)
        }

        repository.saveExerciseSetsWithDate(
            sessionId = 55L,
            exerciseId = 9,
            sets = listOf(
                ActiveSetInput(weight = "1", reps = "60", isCompleted = true),
                ActiveSetInput(weight = "1", reps = "30", isCompleted = false)
            ),
            timestamp = 999L,
            userFeedback = "steady"
        )

        assertEquals(listOf(55L, 55L), setsSlot.captured.map { it.sessionId })
        assertEquals(listOf(9, 9), setsSlot.captured.map { it.exerciseId })
        assertEquals(listOf(true, false), setsSlot.captured.map { it.isCompleted })
        coVerify(exactly = 1) { analyticsDao.replaceExerciseSets(55L, 9, any()) }
        coVerify(exactly = 0) { analyticsDao.getLogsForExerciseOnDate(any(), any(), any()) }
        coVerify(exactly = 0) { analyticsDao.saveFullSessionLog(any(), any()) }
    }

    @Test
    fun `new manual entry creates a separate session instead of guessing by date`() = runTest {
        coEvery { analyticsDao.saveFullSessionLog(any(), any()) } returns 101L

        repository.saveExerciseSetsWithDate(
            sessionId = null,
            exerciseId = 9,
            sets = listOf(ActiveSetInput(weight = "50", reps = "5", isCompleted = true)),
            timestamp = 999L
        )

        coVerify(exactly = 1) { analyticsDao.saveFullSessionLog(any(), any()) }
        coVerify(exactly = 0) { analyticsDao.getLogsForExerciseOnDate(any(), any(), any()) }
        coVerify(exactly = 0) { analyticsDao.replaceExerciseSets(any(), any(), any()) }
    }

    private class RecordingTransactionProvider : TransactionProvider {
        var inTransaction = false
            private set

        override suspend fun <R> runInTransaction(block: suspend () -> R): R {
            check(!inTransaction)
            inTransaction = true
            return try {
                block()
            } finally {
                inTransaction = false
            }
        }
    }

    private object FixedClock : AppClock {
        override fun now(): Long = 1_000L
        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    }
}
