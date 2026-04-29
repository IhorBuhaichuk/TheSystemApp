package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName   = "schedule_task_cross_ref",
    primaryKeys = ["scheduleId", "taskTemplateId"],
    indices = [
        Index(value = ["taskTemplateId"])
    ]
)
data class ScheduleTaskCrossRef(
    val scheduleId: Int,
    val taskTemplateId: Int
)
