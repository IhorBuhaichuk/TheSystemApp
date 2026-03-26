package com.ihor.thesystem.data.repository_impl

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

    // =========================================
    // MAPPER ФУНКЦІЇ (Entity <-> Domain)
    // =========================================

    private fun WorkoutSession.toLogEntity(): WorkoutSessionLogEntity {
        return WorkoutSessionLogEntity(
            sessionId = this.sessionId,
            questId = this.questId,
            timestamp = this.timestamp,
            totalTonnage = this.totalTonnage,
            cycleDay = this.cycleDay,
            durationMinutes = 0
        )
    }

    private fun ExerciseSet.toLogEntity(): ExerciseSetLogEntity {
        return ExerciseSetLogEntity(
            setId = this.setId,
            sessionId = this.sessionId,
            exerciseId = this.exerciseId.toIntOrNull() ?: 0,
            weight = this.weight,
            reps = this.reps,
            isCompleted = this.isCompleted
        )
    }

    private fun WorkoutDirective.toEntity(): WorkoutDirectiveEntity {
        return WorkoutDirectiveEntity(
            exerciseId = this.exerciseId.toIntOrNull() ?: 0,
            targetWeight = this.targetWeight,
            targetSets = this.targetSets,
            targetReps = this.targetReps
        )
    }
}
