package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.data.local.room.dao.QuestLogDao
import com.ihor.thesystem.data.local.room.entity.QuestType
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.ExerciseCategory
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
    suspend operator fun invoke(): Result<Triple<Int, Int, Int>, DomainError> {
        val player = playerRepo.getPlayer().firstOrNull() 
            ?: return Result.Success(Triple(0, 0, 0))

        // ─── STR (Сила, 0-100) ───
        val strengthExerciseIds = matrixRepo.getExerciseIdsByCategory(ExerciseCategory.STRENGTH)
        val matrixEntries = matrixRepo.getAllEntries().first()
        
        var totalRankWeight = 0
        if (strengthExerciseIds.isNotEmpty()) {
            strengthExerciseIds.forEach { id ->
                val entry = matrixEntries.find { it.exerciseId == id }
                totalRankWeight += entry?.currentRank?.weight ?: Rank.E.weight
            }
            // 24.0 (4 вправи * макс. ранг 6) замінюємо на динамічний розрахунок
            val maxPossibleWeight = strengthExerciseIds.size * 6.0
            val calculatedStr = (totalRankWeight / maxPossibleWeight * 100).toInt().coerceIn(0, 100)
            
            return processRemainingAttributes(player, calculatedStr)
        } else {
            return processRemainingAttributes(player, 0)
        }
    }

    private suspend fun processRemainingAttributes(player: com.ihor.thesystem.domain.model.Player, calculatedStr: Int): Result<Triple<Int, Int, Int>, DomainError> {
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

        val resultValues = Triple(calculatedStr, calculatedEnd, calculatedDis)

        // ─── Збереження ───
        if (player.strAttribute == calculatedStr && player.endAttribute == calculatedEnd 
            && player.disAttribute == calculatedDis) return Result.Success(resultValues)

        val updatedPlayer = player.copy(
            strAttribute = calculatedStr,
            endAttribute = calculatedEnd,
            disAttribute = calculatedDis
        )
        
        return when (val updateResult = playerRepo.updatePlayer(updatedPlayer)) {
            is Result.Error -> Result.Error(updateResult.error)
            is Result.Success -> Result.Success(resultValues)
        }
    }
}
