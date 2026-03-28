package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ihor.thesystem.data.local.room.entity.ProtocolTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocolTemplateDao {
    @Query("SELECT * FROM protocol_template WHERE cycleDay = :day")
    fun getTemplatesForDay(day: Int): Flow<List<ProtocolTemplateEntity>>

    @Query("SELECT * FROM protocol_template WHERE cycleDay = :day")
    suspend fun getTemplatesForDaySync(day: Int): List<ProtocolTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: ProtocolTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<ProtocolTemplateEntity>)

    @Query("DELETE FROM protocol_template")
    suspend fun clearAll()
}
