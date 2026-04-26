package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.MuscleGroup
import com.ihor.thesystem.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ExerciseExtendedDetails(
    val id: Int,
    val name: String,
    val muscleGroups: List<MuscleGroup>,
    val equipment: String?,
    val instructions: String?,
    val gifUrl: String?
)

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun getAllExercises(): Flow<List<ExerciseDetails>> =
        workoutDao.getAllExercises().map { entities ->
            entities.map { ExerciseDetails(it.id, it.name, it.muscleGroups) }
        }

    override suspend fun getAllExercisesSync(): List<ExerciseDetails> =
        workoutDao.getAllExercisesSync().map { ExerciseDetails(it.id, it.name, it.muscleGroups) }

    fun getAllExercisesExtended(): Flow<List<ExerciseExtendedDetails>> =
        workoutDao.getAllExercises().map { entities ->
            entities.map { 
                ExerciseExtendedDetails(
                    it.id, it.name, it.muscleGroups, it.equipment, it.instructions, it.gifUrl
                )
            }
        }

    override suspend fun getExerciseNameById(id: Int): String? =
        workoutDao.getExerciseNameById(id)
}
