package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
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
    abstract suspend fun insertSetLog(log: ExerciseSetLogEntity): Long

    @Update
    abstract suspend fun updateSetLog(log: ExerciseSetLogEntity)

    /**
     * Знаходить всі логи вправи за вказаний діапазон часу.
     */
    @Query("""
        SELECT e.* FROM exercise_set_logs e
        JOIN workout_session_logs s ON e.sessionId = s.sessionId
        WHERE e.exerciseId = :exerciseId AND s.timestamp BETWEEN :startOfDay AND :endOfDay
        ORDER BY s.timestamp ASC
    """)
    abstract suspend fun getLogsForExerciseOnDate(exerciseId: Int, startOfDay: Long, endOfDay: Long): List<ExerciseSetLogEntity>

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
    @Query("SELECT * FROM workout_session_logs ORDER BY timestamp DESC LIMIT 100")
    abstract fun getAllSessionLogs(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("""
        SELECT * FROM workout_session_logs 
        WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = date(:dateMillis / 1000, 'unixepoch', 'localtime')
        LIMIT 50
    """)
    abstract fun getSessionLogsByDate(dateMillis: Long): Flow<List<SessionWithSets>>

    /**
     * Отримання історії ваги для всіх вправ одним запитом
     */
    @Query("""
        SELECT e.weight, s.timestamp, e.exerciseId
        FROM exercise_set_logs e
        JOIN workout_session_logs s ON e.sessionId = s.sessionId
        ORDER BY s.timestamp ASC
    """)
    abstract fun getAllWeightHistories(): Flow<List<ExerciseWeightHistoryWithId>>

    @Query("""
        SELECT MAX(weight) as weight, s.timestamp
        FROM exercise_set_logs e
        JOIN workout_session_logs s ON s.sessionId = e.sessionId
        WHERE e.exerciseId = :exerciseId
        GROUP BY date(s.timestamp / 1000, 'unixepoch')
        ORDER BY s.timestamp DESC
        LIMIT 100
    """)
    abstract fun getWeightHistoryForExercise(exerciseId: Int): Flow<List<ExerciseWeightHistory>>

    @Query("""
        SELECT 
            MIN(timestamp) AS dateUnixTimestamp, 
            SUM(totalTonnage) AS totalTonnage
        FROM workout_session_logs
        WHERE timestamp BETWEEN :start AND :end
        GROUP BY date(timestamp / 1000, 'unixepoch')
        ORDER BY dateUnixTimestamp ASC
        LIMIT 365
    """)
    abstract fun getDailyTonnageStats(start: Long, end: Long): Flow<List<DailyTonnageStats>>

    @Query("SELECT MAX(totalTonnage) FROM workout_session_logs")
    abstract suspend fun getPeakTonnage(): Double?

    @Query("SELECT * FROM workout_directives WHERE exerciseId = :exerciseId")
    abstract suspend fun getDirectiveForExercise(exerciseId: Int): WorkoutDirectiveEntity?
    
    @Query("DELETE FROM workout_directives")
    abstract suspend fun clearDirectives()

    @Query("DELETE FROM exercise_set_logs WHERE sessionId = :sessionId")
    abstract suspend fun deleteSetsBySession(sessionId: Long)

    @Query("SELECT * FROM exercise_set_logs WHERE exerciseId = :id ORDER BY setId DESC LIMIT 3")
    abstract suspend fun getLastSetsForExercise(id: Int): List<ExerciseSetLogEntity>

    @Query("""
        SELECT e.* FROM exercise_set_logs e
        JOIN workout_session_logs s ON e.sessionId = s.sessionId
        WHERE e.exerciseId = :exerciseId 
        ORDER BY s.timestamp DESC 
        LIMIT 5
    """)
    abstract suspend fun getRecentLogsForExercise(exerciseId: Int): List<ExerciseSetLogEntity>
}

data class ExerciseWeightHistoryWithId(
    val weight: Double,
    val timestamp: Long,
    val exerciseId: Int
)

data class ExerciseWeightHistory(
    val weight: Double,
    val timestamp: Long
)
