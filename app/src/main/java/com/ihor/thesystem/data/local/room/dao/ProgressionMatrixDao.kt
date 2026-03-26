package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.ProgressionMatrixEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressionMatrixDao {
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
}