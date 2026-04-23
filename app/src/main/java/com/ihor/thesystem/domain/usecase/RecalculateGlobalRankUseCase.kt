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

        // Розрахунок на основі медіани топ-5 найкращих вправ
        val scores = entries
            .map { it.currentRank.weight }
            .sortedByDescending { it }
            .take(5)
        
        val globalRankValue = if (scores.isNotEmpty()) {
            val mid = scores.size / 2
            if (scores.size % 2 == 0) {
                ((scores[mid - 1] + scores[mid]) / 2.0).roundToInt()
            } else {
                scores[mid]
            }
        } else 1

        val newGlobalRank = Rank.fromValue(globalRankValue.coerceIn(1, 6))

        // 4. Зберігаємо новий ранг у гравця
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        if (player.globalRank != newGlobalRank) {
            playerRepo.updatePlayer(player.copy(globalRank = newGlobalRank))
        }
    }
}
