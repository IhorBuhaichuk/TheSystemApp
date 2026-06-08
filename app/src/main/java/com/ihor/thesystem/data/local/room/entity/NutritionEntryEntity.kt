package com.ihor.thesystem.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ihor.thesystem.domain.model.MealsQuality
import com.ihor.thesystem.domain.model.NutritionGoalMode

@Entity(
    tableName = "nutrition_entries",
    indices = [Index("dateEpochDay")]
)
data class NutritionEntryEntity(
    @PrimaryKey val dateEpochDay: Long,
    val proteinHit: Boolean,
    val waterHit: Boolean,
    val mealsQuality: MealsQuality = MealsQuality.NORMAL,
    val bodyWeight: Float? = null,
    val goalMode: NutritionGoalMode = NutritionGoalMode.MAINTENANCE,
    val note: String? = null
)
