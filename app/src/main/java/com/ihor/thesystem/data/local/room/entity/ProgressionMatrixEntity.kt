package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ihor.thesystem.domain.model.Rank

@Entity(tableName = "progression_matrix")
data class ProgressionMatrixEntity(
    @PrimaryKey
    val exerciseId: Int,
    val startWeight: Float,
    val targetWeight: Float,
    val currentWeight: Float,
    val targetWeightNote: String? = null,
    val currentRank: Rank = Rank.E,
    val completedCycles: Int = 0,
    val isPromotionPending: Boolean = false,
    val nextRecommendedWeight: Double? = null,
    val nextRecommendedSets: Int? = null,
    val nextRecommendedReps: String? = null,
    val lastAiFeedback: String? = null
)
