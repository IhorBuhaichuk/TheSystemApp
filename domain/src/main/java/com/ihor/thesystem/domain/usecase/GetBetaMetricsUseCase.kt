package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BetaMetrics
import com.ihor.thesystem.domain.model.BetaMetricsEventState
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.repository.BetaMetricsRepository
import com.ihor.thesystem.domain.repository.OnboardingRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AppClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import javax.inject.Inject

class GetBetaMetricsUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val workoutAnalyticsRepository: WorkoutAnalyticsRepository,
    private val playerRepository: PlayerRepository,
    private val systemConfigRepository: SystemConfigRepository,
    private val scheduleRepository: ScheduleRepository,
    private val betaMetricsRepository: BetaMetricsRepository,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val clock: AppClock
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<BetaMetrics> {
        val baseFlow = combine(
            onboardingRepository.isOnboardingCompleted(),
            workoutAnalyticsRepository.getAllLogs(),
            playerRepository.getPlayer(),
            systemConfigRepository.getConfigFlow(),
            betaMetricsRepository.observeEventState()
        ) { onboardingCompleted, workoutLogs, player, config, eventState ->
            BetaMetricsBase(
                onboardingCompleted = onboardingCompleted,
                workoutLogs = workoutLogs,
                player = player,
                config = config ?: SystemConfig(),
                eventState = eventState
            )
        }

        return baseFlow.flatMapLatest { base ->
            val cycleDays = base.config.cycleDaysPerMicrocycle.coerceAtLeast(1)
            val dayNumbers = (1..cycleDays).toList()
            val schedulesFlow = if (dayNumbers.isEmpty()) {
                flowOf(emptyList())
            } else {
                scheduleRepository.getSchedulesForDays(dayNumbers)
            }

            schedulesFlow.combine(flowOf(base)) { schedules, currentBase ->
                val zoneId = clock.zoneId()
                val today = Instant.ofEpochMilli(clock.now()).atZone(zoneId).toLocalDate()
                val schedulesByCycleDay: Map<Int, ScheduleDay> = schedules.associateBy { it.cycleDay }

                BetaMetricsAggregator.aggregate(
                    onboardingCompleted = currentBase.onboardingCompleted,
                    workoutLogs = currentBase.workoutLogs,
                    player = currentBase.player,
                    schedulesByCycleDay = schedulesByCycleDay,
                    eventState = currentBase.eventState,
                    today = today,
                    zoneId = zoneId,
                    cycleDayForDate = { date ->
                        resolveTrainingCycleDay(
                            targetDate = date,
                            config = currentBase.config,
                            fallbackCurrentCycleDay = currentBase.player?.currentCycleDay
                        )
                    }
                )
            }
        }
    }
}

private data class BetaMetricsBase(
    val onboardingCompleted: Boolean,
    val workoutLogs: List<WorkoutLog>,
    val player: Player?,
    val config: SystemConfig,
    val eventState: BetaMetricsEventState
)
