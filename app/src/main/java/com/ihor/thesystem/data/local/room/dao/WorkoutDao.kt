package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutExerciseCrossRef
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesSync(): List<ExerciseEntity>

    @Query("SELECT name FROM exercises WHERE id = :id")
    suspend fun getExerciseNameById(id: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: WorkoutExerciseCrossRef)

    @Transaction
    @Query("""
        SELECT e.* FROM exercises e
        INNER JOIN workout_exercise_cross_ref xr ON e.id = xr.exerciseId
        INNER JOIN workout_templates wt ON xr.workoutTemplateId = wt.id
        WHERE wt.id = :templateId
        ORDER BY xr.orderIndex ASC
    """)
    fun getOrderedExercisesForTemplate(templateId: Int): Flow<List<ExerciseEntity>>

    @Query("SELECT name FROM workout_templates WHERE id = :templateId")
    suspend fun getTemplateNameSync(templateId: Int): String?

    @Transaction
    @Query("""
        SELECT e.* FROM exercises e
        INNER JOIN workout_exercise_cross_ref xr ON e.id = xr.exerciseId
        WHERE xr.workoutTemplateId = :templateId
        ORDER BY xr.orderIndex ASC
    """)
    suspend fun getExercisesForTemplateSync(templateId: Int): List<ExerciseEntity>

    @Query("DELETE FROM workout_templates")
    suspend fun deleteAllTemplates()
}


