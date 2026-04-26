package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.ExerciseDetails
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllExercises(): Flow<List<ExerciseDetails>>
    suspend fun getAllExercisesSync(): List<ExerciseDetails>
    suspend fun getExerciseNameById(id: Int): String?
}
