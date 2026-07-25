package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.repository.BetaMetricsRepository
import javax.inject.Inject

class RecordTodayOrderDecisionUseCase @Inject constructor(
    private val betaMetricsRepository: BetaMetricsRepository
) {
    suspend operator fun invoke(decision: TodayTrainingDecision) {
        betaMetricsRepository.recordTodayOrderDecision(
            epochDay = decision.dateEpochDay,
            decisionType = decision.decisionType
        )
    }
}
