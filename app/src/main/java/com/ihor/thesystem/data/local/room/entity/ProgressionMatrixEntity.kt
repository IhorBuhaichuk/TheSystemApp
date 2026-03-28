package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progression_matrix")
data class ProgressionMatrixEntity(
    @PrimaryKey
    val exerciseId: Int,
    val startWeight: Float,
    val targetWeight: Float,
    val currentWeight: Float,
    val targetWeightNote: String? = null
)
