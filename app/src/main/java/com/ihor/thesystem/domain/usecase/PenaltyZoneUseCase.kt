package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Перевіряє умови штрафної зони після кожного Main Quest.
 * Активує штраф якщо 2 провали поспіль.
 * Знімає штраф після 2 успішних поспіль.
 * 
 * ВАЖЛИВО: Цей UseCase лише змінює прапорець isPenaltyActive.
 * Розрахунок зниженої ваги відбувається динамічно в CalculateEffectiveWeightUseCase.
 */
class CheckPenaltyZoneUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val questRepo:  QuestRepository
) {
    suspend operator fun invoke() {
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        val lastTwo = questRepo.getLastTwoMainQuestsStatus()

        val twoConsecutiveFails = lastTwo.size >= 2 &&
                lastTwo.all { it == DomainQuestStatus.FAILED }

        val twoConsecutiveSuccess = lastTwo.size >= 2 &&
                lastTwo.all { it == DomainQuestStatus.COMPLETED }

        when {
            // Активуємо штраф (без деструктивного перезапису ваг)
            twoConsecutiveFails && !player.isPenaltyActive -> {
                playerRepo.updatePlayer(player.copy(isPenaltyActive = true))
            }
            // Знімаємо штраф
            twoConsecutiveSuccess && player.isPenaltyActive -> {
                playerRepo.updatePlayer(player.copy(isPenaltyActive = false))
            }
        }
    }
}

/**
 * Активує штраф вручну (наприклад, через дебаф "Хвороба").
 */
class ActivatePenaltyManuallyUseCase @Inject constructor(
    private val playerRepo: PlayerRepository
) {
    suspend operator fun invoke() {
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        if (!player.isPenaltyActive) {
            playerRepo.updatePlayer(player.copy(isPenaltyActive = true))
        }
    }
}
