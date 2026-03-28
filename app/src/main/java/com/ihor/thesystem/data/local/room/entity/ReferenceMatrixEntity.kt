package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reference_matrix")
data class ReferenceMatrixEntity(
    @PrimaryKey
    val exerciseId: String,
    val exerciseName: String,
    val weightType: WeightType,
    val progressionStep: Double,
    val milestones: Map<String, Double>,
    val repsMilestones: Map<String, Int>? = null
)
