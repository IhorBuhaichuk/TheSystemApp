package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BodyWeightLog
import com.ihor.thesystem.domain.model.NutritionEntry
import com.ihor.thesystem.domain.model.NutritionFloorStatus
import com.ihor.thesystem.domain.model.NutritionFloorTargetStatus
import com.ihor.thesystem.domain.model.NutritionGoalMode
import com.ihor.thesystem.domain.model.WeightTrend
import com.ihor.thesystem.domain.repository.NutritionRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.util.AppClock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

class SaveNutritionEntryUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    suspend operator fun invoke(entry: NutritionEntry) {
        repository.saveEntry(entry)
    }
}

class GetNutritionFloorStatusUseCase @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val playerRepository: PlayerRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(referenceDate: LocalDate = today()): NutritionFloorStatus {
        val endEpochDay = referenceDate.toEpochDay()
        val startEpochDay = endEpochDay - LOOKBACK_DAYS + 1
        val entries = nutritionRepository
            .getEntriesBetween(startEpochDay, endEpochDay)
            .sortedBy { it.dateEpochDay }
        val bodyWeightPoints = bodyWeightPoints(entries, startEpochDay, endEpochDay)
        val weeklyAverage = bodyWeightPoints
            .map { it.weight }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()

        val proteinStatus = floorStatus(entries.map { it.proteinHit })
        val hydrationStatus = floorStatus(entries.map { it.waterHit })
        val goalMode = entries.maxByOrNull { it.dateEpochDay }?.goalMode ?: NutritionGoalMode.MAINTENANCE

        return NutritionFloorStatus(
            proteinStatus = proteinStatus,
            hydrationStatus = hydrationStatus,
            weeklyWeightAverage = weeklyAverage,
            trend = trendFor(bodyWeightPoints),
            goalMode = goalMode,
            recommendation = recommendationFor(
                proteinStatus = proteinStatus,
                hydrationStatus = hydrationStatus,
                trend = trendFor(bodyWeightPoints),
                goalMode = goalMode,
                hasEntries = entries.isNotEmpty()
            )
        )
    }

    private suspend fun bodyWeightPoints(
        entries: List<NutritionEntry>,
        startEpochDay: Long,
        endEpochDay: Long
    ): List<WeightPoint> {
        val fromWeightLog = playerRepository.getWeightHistory(100)
            .firstOrNull()
            .orEmpty()
            .mapNotNull { it.toWeightPointOrNull() }
            .filter { it.dateEpochDay in startEpochDay..endEpochDay }
            .associateBy { it.dateEpochDay }

        val fromNutrition = entries
            .mapNotNull { entry ->
                entry.bodyWeight?.let { weight ->
                    WeightPoint(dateEpochDay = entry.dateEpochDay, weight = weight)
                }
            }
            .associateBy { it.dateEpochDay }

        return (fromWeightLog + fromNutrition)
            .values
            .sortedBy { it.dateEpochDay }
    }

    private fun BodyWeightLog.toWeightPointOrNull(): WeightPoint? {
        if (weight <= 0f) return null
        val epochDay = Instant.ofEpochMilli(timestamp)
            .atZone(clock.zoneId())
            .toLocalDate()
            .toEpochDay()
        return WeightPoint(dateEpochDay = epochDay, weight = weight)
    }

    private fun floorStatus(hits: List<Boolean>): NutritionFloorTargetStatus {
        if (hits.isEmpty()) return NutritionFloorTargetStatus.UNKNOWN
        val missedDays = hits.count { !it }
        return when {
            missedDays >= MISSED_DAYS_THRESHOLD -> NutritionFloorTargetStatus.MISSED
            hits.any { it } -> NutritionFloorTargetStatus.HIT
            else -> NutritionFloorTargetStatus.UNKNOWN
        }
    }

    private fun trendFor(points: List<WeightPoint>): WeightTrend {
        if (points.size < 2) return WeightTrend.STABLE
        val delta = points.last().weight - points.first().weight
        return when {
            delta <= -TREND_THRESHOLD_KG -> WeightTrend.DOWN
            delta >= TREND_THRESHOLD_KG -> WeightTrend.UP
            else -> WeightTrend.STABLE
        }
    }

    private fun recommendationFor(
        proteinStatus: NutritionFloorTargetStatus,
        hydrationStatus: NutritionFloorTargetStatus,
        trend: WeightTrend,
        goalMode: NutritionGoalMode,
        hasEntries: Boolean
    ): String =
        when {
            !hasEntries -> "Даних по нутриціології поки недостатньо. Почни з білка і води."
            proteinStatus == NutritionFloorTargetStatus.MISSED &&
                hydrationStatus == NutritionFloorTargetStatus.MISSED ->
                "Закрий базу: білок і вода кілька днів поспіль просідають."
            proteinStatus == NutritionFloorTargetStatus.MISSED ->
                "Підтягни білок. Не треба рахувати калорії, просто закрий денний мінімум."
            hydrationStatus == NutritionFloorTargetStatus.MISSED ->
                "Підтягни воду. Це легкий сигнал для відновлення, не штраф."
            goalMode == NutritionGoalMode.DEFICIT && trend == WeightTrend.UP ->
                "Режим дефіциту: тримай білок/воду і спостерігай за 7-day average."
            goalMode == NutritionGoalMode.GAIN && trend == WeightTrend.DOWN ->
                "Режим набору: додай якісний прийом їжі без складного трекінгу."
            else -> "Нутриціологічна база стабільна. Тримай простий мінімум."
        }

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()

    private data class WeightPoint(
        val dateEpochDay: Long,
        val weight: Float
    )

    private companion object {
        const val LOOKBACK_DAYS = 7L
        const val MISSED_DAYS_THRESHOLD = 2
        const val TREND_THRESHOLD_KG = 0.3f
    }
}
