package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.ReadinessEntry
import kotlinx.coroutines.flow.Flow

interface ReadinessRepository {
    fun observeEntryForDate(dateEpochDay: Long): Flow<ReadinessEntry?>
    suspend fun getEntryForDate(dateEpochDay: Long): ReadinessEntry?
    suspend fun getEntriesBetween(startEpochDay: Long, endEpochDay: Long): List<ReadinessEntry>
    suspend fun saveEntry(entry: ReadinessEntry): Long
    suspend fun deleteEntry(id: Long)
}
