package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.SystemConfigRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.round

class CalculateEffectiveWeightUseCase @Inject constructor(
    private val configRepo: SystemConfigRepository
) {
    /**
     * Calculates the effective weight by applying penalties.
     * Formula: BaseWeight * (1.0 - SystemPenalty)
     * Absolute Floor: 50% of BaseWeight.
     * Rounding: To nearest 0.25 kg.
     */
    suspend operator fun invoke(baseWeight: Double, isPenaltyActive: Boolean): Double {
        val config = configRepo.getConfigFlow().firstOrNull() ?: return baseWeight

        var totalPenaltyPercent = 0
        if (isPenaltyActive) {
            totalPenaltyPercent += config.defaultPenalty
        }

        // Max penalty is 50% (Floor)
        val finalPenaltyFactor = (totalPenaltyPercent.toDouble() / PERCENT_DIVISOR).coerceAtMost(MAX_PENALTY_FACTOR)
        val effectiveWeight = baseWeight * (1.0 - finalPenaltyFactor)

        // Round to nearest 0.25
        return round(effectiveWeight * ROUNDING_GRANULARITY) / ROUNDING_GRANULARITY
    }

    companion object {
        private const val PERCENT_DIVISOR      = 100.0
        private const val MAX_PENALTY_FACTOR   = 0.5
        private const val ROUNDING_GRANULARITY = 4.0  // округлення до 0.25 кг
    }
}
