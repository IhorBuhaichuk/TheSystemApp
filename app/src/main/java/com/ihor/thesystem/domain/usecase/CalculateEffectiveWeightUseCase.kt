package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.DebuffRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.round

class CalculateEffectiveWeightUseCase @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val debuffRepo: DebuffRepository
) {
    /**
     * Calculates the effective weight by applying penalties and debuffs.
     * Formula: BaseWeight * (1.0 - (SystemPenalty + ActiveDebuffsPenalty))
     * Absolute Floor: 50% of BaseWeight.
     * Rounding: To nearest 0.25 kg.
     */
    suspend operator fun invoke(baseWeight: Double, isPenaltyActive: Boolean): Double {
        val config = configRepo.getConfigFlow().firstOrNull() ?: return baseWeight
        val activeDebuffs = debuffRepo.getActiveDebuffs().firstOrNull() ?: emptyList()

        var totalPenaltyPercent = 0
        if (isPenaltyActive) {
            totalPenaltyPercent += config.defaultPenalty
        }
        totalPenaltyPercent += activeDebuffs.sumOf { it.penaltyPercent }

        // Max penalty is 50% (Floor)
        val finalPenaltyFactor = (totalPenaltyPercent.toDouble() / 100.0).coerceAtMost(0.5)
        val effectiveWeight = baseWeight * (1.0 - finalPenaltyFactor)

        // Round to nearest 0.25
        return round(effectiveWeight * 4) / 4
    }
}
