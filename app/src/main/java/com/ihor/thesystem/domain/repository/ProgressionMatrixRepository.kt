package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.data.local.room.entity.ReferenceMatrixEntity
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput
import kotlinx.coroutines.flow.Flow

interface ProgressionMatrixRepository {
    fun getAllEntries(): Flow<List<ProgressionMatrixEntry>>
    suspend fun updateCurrentWeight(exerciseId: Int, newWeight: Float)
    suspend fun updateMatrixGoals(exerciseId: Int, startWeight: Float, targetWeight: Float)
    suspend fun saveExerciseSets(exerciseId: Int, sets: List<WorkoutSetInput>)
    suspend fun saveExerciseSetsWithDate(exerciseId: Int, sets: List<WorkoutSetInput>, timestamp: Long, userFeedback: String? = null)
    
    suspend fun getReferenceForExercise(name: String): ReferenceMatrixEntity?
    fun getAllReferences(): Flow<List<ReferenceMatrixEntity>>
    
    suspend fun completeCycle(exerciseId: Int)
    suspend fun recalculateGlobalRank()
    suspend fun setPromotionPending(exerciseId: Int, pending: Boolean)
    suspend fun promoteRank(exerciseId: Int)
    suspend fun updateRank(exerciseId: Int, newRank: Rank)

    /**
     * Оновлює цільові показники вправи на основі рекомендацій ШІ.
     */
    suspend fun updateTarget(exerciseId: Long, weight: Double, sets: Int, reps: String, aiFeedback: String? = null)
}

data class ProgressionMatrixEntry(
    val id: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val startWeight: Float,
    val targetWeight: Float,
    val currentWeight: Float,
    val targetWeightNote: String?,
    val weeklyStep: Float,
    val progressPercent: Float,
    val currentRank: Rank = Rank.E,
    val completedCycles: Int = 0,
    val isPromotionPending: Boolean = false,
    val nextRecommendedWeight: Double? = null,
    val nextRecommendedSets: Int? = null,
    val nextRecommendedReps: String? = null,
    val lastAiFeedback: String? = null
)
