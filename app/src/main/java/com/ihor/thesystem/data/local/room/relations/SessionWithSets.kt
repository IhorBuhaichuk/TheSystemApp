package com.ihor.thesystem.data.local.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.ihor.thesystem.data.local.room.entity.ExerciseSetLogEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutSessionLogEntity

data class SessionWithSets(
    @Embedded val session: WorkoutSessionLogEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val sets: List<ExerciseSetLogEntity>
)
