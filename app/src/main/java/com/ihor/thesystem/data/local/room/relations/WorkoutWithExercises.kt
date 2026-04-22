package com.ihor.thesystem.data.local.room.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ihor.thesystem.data.local.room.entity.ExerciseEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutExerciseCrossRef
import com.ihor.thesystem.data.local.room.entity.WorkoutTemplateEntity

data class WorkoutWithExercises(
    val workout: WorkoutTemplateEntity,
    val exercises: List<ExerciseEntity>
)