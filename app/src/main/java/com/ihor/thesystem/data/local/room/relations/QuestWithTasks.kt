package com.ihor.thesystem.data.local.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.ihor.thesystem.data.local.room.entity.QuestEntity
import com.ihor.thesystem.data.local.room.entity.QuestTaskEntity

data class QuestWithTasks(
    @Embedded val quest: QuestEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "questId"
    )
    val tasks: List<QuestTaskEntity>
)