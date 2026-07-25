package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DayFinalizationResult
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SyncTodayStateUseCaseTest {

    private val configRepo: SystemConfigRepository = mockk()
    private val finalizeDay: FinalizeDayUseCase = mockk()
    private val generateDailyQuests: GenerateDailyQuestsUseCase = mockk()
    private val calculateAttributes: CalculateAttributesUseCase = mockk()
    private val logger = NoOpLogger()
    private var config = SystemConfig(lastInitEpochDay = 0L)

    @Test
    fun `new database rest day without quests can refresh repeatedly without finalizing`() = runTest {
        arrangeConfig()
        val useCase = useCase()

        val first = useCase()
        val second = useCase()

        assertTrue(first is Result.Success)
        assertTrue(second is Result.Success)
        coVerify(exactly = 0) { finalizeDay(false) }
        coVerify(exactly = 2) { generateDailyQuests() }
        coVerify(exactly = 1) { calculateAttributes() }
    }

    @Test
    fun `same day sync refreshes quests without recalculating stable attributes`() = runTest {
        config = SystemConfig(lastInitEpochDay = TODAY.toEpochDay())
        arrangeConfig()
        val useCase = useCase()

        useCase()
        useCase()

        coVerify(exactly = 0) { finalizeDay(false) }
        coVerify(exactly = 2) { generateDailyQuests() }
        coVerify(exactly = 0) { calculateAttributes() }
    }

    @Test
    fun `explicit daily init recalculates attributes on the same day`() = runTest {
        config = SystemConfig(
            lastInitEpochDay = TODAY.toEpochDay(),
            needsDailyInit = true
        )
        arrangeConfig()
        val useCase = useCase()

        useCase()

        coVerify(exactly = 1) { generateDailyQuests() }
        coVerify(exactly = 1) { calculateAttributes() }
    }

    @Test
    fun `missed days are caught up exactly once`() = runTest {
        config = SystemConfig(lastInitEpochDay = LAST_SYNC.toEpochDay())
        arrangeConfig()
        coEvery { finalizeDay(false) } answers {
            config = config.copy(lastInitEpochDay = TODAY.toEpochDay())
            Result.Success(DayFinalizationResult.Success)
        }
        val useCase = useCase()

        useCase()
        useCase()

        coVerify(exactly = 1) { finalizeDay(false) }
    }

    private fun arrangeConfig() {
        every { configRepo.getConfigFlow() } answers { flow { emit(config) } }
        coEvery { configRepo.saveLastInitDate(any()) } answers {
            config = config.copy(lastInitEpochDay = invocation.args[0] as Long)
        }
        coEvery { configRepo.setNeedsDailyInit(any()) } answers {
            config = config.copy(needsDailyInit = invocation.args[0] as Boolean)
        }
        coEvery { generateDailyQuests() } just runs
        coEvery { calculateAttributes() } returns Result.Success(CalculatedAttributes(emptyMap()))
    }

    private fun useCase(): SyncTodayStateUseCase =
        SyncTodayStateUseCase(
            transactionProvider = RecordingTransactionProvider(),
            configRepo = configRepo,
            finalizeDay = finalizeDay,
            generateDailyQuests = generateDailyQuests,
            calculateAttributes = calculateAttributes,
            clock = FixedClock(TODAY),
            logger = logger
        )

    private class RecordingTransactionProvider : TransactionProvider {
        override suspend fun <R> runInTransaction(block: suspend () -> R): R = block()
    }

    private class FixedClock(private val today: LocalDate) : AppClock {
        override fun now(): Long =
            today.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = TEST_ZONE
    }

    private class NoOpLogger : AppLogger {
        override fun d(message: String, tag: String?) = Unit
        override fun i(message: String, tag: String?) = Unit
        override fun w(message: String, tag: String?) = Unit
        override fun e(throwable: Throwable?, message: String, tag: String?) = Unit
    }

    private companion object {
        val TEST_ZONE: ZoneId = ZoneId.of("UTC")
        val LAST_SYNC: LocalDate = LocalDate.of(2026, 5, 8)
        val TODAY: LocalDate = LocalDate.of(2026, 5, 11)
    }
}
