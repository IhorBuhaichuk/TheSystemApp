package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BodyWeightLog
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.NutritionEntry
import com.ihor.thesystem.domain.model.NutritionFloorTargetStatus
import com.ihor.thesystem.domain.model.NutritionGoalMode
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.WeightTrend
import com.ihor.thesystem.domain.repository.NutritionRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

class GetNutritionFloorStatusUseCaseTest {

    private val clock = FixedClock(TODAY)
    private val playerRepository = FakePlayerRepository()
    private val nutritionRepository = FakeNutritionRepository()
    private val useCase = GetNutritionFloorStatusUseCase(
        nutritionRepository = nutritionRepository,
        playerRepository = playerRepository,
        clock = clock
    )

    @Test
    fun `weekly average weight uses nutrition body weight entries`() = runTest {
        nutritionRepository.entries = listOf(
            entry(TODAY.minusDays(2), bodyWeight = 80f),
            entry(TODAY.minusDays(1), bodyWeight = 81f),
            entry(TODAY, bodyWeight = 82f)
        )

        val result = useCase(TODAY)

        assertTrue(abs(requireNotNull(result.weeklyWeightAverage) - 81f) < 0.001f)
        assertEquals(WeightTrend.UP, result.trend)
    }

    @Test
    fun `trend calculation detects weight moving down`() = runTest {
        nutritionRepository.entries = listOf(
            entry(TODAY.minusDays(6), bodyWeight = 82.0f),
            entry(TODAY.minusDays(3), bodyWeight = 81.5f),
            entry(TODAY, bodyWeight = 81.0f)
        )

        val result = useCase(TODAY)

        assertEquals(WeightTrend.DOWN, result.trend)
    }

    @Test
    fun `missing data falls back to unknown floor and stable trend`() = runTest {
        nutritionRepository.entries = emptyList()
        playerRepository.weightHistory = emptyList()

        val result = useCase(TODAY)

        assertEquals(NutritionFloorTargetStatus.UNKNOWN, result.proteinStatus)
        assertEquals(NutritionFloorTargetStatus.UNKNOWN, result.hydrationStatus)
        assertNull(result.weeklyWeightAverage)
        assertEquals(WeightTrend.STABLE, result.trend)
        assertTrue(result.recommendation.contains("недостатньо"))
    }

    private fun entry(
        date: LocalDate,
        proteinHit: Boolean = true,
        waterHit: Boolean = true,
        bodyWeight: Float? = null,
        goalMode: NutritionGoalMode = NutritionGoalMode.MAINTENANCE
    ): NutritionEntry =
        NutritionEntry(
            dateEpochDay = date.toEpochDay(),
            proteinHit = proteinHit,
            waterHit = waterHit,
            bodyWeight = bodyWeight,
            goalMode = goalMode
        )

    private class FakeNutritionRepository : NutritionRepository {
        var entries: List<NutritionEntry> = emptyList()

        override fun observeEntryForDate(dateEpochDay: Long): Flow<NutritionEntry?> =
            flowOf(entries.firstOrNull { it.dateEpochDay == dateEpochDay })

        override suspend fun getEntryForDate(dateEpochDay: Long): NutritionEntry? =
            entries.firstOrNull { it.dateEpochDay == dateEpochDay }

        override suspend fun getEntriesBetween(startEpochDay: Long, endEpochDay: Long): List<NutritionEntry> =
            entries.filter { it.dateEpochDay in startEpochDay..endEpochDay }

        override suspend fun saveEntry(entry: NutritionEntry) {
            entries = entries.filterNot { it.dateEpochDay == entry.dateEpochDay } + entry
        }

        override suspend fun deleteEntry(dateEpochDay: Long) {
            entries = entries.filterNot { it.dateEpochDay == dateEpochDay }
        }
    }

    private class FakePlayerRepository : PlayerRepository {
        var weightHistory: List<BodyWeightLog> = emptyList()

        override fun getPlayer(): Flow<Player?> = flowOf(null)
        override suspend fun getPlayerSnapshot(): Player? = null
        override fun getLatestWeight(): Flow<Float?> = flowOf(null)
        override fun getWeightHistory(limit: Int): Flow<List<BodyWeightLog>> = flowOf(weightHistory.take(limit))
        override suspend fun updatePlayer(player: Player): Result<Unit, DataError.Local> = Result.Success(Unit)
        override suspend fun logWeight(weight: Float): Result<Unit, DataError.Local> = Result.Success(Unit)
        override suspend fun updateHeight(height: Float): Result<Unit, DataError.Local> = Result.Success(Unit)
        override suspend fun updateAge(age: Int): Result<Unit, DataError.Local> = Result.Success(Unit)
        override suspend fun updateCurrentCycleDay(day: Int): Result<Unit, DataError.Local> = Result.Success(Unit)
        override suspend fun getWeightByDate(dateMillis: Long): Result<Float?, DataError.Local> = Result.Success(null)
        override suspend fun getWeightAtOrBefore(timestamp: Long): Result<Float?, DataError.Local> = Result.Success(null)
    }

    private class FixedClock(private val today: LocalDate) : AppClock {
        override fun now(): Long =
            today.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = TEST_ZONE
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 6, 8)
        val TEST_ZONE: ZoneId = ZoneId.of("UTC")
    }
}
