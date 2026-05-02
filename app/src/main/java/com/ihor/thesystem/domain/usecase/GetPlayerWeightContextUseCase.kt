package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.core.util.getOrNull
import com.ihor.thesystem.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import javax.inject.Inject

data class PlayerWeightContext(
    val currentWeight: Double?,
    val weightSixMonthsAgo: Float?
)

class GetPlayerWeightContextUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(): PlayerWeightContext {
        val now = clock.now()
        val currentWeight = playerRepository.getLatestWeight().firstOrNull()?.toDouble()
        val zoneId = clock.zoneId()
        
        // Використання java.time для розрахунку дати 6 місяців тому
        val dateSixMonthsAgo = Instant.ofEpochMilli(now)
            .atZone(zoneId)
            .toLocalDate()
            .minusMonths(6)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val weightSixMonthsAgo = playerRepository.getWeightAtOrBefore(dateSixMonthsAgo).getOrNull() 
            ?: currentWeight?.toFloat()

        return PlayerWeightContext(
            currentWeight = currentWeight,
            weightSixMonthsAgo = weightSixMonthsAgo
        )
    }
}
