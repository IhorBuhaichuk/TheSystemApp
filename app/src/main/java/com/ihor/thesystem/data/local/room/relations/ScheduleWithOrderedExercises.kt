package com.ihor.thesystem.data.local.room.relations

import com.ihor.thesystem.data.local.room.entity.DailyTaskTemplateEntity
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.ScheduleEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity

data class ScheduleWithOrderedExercises(
    val schedule: ScheduleEntity,
    val workoutTemplate: WorkoutTemplateEntity?,
    val dailyTasks: List<DailyTaskTemplateEntity>,
    val exercises: List<ExerciseEntity>
)
