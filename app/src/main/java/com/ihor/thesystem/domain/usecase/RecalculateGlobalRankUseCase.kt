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

        // Розрахунок на основі топ-5 найкращих вправ користувача (щоб нові вправи не тягнули ранг вниз)
        val topScores = entries
            .map { it.currentRank.weight }
            .sortedByDescending { it }
            .take(5)
        
        val averageScore = topScores.average()
        
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
