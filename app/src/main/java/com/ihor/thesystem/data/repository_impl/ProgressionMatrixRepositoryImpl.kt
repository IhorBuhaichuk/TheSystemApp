package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.PlayerDao
import com.ihor.thesystem.data.local.room.dao.ProgressionMatrixDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.dao.WorkoutAnalyticsDao
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.roundToInt
import java.util.Calendar

class ProgressionMatrixRepositoryImpl @Inject constructor(
    private val matrixDao:    ProgressionMatrixDao,
    private val workoutDao:   WorkoutDao,
    private val analyticsDao: WorkoutAnalyticsDao,
    private val playerDao:    PlayerDao
) : ProgressionMatrixRepository {

    override fun getAllEntries(): Flow<List<ProgressionMatrixEntry>> =
        matrixDao.getAllEntriesWithNames().map { list ->
            list.map { item ->
                item.entity.toDomain(item.exerciseName, matrixWeeks = 48)
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
        saveExerciseSetsWithDate(exerciseId, sets, System.currentTimeMillis())
    }

    override suspend fun saveExerciseSetsWithDate(exerciseId: Int, sets: List<WorkoutSetInput>, timestamp: Long) {
        val validSets = sets.filter { it.weight.isNotEmpty() && it.reps.isNotEmpty() }
        if (validSets.isEmpty()) return

        // Визначаємо межі дня для пошуку дублікатів
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val totalTonnage = validSets.sumOf { 
            (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toIntOrNull() ?: 0)
        }

        // Перевіряємо, чи вже був запис цієї вправи сьогодні
        val existingSetLog = analyticsDao.getLogForExerciseOnDate(exerciseId, startOfDay, endOfDay)

        if (existingSetLog != null) {
            // Оновлюємо існуючу сесію замість створення нової
            val sessionId = existingSetLog.sessionId
            
            // 1. Оновлюємо заголовок сесії (тоннаж та час)
            analyticsDao.insertSessionLog(
                WorkoutSessionLogEntity(
                    sessionId = sessionId,
                    questId = 0,
                    timestamp = timestamp,
                    totalTonnage = totalTonnage,
                    cycleDay = 0,
                    durationMinutes = 0
                )
            )

            // 2. Перезаписуємо підходи
            analyticsDao.deleteSetsBySession(sessionId)
            val entities = validSets.map { input ->
                ExerciseSetLogEntity(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    weight = input.weight.toDoubleOrNull() ?: 0.0,
                    reps = input.reps.toIntOrNull() ?: 0,
                    isCompleted = true
                )
            }
            analyticsDao.insertSetLogs(entities)
        } else {
            // Якщо записів сьогодні не було - створюємо нову сесію
            val sessionLog = WorkoutSessionLogEntity(
                questId = 0,
                timestamp = timestamp,
                totalTonnage = totalTonnage,
                cycleDay = 0,
                durationMinutes = 0
            )

            val entities = validSets.map { input ->
                ExerciseSetLogEntity(
                    sessionId = 0,
                    exerciseId = exerciseId,
                    weight = input.weight.toDoubleOrNull() ?: 0.0,
                    reps = input.reps.toIntOrNull() ?: 0,
                    isCompleted = true
                )
            }
            analyticsDao.saveFullSessionLog(sessionLog, entities)
        }
        
        // Оновлюємо поточну вагу в матриці (беремо максимум з нових підходів)
        val maxWeight = validSets.mapNotNull { it.weight.toFloatOrNull() }.maxOrNull()
        if (maxWeight != null) {
            updateCurrentWeight(exerciseId, maxWeight)
        }
    }

    override suspend fun getReferenceForExercise(name: String): ReferenceMatrixEntity? {
        return matrixDao.getReferenceByName(name)
    }

    override fun getAllReferences(): Flow<List<ReferenceMatrixEntity>> {
        return matrixDao.getAllReferences()
    }

    override suspend fun completeCycle(exerciseId: Int) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        val nextCycles = existing.completedCycles + 1
        
        matrixDao.update(existing.copy(
            completedCycles = nextCycles,
            isPromotionPending = true
        ))
    }

    override suspend fun promoteRank(exerciseId: Int) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        val nextRank = when (existing.currentRank) {
            Rank.E -> Rank.D
            Rank.D -> Rank.C
            Rank.C -> Rank.B
            Rank.B -> Rank.A
            Rank.A -> Rank.S
            Rank.S -> Rank.S
        }
        matrixDao.update(existing.copy(
            isPromotionPending = false,
            currentRank = nextRank
        ))
        recalculateGlobalRank()
    }

    override suspend fun updateRank(exerciseId: Int, newRank: Rank) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        if (existing.currentRank != newRank) {
            matrixDao.update(existing.copy(currentRank = newRank))
        }
    }

    override suspend fun setPromotionPending(exerciseId: Int, pending: Boolean) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        matrixDao.update(existing.copy(isPromotionPending = pending))
    }

    override suspend fun recalculateGlobalRank() {
        val entries = matrixDao.getAllEntriesWithNames().first()
        if (entries.isEmpty()) return
        
        val avgValue = entries.map { it.entity.currentRank.value }.average().roundToInt()
        val globalRank = Rank.fromValue(avgValue)
        
        val player = playerDao.getPlayerSync() ?: return
        playerDao.update(player.copy(globalRank = globalRank))
    }

    override suspend fun updateTarget(exerciseId: Long, weight: Double, sets: Int, reps: String) {
        val id = exerciseId.toInt()
        val existing = matrixDao.getEntryForExerciseSync(id) ?: return
        matrixDao.update(existing.copy(
            nextRecommendedWeight = weight,
            nextRecommendedSets = sets,
            nextRecommendedReps = reps,
            targetWeightNote = "Рекомендація AI ($sets sets, $reps reps)"
        ))
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
        id                = this.exerciseId,
        exerciseId        = this.exerciseId,
        exerciseName      = exerciseName,
        startWeight       = startWeight,
        targetWeight      = targetWeight,
        currentWeight     = currentWeight,
        targetWeightNote  = targetWeightNote,
        weeklyStep        = weeklyStep,
        progressPercent   = progress,
        currentRank       = currentRank,
        completedCycles   = completedCycles,
        isPromotionPending = isPromotionPending,
        nextRecommendedWeight = nextRecommendedWeight,
        nextRecommendedSets = nextRecommendedSets,
        nextRecommendedReps = nextRecommendedReps
    )
}
