package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.DebuffRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CalculateEffectiveWeightUseCase @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val debuffRepo: DebuffRepository
) {
    suspend operator fun invoke(baseWeight: Float, isPenaltyActive: Boolean): Float {
        val config = configRepo.getConfig().firstOrNull() ?: return baseWeight
        val activeDebuffs = debuffRepo.getActiveDebuffs().firstOrNull() ?: emptyList()
        
        var totalPenaltyPercent = 0
        
        if (isPenaltyActive) {
            totalPenaltyPercent += config.defaultPenalty
        }
        
        totalPenaltyPercent += activeDebuffs.sumOf { it.penaltyPercent }
        
        val multiplier = (100 - totalPenaltyPercent).coerceAtMost(100).coerceAtLeast(50) / 100f
        return baseWeight * multiplier
    }
}
