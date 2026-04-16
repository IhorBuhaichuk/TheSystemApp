package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ScheduleDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val workoutDao: WorkoutDao
) : ScheduleRepository {

    override fun getScheduleForDay(day: Int): Flow<ScheduleDay?> =
        scheduleDao.getScheduleForDay(day).map { details ->
            if (details != null) mapToDomain(details) else null
        }

    override fun getSchedulesForDays(days: List<Int>): Flow<List<ScheduleDay>> =
        scheduleDao.getSchedulesForDays(days).map { list ->
            list.map { mapToDomain(it) }
        }

    private suspend fun mapToDomain(details: com.ihor.thesystem.data.local.room.relations.ScheduleWithDetails): ScheduleDay {
        val templateId = details.schedule.workoutTemplateId
        val templateName = templateId?.let { workoutDao.getTemplateNameSync(it) }
        val exercises = templateId?.let { workoutDao.getExercisesForTemplateSync(it) } ?: emptyList()
        
        return ScheduleDay(
            id                  = details.schedule.id,
            cycleDay            = details.schedule.cycleDay,
            workoutTemplateId   = templateId,
            workoutTemplateName = templateName,
            dailyTaskNames      = details.dailyTasks.map { it.name },
            exercises           = exercises.map { ExerciseDetails(it.id, it.name) }
        )
    }
}
