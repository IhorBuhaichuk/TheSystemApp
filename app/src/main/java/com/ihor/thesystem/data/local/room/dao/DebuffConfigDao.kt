package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.DebuffConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebuffConfigDao {
    @Query("SELECT * FROM debuff_config")
    fun getAllDebuffs(): Flow<List<DebuffConfigEntity>>

    @Query("SELECT * FROM debuff_config WHERE isActive = 1")
    fun getActiveDebuffs(): Flow<List<DebuffConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debuff: DebuffConfigEntity)

    @Update
    suspend fun update(debuff: DebuffConfigEntity)
}