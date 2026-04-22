package com.ihor.thesystem.data.local.room.relations

import androidx.room.Embedded
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity

data class OrderedExerciseRecord(
    @Embedded val exercise: ExerciseEntity,
    val orderIndex: Int
)

data class WorkoutWithExercises(
    val workout: WorkoutTemplateEntity,
    val exercises: List<OrderedExerciseRecord>
)
