package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ihor.thesystem.data.local.room.entity.NutritionEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun observeEntryForDate(dateEpochDay: Long): Flow<NutritionEntryEntity?>

    @Query("SELECT * FROM nutrition_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getEntryForDate(dateEpochDay: Long): NutritionEntryEntity?

    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY dateEpochDay DESC
        """
    )
    suspend fun getEntriesBetween(
        startEpochDay: Long,
        endEpochDay: Long
    ): List<NutritionEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: NutritionEntryEntity)

    @Query("DELETE FROM nutrition_entries WHERE dateEpochDay = :dateEpochDay")
    suspend fun deleteEntry(dateEpochDay: Long)
}
