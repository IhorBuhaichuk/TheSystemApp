package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.ReadinessDao
import com.ihor.thesystem.data.local.room.entity.ReadinessEntryEntity
import com.ihor.thesystem.domain.model.ReadinessEntry
import com.ihor.thesystem.domain.model.ReadinessInput
import com.ihor.thesystem.domain.repository.ReadinessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReadinessRepositoryImpl @Inject constructor(
    private val readinessDao: ReadinessDao
) : ReadinessRepository {

    override fun observeEntryForDate(dateEpochDay: Long): Flow<ReadinessEntry?> =
        readinessDao.observeEntryForDate(dateEpochDay).map { it?.toDomain() }

    override suspend fun getEntryForDate(dateEpochDay: Long): ReadinessEntry? =
        readinessDao.getEntryForDate(dateEpochDay)?.toDomain()

    override suspend fun getEntriesBetween(
        startEpochDay: Long,
        endEpochDay: Long
    ): List<ReadinessEntry> =
        readinessDao.getEntriesBetween(startEpochDay, endEpochDay).map { it.toDomain() }

    override suspend fun saveEntry(entry: ReadinessEntry): Long =
        readinessDao.upsert(entry.toEntity())

    override suspend fun deleteEntry(id: Long) {
        readinessDao.deleteEntry(id)
    }

    private fun ReadinessEntryEntity.toDomain(): ReadinessEntry =
        ReadinessEntry(
            id = id,
            dateEpochDay = dateEpochDay,
            input = ReadinessInput(
                sleepHours = sleepHours,
                energy = energy,
                stress = stress,
                soreness = soreness,
                motivation = motivation,
                note = note
            ),
            score = score,
            level = level,
            createdAtMillis = createdAtMillis
        )

    private fun ReadinessEntry.toEntity(): ReadinessEntryEntity =
        ReadinessEntryEntity(
            id = id,
            dateEpochDay = dateEpochDay,
            sleepHours = input.sleepHours,
            energy = input.energy,
            stress = input.stress,
            soreness = input.soreness,
            motivation = input.motivation,
            note = input.note,
            score = score,
            level = level,
            createdAtMillis = createdAtMillis
        )
}
