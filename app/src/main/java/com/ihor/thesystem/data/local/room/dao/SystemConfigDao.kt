package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.SystemConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemConfigDao {
    @Query("SELECT * FROM system_config WHERE id = 1")
    fun getConfig(): Flow<SystemConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: SystemConfigEntity)

    @Update
    suspend fun update(config: SystemConfigEntity)
}