package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.SystemConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemConfigDao {
    @Query("SELECT * FROM system_config WHERE id = 1")
    fun getConfigFlow(): Flow<SystemConfigEntity?>

    @Query("SELECT * FROM system_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigSync(): SystemConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: SystemConfigEntity)

    @Update
    suspend fun update(config: SystemConfigEntity)

    @Query("INSERT OR IGNORE INTO system_config(id) VALUES(1)")
    suspend fun ensureConfigExists()

    @Query("UPDATE system_config SET needsDailyInit = :needed WHERE id = 1")
    suspend fun updateNeedsDailyInit(needed: Boolean)

    @Query("UPDATE system_config SET lastInitEpochDay = :epochDay WHERE id = 1")
    suspend fun updateLastInitEpochDay(epochDay: Long)
}
