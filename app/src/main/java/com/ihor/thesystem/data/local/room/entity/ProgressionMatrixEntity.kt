package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progression_matrix")
data class ProgressionMatrixEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,
    val startWeight: Float,
    val targetWeight: Float,       // -1.0 = дивись targetWeightNote
    val currentWeight: Float,
    val targetWeightNote: String? = null  // напр. "BW+10"
)