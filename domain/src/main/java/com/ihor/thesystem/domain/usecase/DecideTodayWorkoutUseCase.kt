package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.ReadinessInput
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.ReadinessScore
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtInput
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.RecoveryDebtWorkout
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.repository.CalendarCycleRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ReadinessRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AppClock
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class DecideTodayWorkoutUseCase @Inject constructor(
    private val configRepository: SystemConfigRepository,
    private val playerRepository: PlayerRepository,
    private val scheduleRepository: ScheduleRepository,
    private val readinessRepository: ReadinessRepository,
    private val workoutAnalyticsRepository: WorkoutAnalyticsRepository,
    private val questRepository: QuestRepository,
    private val calendarCycleRepository: CalendarCycleRepository,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val calculateReadiness: CalculateReadinessUseCase,
    private val calculateRecoveryDebt: CalculateRecoveryDebtUseCase,
    private val clock: AppClock
) {

    suspend operator fun invoke(targetDate: LocalDate = today()): TodayTrainingDecision {
        val config = configRepository.getConfigFlow().firstOrNull() ?: SystemConfig()
        val player = playerRepository.getPlayer().firstOrNull()
        val dateEpochDay = targetDate.toEpochDay()
        val cycleDay = resolveTrainingCycleDay(
            targetDate = targetDate,
            config = config,
            fallbackCurrentCycleDay = player?.currentCycleDay
        )
        val schedule = scheduleRepository.getScheduleForDay(cycleDay).firstOrNull()
        val readinessContext = readinessFor(dateEpochDay)
        val allLogs = workoutAnalyticsRepository.getAllLogs().firstOrNull().orEmpty()
        val recentLogs = allLogs.recentLogsFor(targetDate)
        val plannedWorkoutEpochDays = plannedWorkoutEpochDays(
            targetDate = targetDate,
            config = config,
            fallbackCurrentCycleDay = player?.currentCycleDay
        )
        val hasTrainingHistory = allLogs.isNotEmpty()
        val plannedDaysForDebt = if (hasTrainingHistory) {
            plannedWorkoutEpochDays.filter { it < dateEpochDay }
        } else {
            emptyList()
        }
        val recoveryDebt = calculateRecoveryDebt(
            RecoveryDebtInput(
                recentWorkouts = recentLogs.toRecoveryDebtWorkouts(),
                plannedWorkoutEpochDays = plannedDaysForDebt,
                readiness = readinessContext.score,
                readinessInput = readinessContext.input,
                referenceEpochDay = dateEpochDay
            )
        )
        val activeMainQuest = questRepository.getActiveMainQuest().firstOrNull()
        val calendarDayType = calendarDayType(targetDate)
        val warnings = mutableListOf<String>().apply {
            addAll(readinessContext.warnings)
            if (calendarDayType == CalendarCycleDayType.RECOVERY) {
                add("Calendar cycle marks today as recovery.")
            }
            if (calendarDayType == CalendarCycleDayType.OFF) {
                add("Calendar cycle marks today as off.")
            }
            if (schedule?.isWorkoutDay == true && activeMainQuest == null) {
                add("No active main quest is available for today's workout.")
            }
        }

        if (readinessContext.score.level == ReadinessLevel.RECOVERY ||
            recoveryDebt.level == RecoveryDebtLevel.CRITICAL
        ) {
            return baseDecision(
                dateEpochDay = dateEpochDay,
                cycleDay = cycleDay,
                schedule = schedule,
                readiness = readinessContext.score,
                recoveryDebt = recoveryDebt,
                warnings = warnings,
                decisionType = TodayTrainingDecisionType.ACTIVE_RECOVERY,
                loadMultiplier = ACTIVE_RECOVERY_LOAD,
                volumeMultiplier = ACTIVE_RECOVERY_VOLUME,
                reason = "Recovery readiness or critical recovery debt blocks hard training.",
                isTrainingAllowed = false
            )
        }

        if (schedule?.isWorkoutDay != true) {
            return baseDecision(
                dateEpochDay = dateEpochDay,
                cycleDay = cycleDay,
                schedule = schedule,
                readiness = readinessContext.score,
                recoveryDebt = recoveryDebt,
                warnings = warnings,
                decisionType = TodayTrainingDecisionType.REST,
                loadMultiplier = 0f,
                volumeMultiplier = 0f,
                reason = "No workout is scheduled for this cycle day.",
                isTrainingAllowed = false
            )
        }

        if (shouldDeload(recoveryDebt, recentLogs)) {
            return baseDecision(
                dateEpochDay = dateEpochDay,
                cycleDay = cycleDay,
                schedule = schedule,
                readiness = readinessContext.score,
                recoveryDebt = recoveryDebt,
                warnings = warnings,
                decisionType = TodayTrainingDecisionType.DELOAD,
                loadMultiplier = DELOAD_LOAD,
                volumeMultiplier = DELOAD_VOLUME,
                reason = "Recovery debt or repeated heavy sessions call for a deload.",
                isTrainingAllowed = true
            )
        }

        val missedBeforeToday = plannedWorkoutEpochDays
            .filter { it < dateEpochDay }
            .count { plannedDay -> allLogs.none { it.session.timestamp.toEpochDay() == plannedDay } }
        if (shouldNoExcuse(missedBeforeToday, hasTrainingHistory, readinessContext.score, recoveryDebt)) {
            val questReason = activeMainQuest
                ?.takeIf { it.type == DomainQuestType.MAIN }
                ?.let { " Active main quest: ${it.title}." }
                .orEmpty()
            return baseDecision(
                dateEpochDay = dateEpochDay,
                cycleDay = cycleDay,
                schedule = schedule,
                readiness = readinessContext.score,
                recoveryDebt = recoveryDebt,
                warnings = warnings,
                decisionType = TodayTrainingDecisionType.NO_EXCUSE,
                loadMultiplier = STANDARD_LOAD,
                volumeMultiplier = STANDARD_VOLUME,
                reason = "The system detected a missed workout. Plan recalculated. Next optimal action: short training.$questReason",
                isTrainingAllowed = true
            )
        }

        return when (readinessContext.score.level) {
            ReadinessLevel.PROGRESS -> {
                if (recoveryDebt.level == RecoveryDebtLevel.LOW) {
                    baseDecision(
                        dateEpochDay = dateEpochDay,
                        cycleDay = cycleDay,
                        schedule = schedule,
                        readiness = readinessContext.score,
                        recoveryDebt = recoveryDebt,
                        warnings = warnings,
                        decisionType = TodayTrainingDecisionType.PROGRESS_ALLOWED,
                        loadMultiplier = PROGRESS_LOAD,
                        volumeMultiplier = STANDARD_VOLUME,
                        reason = "Readiness is high and recovery debt is low.",
                        isTrainingAllowed = true
                    )
                } else {
                    standardDecision(dateEpochDay, cycleDay, schedule, readinessContext.score, recoveryDebt, warnings)
                }
            }
            ReadinessLevel.STANDARD ->
                standardDecision(dateEpochDay, cycleDay, schedule, readinessContext.score, recoveryDebt, warnings)
            ReadinessLevel.REDUCED ->
                baseDecision(
                    dateEpochDay = dateEpochDay,
                    cycleDay = cycleDay,
                    schedule = schedule,
                    readiness = readinessContext.score,
                    recoveryDebt = recoveryDebt,
                    warnings = warnings,
                    decisionType = TodayTrainingDecisionType.NO_EXCUSE,
                    loadMultiplier = NO_EXCUSE_LOAD,
                    volumeMultiplier = NO_EXCUSE_VOLUME,
                    reason = "Readiness is low but recovery is not blocked, so a short bodyweight protocol preserves rhythm.",
                    isTrainingAllowed = true
                )
            ReadinessLevel.RECOVERY ->
                baseDecision(
                    dateEpochDay = dateEpochDay,
                    cycleDay = cycleDay,
                    schedule = schedule,
                    readiness = readinessContext.score,
                    recoveryDebt = recoveryDebt,
                    warnings = warnings,
                    decisionType = TodayTrainingDecisionType.ACTIVE_RECOVERY,
                    loadMultiplier = ACTIVE_RECOVERY_LOAD,
                    volumeMultiplier = ACTIVE_RECOVERY_VOLUME,
                    reason = "Recovery readiness blocks hard training.",
                    isTrainingAllowed = false
                )
        }
    }

    private suspend fun readinessFor(dateEpochDay: Long): ReadinessContext {
        val todayEntry = readinessRepository.getEntryForDate(dateEpochDay)
        if (todayEntry != null) {
            return ReadinessContext(
                score = ReadinessScore(
                    score = todayEntry.score,
                    level = todayEntry.level,
                    reasons = emptyList()
                ),
                input = todayEntry.input,
                warnings = emptyList()
            )
        }

        val latestEntry = readinessRepository
            .getEntriesBetween(dateEpochDay - READINESS_LOOKBACK_DAYS, dateEpochDay)
            .maxByOrNull { it.dateEpochDay }
        if (latestEntry != null) {
            return ReadinessContext(
                score = ReadinessScore(
                    score = latestEntry.score,
                    level = latestEntry.level,
                    reasons = emptyList()
                ),
                input = latestEntry.input,
                warnings = listOf("Using latest available readiness entry from a previous day.")
            )
        }

        val fallbackInput = ReadinessInput()
        return ReadinessContext(
            score = calculateReadiness(fallbackInput),
            input = fallbackInput,
            warnings = listOf("No readiness entry found; using neutral fallback readiness.")
        )
    }

    private suspend fun plannedWorkoutEpochDays(
        targetDate: LocalDate,
        config: SystemConfig,
        fallbackCurrentCycleDay: Int?
    ): List<Long> {
        val dates = (0L until DEBT_LOOKBACK_DAYS).map { offset -> targetDate.minusDays(offset) }
        val dayByDate = dates.associateWith { date ->
            resolveTrainingCycleDay(
                targetDate = date,
                config = config,
                fallbackCurrentCycleDay = fallbackCurrentCycleDay
            )
        }
        val schedulesByDay = scheduleRepository
            .getSchedulesForDays(dayByDate.values.distinct())
            .firstOrNull()
            .orEmpty()
            .associateBy { it.cycleDay }

        return dayByDate
            .filter { (_, cycleDay) -> schedulesByDay[cycleDay]?.isWorkoutDay == true }
            .keys
            .map { it.toEpochDay() }
    }

    private fun standardDecision(
        dateEpochDay: Long,
        cycleDay: Int,
        schedule: ScheduleDay,
        readiness: ReadinessScore,
        recoveryDebt: RecoveryDebt,
        warnings: List<String>
    ): TodayTrainingDecision =
        baseDecision(
            dateEpochDay = dateEpochDay,
            cycleDay = cycleDay,
            schedule = schedule,
            readiness = readiness,
            recoveryDebt = recoveryDebt,
            warnings = warnings,
            decisionType = TodayTrainingDecisionType.STANDARD_TRAINING,
            loadMultiplier = STANDARD_LOAD,
            volumeMultiplier = STANDARD_VOLUME,
            reason = "Readiness supports the planned workout.",
            isTrainingAllowed = true
        )

    private fun baseDecision(
        dateEpochDay: Long,
        cycleDay: Int,
        schedule: ScheduleDay?,
        readiness: ReadinessScore,
        recoveryDebt: RecoveryDebt,
        warnings: List<String>,
        decisionType: TodayTrainingDecisionType,
        loadMultiplier: Float,
        volumeMultiplier: Float,
        reason: String,
        isTrainingAllowed: Boolean
    ): TodayTrainingDecision =
        TodayTrainingDecision(
            dateEpochDay = dateEpochDay,
            cycleDay = cycleDay,
            workoutName = schedule?.workoutTemplateName,
            readinessScore = readiness.score,
            readinessLevel = readiness.level,
            recoveryDebt = recoveryDebt,
            decisionType = decisionType,
            loadMultiplier = loadMultiplier,
            volumeMultiplier = volumeMultiplier,
            reason = reason,
            warnings = warnings,
            selectedWorkoutTemplateId = schedule?.workoutTemplateId,
            isTrainingAllowed = isTrainingAllowed
        )

    private fun shouldNoExcuse(
        missedBeforeToday: Int,
        hasTrainingHistory: Boolean,
        readiness: ReadinessScore,
        recoveryDebt: RecoveryDebt
    ): Boolean =
        hasTrainingHistory &&
            missedBeforeToday > 0 &&
            readiness.level in setOf(ReadinessLevel.PROGRESS, ReadinessLevel.STANDARD) &&
            recoveryDebt.level in setOf(RecoveryDebtLevel.LOW, RecoveryDebtLevel.MODERATE)

    private fun shouldDeload(recoveryDebt: RecoveryDebt, recentLogs: List<WorkoutLog>): Boolean {
        if (recoveryDebt.level == RecoveryDebtLevel.HIGH) return true
        val heavySessions = recentLogs
            .sortedByDescending { it.session.timestamp }
            .takeWhile { it.session.totalTonnage >= HEAVY_SESSION_TONNAGE }
            .size
        return heavySessions >= HEAVY_SESSION_STREAK
    }

    private fun List<WorkoutLog>.recentLogsFor(targetDate: LocalDate): List<WorkoutLog> {
        val startEpochDay = targetDate.minusDays(DEBT_LOOKBACK_DAYS - 1).toEpochDay()
        val endEpochDay = targetDate.toEpochDay()
        return filter { log ->
            val epochDay = log.session.timestamp.toEpochDay()
            epochDay in startEpochDay..endEpochDay
        }
    }

    private fun List<WorkoutLog>.toRecoveryDebtWorkouts(): List<RecoveryDebtWorkout> =
        map { log ->
            RecoveryDebtWorkout(
                dateEpochDay = log.session.timestamp.toEpochDay(),
                tonnage = log.session.totalTonnage,
                completed = true
            )
        }

    private suspend fun calendarDayType(date: LocalDate): CalendarCycleDayType? =
        runCatching {
            calendarCycleRepository.getCalendarCycle()
                .firstOrNull()
                ?.dayForOrNull(date)
                ?.type
        }.getOrNull()

    private fun Long.toEpochDay(): Long =
        Instant.ofEpochMilli(this)
            .atZone(clock.zoneId())
            .toLocalDate()
            .toEpochDay()

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()

    private data class ReadinessContext(
        val score: ReadinessScore,
        val input: ReadinessInput,
        val warnings: List<String>
    )

    private companion object {
        const val READINESS_LOOKBACK_DAYS = 3L
        const val DEBT_LOOKBACK_DAYS = 7L
        const val HEAVY_SESSION_TONNAGE = 8_000.0
        const val HEAVY_SESSION_STREAK = 3
        const val PROGRESS_LOAD = 1.025f
        const val STANDARD_LOAD = 1.0f
        const val STANDARD_VOLUME = 1.0f
        const val REDUCED_LOAD = 0.9f
        const val REDUCED_VOLUME = 0.7f
        const val NO_EXCUSE_LOAD = 0.0f
        const val NO_EXCUSE_VOLUME = 0.45f
        const val ACTIVE_RECOVERY_LOAD = 0.5f
        const val ACTIVE_RECOVERY_VOLUME = 0.4f
        const val DELOAD_LOAD = 0.8f
        const val DELOAD_VOLUME = 0.6f
    }
}
