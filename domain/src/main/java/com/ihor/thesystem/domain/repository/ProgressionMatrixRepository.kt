package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.ActiveSetInput
import kotlinx.coroutines.flow.Flow

interface ProgressionMatrixRepository {
    fun getAllEntries(): Flow<List<ProgressionMatrixEntry>>
    suspend fun getEntrySync(exerciseId: Int): ProgressionMatrixEntry?
    suspend fun updateCurrentWeight(exerciseId: Int, newWeight: Float)
    suspend fun updateMatrixGoals(exerciseId: Int, startWeight: Float, targetWeight: Float)
    
    suspend fun saveExerciseSets(exerciseId: Int, sets: List<ActiveSetInput>)
    suspend fun saveExerciseSetsWithDate(exerciseId: Int, sets: List<ActiveSetInput>, timestamp: Long, userFeedback: String? = null)

    suspend fun getReferenceForExercise(id: Int): Any?
    suspend fun getReferenceForExercise(name: String): Any?
    fun getAllReferences(): Flow<List<Any>>
    
    suspend fun completeCycle(exerciseId: Int)
    suspend fun setPromotionPending(exerciseId: Int, pending: Boolean)
    suspend fun promoteRank(exerciseId: Int)
    suspend fun updateRank(exerciseId: Int, newRank: Rank)

    suspend fun getExerciseIdsByCategory(category: ExerciseCategory): List<Int>

    /**
     * Оновлює цільові показники вправи на основі рекомендацій ШІ.
     */
    suspend fun updateTarget(
        exerciseId: Int,
        weight: Double,
        sets: Int,
        reps: String,
        aiFeedback: String? = null,
        timestamp: Long = System.currentTimeMillis()
    )
}

data class ProgressionMatrixEntry(
    val id: Int,
    val exerciseId: Int,
    val exerciseName: String,
    val exerciseNameUk: String? = null,
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
    val lastAiFeedback: String? = null,
    val lastAnalyzedTimestamp: Long = 0L
)
