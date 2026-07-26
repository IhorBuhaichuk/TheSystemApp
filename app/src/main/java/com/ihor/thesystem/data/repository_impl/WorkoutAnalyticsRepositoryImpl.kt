package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.data.local.room.dao.WorkoutAnalyticsDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.entity.ExerciseSetLogEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutDirectiveEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutSessionLogEntity
import com.ihor.thesystem.data.local.room.relations.SessionWithSets
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class WorkoutAnalyticsRepositoryImpl @Inject constructor(
    private val dao: WorkoutAnalyticsDao,
    private val workoutDao: WorkoutDao,
    private val clock: AppClock
) : WorkoutAnalyticsRepository {

    override suspend fun saveSessionWithSets(
        session: WorkoutSession,
        sets: List<ExerciseSet>
    ): Long {
        return dao.saveFullSessionLog(
            session = session.toEntity(),
            sets = sets.map { it.toEntity() }
        )
    }

    override suspend fun saveDirectives(directives: List<WorkoutDirective>) {
        dao.insertOrReplaceDirectives(directives.map { it.toEntity() })
    }

    override fun getDailyTonnageStatsForMonth(
        monthStart: Long,
        monthEnd: Long
    ): Flow<List<DailyTonnageStats>> {
        return dao.getDailyTonnageStats(monthStart, monthEnd).map { rows ->
            WorkoutAnalyticsLocalDayGrouper.groupTonnageByLocalDay(rows, clock.zoneId())
        }
    }

    override fun getSessionById(sessionId: Long): Flow<WorkoutLog?> {
        return dao.getSessionLogById(sessionId).map { it?.toDomain() }
    }

    override fun getSessionsByDate(dateMillis: Long): Flow<List<WorkoutLog>> {
        val (startOfDay, endOfDay) = dayBounds(dateMillis)
        return dao.getSessionLogsBetween(startOfDay, endOfDay).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllLogs(): Flow<List<WorkoutLog>> {
        return dao.getAllSessionLogs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getLogsBetween(
        startInclusive: Long,
        endExclusive: Long
    ): Flow<List<WorkoutLog>> {
        return dao.getSessionLogsForStatistics(startInclusive, endExclusive).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getWeightHistory(exerciseId: Int): Flow<List<WeightHistoryEntry>> {
        return dao.getWeightHistoryForExercise(exerciseId).map { list ->
            WorkoutAnalyticsLocalDayGrouper.dailyMaxWeightHistory(
                rows = list.map { WeightHistoryEntry(it.weight, it.timestamp) },
                zoneId = clock.zoneId()
            )
        }
    }

    override fun getWeightHistoriesBetween(
        startInclusive: Long,
        endExclusive: Long
    ): Flow<List<WeightHistoryWithId>> {
        return dao.getWeightHistoriesBetween(startInclusive, endExclusive).map { list ->
            list.map { WeightHistoryWithId(it.weight, it.timestamp, it.exerciseId) }
        }
    }

    override suspend fun getLogsForExerciseOnDate(
        exerciseId: Int,
        startOfDay: Long,
        endOfDay: Long
    ): List<ExerciseSet> {
        return dao.getLogsForExerciseOnDate(exerciseId, startOfDay, endOfDay).map { it.toDomain() }
    }

    override suspend fun updateSetLog(log: ExerciseSet) {
        dao.updateSetLog(log.toEntity())
    }

    override suspend fun insertSetLog(log: ExerciseSet): Long {
        return dao.insertSetLog(log.toEntity())
    }

    override suspend fun saveSetLogs(logs: List<ExerciseSet>) {
        dao.insertSetLogs(logs.map { it.toEntity() })
    }

    override suspend fun deleteSetsBySession(sessionId: Long) {
        dao.deleteSetsBySession(sessionId)
    }

    override suspend fun getRecentLogsForExercise(exerciseId: Int): List<ExerciseSet> {
        return dao.getRecentLogsForExercise(exerciseId).map { it.toDomain() }
    }

    override suspend fun getLastSetsForExercise(exerciseId: Int): List<ExerciseSet> {
        return dao.getLastSetsForExercise(exerciseId).map { it.toDomain() }
    }

    override suspend fun getLastSetsForExercises(
        exerciseIds: List<Int>
    ): Map<Int, List<ExerciseSet>> {
        if (exerciseIds.isEmpty()) return emptyMap()
        return dao.getLastSetsForExercises(exerciseIds.distinct())
            .map { it.toDomain() }
            .groupBy { it.exerciseId }
    }

    override suspend fun updateSessionLog(session: WorkoutSession) {
        dao.insertSessionLog(session.toEntity())
    }

    override suspend fun saveFullSessionLog(
        session: WorkoutSession,
        sets: List<ExerciseSet>
    ): Long {
        return dao.saveFullSessionLog(session.toEntity(), sets.map { it.toEntity() })
    }

    override suspend fun getAllExercisesMap(): Map<Int, String> {
        return workoutDao.getAllExercisesSync().associate { it.id to it.name }
    }

    private fun dayBounds(dateMillis: Long): Pair<Long, Long> {
        val date = Instant.ofEpochMilli(dateMillis)
            .atZone(clock.zoneId())
            .toLocalDate()
        val startOfDay = date.atStartOfDay(clock.zoneId()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(clock.zoneId()).toInstant().toEpochMilli() - 1
        return startOfDay to endOfDay
    }

    // =========================================
    // MAPPER ФУНКЦІЇ
    // =========================================

    private fun WorkoutSession.toEntity() = WorkoutSessionLogEntity(
        sessionId = this.sessionId,
        questId = this.questId,
        timestamp = this.timestamp,
        totalTonnage = this.totalTonnage,
        cycleDay = this.cycleDay,
        durationMinutes = this.durationMinutes
    )

    private fun WorkoutSessionLogEntity.toDomain() = WorkoutSession(
        sessionId = this.sessionId,
        questId = this.questId,
        timestamp = this.timestamp,
        totalTonnage = this.totalTonnage,
        cycleDay = this.cycleDay,
        durationMinutes = this.durationMinutes
    )

    private fun ExerciseSet.toEntity() = ExerciseSetLogEntity(
        setId = this.setId,
        sessionId = this.sessionId,
        exerciseId = this.exerciseId,
        weight = this.weight,
        reps = this.reps,
        isCompleted = this.isCompleted,
        userFeedback = this.userFeedback
    )

    private fun ExerciseSetLogEntity.toDomain() = ExerciseSet(
        setId = this.setId,
        sessionId = this.sessionId,
        exerciseId = this.exerciseId,
        weight = this.weight,
        reps = this.reps,
        isCompleted = this.isCompleted,
        userFeedback = this.userFeedback
    )

    private fun WorkoutDirective.toEntity() = WorkoutDirectiveEntity(
        exerciseId = this.exerciseId,
        targetWeight = this.targetWeight,
        targetSets = this.targetSets,
        targetReps = this.targetReps
    )

    private fun SessionWithSets.toDomain() = WorkoutLog(
        session = this.session.toDomain(),
        sets = this.sets.map { it.toDomain() }
    )
}

internal object WorkoutAnalyticsLocalDayGrouper {
    fun groupTonnageByLocalDay(
        rows: List<DailyTonnageStats>,
        zoneId: ZoneId
    ): List<DailyTonnageStats> =
        rows.groupBy { row -> localDate(row.dateUnixTimestamp, zoneId) }
            .map { (date, entries) ->
                DailyTonnageStats(
                    dateUnixTimestamp = date.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                    totalTonnage = entries.sumOf { it.totalTonnage }
                )
            }
            .sortedBy { it.dateUnixTimestamp }

    fun dailyMaxWeightHistory(
        rows: List<WeightHistoryEntry>,
        zoneId: ZoneId
    ): List<WeightHistoryEntry> =
        rows.groupBy { row -> localDate(row.timestamp, zoneId) }
            .map { (_, entries) ->
                val maxWeight = entries.maxOf { it.weight }
                val timestamp = entries
                    .filter { it.weight == maxWeight }
                    .maxOf { it.timestamp }
                WeightHistoryEntry(
                    weight = maxWeight,
                    timestamp = timestamp
                )
            }
            .sortedByDescending { it.timestamp }
            .take(100)

    private fun localDate(timestamp: Long, zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(timestamp)
            .atZone(zoneId)
            .toLocalDate()
}
