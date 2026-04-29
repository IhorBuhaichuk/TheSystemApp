package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.TransactionRollbackException
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject
import java.time.Instant

class FinalizeDayUseCase @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val configRepo: SystemConfigRepository,
    private val generateDailyQuests: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase,
    private val advanceCycleDayStatus: AdvanceCycleDayUseCase,
    private val clock: AppClock
) {
    /**
     * Finalizes the current day, updates player stats, archives quests, and prepares the next day.
     * This is the single source of truth for day transitions.
     */
    suspend operator fun invoke(forceComplete: Boolean = false): Result<DayFinalizationResult, DomainError> {
        return try {
            Timber.d("Starting day finalization (forceComplete=$forceComplete)")

            // 1. Pre-fetch data
            val player = playerRepo.getPlayer().firstOrNull()
                ?: return Result.Error(DataError.Local.NOT_FOUND)
            val config = configRepo.getConfigFlow().firstOrNull()
                ?: SystemConfig()

            // 2. Atomic updates
            val transactionResult = transactionProvider.runInTransaction {
                val activeQuests = questRepo.getActiveQuests().firstOrNull().orEmpty()

                // A) Update active quests to COMPLETED/FAILED and log results
                val statusResult = advanceCycleDayStatus(forceComplete)
                if (statusResult is Result.Error) {
                    throw TransactionRollbackException(statusResult.error)
                }

                // B) Evaluate player progress based on MAIN quests
                val processedMainQuests = activeQuests.filter { it.type == DomainQuestType.MAIN }.map { q ->
                    val hasTasks = q.tasks.isNotEmpty()
                    val allDone = hasTasks && q.tasks.all { it.isCompleted }
                    val isSuccess = if (!hasTasks) true else (allDone || forceComplete)
                    q.copy(status = if (isSuccess) DomainQuestStatus.COMPLETED else DomainQuestStatus.FAILED)
                }

                val wasPenaltyActive = player.isPenaltyActive
                val (playerAfterXP, levelUpTriggered) = player.evaluateQuests(processedMainQuests).checkLevelUp()
                val finalPlayer = playerAfterXP.advanceTime(config)

                // C) Persist state
                when (val updateResult = playerRepo.updatePlayer(finalPlayer)) {
                    is Result.Success -> Unit
                    is Result.Error -> throw TransactionRollbackException(updateResult.error)
                }
                questRepo.archiveActiveQuests()

                // D) Update config flags
                val today = Instant.ofEpochMilli(clock.now())
                    .atZone(clock.zoneId())
                    .toLocalDate()
                    .toEpochDay()
                configRepo.saveLastInitDate(today)
                configRepo.setNeedsDailyInit(true)

                when {
                    levelUpTriggered -> DayFinalizationResult.LevelUp
                    !wasPenaltyActive && finalPlayer.isPenaltyActive -> DayFinalizationResult.PenaltyZoneEntered
                    else -> DayFinalizationResult.Success
                }
            }

            // 3. Post-transaction initialization (Heavy work)
            generateDailyQuests.invoke()
            calculateAttributes.invoke()
            
            // 4. Mark initialization as complete
            configRepo.setNeedsDailyInit(false)

            Timber.d("Day finalization completed successfully: $transactionResult")
            Result.Success(transactionResult)

        } catch (e: TransactionRollbackException) {
            Timber.e(e, "Transaction rolled back during day finalization")
            Result.Error(e.error)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during day finalization")
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }
}
