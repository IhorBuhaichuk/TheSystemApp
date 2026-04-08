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

        // 1. Переводимо ранги у числові еквіваленти (E=0..S=5)
        val rankValues = entries.map { it.currentRank.value }
        
        // 2. Вираховуємо середнє арифметичне
        val averageValue = rankValues.average()
        
        // 3. Конвертуємо назад у Rank (округлюємо до найближчого цілого)
        val globalRankValue = averageValue.roundToInt().coerceIn(0, 5)
        val newGlobalRank = Rank.fromValue(globalRankValue)

        // 4. Зберігаємо у гравця
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        if (player.globalRank != newGlobalRank) {
            playerRepo.updatePlayer(player.copy(globalRank = newGlobalRank))
        }
    }
}
