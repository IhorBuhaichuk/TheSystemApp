package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.MuscleGroup

@Entity(
    tableName = "exercises",
    indices = [
        Index("category"),
        Index("equipment"),
        Index("level"),
        Index("mechanic"),
        Index("force"),
        Index("isCoreSystemExercise")
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val externalId: String? = null,
    val name: String,
    val nameUk: String? = null,
    val category: ExerciseCategory = ExerciseCategory.UNKNOWN,
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val equipment: String? = null,
    val level: String? = null,
    val mechanic: String? = null,
    val force: String? = null,
    val instructions: String? = null,
    val gifUrl: String? = null,
    val trackingMode: String? = null,
    val isCoreSystemExercise: Boolean = false,
    val coreMetadataVersion: Int = 0,
    val movementPattern: String? = null,
    val techniqueTips: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val substitutionExternalIds: List<String> = emptyList()
)
