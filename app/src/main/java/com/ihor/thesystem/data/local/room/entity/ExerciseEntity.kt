package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.MuscleGroup

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val externalId: String? = null,
    val name: String,
    val category: ExerciseCategory = ExerciseCategory.UNKNOWN,
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val equipment: String? = null,
    val instructions: String? = null,
    val gifUrl: String? = null
)
