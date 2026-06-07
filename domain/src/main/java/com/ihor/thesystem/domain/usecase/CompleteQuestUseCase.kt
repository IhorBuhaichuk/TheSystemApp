package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.PlayerProgressionPolicy
import com.ihor.thesystem.domain.model.PlayerProgressionConfig
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.QuestCompletionPolicy
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.util.TransactionRollbackException
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

enum class QuestCompletionMode {
    TaskUpdate,
    DayFinalization
}

data class QuestCompletionOutcome(
    val questId: Int,
    val status: DomainQuestStatus,
    val rewardGranted: Boolean,
    val logWritten: Boolean,
    val penaltyActivated: Boolean
)

class CompleteQuestUseCase @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository,
    private val logger: AppLogger
) {
    suspend operator fun invoke(
        questId: Int,
        mode: QuestCompletionMode = QuestCompletionMode.TaskUpdate,
        forceComplete: Boolean = false
    ): Result<QuestCompletionOutcome, DomainError> {
        return try {
            val outcome = transactionProvider.runInTransaction {
                val quest = questRepository.getQuestById(questId)
                    ?: throw TransactionRollbackException(DataError.Local.NOT_FOUND)

                if (quest.status != DomainQuestStatus.ACTIVE) {
                    return@runInTransaction QuestCompletionOutcome(
                        questId = quest.id,
                        status = quest.status,
                        rewardGranted = false,
                        logWritten = false,
                        penaltyActivated = false
                    )
                }

                val resolution = when (mode) {
                    QuestCompletionMode.TaskUpdate -> QuestCompletionPolicy.resolveAfterTaskUpdate(
                        taskCount = quest.tasks.size,
                        completedTaskCount = quest.tasks.count { it.isCompleted }
                    ).let { status ->
                        QuestCompletionPolicy.resultForStatus(status)
                    }
                    QuestCompletionMode.DayFinalization ->
                        QuestCompletionPolicy.resolveForDayFinalization(quest, forceComplete)
                }

                if (resolution.status == DomainQuestStatus.ACTIVE) {
                    return@runInTransaction QuestCompletionOutcome(
                        questId = quest.id,
                        status = DomainQuestStatus.ACTIVE,
                        rewardGranted = false,
                        logWritten = false,
                        penaltyActivated = false
                    )
                }

                questRepository.updateQuestStatus(quest.id, resolution.status)
                questRepository.logQuestResult(
                    questId = quest.id,
                    questType = quest.type,
                    wasSuccessful = resolution.wasSuccessful
                )

                val progression = applyMainQuestProgression(
                    quest = quest,
                    wasSuccessful = resolution.wasSuccessful
                )

                QuestCompletionOutcome(
                    questId = quest.id,
                    status = resolution.status,
                    rewardGranted = progression.rewardGranted,
                    logWritten = true,
                    penaltyActivated = progression.penaltyActivated
                )
            }

            Result.Success(outcome)
        } catch (e: TransactionRollbackException) {
            Result.Error(e.error)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e, "Unexpected error during quest completion")
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }

    private suspend fun applyMainQuestProgression(
        quest: Quest,
        wasSuccessful: Boolean
    ): QuestProgressionSideEffect {
        if (quest.type != DomainQuestType.MAIN) {
            return QuestProgressionSideEffect.None
        }

        val player = playerRepository.getPlayerSnapshot()
            ?: throw TransactionRollbackException(DataError.Local.NOT_FOUND)

        val progression = if (wasSuccessful) {
            val progressionConfig = quest.systemTemplateType
                ?.let { PlayerProgressionConfig(workoutCompletionXp = it.completionXp) }
                ?: PlayerProgressionConfig()
            PlayerProgressionPolicy.applyMainQuestSuccess(
                player = player,
                reward = true,
                progressionConfig = progressionConfig
            )
        } else {
            PlayerProgressionPolicy.applyMainQuestFailure(player)
        }

        when (val updateResult = playerRepository.updatePlayer(progression.player)) {
            is Result.Success -> Unit
            is Result.Error -> throw TransactionRollbackException(updateResult.error)
        }

        return QuestProgressionSideEffect(
            rewardGranted = wasSuccessful,
            penaltyActivated = progression.penaltyActivated
        )
    }

    private data class QuestProgressionSideEffect(
        val rewardGranted: Boolean,
        val penaltyActivated: Boolean
    ) {
        companion object {
            val None = QuestProgressionSideEffect(
                rewardGranted = false,
                penaltyActivated = false
            )
        }
    }
}
