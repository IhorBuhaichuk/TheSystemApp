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
import com.ihor.thesystem.data.local.room.entity.WorkoutExerciseCrossRef
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

    override fun getAllExercises(): Flow<List<ExerciseDetails>> =
        workoutDao.getAllExercises().map { entities ->
            entities.map { ExerciseDetails(it.id, it.name) }
        }

    override suspend fun createExercise(name: String) {
        workoutDao.insertExercise(ExerciseEntity(name = name))
    }

    override suspend fun deleteExercise(exerciseId: Int) {
        val exercise = ExerciseEntity(id = exerciseId, name = "")
        workoutDao.deleteExercise(exercise)
    }

    override suspend fun saveWorkoutForDay(cycleDay: Int, workoutName: String, exerciseIds: List<Int>) {
        // 1. Find or create ScheduleEntity for this day
        val scheduleWithDetails = scheduleDao.getScheduleForDay(cycleDay).firstOrNull()
        val schedule = scheduleWithDetails?.schedule ?: ScheduleEntity(cycleDay = cycleDay)

        // 2. Find or create WorkoutTemplateEntity
        val templateId = schedule.workoutTemplateId ?: 0
        val existingTemplate = if (templateId != 0) workoutDao.getTemplateById(templateId) else null
        
        val newTemplateId = if (existingTemplate != null) {
            workoutDao.insertTemplate(existingTemplate.copy(name = workoutName))
            templateId
        } else {
            workoutDao.insertTemplate(WorkoutTemplateEntity(name = workoutName)).toInt()
        }

        // 3. Update Schedule with new template if it was null
        if (schedule.workoutTemplateId == null) {
            val updatedSchedule = schedule.copy(workoutTemplateId = newTemplateId)
            if (schedule.id == 0) {
                scheduleDao.insertSchedule(updatedSchedule)
            } else {
                scheduleDao.updateSchedule(updatedSchedule)
            }
        }

        // 4. Update CrossRefs
        workoutDao.deleteAllCrossRefsForTemplate(newTemplateId)
        exerciseIds.forEachIndexed { index, exerciseId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(newTemplateId, exerciseId, index))
        }
    }

    override suspend fun removeExerciseFromDay(cycleDay: Int, exerciseId: Int) {
        val scheduleWithDetails = scheduleDao.getScheduleForDay(cycleDay).firstOrNull()
        val templateId = scheduleWithDetails?.schedule?.workoutTemplateId ?: return
        workoutDao.deleteCrossRef(templateId, exerciseId)
    }
}
