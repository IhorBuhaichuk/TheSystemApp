package com.ihor.thesystem.data.local.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.ihor.thesystem.data.local.room.entity.WorkoutDraftEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutDraftSetEntity

data class WorkoutDraftWithSets(
    @Embedded val draft: WorkoutDraftEntity,
    @Relation(
        parentColumn = "draftId",
        entityColumn = "draftId"
    )
    val sets: List<WorkoutDraftSetEntity>
)

