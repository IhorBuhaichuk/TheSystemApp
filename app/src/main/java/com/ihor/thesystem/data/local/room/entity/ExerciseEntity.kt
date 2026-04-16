package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ihor.thesystem.domain.model.ExerciseCategory

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: ExerciseCategory = ExerciseCategory.UNKNOWN
)
