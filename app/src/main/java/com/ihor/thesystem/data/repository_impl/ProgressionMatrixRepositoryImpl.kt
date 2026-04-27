package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.R
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneId

class ProgressionMatrixRepositoryImpl @Inject constructor(
    private val matrixDao:    ProgressionMatrixDao,
    private val analyticsDao: WorkoutAnalyticsDao,
    @ApplicationContext private val context: Context
) : ProgressionMatrixRepository {

    override fun getAllEntries(): Flow<List<ProgressionMatrixEntry>> =
        matrixDao.getAllEntriesWithNames().map { list ->
            list.map { item ->
                item.entity.toDomain(item.exerciseName, item.exerciseNameUk, matrixWeeks = 48)
            }
        }

    override suspend fun getEntrySync(exerciseId: Int): ProgressionMatrixEntry? {
        val item = matrixDao.getEntryWithExerciseNameSync(exerciseId) ?: return null
        return item.entity.toDomain(item.exerciseName, item.exerciseNameUk, matrixWeeks = 48)
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

    override suspend fun saveExerciseSets(exerciseId: Int, sets: List<ActiveSetInput>) {
        saveExerciseSetsWithDate(exerciseId, sets, System.currentTimeMillis())
    }

    override suspend fun saveExerciseSetsWithDate(exerciseId: Int, sets: List<ActiveSetInput>, timestamp: Long, userFeedback: String?) {
        val validSets = sets.filter { it.weight.isNotEmpty() && it.reps.isNotEmpty() }
        if (validSets.isEmpty()) return

        val zoneId = ZoneId.systemDefault()
        val date = java.time.Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val totalTonnage = validSets.sumOf { 
            (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toIntOrNull() ?: 0)
        }

        val existingLogs = analyticsDao.getLogsForExerciseOnDate(exerciseId, startOfDay, endOfDay)

        if (existingLogs.isNotEmpty()) {
            val sessionId = existingLogs.first().sessionId
            
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

            analyticsDao.deleteSetsBySession(sessionId)
            val entities = validSets.map { input ->
                ExerciseSetLogEntity(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    weight = input.weight.toDoubleOrNull() ?: 0.0,
                    reps = input.reps.toIntOrNull() ?: 0,
                    isCompleted = true,
                    userFeedback = userFeedback
                )
            }
            analyticsDao.insertSetLogs(entities)
        } else {
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
                    isCompleted = true,
                    userFeedback = userFeedback
                )
            }
            analyticsDao.saveFullSessionLog(sessionLog, entities)
        }
        
        val maxWeight = validSets.mapNotNull { it.weight.toFloatOrNull() }.maxOrNull()
        if (maxWeight != null) {
            updateCurrentWeight(exerciseId, maxWeight)
        }
    }

    override suspend fun getReferenceForExercise(id: Int): ReferenceMatrixEntity? {
        return matrixDao.getReferenceById(id)
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

    override suspend fun getExerciseIdsByCategory(category: ExerciseCategory): List<Int> {
        return matrixDao.getExerciseIdsByCategory(category)
    }

    override suspend fun updateTarget(
        exerciseId: Int,
        weight: Double,
        sets: Int,
        reps: String,
        aiFeedback: String?,
        timestamp: Long
    ) {
        val existing = matrixDao.getEntryForExerciseSync(exerciseId) ?: return
        val note = UiText.StringResource(R.string.text_ai_recommendation_note, listOf(sets, reps)).asString(context)
        matrixDao.update(existing.copy(
            nextRecommendedWeight = weight,
            nextRecommendedSets = sets,
            nextRecommendedReps = reps,
            lastAiFeedback = aiFeedback,
            lastAnalyzedTimestamp = timestamp,
            targetWeightNote = note
        ))
    }
}

private fun ProgressionMatrixEntity.toDomain(
    exerciseName: String,
    exerciseNameUk: String?,
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
        exerciseNameUk    = exerciseNameUk,
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
        nextRecommendedReps = nextRecommendedReps,
        lastAiFeedback = lastAiFeedback,
        lastAnalyzedTimestamp = lastAnalyzedTimestamp
    )
}
