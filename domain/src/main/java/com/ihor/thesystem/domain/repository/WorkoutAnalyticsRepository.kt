package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.model.WeightHistoryEntry
import com.ihor.thesystem.domain.model.WeightHistoryWithId
import kotlinx.coroutines.flow.Flow

/**
 * Модель для передачі агрегованої статистики тоннажу по днях.
 */
data class DailyTonnageStats(
    val dateUnixTimestamp: Long,
    val totalTonnage: Double
)

interface WorkoutAnalyticsRepository {
    suspend fun saveSessionWithSets(session: WorkoutSession, sets: List<ExerciseSet>): Long
    suspend fun saveDirectives(directives: List<WorkoutDirective>)
    fun getDailyTonnageStatsForMonth(monthStart: Long, monthEnd: Long): Flow<List<DailyTonnageStats>>
    fun getSessionById(sessionId: Long): Flow<WorkoutLog?>
    fun getSessionsByDate(dateMillis: Long): Flow<List<WorkoutLog>>
    fun getAllLogs(): Flow<List<WorkoutLog>>
    
    // Отримання історії для однієї вправи
    fun getWeightHistory(exerciseId: Int): Flow<List<WeightHistoryEntry>>
    
    // ОПТИМІЗАЦІЯ: Отримання всього списку історій за раз
    fun getAllWeightHistories(): Flow<List<WeightHistoryWithId>>

    // Нові методи для фіксу дублікатів та контексту
    suspend fun getLogsForExerciseOnDate(exerciseId: Int, startOfDay: Long, endOfDay: Long): List<ExerciseSet>
    suspend fun updateSetLog(log: ExerciseSet)
    suspend fun insertSetLog(log: ExerciseSet): Long
    suspend fun saveSetLogs(logs: List<ExerciseSet>)
    suspend fun deleteSetsBySession(sessionId: Long)
    suspend fun getRecentLogsForExercise(exerciseId: Int): List<ExerciseSet>
    suspend fun getLastSetsForExercise(exerciseId: Int): List<ExerciseSet>
    suspend fun getLastSetsForExercises(exerciseIds: List<Int>): Map<Int, List<ExerciseSet>>

    // Методи для роботи з сесіями (Business logic move)
    suspend fun updateSessionLog(session: WorkoutSession)
    suspend fun saveFullSessionLog(session: WorkoutSession, sets: List<ExerciseSet>): Long

    /**
     * Повертає мапу вправ: ID -> Назва.
     */
    suspend fun getAllExercisesMap(): Map<Int, String>
}
