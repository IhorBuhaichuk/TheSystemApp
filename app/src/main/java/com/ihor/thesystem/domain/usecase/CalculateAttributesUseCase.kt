package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
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
    private val playerRepo: PlayerRepository
) {
    /**
     * Оновлює RPG-атрибути гравця.
     * @param exerciseId Якщо передано, перераховуються лише групи м'язів, пов'язані з цією вправою.
     *                   Якщо null - повний перерахунок.
     */
    suspend operator fun invoke(exerciseId: Int? = null): Result<CalculatedAttributes, DomainError> {
        val player = playerRepo.getPlayer().firstOrNull() 
            ?: return Result.Error(DataError.Local.NOT_FOUND)

        val matrixEntries = matrixRepo.getAllEntries().first()
        
        // Визначаємо групи м'язів для оновлення
        val groupsToUpdate = if (exerciseId != null) {
            val exercise = matrixEntries.find { it.exerciseId == exerciseId }
            if (exercise != null) {
                MuscleGroupMapper.getMuscleGroupsForExercise(exercise.exerciseName)
            } else {
                MuscleGroup.entries
            }
        } else {
            MuscleGroup.entries
        }

        val muscleMap = mutableMapOf<MuscleGroup, Float>()
        
        // Поточні значення атрибутів з гравця для тих груп, які ми не оновлюємо
        MuscleGroup.entries.forEach { group ->
            muscleMap[group] = when(group) {
                MuscleGroup.CHEST -> player.chestAttr.toFloat()
                MuscleGroup.BACK -> player.backAttr.toFloat()
                MuscleGroup.SHOULDERS -> player.shouldersAttr.toFloat()
                MuscleGroup.QUADS -> player.quadsAttr.toFloat()
                MuscleGroup.HAMSTRINGS_GLUTES -> player.legsAttr.toFloat()
                MuscleGroup.ARMS -> player.armsAttr.toFloat()
                else -> 0f
            }
        }

        // Перераховуємо лише необхідні групи
        groupsToUpdate.forEach { group ->
            val groupExercises = matrixEntries.filter { 
                MuscleGroupMapper.getMuscleGroupsForExercise(it.exerciseName).contains(group)
            }
            
            if (groupExercises.isEmpty()) {
                muscleMap[group] = 0f
            } else {
                val totalRankWeight = groupExercises.sumOf { it.currentRank.weight }
                // Максимальна вага рангу береться з Rank.S (6)
                val maxPossibleWeight = groupExercises.size * Rank.S.weight.toDouble()
                muscleMap[group] = (totalRankWeight / maxPossibleWeight * 100).toFloat().coerceIn(0f, 100f)
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

        // Оновлюємо базу тільки якщо дані змінилися
        if (isPlayerChanged(player, updatedPlayer)) {
            playerRepo.updatePlayer(updatedPlayer)
        }

        return Result.Success(CalculatedAttributes(muscleAttributes = muscleMap))
    }

    private fun isPlayerChanged(old: Player, new: Player): Boolean {
        return old.chestAttr != new.chestAttr ||
               old.backAttr != new.backAttr ||
               old.shouldersAttr != new.shouldersAttr ||
               old.quadsAttr != new.quadsAttr ||
               old.legsAttr != new.legsAttr ||
               old.armsAttr != new.armsAttr
    }
}
