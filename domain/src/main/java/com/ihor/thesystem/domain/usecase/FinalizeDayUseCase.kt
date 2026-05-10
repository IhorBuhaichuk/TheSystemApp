package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.TransactionRollbackException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import java.time.Instant
import java.time.LocalDate

class FinalizeDayUseCase @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val generateDailyQuests: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase,
    private val advanceCycleDayStatus: AdvanceCycleDayUseCase,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val clock: AppClock,
    private val logger: AppLogger
) {
    /**
     * Finalizes the current day, updates player stats, archives quests, and prepares the next day.
     * This is the single source of truth for day transitions.
     */
    suspend operator fun invoke(forceComplete: Boolean = false): Result<DayFinalizationResult, DomainError> {
        return try {
            logger.d("Starting day finalization (forceComplete=$forceComplete)")

            // 1. Pre-fetch data
            val player = playerRepo.getPlayer().firstOrNull()
                ?: return Result.Error(DataError.Local.NOT_FOUND)
            val config = configRepo.getConfigFlow().firstOrNull()
                ?: SystemConfig()
            val todayEpochDay = todayEpochDay()
            val daysToAdvance = daysToAdvance(config, todayEpochDay)
            if (daysToAdvance <= 0L) {
                logger.d("Day finalization skipped: today is already synchronized")
                return Result.Success(DayFinalizationResult.None)
            }

            val hasMissedTrainingDay = !forceComplete &&
                hasMissedScheduledWorkoutDay(config, player, todayEpochDay)

            // 2. Atomic updates
            val transactionResult = transactionProvider.runInTransaction {
                val activeQuests = questRepo.getActiveQuests().firstOrNull().orEmpty()
                activeQuests.forEach {
                    QuestCompletionPolicy.resolveForDayFinalization(it, forceComplete)
                }
                val wasPenaltyActive = player.isPenaltyActive

                // A) Update active quests to COMPLETED/FAILED and log results
                val statusResult = advanceCycleDayStatus(forceComplete)
                if (statusResult is Result.Error) {
                    throw TransactionRollbackException(statusResult.error)
                }

                // B) Apply day-level progression that is not quest reward itself.
                val playerAfterQuestCompletion = playerRepo.getPlayerSnapshot() ?: player
                val playerAfterMissedTrainingCheck = if (hasMissedTrainingDay) {
                    PlayerProgressionPolicy.applyMissedScheduledWorkout(playerAfterQuestCompletion)
                } else {
                    playerAfterQuestCompletion
                }
                val (playerAfterXP, levelUpTriggered) = playerAfterMissedTrainingCheck.checkLevelUp()
                val finalPlayer = playerAfterXP.advanceTime(config, daysToAdvance)

                // C) Persist state
                when (val updateResult = playerRepo.updatePlayer(finalPlayer)) {
                    is Result.Success -> Unit
                    is Result.Error -> throw TransactionRollbackException(updateResult.error)
                }
                questRepo.archiveActiveQuests()

                // D) Update config flags
                configRepo.saveLastInitDate(todayEpochDay)
                configRepo.setNeedsDailyInit(true)

                // Keep next-day initialization atomic with archiving and player updates.
                generateDailyQuests()
                when (val attributesResult = calculateAttributes()) {
                    is Result.Success -> Unit
                    is Result.Error -> throw TransactionRollbackException(attributesResult.error)
                }
                configRepo.setNeedsDailyInit(false)

                when {
                    levelUpTriggered -> DayFinalizationResult.LevelUp
                    !wasPenaltyActive && finalPlayer.isPenaltyActive -> DayFinalizationResult.PenaltyZoneEntered
                    else -> DayFinalizationResult.Success
                }
            }

            logger.d("Day finalization completed successfully: $transactionResult")
            Result.Success(transactionResult)

        } catch (e: TransactionRollbackException) {
            logger.e(e, "Transaction rolled back during day finalization")
            Result.Error(e.error)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e, "Unexpected error during day finalization")
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }

    private fun todayEpochDay(): Long =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
            .toEpochDay()

    private fun daysToAdvance(config: SystemConfig, todayEpochDay: Long): Long {
        val lastInitEpochDay = config.lastInitEpochDay
        return if (lastInitEpochDay > 0L && lastInitEpochDay < todayEpochDay) {
            todayEpochDay - lastInitEpochDay
        } else {
            0L
        }
    }

    private suspend fun hasMissedScheduledWorkoutDay(
        config: SystemConfig,
        player: Player,
        todayEpochDay: Long
    ): Boolean {
        val lastInitEpochDay = config.lastInitEpochDay
        val skippedDays = todayEpochDay - lastInitEpochDay - 1L
        if (lastInitEpochDay <= 0L || skippedDays <= 0L) return false

        val cycleLength = config.cycleDaysPerMicrocycle.coerceAtLeast(1)
        val cycleDaysToCheck = if (skippedDays >= cycleLength.toLong()) {
            (1..cycleLength).toList()
        } else {
            (lastInitEpochDay + 1 until todayEpochDay)
                .map { epochDay ->
                    resolveTrainingCycleDay(
                        targetDate = LocalDate.ofEpochDay(epochDay),
                        config = config,
                        fallbackCurrentCycleDay = player.currentCycleDay
                    )
                }
                .distinct()
        }

        return cycleDaysToCheck.any { cycleDay ->
            scheduleRepo.getScheduleForDay(cycleDay).firstOrNull()?.isWorkoutDay == true
        }
    }
}
