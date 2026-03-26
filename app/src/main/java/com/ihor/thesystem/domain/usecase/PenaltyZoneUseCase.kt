package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Перевіряє умови штрафної зони після кожного Main Quest.
 * Активує штраф якщо 2 провали поспіль або хвороба.
 * Знімає штраф після 2 успішних поспіль.
 */
class CheckPenaltyZoneUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val questRepo:  QuestRepository,
    private val matrixRepo: ProgressionMatrixRepository
) {
    suspend operator fun invoke() {
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        val lastTwo = questRepo.getLastTwoMainQuestsStatus()

        val twoConsecutiveFails = lastTwo.size >= 2 &&
                lastTwo.all { it == DomainQuestStatus.FAILED }

        val twoConsecutiveSuccess = lastTwo.size >= 2 &&
                lastTwo.all { it == DomainQuestStatus.COMPLETED }

        when {
            // Активуємо штраф
            twoConsecutiveFails && !player.isPenaltyActive -> {
                playerRepo.updatePlayer(player.copy(isPenaltyActive = true))
                applyWeightPenalty(penaltyPercent = 20)
            }
            // Знімаємо штраф
            twoConsecutiveSuccess && player.isPenaltyActive -> {
                playerRepo.updatePlayer(player.copy(isPenaltyActive = false))
            }
        }
    }

    private suspend fun applyWeightPenalty(penaltyPercent: Int) {
        val entries = matrixRepo.getAllEntries().firstOrNull() ?: return
        val factor  = 1f - (penaltyPercent / 100f)
        entries.forEach { entry ->
            val reduced = entry.currentWeight * factor
            matrixRepo.updateCurrentWeight(entry.exerciseId, reduced)
        }
    }
}

/**
 * Активує штраф вручну (наприклад, через дебаф "Хвороба").
 */
class ActivatePenaltyManuallyUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(penaltyPercent: Int = 20) {
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        if (player.isPenaltyActive) return
        playerRepo.updatePlayer(player.copy(isPenaltyActive = true))
        val entries = matrixRepo.getAllEntries().firstOrNull() ?: return
        val factor  = 1f - (penaltyPercent / 100f)
        entries.forEach { entry ->
            matrixRepo.updateCurrentWeight(entry.exerciseId, entry.currentWeight * factor)
        }
    }
}