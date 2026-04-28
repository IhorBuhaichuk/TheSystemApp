package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.data.local.room.entity.ReferenceMatrixEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class SetRecommendation(
    val weight: Double,
    val reps: Int,
    val sets: Int = 3,
    val isProgression: Boolean
)

class CalculateRecommendedSetUseCase @Inject constructor(
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val matrixRepo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int, exerciseName: String): SetRecommendation {
        // 1. Отримуємо останні 3 сети з БД (Оптимізовано)
        val lastSets: List<ExerciseSet> = analyticsRepo.getLastSetsForExercise(exerciseId)

        val entry = matrixRepo.getEntrySync(exerciseId)
        val startWeight = entry?.startWeight?.toDouble() ?: 0.0
        val progressionStep = 2.5 // Default step

        if (lastSets.isEmpty()) {
            // Якщо раніше не робили - стартуємо з M0
            return SetRecommendation(
                weight = startWeight,
                reps = 12,
                isProgression = false
            )
        }

        // 4. Аналіз успішності (3x12 в ОСТАННІЙ сесії)
        val targetReps = 12
        // Тепер lastSets містить всі сети ОСТАННЬОЇ сесії завдяки оновленому DAO
        val wasSuccessful = lastSets.size >= 3 && lastSets.all { it.reps >= targetReps }
        val lastWeight = lastSets.first().weight // Тепер це вага з останнього тренування

        return if (wasSuccessful) {
            // Якщо закрили 3х12 - підвищуємо вагу, скидаємо повтори до 8
            SetRecommendation(
                weight = lastWeight + progressionStep,
                reps = 8, 
                isProgression = true
            )
        } else {
            // Якщо не закрили - вага та ж, пробуємо зробити більше повторів
            val lastMaxReps = lastSets.maxOf { it.reps }
            SetRecommendation(
                weight = lastWeight,
                reps = (lastMaxReps + 1).coerceAtMost(12),
                isProgression = false
            )
        }
    }
}
