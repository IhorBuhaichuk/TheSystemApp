package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.RankProgressionPolicy
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class RecalculateGlobalRankUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val playerRepo: PlayerRepository
) {
    suspend operator fun invoke() {
        val entries = matrixRepo.getAllEntries().first()
        val newGlobalRank = RankProgressionPolicy.resolveGlobalRank(
            entries.map { it.currentRank }
        ) ?: return

        val player = playerRepo.getPlayer().firstOrNull() ?: return
        if (player.globalRank != newGlobalRank) {
            playerRepo.updatePlayer(player.copy(globalRank = newGlobalRank))
        }
    }
}
