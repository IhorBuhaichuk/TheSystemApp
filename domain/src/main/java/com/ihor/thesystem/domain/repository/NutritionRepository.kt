package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.NutritionEntry
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun observeEntryForDate(dateEpochDay: Long): Flow<NutritionEntry?>
    suspend fun getEntryForDate(dateEpochDay: Long): NutritionEntry?
    suspend fun getEntriesBetween(startEpochDay: Long, endEpochDay: Long): List<NutritionEntry>
    suspend fun saveEntry(entry: NutritionEntry)
    suspend fun deleteEntry(dateEpochDay: Long)
}
