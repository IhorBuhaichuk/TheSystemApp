package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ProgressRankConfig
import com.ihor.thesystem.domain.model.Rank
import javax.inject.Inject

class CalculateProgressRankUseCase @Inject constructor() {
    operator fun invoke(
        currentWeight: Double,
        startWeight: Double,
        targetWeight: Double,
        config: ProgressRankConfig = ProgressRankConfig()
    ): Rank? {
        if (currentWeight <= 0.0 || targetWeight <= startWeight) return null

        val progress = ((currentWeight - startWeight) / (targetWeight - startWeight))
            .coerceAtLeast(0.0)
        return config.thresholds
            .sortedBy { it.minProgress }
            .lastOrNull { progress >= it.minProgress }
            ?.rank
    }
}
