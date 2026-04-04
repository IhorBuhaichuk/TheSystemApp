package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_log ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_log ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLog(): Flow<WeightLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WeightLogEntity)

    @Delete
    suspend fun delete(log: WeightLogEntity)
}
