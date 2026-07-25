package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.BetaMetricsEventState
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import kotlinx.coroutines.flow.Flow

interface BetaMetricsRepository {
    fun observeEventState(): Flow<BetaMetricsEventState>
    suspend fun markAppOpened(epochDay: Long)
    suspend fun recordTodayOrderDecision(epochDay: Long, decisionType: TodayTrainingDecisionType)
}
