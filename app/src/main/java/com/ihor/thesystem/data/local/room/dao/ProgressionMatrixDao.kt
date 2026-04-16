package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.ProgressionMatrixEntity
import com.ihor.thesystem.data.local.room.entity.ReferenceMatrixEntity
import com.ihor.thesystem.domain.model.ExerciseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressionMatrixDao {
    @Query("""
        SELECT pm.*, e.name as exerciseName 
        FROM progression_matrix pm
        JOIN exercises e ON pm.exerciseId = e.id
    """)
    fun getAllEntriesWithNames(): Flow<List<ProgressionMatrixWithExercise>>

    @Query("SELECT * FROM progression_matrix")
    fun getAllEntries(): Flow<List<ProgressionMatrixEntity>>

    @Query("SELECT * FROM progression_matrix WHERE exerciseId = :exerciseId")
    fun getEntryForExercise(exerciseId: Int): Flow<ProgressionMatrixEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ProgressionMatrixEntity)

    @Update
    suspend fun update(entry: ProgressionMatrixEntity)

    @Query("SELECT * FROM progression_matrix WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getEntryForExerciseSync(exerciseId: Int): ProgressionMatrixEntity?

    @Query("""
        SELECT pm.*, e.name as exerciseName 
        FROM progression_matrix pm
        JOIN exercises e ON pm.exerciseId = e.id
        WHERE pm.exerciseId = :exerciseId LIMIT 1
    """)
    suspend fun getEntryWithExerciseName(exerciseId: Int): ProgressionMatrixWithExercise?

    @Query("SELECT id FROM exercises WHERE category = :category")
    suspend fun getExerciseIdsByCategory(category: ExerciseCategory): List<Int>

    // --- Reference Matrix Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReference(entry: ReferenceMatrixEntity)

    @Query("SELECT * FROM reference_matrix")
    fun getAllReferences(): Flow<List<ReferenceMatrixEntity>>

    @Query("SELECT * FROM reference_matrix WHERE exerciseName = :name LIMIT 1")
    suspend fun getReferenceByName(name: String): ReferenceMatrixEntity?

    @Query("SELECT * FROM reference_matrix WHERE exerciseId = :id LIMIT 1")
    suspend fun getReferenceById(id: String): ReferenceMatrixEntity?
}

data class ProgressionMatrixWithExercise(
    @Embedded val entity: ProgressionMatrixEntity,
    val exerciseName: String
)
