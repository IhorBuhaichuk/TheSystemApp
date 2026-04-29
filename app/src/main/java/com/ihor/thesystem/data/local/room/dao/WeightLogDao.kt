package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_log ORDER BY timestamp DESC LIMIT :limit")
    fun getAllLogs(limit: Int = 100): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_log ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLog(): Flow<WeightLogEntity?>

    @Query("""
        SELECT weight FROM weight_log 
        WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = date(:dateMillis / 1000, 'unixepoch', 'localtime') 
        ORDER BY timestamp DESC LIMIT 1
    """)
    suspend fun getWeightByDate(dateMillis: Long): Float?

    @Query("SELECT * FROM weight_log WHERE timestamp <= :targetTimestamp ORDER BY timestamp DESC LIMIT 1")
    suspend fun getWeightAtOrBefore(targetTimestamp: Long): WeightLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WeightLogEntity)

    @Delete
    suspend fun delete(log: WeightLogEntity)
}
