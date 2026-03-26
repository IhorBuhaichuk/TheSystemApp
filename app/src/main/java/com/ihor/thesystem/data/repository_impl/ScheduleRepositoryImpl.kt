package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ScheduleDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
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
            details?.let { d ->
                val templateId   = d.schedule.workoutTemplateId
                val templateName = templateId?.let { workoutDao.getTemplateName(it) }
                val exercises    = templateId?.let { workoutDao.getExerciseNamesForTemplate(it) }
                    ?: emptyList()
                ScheduleDay(
                    id                  = d.schedule.id,
                    cycleDay            = d.schedule.cycleDay,
                    workoutTemplateId   = templateId,
                    workoutTemplateName = templateName,
                    dailyTaskNames      = d.dailyTasks.map { it.name },
                    exerciseNames       = exercises
                )
            }
        }
}