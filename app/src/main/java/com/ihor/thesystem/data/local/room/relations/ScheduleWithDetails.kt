package com.ihor.thesystem.data.local.room.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ihor.thesystem.data.local.room.entity.DailyTaskTemplateEntity
import com.ihor.thesystem.data.local.room.entity.ScheduleEntity
import com.ihor.thesystem.data.local.room.entity.ScheduleTaskCrossRef

data class ScheduleWithDetails(
    @Embedded val schedule: ScheduleEntity,
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