package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.DailyTaskTemplateEntity
import com.ihor.thesystem.data.local.room.entity.ScheduleEntity
import com.ihor.thesystem.data.local.room.entity.ScheduleTaskCrossRef
import com.ihor.thesystem.data.local.room.relations.ScheduleWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Transaction
    @Query("SELECT * FROM schedule ORDER BY cycleDay ASC")
    fun getAllSchedulesWithDetails(): Flow<List<ScheduleWithDetails>>

    @Transaction
    @Query("SELECT * FROM schedule WHERE cycleDay = :day")
    fun getScheduleForDay(day: Int): Flow<ScheduleWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTaskTemplate(task: DailyTaskTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleTaskCrossRef(crossRef: ScheduleTaskCrossRef)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)
}