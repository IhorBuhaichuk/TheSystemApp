package com.ihor.thesystem.domain.usecase

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
        var newClass = player.playerClass

        if (newWeek > 4) {
            newWeek   = 1
            newMonth += 1
            newClass  = resolveClass(newMonth)
        }

        playerRepo.updatePlayer(
            player.copy(
                currentWeek  = newWeek,
                currentMonth = newMonth,
                playerClass  = newClass
            )
        )
    }

    private fun resolveClass(month: Int): String = when (month) {
        1    -> "ПРОБУДЖЕНИЙ"
        2    -> "УЧЕНЬ"
        3    -> "ПОСЛІДОВНИК"
        4    -> "ВОЇН"
        5    -> "ВЕТЕРАН"
        6    -> "МАЙСТЕР"
        7    -> "ЕЛІТНИЙ ВОЇН"
        8    -> "ЧЕМПІОН"
        9    -> "ЛЕГЕНДА"
        10   -> "БЕЗСМЕРТНИЙ"
        11   -> "НАПІВБОГ"
        12   -> "СИСТЕМА"
        else -> "TRANSCENDED"
    }
}