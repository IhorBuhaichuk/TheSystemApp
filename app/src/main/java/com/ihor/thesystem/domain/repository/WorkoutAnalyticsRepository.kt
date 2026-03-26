package com.ihor.thesystem.domain.repository

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
    /**
     * Зберігає загальні дані сесії та список виконаних підходів в єдиній транзакції.
     * Повертає ID створеної сесії.
     */
    suspend fun saveSessionWithSets(session: WorkoutSession, sets: List<ExerciseSet>): Long

    /**
     * Зберігає або оновлює (замінює) директиви для наступного тренування.
     */
    suspend fun saveDirectives(directives: List<WorkoutDirective>)

    /**
     * Отримує згруповану статистику тоннажу за вказаний період (по днях).
     */
    fun getDailyTonnageStatsForMonth(monthStart: Long, monthEnd: Long): Flow<List<DailyTonnageStats>>
}