package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Викликається після завершення Main Quest.
 * Інкрементує тиждень. Якщо тижнів >= 4 — підвищує місяць і клас.
 */
class LevelUpUseCase @Inject constructor(
    private val playerRepo: PlayerRepository
) {
    suspend operator fun invoke() {
        val player = playerRepo.getPlayer().firstOrNull() ?: return

        var newWeek  = player.currentWeek + 1
        var newMonth = player.currentMonth
        var newRank = PlayerRank.fromMonth(newMonth)

        if (newWeek > 4) {
            newWeek   = 1
            newMonth += 1
            newRank  = PlayerRank.fromMonth(newMonth)
        }

        playerRepo.updatePlayer(
            player.copy(
                currentWeek  = newWeek,
                currentMonth = newMonth,
                playerClass  = newRank.title
            )
        )
    }
}
