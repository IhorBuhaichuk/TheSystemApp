package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.WorkoutAnalyticsDao
import com.ihor.thesystem.data.local.room.entity.ExerciseSetEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutDirectiveEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutSessionEntity
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
        return dao.saveSessionWithSets(
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
        return dao.getDailyTonnageStatsForMonth(monthStart, monthEnd)
    }

    // =========================================
    // MAPPER ФУНКЦІЇ (Entity <-> Domain)
    // =========================================

    private fun WorkoutSession.toEntity(): WorkoutSessionEntity {
        return WorkoutSessionEntity(
            sessionId = this.sessionId,
            questId = this.questId,
            timestamp = this.timestamp,
            totalTonnage = this.totalTonnage,
            cycleDay = this.cycleDay
        )
    }

    private fun ExerciseSet.toEntity(): ExerciseSetEntity {
        return ExerciseSetEntity(
            setId = this.setId,
            sessionId = this.sessionId, // Значення буде автоматично оновлено в DAO під час транзакції
            exerciseId = this.exerciseId,
            weight = this.weight,
            reps = this.reps,
            isCompleted = this.isCompleted
        )
    }

    private fun WorkoutDirective.toEntity(): WorkoutDirectiveEntity {
        return WorkoutDirectiveEntity(
            exerciseId = this.exerciseId,
            targetWeight = this.targetWeight,
            targetSets = this.targetSets,
            targetReps = this.targetReps
        )
    }
}