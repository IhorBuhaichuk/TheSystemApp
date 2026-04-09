package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

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
