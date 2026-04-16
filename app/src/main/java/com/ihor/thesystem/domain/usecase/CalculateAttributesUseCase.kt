package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.data.local.room.dao.QuestLogDao
import com.ihor.thesystem.data.local.room.entity.QuestType
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CalculateAttributesUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val questLogDao: QuestLogDao,
    private val playerRepo: PlayerRepository
) {
    suspend operator fun invoke(): Triple<Int, Int, Int> {
        val player = playerRepo.getPlayer().firstOrNull() ?: return Triple(0, 0, 0)

        // ─── STR (Сила, 0-100) ───
        val strengthExerciseIds = listOf(6, 8, 12, 13)
        val matrixEntries = matrixRepo.getAllEntries().first()
        
        var totalRankWeight = 0
        strengthExerciseIds.forEach { id ->
            val entry = matrixEntries.find { it.exerciseId == id }
            totalRankWeight += entry?.currentRank?.weight ?: Rank.E.weight
        }
        val calculatedStr = (totalRankWeight / 24.0 * 100).toInt().coerceIn(0, 100)

        // ─── END (Витривалість, 0-100) ───
        val recentLogs = questLogDao.getLastNLogs(20)
        val mainLogs = recentLogs.filter { it.questType == QuestType.MAIN }
        
        val calculatedEnd = if (mainLogs.isEmpty()) {
            0
        } else {
            val successfulCount = mainLogs.count { it.wasSuccessful }
            (successfulCount.toDouble() / mainLogs.size * 100).toInt().coerceIn(0, 100)
        }

        // ─── DIS (Дисципліна, 0-100) ───
        val disBase = when (player.consecutiveMainQuestFailures) {
            0 -> 100
            1 -> 75
            2 -> 40
            else -> 10
        }
        val calculatedDis = if (player.isPenaltyActive) minOf(disBase, 40) else disBase

        // ─── Збереження ───
        if (player.strAttribute == calculatedStr && player.endAttribute == calculatedEnd 
            && player.disAttribute == calculatedDis) return Triple(calculatedStr, calculatedEnd, calculatedDis)

        val updatedPlayer = player.copy(
            strAttribute = calculatedStr,
            endAttribute = calculatedEnd,
            disAttribute = calculatedDis
        )
        playerRepo.updatePlayer(updatedPlayer)

        return Triple(calculatedStr, calculatedEnd, calculatedDis)
    }
}
