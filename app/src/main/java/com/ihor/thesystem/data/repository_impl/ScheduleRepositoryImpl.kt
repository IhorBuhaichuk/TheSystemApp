package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ScheduleDao
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun getScheduleForDay(day: Int): Flow<ScheduleDay?> =
        scheduleDao.getScheduleForDay(day).map { details ->
            details?.let { mapToDomain(it) }
        }

    override fun getSchedulesForDays(days: List<Int>): Flow<List<ScheduleDay>> =
        scheduleDao.getSchedulesForDays(days).map { list ->
            list.map { mapToDomain(it) }
        }

    private fun mapToDomain(details: com.ihor.thesystem.data.local.room.relations.ScheduleWithDetails): ScheduleDay {
        return ScheduleDay(
            id                  = details.schedule.id,
            cycleDay            = details.schedule.cycleDay,
            workoutTemplateId   = details.schedule.workoutTemplateId,
            workoutTemplateName = details.workoutTemplate?.name,
            dailyTaskNames      = details.dailyTasks.map { it.name },
            exercises           = details.exercises.map { ExerciseDetails(it.id, it.name) }
        )
    }
}
