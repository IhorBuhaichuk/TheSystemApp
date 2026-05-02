package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ihor.thesystem.data.local.room.entity.CalendarCycleConfigEntity
import com.ihor.thesystem.data.local.room.entity.CalendarCycleDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarCycleDao {
    @Query("SELECT * FROM calendar_cycle_config WHERE id = 1")
    fun getConfigFlow(): Flow<CalendarCycleConfigEntity?>

    @Query("SELECT * FROM calendar_cycle_day WHERE cycleId = 1 ORDER BY dayIndex ASC")
    fun getDaysFlow(): Flow<List<CalendarCycleDayEntity>>

    @Query("SELECT * FROM calendar_cycle_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigSync(): CalendarCycleConfigEntity?

    @Query("SELECT * FROM calendar_cycle_day WHERE cycleId = 1 ORDER BY dayIndex ASC")
    suspend fun getDaysSync(): List<CalendarCycleDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: CalendarCycleConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<CalendarCycleDayEntity>)

    @Query("DELETE FROM calendar_cycle_day WHERE cycleId = 1")
    suspend fun clearDays()

    @Transaction
    suspend fun replaceCycle(config: CalendarCycleConfigEntity, days: List<CalendarCycleDayEntity>) {
        insertConfig(config)
        clearDays()
        insertDays(days)
    }
}
