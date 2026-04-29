package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.core.util.OneRepMaxCalculator
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.AnnualMatrixProvider
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.model.ActiveSetInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject

class GetProgressionMatrixUseCase @Inject constructor(
    private val repo: ProgressionMatrixRepository
) {
    operator fun invoke(): Flow<List<ProgressionMatrixEntry>> = repo.getAllEntries()
}

class UpdateExerciseWeightUseCase @Inject constructor(
    private val repo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int, newWeight: Float) {
        if (newWeight > 0f) repo.updateCurrentWeight(exerciseId, newWeight)
    }
}

class GetLogForDateUseCase @Inject constructor(
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(exerciseId: Int, date: LocalDate): List<ExerciseSet> {
        val zoneId = clock.zoneId()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return analyticsRepo.getLogsForExerciseOnDate(exerciseId, startOfDay, endOfDay)
    }
}

class SaveExerciseSetsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val playerRepo: PlayerRepository,
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase,
    private val clock: AppClock
) {
    suspend operator fun invoke(exerciseId: Int, sets: List<ActiveSetInput>, date: LocalDate, userFeedback: String?) {
        val parsedSets = sets.mapNotNull { it.toParsedSet() }
        if (parsedSets.isEmpty()) return

        val timestamp = date.atStartOfDay(clock.zoneId()).toInstant().toEpochMilli()

        // 1. Збереження логу підходів
        matrixRepo.saveExerciseSetsWithDate(exerciseId, sets, timestamp, userFeedback)

        // 2. Алгоритм автоматичного підвищення рангу
        val maxWeight = parsedSets.maxOf { it.weight }
        val maxReps = parsedSets.filter { it.weight == maxWeight }
            .maxOfOrNull { it.reps } ?: 1

        // - Розрахуй 1RM введеного результату
        val current1RM = OneRepMaxCalculator.calculate(maxWeight, maxReps)

        // - Отримай поточну вправу та вагу гравця
        val entry = matrixRepo.getEntrySync(exerciseId) ?: return
        val playerWeight = playerRepo.getLatestWeight().firstOrNull()?.toDouble() ?: 80.0

        // - Визнач newRank за допомогою AnnualMatrixProvider через exerciseId
        val newRank = AnnualMatrixProvider.getExerciseRankById(exerciseId, current1RM, playerWeight)

        // - Перевір математичну вагу (weight: E=1..S=6)
        if (newRank.weight > entry.currentRank.weight) {
            // - Онови currentRank у базі даних
            matrixRepo.updateRank(exerciseId, newRank)
            
            // - Онови середній арифметичний ранг гравця
            recalculateGlobalRank()
        }
    }

    private data class ParsedSet(val weight: Double, val reps: Int)

    private fun ActiveSetInput.toParsedSet(): ParsedSet? {
        val parsedWeight = weight.replace(',', '.').toDoubleOrNull()
            ?.takeIf { it > 0.0 }
            ?: return null
        val parsedReps = reps.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: return null

        return ParsedSet(parsedWeight, parsedReps)
    }
}
