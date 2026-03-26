package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ProgressionMatrixDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.dao.WorkoutAnalyticsDao
import com.ihor.thesystem.data.local.room.entity.ProgressionMatrixEntity
import com.ihor.thesystem.data.local.room.entity.ExerciseSetEntity
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProgressionMatrixRepositoryImpl @Inject constructor(
    private val matrixDao:    ProgressionMatrixDao,
    private val workoutDao:   WorkoutDao,
    private val analyticsDao: WorkoutAnalyticsDao
) : ProgressionMatrixRepository {

    override fun getAllEntries(): Flow<List<ProgressionMatrixEntry>> =
        matrixDao.getAllEntries().map { list ->
            list.map { entity ->
                val name = workoutDao.getExerciseNameById(entity.exerciseId)
                    ?: "Вправа ${entity.exerciseId}"
                entity.toDomain(name, matrixWeeks = 48)
            }
        }

    override suspend fun updateCurrentWeight(exerciseId: Int, newWeight: Float) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        matrixDao.update(existing.copy(currentWeight = newWeight))
    }

    override suspend fun updateMatrixGoals(exerciseId: Int, startWeight: Float, targetWeight: Float) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        matrixDao.update(existing.copy(
            startWeight = startWeight,
            targetWeight = targetWeight,
            currentWeight = startWeight
        ))
    }

    override suspend fun saveExerciseSets(exerciseId: Int, sets: List<WorkoutSetInput>) {
        val sessionId = System.currentTimeMillis() // Спрощено: в реальному додатку прив'язка до активної сесії
        val entities = sets.filter { it.weight.isNotEmpty() && it.reps.isNotEmpty() }.map { input ->
            ExerciseSetEntity(
                sessionId = sessionId,
                exerciseId = exerciseId.toString(),
                weight = input.weight.toDoubleOrNull() ?: 0.0,
                reps = input.reps.toIntOrNull() ?: 0,
                isCompleted = true
            )
        }
        
        if (entities.isNotEmpty()) {
            analyticsDao.insertSets(entities)
            
            val maxWeight = sets.mapNotNull { it.weight.toFloatOrNull() }.maxOrNull()
            if (maxWeight != null) {
                updateCurrentWeight(exerciseId, maxWeight)
            }
        }
    }
}

private fun ProgressionMatrixEntity.toDomain(
    exerciseName: String,
    matrixWeeks: Int
): ProgressionMatrixEntry {
    val weeklyStep = if (targetWeight > 0f && matrixWeeks > 0)
        (targetWeight - startWeight) / matrixWeeks else 0f

    val range    = if (targetWeight > 0f) targetWeight - startWeight else 1f
    val progress = if (range > 0f)
        ((currentWeight - startWeight) / range).coerceIn(0f, 1f) else 0f

    return ProgressionMatrixEntry(
        id                = id,
        exerciseId        = exerciseId,
        exerciseName      = exerciseName,
        startWeight       = startWeight,
        targetWeight      = targetWeight,
        currentWeight     = currentWeight,
        targetWeightNote  = targetWeightNote,
        weeklyStep        = weeklyStep,
        progressPercent   = progress
    )
}
