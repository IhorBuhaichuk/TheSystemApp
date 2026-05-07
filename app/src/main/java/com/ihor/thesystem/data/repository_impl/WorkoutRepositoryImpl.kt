package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
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
            entities.map { it.toDomain() }
        }

    override suspend fun getAllExercisesSync(): List<ExerciseDetails> =
        workoutDao.getAllExercisesSync().map { it.toDomain() }

    override fun searchExercises(
        query: String?,
        muscles: List<String>,
        equipment: List<String>,
        levels: List<String>,
        mechanics: List<String>,
        forces: List<String>
    ): Flow<List<ExerciseDetails>> =
        workoutDao.searchExercises(
            query = query,
            equipment = equipment, equipmentCount = equipment.size,
            levels = levels, levelsCount = levels.size,
            mechanics = mechanics, mechanicsCount = mechanics.size,
            forces = forces, forcesCount = forces.size
        ).map { entities ->
            val domainExercises = entities.map { it.toDomain() }
            if (muscles.isEmpty()) domainExercises
            else domainExercises.filter { exercise ->
                exercise.muscleGroups.any { it.name in muscles }
            }
        }

    private fun ExerciseEntity.toDomain() = ExerciseDetails(
        id = id,
        name = name,
        nameUk = nameUk,
        category = category,
        muscleGroups = muscleGroups,
        equipment = equipment,
        level = level,
        mechanic = mechanic,
        force = force,
        gifUrl = gifUrl,
        externalId = externalId,
        trackingMode = trackingMode
    )

    override suspend fun getExerciseNameById(id: Int): String? =
        workoutDao.getExerciseNameById(id)
}
