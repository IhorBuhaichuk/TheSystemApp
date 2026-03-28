package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.data.local.room.entity.ReferenceMatrixEntity
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput
import kotlinx.coroutines.flow.Flow

interface ProgressionMatrixRepository {
    fun getAllEntries(): Flow<List<ProgressionMatrixEntry>>
    suspend fun updateCurrentWeight(exerciseId: Int, newWeight: Float)
    suspend fun updateMatrixGoals(exerciseId: Int, startWeight: Float, targetWeight: Float)
    suspend fun saveExerciseSets(exerciseId: Int, sets: List<WorkoutSetInput>)
    
    suspend fun getReferenceForExercise(name: String): ReferenceMatrixEntity?
    fun getAllReferences(): Flow<List<ReferenceMatrixEntity>>
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
    val progressPercent: Float
)
