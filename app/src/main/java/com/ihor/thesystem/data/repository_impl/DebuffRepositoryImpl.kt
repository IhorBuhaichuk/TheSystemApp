package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.repository.ActiveDebuff
import com.ihor.thesystem.domain.repository.DebuffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebuffRepositoryImpl @Inject constructor() : DebuffRepository {
    override fun getActiveDebuffs(): Flow<List<ActiveDebuff>> {
        // Тимчасова реалізація. В майбутньому буде братися з БД або іншого джерела.
        // Наприклад, "Втома ЦНС" з великим штрафом.
        return flowOf(listOf(ActiveDebuff("Втома ЦНС", 15)))
    }
}
