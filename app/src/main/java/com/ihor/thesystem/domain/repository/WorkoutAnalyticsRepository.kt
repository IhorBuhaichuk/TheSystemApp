package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistory
import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistoryWithId
import com.ihor.thesystem.data.local.room.relations.SessionWithSets
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.model.WorkoutSession
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
    fun getSessionsByDate(dateMillis: Long): Flow<List<SessionWithSets>>
    fun getAllLogs(): Flow<List<SessionWithSets>>
    
    // Отримання історії для однієї вправи
    fun getWeightHistory(exerciseId: Int): Flow<List<ExerciseWeightHistory>>
    
    // ОПТИМІЗАЦІЯ: Отримання всієї історії за один запит
    fun getAllWeightHistories(): Flow<List<ExerciseWeightHistoryWithId>>
}
