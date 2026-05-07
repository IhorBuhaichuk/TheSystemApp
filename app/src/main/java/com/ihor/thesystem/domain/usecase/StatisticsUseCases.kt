package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.RankProgressionPolicy
import com.ihor.thesystem.domain.model.toExerciseSetOrNull
import com.ihor.thesystem.domain.model.toStoredActiveSetInputOrNull
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
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase,
    private val calculateProgressRank: CalculateProgressRankUseCase,
    private val clock: AppClock
) {
    suspend operator fun invoke(
        exerciseId: Int,
        sets: List<ActiveSetInput>,
        date: LocalDate,
        userFeedback: String?,
        trackingMode: ExerciseTrackingMode = ExerciseTrackingMode.WEIGHT_REPS
    ) {
        val parsedSets = sets.mapNotNull { it.toExerciseSetOrNull(exerciseId, trackingMode) }
        if (parsedSets.isEmpty()) return

        val timestamp = date.atStartOfDay(clock.zoneId()).toInstant().toEpochMilli()
        val storedInputs = sets.mapNotNull { it.toStoredActiveSetInputOrNull(trackingMode) }

        // 1. Збереження логу підходів
        matrixRepo.saveExerciseSetsWithDate(exerciseId, storedInputs, timestamp, userFeedback)

        if (!trackingMode.usesWeightInput) return

        // 2. Алгоритм автоматичного підвищення рангу
        val maxWeight = parsedSets.maxOf { it.weight }

        val entry = matrixRepo.getEntrySync(exerciseId) ?: return

        val newRank = calculateProgressRank(
            currentWeight = maxWeight,
            startWeight = entry.startWeight.toDouble(),
            targetWeight = entry.targetWeight.toDouble()
        ) ?: return

        // - Перевір математичну вагу (weight: E=1..S=6)
        if (RankProgressionPolicy.shouldPromote(entry.currentRank, newRank)) {
            // - Онови currentRank у базі даних
            matrixRepo.updateRank(exerciseId, newRank)
            
            // - Онови середній арифметичний ранг гравця
            recalculateGlobalRank()
        }
    }
}
