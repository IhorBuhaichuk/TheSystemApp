package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.BetaMetricsRepository
import com.ihor.thesystem.domain.util.AppClock
import java.time.Instant
import javax.inject.Inject

class RecordBetaAppOpenUseCase @Inject constructor(
    private val betaMetricsRepository: BetaMetricsRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke() {
        val todayEpochDay = Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
            .toEpochDay()
        betaMetricsRepository.markAppOpened(todayEpochDay)
    }
}
