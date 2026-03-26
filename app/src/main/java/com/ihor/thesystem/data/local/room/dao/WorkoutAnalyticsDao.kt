package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ihor.thesystem.data.local.room.entity.ExerciseSetEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutDirectiveEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutSessionEntity
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutAnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSets(sets: List<ExerciseSetEntity>)

    /**
     * Зберігає нові директиви. Якщо для конкретної вправи вже є директива,
     * вона буде замінена новою завдяки OnConflictStrategy.REPLACE.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplaceDirectives(directives: List<WorkoutDirectiveEntity>)

    /**
     * Виконує збереження сесії та всіх її підходів як єдину неподільну операцію (транзакцію).
     * Якщо щось піде не так під час збереження підходів, сесія теж не збережеться,
     * що захищає базу від "битих" або неповних даних.
     */
    @Transaction
    open suspend fun saveSessionWithSets(session: WorkoutSessionEntity, sets: List<ExerciseSetEntity>): Long {
        val sessionId = insertSession(session)
        // Прив'язуємо кожен підхід до щойно створеної сесії
        val setsWithSessionId = sets.map { it.copy(sessionId = sessionId) }
        insertSets(setsWithSessionId)
        return sessionId
    }

    /**
     * Отримує статистику тоннажу за вказаний період.
     * Групує всі сесії по днях і рахує сумарний тоннаж за кожен день.
     */
    @Query("""
        SELECT 
            MIN(timestamp) AS dateUnixTimestamp, 
            SUM(totalTonnage) AS totalTonnage
        FROM workout_sessions
        WHERE timestamp BETWEEN :monthStart AND :monthEnd
        GROUP BY date(timestamp / 1000, 'unixepoch')
        ORDER BY dateUnixTimestamp ASC
    """)
    abstract fun getDailyTonnageStatsForMonth(monthStart: Long, monthEnd: Long): Flow<List<DailyTonnageStats>>
}