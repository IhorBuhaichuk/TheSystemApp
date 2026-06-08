package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.NutritionDao
import com.ihor.thesystem.data.local.room.entity.NutritionEntryEntity
import com.ihor.thesystem.domain.model.NutritionEntry
import com.ihor.thesystem.domain.repository.NutritionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val nutritionDao: NutritionDao
) : NutritionRepository {

    override fun observeEntryForDate(dateEpochDay: Long): Flow<NutritionEntry?> =
        nutritionDao.observeEntryForDate(dateEpochDay).map { it?.toDomain() }

    override suspend fun getEntryForDate(dateEpochDay: Long): NutritionEntry? =
        nutritionDao.getEntryForDate(dateEpochDay)?.toDomain()

    override suspend fun getEntriesBetween(
        startEpochDay: Long,
        endEpochDay: Long
    ): List<NutritionEntry> =
        nutritionDao.getEntriesBetween(startEpochDay, endEpochDay).map { it.toDomain() }

    override suspend fun saveEntry(entry: NutritionEntry) {
        nutritionDao.upsert(entry.toEntity())
    }

    override suspend fun deleteEntry(dateEpochDay: Long) {
        nutritionDao.deleteEntry(dateEpochDay)
    }

    private fun NutritionEntryEntity.toDomain(): NutritionEntry =
        NutritionEntry(
            dateEpochDay = dateEpochDay,
            proteinHit = proteinHit,
            waterHit = waterHit,
            mealsQuality = mealsQuality,
            bodyWeight = bodyWeight,
            goalMode = goalMode,
            note = note
        )

    private fun NutritionEntry.toEntity(): NutritionEntryEntity =
        NutritionEntryEntity(
            dateEpochDay = dateEpochDay,
            proteinHit = proteinHit,
            waterHit = waterHit,
            mealsQuality = mealsQuality,
            bodyWeight = bodyWeight,
            goalMode = goalMode,
            note = note
        )
}
