package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutExerciseCrossRef
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity
import com.ihor.thesystem.data.local.room.relations.OrderedExerciseRecord
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
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: WorkoutExerciseCrossRef)

    @Transaction
    @Query("""
        SELECT e.*, xr.orderIndex FROM exercises e
        INNER JOIN workout_exercise_cross_ref xr ON e.id = xr.exerciseId
        WHERE xr.workoutTemplateId = :templateId
        ORDER BY xr.orderIndex ASC
    """)
    fun getOrderedExercisesForTemplate(templateId: Int): Flow<List<OrderedExerciseRecord>>

    @Query("SELECT name FROM workout_templates WHERE id = :templateId")
    suspend fun getTemplateNameSync(templateId: Int): String?

    @Transaction
    @Query("""
        SELECT e.*, xr.orderIndex FROM exercises e
        INNER JOIN workout_exercise_cross_ref xr ON e.id = xr.exerciseId
        WHERE xr.workoutTemplateId = :templateId
        ORDER BY xr.orderIndex ASC
    """)
    suspend fun getExercisesForTemplateSync(templateId: Int): List<OrderedExerciseRecord>

    @Query("DELETE FROM workout_templates")
    suspend fun deleteAllTemplates()

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM workout_exercise_cross_ref WHERE workoutTemplateId = :templateId")
    suspend fun deleteAllCrossRefsForTemplate(templateId: Int)

    @Query("DELETE FROM workout_exercise_cross_ref WHERE workoutTemplateId = :templateId AND exerciseId = :exerciseId")
    suspend fun deleteCrossRef(templateId: Int, exerciseId: Int)

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getTemplateById(id: Int): WorkoutTemplateEntity?
}


