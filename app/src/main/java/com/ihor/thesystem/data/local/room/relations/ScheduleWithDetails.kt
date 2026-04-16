package com.ihor.thesystem.data.local.room.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ihor.thesystem.data.local.room.entity.DailyTaskTemplateEntity
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.ScheduleEntity
import com.ihor.thesystem.data.local.room.entity.ScheduleTaskCrossRef
import com.ihor.thesystem.data.local.room.entity.WorkoutExerciseCrossRef
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity

data class ScheduleWithDetails(
    @Embedded val schedule: ScheduleEntity,

    @Relation(
        parentColumn = "workoutTemplateId",
        entityColumn = "id"
    )
    val workoutTemplate: WorkoutTemplateEntity?,

    @Relation(
        parentColumn = "workoutTemplateId",
        entityColumn = "id",
        associateBy = Junction(
            value = WorkoutExerciseCrossRef::class,
            parentColumn = "workoutTemplateId",
            entityColumn = "exerciseId"
        )
    )
    val exercises: List<ExerciseEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy  = Junction(
            value        = ScheduleTaskCrossRef::class,
            parentColumn = "scheduleId",
            entityColumn = "taskTemplateId"
        )
    )
    val dailyTasks: List<DailyTaskTemplateEntity>
)
