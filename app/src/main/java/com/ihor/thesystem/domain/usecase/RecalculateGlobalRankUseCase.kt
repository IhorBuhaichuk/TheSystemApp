package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.roundToInt

class RecalculateGlobalRankUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val playerRepo: PlayerRepository
) {
    suspend operator fun invoke() {
        val entries = matrixRepo.getAllEntries().first()
        if (entries.isEmpty()) return

        // 1. Отримуємо суму числових значень рангів (E=1..S=6)
        val totalScore = entries.sumOf { it.currentRank.weight }
        
        // 2. Вираховуємо середнє арифметичне
        val averageScore = totalScore.toDouble() / entries.size
        
        // 3. Конвертуємо результат назад у Rank (округлюємо до найближчого цілого)
        val globalRankValue = averageScore.roundToInt().coerceIn(1, 6)
        val newGlobalRank = Rank.fromValue(globalRankValue)

        // 4. Зберігаємо новий ранг у гравця
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        if (player.globalRank != newGlobalRank) {
            playerRepo.updatePlayer(player.copy(globalRank = newGlobalRank))
        }
    }
}
