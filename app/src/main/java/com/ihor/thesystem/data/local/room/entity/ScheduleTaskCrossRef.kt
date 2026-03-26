package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity

@Entity(
    tableName   = "schedule_task_cross_ref",
    primaryKeys = ["scheduleId", "taskTemplateId"]
)
data class ScheduleTaskCrossRef(
    val scheduleId: Int,
    val taskTemplateId: Int
)