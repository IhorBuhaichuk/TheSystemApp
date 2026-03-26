package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutExerciseCrossRef
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity
import com.ihor.thesystem.data.local.room.relations.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workout_template")
    fun getAllWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_template WHERE id = :id")
    fun getWorkoutWithExercises(id: Int): Flow<WorkoutWithExercises?>

    @Query("SELECT name FROM workout_template WHERE id = :id")
    suspend fun getTemplateName(id: Int): String?

    @Query("""
        SELECT e.name FROM exercise e 
        INNER JOIN workout_exercise_cross_ref cr ON e.id = cr.exerciseId 
        WHERE cr.workoutTemplateId = :templateId 
        ORDER BY cr.orderIndex ASC
    """)
    suspend fun getExerciseNamesForTemplate(templateId: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: WorkoutExerciseCrossRef)

    @Query("SELECT name FROM exercise WHERE id = :id")
    suspend fun getExerciseNameById(id: Int): String?

    @Query("SELECT * FROM exercise WHERE id = :id")
    suspend fun getExerciseById(id: Int): ExerciseEntity?

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplateEntity)
}