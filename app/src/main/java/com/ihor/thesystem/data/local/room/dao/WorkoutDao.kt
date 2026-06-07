package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutExerciseCrossRef
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity
import com.ihor.thesystem.data.local.room.relations.OrderedExerciseRecord
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesSync(): List<ExerciseEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Int): ExerciseEntity?

    @Query("""
        SELECT * FROM exercises
        WHERE (
            :query IS NULL OR
            name LIKE '%' || :query || '%' OR
            nameUk LIKE '%' || :query || '%'
        )
        AND (:equipmentCount = 0 OR equipment IN (:equipment))
        AND (:levelsCount = 0 OR level IN (:levels))
        AND (:mechanicsCount = 0 OR mechanic IN (:mechanics))
        AND (:forcesCount = 0 OR force IN (:forces))
    """)
    fun searchExercises(
        query: String?,
        equipment: List<String>, equipmentCount: Int,
        levels: List<String>, levelsCount: Int,
        mechanics: List<String>, mechanicsCount: Int,
        forces: List<String>, forcesCount: Int
    ): Flow<List<ExerciseEntity>>

    @RawQuery(observedEntities = [ExerciseEntity::class])
    fun searchExercises(query: SupportSQLiteQuery): Flow<List<ExerciseEntity>>

    @Query("SELECT name FROM exercises WHERE id = :id")
    suspend fun getExerciseNameById(id: Int): String?

    @Query("UPDATE exercises SET trackingMode = :trackingMode WHERE id = :id")
    suspend fun updateExerciseTrackingMode(id: Int, trackingMode: String?)

    @Query("""
        UPDATE exercises
        SET
            isCoreSystemExercise = 0,
            movementPattern = NULL,
            techniqueTips = '[]',
            commonMistakes = '[]',
            substitutionExternalIds = '[]'
    """)
    suspend fun clearCoreExerciseMetadata()

    @Query("""
        UPDATE exercises
        SET
            isCoreSystemExercise = 1,
            movementPattern = :movementPattern,
            techniqueTips = :techniqueTips,
            commonMistakes = :commonMistakes,
            substitutionExternalIds = :substitutionExternalIds
        WHERE externalId = :externalId
    """)
    suspend fun updateCoreExerciseMetadata(
        externalId: String,
        movementPattern: String?,
        techniqueTips: String,
        commonMistakes: String,
        substitutionExternalIds: String
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

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
