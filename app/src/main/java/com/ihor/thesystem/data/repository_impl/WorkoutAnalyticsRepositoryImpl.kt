package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistory
import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistoryWithId
import com.ihor.thesystem.data.local.room.dao.WorkoutAnalyticsDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.local.room.relations.SessionWithSets
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkoutAnalyticsRepositoryImpl @Inject constructor(
    private val dao: WorkoutAnalyticsDao,
    private val workoutDao: WorkoutDao
) : WorkoutAnalyticsRepository {

    override suspend fun saveSessionWithSets(
        session: WorkoutSession,
        sets: List<ExerciseSet>
    ): Long {
        return dao.saveFullSessionLog(
            session = session.toLogEntity(),
            sets = sets.map { it.toLogEntity() }
        )
    }

    override suspend fun saveDirectives(directives: List<WorkoutDirective>) {
        dao.insertOrReplaceDirectives(directives.map { it.toEntity() })
    }

    override fun getDailyTonnageStatsForMonth(
        monthStart: Long,
        monthEnd: Long
    ): Flow<List<DailyTonnageStats>> {
        return dao.getDailyTonnageStats(monthStart, monthEnd)
    }

    override fun getSessionsByDate(dateMillis: Long): Flow<List<SessionWithSets>> {
        return dao.getSessionLogsByDate(dateMillis)
    }

    override fun getAllLogs(): Flow<List<SessionWithSets>> {
        return dao.getAllSessionLogs()
    }

    override fun getWeightHistory(exerciseId: Int): Flow<List<ExerciseWeightHistory>> {
        return dao.getWeightHistoryForExercise(exerciseId)
    }

    override fun getAllWeightHistories(): Flow<List<ExerciseWeightHistoryWithId>> {
        return dao.getAllWeightHistories()
    }

    override suspend fun getLogForExerciseOnDate(
        exerciseId: Int,
        startOfDay: Long,
        endOfDay: Long
    ): ExerciseSetLogEntity? {
        return dao.getLogForExerciseOnDate(exerciseId, startOfDay, endOfDay)
    }

    override suspend fun updateSetLog(log: ExerciseSetLogEntity) {
        dao.updateSetLog(log)
    }

    override suspend fun insertSetLog(log: ExerciseSetLogEntity): Long {
        return dao.insertSetLog(log)
    }

    override suspend fun saveSetLogs(logs: List<ExerciseSetLogEntity>) {
        dao.insertSetLogs(logs)
    }

    override suspend fun deleteSetsBySession(sessionId: Long) {
        dao.deleteSetsBySession(sessionId)
    }

    override suspend fun getRecentLogsForExercise(exerciseId: Int): List<ExerciseSetLogEntity> {
        return dao.getRecentLogsForExercise(exerciseId)
    }

    override suspend fun updateSessionLog(session: WorkoutSessionLogEntity) {
        dao.insertSessionLog(session)
    }

    override suspend fun saveFullSessionLog(
        session: WorkoutSessionLogEntity,
        sets: List<ExerciseSetLogEntity>
    ): Long {
        return dao.saveFullSessionLog(session, sets)
    }

    override suspend fun getAllExercisesMap(): Map<Int, String> {
        return workoutDao.getAllExercisesSync().associate { it.id to it.name }
    }

    // =========================================
    // MAPPER ФУНКЦІЇ
    // =========================================

    private fun WorkoutSession.toLogEntity() = WorkoutSessionLogEntity(
        sessionId = this.sessionId,
        questId = this.questId,
        timestamp = this.timestamp,
        totalTonnage = this.totalTonnage,
        cycleDay = this.cycleDay,
        durationMinutes = 0
    )

    private fun ExerciseSet.toLogEntity() = ExerciseSetLogEntity(
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
}
