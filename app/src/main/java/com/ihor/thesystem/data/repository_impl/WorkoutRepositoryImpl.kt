package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.MuscleGroup
import com.ihor.thesystem.domain.repository.WorkoutRepository
import androidx.sqlite.db.SimpleSQLiteQuery
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
            entities.map { it.toExerciseDetails() }
        }

    override suspend fun getAllExercisesSync(): List<ExerciseDetails> =
        workoutDao.getAllExercisesSync().map { it.toExerciseDetails() }

    override fun searchExercises(
        query: String?,
        muscles: List<String>,
        equipment: List<String>,
        levels: List<String>,
        mechanics: List<String>,
        forces: List<String>
    ): Flow<List<ExerciseDetails>> =
        workoutDao.searchExercises(
            buildSearchQuery(
                query = query,
                muscles = muscles,
                equipment = equipment,
                levels = levels,
                mechanics = mechanics,
                forces = forces
            )
        ).map { entities -> entities.map { it.toExerciseDetails() } }

    private fun buildSearchQuery(
        query: String?,
        muscles: List<String>,
        equipment: List<String>,
        levels: List<String>,
        mechanics: List<String>,
        forces: List<String>
    ): SimpleSQLiteQuery {
        val sql = StringBuilder("SELECT * FROM exercises WHERE 1 = 1")
        val args = mutableListOf<Any?>()

        query?.takeIf { it.isNotBlank() }?.let { value ->
            sql.append(" AND (name LIKE ? OR nameUk LIKE ?)")
            val likeQuery = "%$value%"
            args.add(likeQuery)
            args.add(likeQuery)
        }

        sql.appendInClause("equipment", equipment, args)
        sql.appendInClause("level", levels, args)
        sql.appendInClause("mechanic", mechanics, args)
        sql.appendInClause("force", forces, args)
        sql.appendMuscleFilter(muscles, args)

        return SimpleSQLiteQuery(sql.toString(), args.toTypedArray())
    }

    private fun StringBuilder.appendInClause(
        column: String,
        values: List<String>,
        args: MutableList<Any?>
    ) {
        val filteredValues = values.filter { it.isNotBlank() }
        if (filteredValues.isEmpty()) return

        append(" AND ")
        append(column)
        append(" IN (")
        append(filteredValues.joinToString(",") { "?" })
        append(")")
        args.addAll(filteredValues)
    }

    private fun StringBuilder.appendMuscleFilter(
        muscles: List<String>,
        args: MutableList<Any?>
    ) {
        val filteredMuscles = muscles.filter { it.isNotBlank() }
        if (filteredMuscles.isEmpty()) return

        append(" AND (")
        filteredMuscles.forEachIndexed { index, muscle ->
            if (index > 0) append(" OR ")
            append("(',' || IFNULL(muscleGroups, '') || ',') LIKE ?")
            args.add("%,$muscle,%")
        }
        append(")")
    }

    override suspend fun getExerciseNameById(id: Int): String? =
        workoutDao.getExerciseNameById(id)
}
