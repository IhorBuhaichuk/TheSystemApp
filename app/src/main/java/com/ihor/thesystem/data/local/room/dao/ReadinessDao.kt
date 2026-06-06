package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ihor.thesystem.data.local.room.entity.ReadinessEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadinessDao {

    @Query("SELECT * FROM readiness_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun observeEntryForDate(dateEpochDay: Long): Flow<ReadinessEntryEntity?>

    @Query("SELECT * FROM readiness_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getEntryForDate(dateEpochDay: Long): ReadinessEntryEntity?

    @Query(
        """
        SELECT * FROM readiness_entries
        WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY dateEpochDay DESC
        """
    )
    suspend fun getEntriesBetween(
        startEpochDay: Long,
        endEpochDay: Long
    ): List<ReadinessEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ReadinessEntryEntity): Long

    @Query("DELETE FROM readiness_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)
}
