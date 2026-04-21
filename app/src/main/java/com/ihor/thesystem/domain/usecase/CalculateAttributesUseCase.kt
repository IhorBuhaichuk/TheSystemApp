package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.data.local.room.dao.QuestLogDao
import com.ihor.thesystem.data.local.room.entity.QuestType
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.MuscleGroupMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class CalculatedAttributes(
    val muscleAttributes: Map<MuscleGroup, Float>
)

class CalculateAttributesUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val questLogDao: QuestLogDao,
    private val playerRepo: PlayerRepository
) {
    suspend operator fun invoke(): Result<CalculatedAttributes, DomainError> {
        val player = playerRepo.getPlayer().firstOrNull() 
            ?: return Result.Error(DataError.Local.NOT_FOUND)

        val matrixEntries = matrixRepo.getAllEntries().first()
        
        // Muscle Group RPG Attributes
        val muscleMap = mutableMapOf<MuscleGroup, Float>()
        MuscleGroup.entries.forEach { group ->
            val groupExercises = matrixEntries.filter { 
                MuscleGroupMapper.getMuscleGroupsForExercise(it.exerciseName).contains(group)
            }
            if (groupExercises.isEmpty()) {
                muscleMap[group] = 0f
            } else {
                val totalRank = groupExercises.sumOf { it.currentRank.weight }
                val maxPossible = groupExercises.size * 6.0
                muscleMap[group] = (totalRank / maxPossible * 100).toFloat().coerceIn(0f, 100f)
            }
        }

        val updatedPlayer = player.copy(
            chestAttr = muscleMap[MuscleGroup.CHEST]?.toInt() ?: 0,
            backAttr = muscleMap[MuscleGroup.BACK]?.toInt() ?: 0,
            shouldersAttr = muscleMap[MuscleGroup.SHOULDERS]?.toInt() ?: 0,
            quadsAttr = muscleMap[MuscleGroup.QUADS]?.toInt() ?: 0,
            legsAttr = muscleMap[MuscleGroup.HAMSTRINGS_GLUTES]?.toInt() ?: 0,
            armsAttr = muscleMap[MuscleGroup.ARMS]?.toInt() ?: 0
        )

        // Only update if changed to avoid Flow loops
        if (updatedPlayer.chestAttr != player.chestAttr ||
            updatedPlayer.backAttr != player.backAttr ||
            updatedPlayer.shouldersAttr != player.shouldersAttr ||
            updatedPlayer.quadsAttr != player.quadsAttr ||
            updatedPlayer.legsAttr != player.legsAttr ||
            updatedPlayer.armsAttr != player.armsAttr
        ) {
            playerRepo.updatePlayer(updatedPlayer)
        }

        return Result.Success(
            CalculatedAttributes(
                muscleAttributes = muscleMap
            )
        )
    }
}

