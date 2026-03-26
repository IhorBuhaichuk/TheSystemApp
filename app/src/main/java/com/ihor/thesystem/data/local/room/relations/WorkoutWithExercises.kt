package com.ihor.thesystem.data.local.room.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutExerciseCrossRef
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity

data class WorkoutWithExercises(
    @Embedded val workout: WorkoutTemplateEntity,
    @Relation(
        parentColumn    = "id",
        entityColumn    = "id",
        associateBy     = Junction(
            value           = WorkoutExerciseCrossRef::class,
            parentColumn    = "workoutTemplateId",
            entityColumn    = "exerciseId"
        )
    )
    val exercises: List<ExerciseEntity>
)