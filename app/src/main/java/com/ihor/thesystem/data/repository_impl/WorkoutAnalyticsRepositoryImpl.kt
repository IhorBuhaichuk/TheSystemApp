package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistory
import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistoryWithId
import com.ihor.thesystem.data.local.room.dao.WorkoutAnalyticsDao
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
    private val dao: WorkoutAnalyticsDao
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

    // ОПТИМІЗАЦІЯ: Отримання всього списку історій за раз
    override fun getAllWeightHistories(): Flow<List<ExerciseWeightHistoryWithId>> {
        return dao.getAllWeightHistories()
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
        exerciseId = this.exerciseId.toIntOrNull() ?: 0,
        weight = this.weight,
        reps = this.reps,
        isCompleted = this.isCompleted
    )

    private fun WorkoutDirective.toEntity() = WorkoutDirectiveEntity(
        exerciseId = this.exerciseId.toIntOrNull() ?: 0,
        targetWeight = this.targetWeight,
        targetSets = this.targetSets,
        targetReps = this.targetReps
    )
}
