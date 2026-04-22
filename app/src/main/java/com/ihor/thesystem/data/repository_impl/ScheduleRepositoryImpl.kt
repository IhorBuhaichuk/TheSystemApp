package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ScheduleDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.entity.DailyTaskTemplateEntity
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.ScheduleEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity
import com.ihor.thesystem.data.local.room.relations.OrderedExerciseRecord
import com.ihor.thesystem.data.local.room.relations.ScheduleWithDetails
import com.ihor.thesystem.data.local.room.relations.ScheduleWithOrderedExercises
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.repository.ScheduleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val workoutDao: WorkoutDao
) : ScheduleRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getScheduleForDay(day: Int): Flow<ScheduleDay?> =
        scheduleDao.getScheduleForDay(day).flatMapLatest { details: ScheduleWithDetails? ->
            if (details == null) return@flatMapLatest flowOf(null)
            
            val templateId = details.schedule.workoutTemplateId
            if (templateId != null) {
                workoutDao.getOrderedExercisesForTemplate(templateId).map { orderedExercises ->
                    assembleAndMap(details, orderedExercises)
                }
            } else {
                flowOf(assembleAndMap(details, emptyList()))
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSchedulesForDays(days: List<Int>): Flow<List<ScheduleDay>> =
        scheduleDao.getSchedulesForDays(days).flatMapLatest { list: List<ScheduleWithDetails> ->
            if (list.isEmpty()) return@flatMapLatest flowOf(emptyList())
            
            val flows = list.map { details ->
                val templateId = details.schedule.workoutTemplateId
                if (templateId != null) {
                    workoutDao.getOrderedExercisesForTemplate(templateId).map { orderedExercises ->
                        assembleAndMap(details, orderedExercises)
                    }
                } else {
                    flowOf(assembleAndMap(details, emptyList()))
                }
            }
            combine(flows) { it.toList() }
        }

    private fun assembleAndMap(
        details: ScheduleWithDetails,
        orderedExercises: List<OrderedExerciseRecord>
    ): ScheduleDay {
        val scheduleWithOrdered = ScheduleWithOrderedExercises(
            schedule = details.schedule,
            workoutTemplate = details.workoutTemplate,
            dailyTasks = details.dailyTasks,
            exercises = orderedExercises
        )
        return mapToDomain(scheduleWithOrdered)
    }

    private fun mapToDomain(ordered: ScheduleWithOrderedExercises): ScheduleDay {
        return ScheduleDay(
            id                  = ordered.schedule.id,
            cycleDay            = ordered.schedule.cycleDay,
            workoutTemplateId   = ordered.schedule.workoutTemplateId,
            workoutTemplateName = ordered.workoutTemplate?.name,
            dailyTaskNames      = ordered.dailyTasks.map { it.name },
            exercises           = ordered.exercises.map { ExerciseDetails(it.exercise.id, it.exercise.name) }
        )
    }
}
