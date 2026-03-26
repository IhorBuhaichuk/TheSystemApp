package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.local.room.relations.SessionWithSets
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutAnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSessionLog(session: WorkoutSessionLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSetLogs(sets: List<ExerciseSetLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplaceDirectives(directives: List<WorkoutDirectiveEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMilestone(milestone: ExerciseMilestoneEntity)

    @Transaction
    open suspend fun saveFullSessionLog(session: WorkoutSessionLogEntity, sets: List<ExerciseSetLogEntity>): Long {
        val sessionId = insertSessionLog(session)
        val setsWithId = sets.map { it.copy(sessionId = sessionId) }
        insertSetLogs(setsWithId)
        return sessionId
    }

    @Transaction
    @Query("SELECT * FROM workout_session_logs ORDER BY timestamp DESC")
    abstract fun getAllSessionLogs(): Flow<List<SessionWithSets>>

    /**
     * Статистика тоннажу по днях для графіка
     */
    @Query("""
        SELECT 
            MIN(timestamp) AS dateUnixTimestamp, 
            SUM(totalTonnage) AS totalTonnage
        FROM workout_session_logs
        WHERE timestamp BETWEEN :start AND :end
        GROUP BY date(timestamp / 1000, 'unixepoch')
        ORDER BY dateUnixTimestamp ASC
    """)
    abstract fun getDailyTonnageStats(start: Long, end: Long): Flow<List<DailyTonnageStats>>

    /**
     * Розрахунок пікового тоннажу за весь час
     */
    @Query("SELECT MAX(totalTonnage) FROM workout_session_logs")
    abstract suspend fun getPeakTonnage(): Double?

    /**
     * Отримання останніх директив для вправи
     */
    @Query("SELECT * FROM workout_directives WHERE exerciseId = :exerciseId")
    abstract suspend fun getDirectiveForExercise(exerciseId: String): WorkoutDirectiveEntity?
    
    @Query("DELETE FROM workout_directives")
    abstract suspend fun clearDirectives()
}
