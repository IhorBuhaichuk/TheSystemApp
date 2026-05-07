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
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
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
            exercises           = ordered.exercises.map { 
                ExerciseDetails(
                    id = it.exercise.id, 
                    name = it.exercise.name, 
                    nameUk = it.exercise.nameUk,
                    category = it.exercise.category,
                    muscleGroups = it.exercise.muscleGroups,
                    equipment = it.exercise.equipment,
                    level = it.exercise.level,
                    mechanic = it.exercise.mechanic,
                    force = it.exercise.force,
                    gifUrl = it.exercise.gifUrl,
                    externalId = it.exercise.externalId,
                    trackingMode = it.exercise.trackingMode
                ) 
            }
        )
    }

    override fun getAllExercises(): Flow<List<ExerciseDetails>> =
        workoutDao.getAllExercises().map { entities ->
            entities.map { 
                ExerciseDetails(
                    id = it.id, 
                    name = it.name, 
                    nameUk = it.nameUk,
                    category = it.category,
                    muscleGroups = it.muscleGroups,
                    equipment = it.equipment,
                    level = it.level,
                    mechanic = it.mechanic,
                    force = it.force,
                    gifUrl = it.gifUrl,
                    externalId = it.externalId,
                    trackingMode = it.trackingMode
                ) 
            }
        }

    override suspend fun createExercise(name: String): Int {
        return workoutDao.insertExercise(
            ExerciseEntity(
                name = name,
                trackingMode = ExerciseTrackingMode.WEIGHT_REPS.name
            )
        ).toInt()
    }

    override suspend fun deleteExercise(exerciseId: Int) {
        val exercise = ExerciseEntity(id = exerciseId, name = "")
        workoutDao.deleteExercise(exercise)
    }

    override suspend fun updateExerciseTrackingMode(exerciseId: Int, trackingMode: String?) {
        workoutDao.updateExerciseTrackingMode(exerciseId, trackingMode)
    }

    override suspend fun saveWorkoutForDay(cycleDay: Int, workoutName: String, exerciseIds: List<Int>) {
        // 1. Find or create ScheduleEntity for this day
        val scheduleWithDetails = scheduleDao.getScheduleForDay(cycleDay).firstOrNull()
        val schedule = scheduleWithDetails?.schedule ?: ScheduleEntity(cycleDay = cycleDay)

        if (exerciseIds.isEmpty()) {
            // If exercise list is empty, we treat this as a non-workout day
            val updatedSchedule = schedule.copy(workoutTemplateId = null)
            if (schedule.id == 0) {
                scheduleDao.insertSchedule(updatedSchedule)
            } else {
                scheduleDao.updateSchedule(updatedSchedule)
            }
            return
        }

        // 2. Find or create WorkoutTemplateEntity
        val templateId = schedule.workoutTemplateId ?: 0
        val existingTemplate = if (templateId != 0) workoutDao.getTemplateById(templateId) else null
        
        val newTemplateId = if (existingTemplate != null) {
            workoutDao.insertTemplate(existingTemplate.copy(name = workoutName))
            templateId
        } else {
            workoutDao.insertTemplate(WorkoutTemplateEntity(name = workoutName)).toInt()
        }

        // 3. Update Schedule with new template
        val updatedSchedule = schedule.copy(workoutTemplateId = newTemplateId)
        if (schedule.id == 0) {
            scheduleDao.insertSchedule(updatedSchedule)
        } else {
            scheduleDao.updateSchedule(updatedSchedule)
        }

        // 4. Update CrossRefs
        workoutDao.deleteAllCrossRefsForTemplate(newTemplateId)
        exerciseIds.forEachIndexed { index, exerciseId ->
            workoutDao.insertCrossRef(WorkoutExerciseCrossRef(newTemplateId, exerciseId, index))
        }
    }

    override suspend fun removeExerciseFromDay(cycleDay: Int, exerciseId: Int) {
        val scheduleWithDetails = scheduleDao.getScheduleForDay(cycleDay).firstOrNull() ?: return
        val schedule = scheduleWithDetails.schedule
        val templateId = schedule.workoutTemplateId ?: return

        // 1. Delete the exercise cross-reference
        workoutDao.deleteCrossRef(templateId, exerciseId)

        // 2. Check if any exercises remain for this template
        val remainingExercises = workoutDao.getExercisesForTemplateSync(templateId)
        if (remainingExercises.isEmpty()) {
            // 3. If no exercises remain, set workoutTemplateId to null (Active Recovery)
            val updatedSchedule = schedule.copy(workoutTemplateId = null)
            scheduleDao.updateSchedule(updatedSchedule)
        }
    }
}
