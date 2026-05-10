package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class CalculatedAttributes(
    val muscleAttributes: Map<MuscleGroup, Float>
)

class CalculateAttributesUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val playerRepo: PlayerRepository,
    private val workoutRepo: WorkoutRepository
) {
    /**
     * Оновлює RPG-атрибути гравця.
     */
    suspend operator fun invoke(exerciseId: Int? = null): Result<CalculatedAttributes, DomainError> {
        val player = playerRepo.getPlayer().firstOrNull() 
            ?: return Result.Error(DataError.Local.NOT_FOUND)

        val matrixEntries = matrixRepo.getAllEntries().first()
        val allExercises = workoutRepo.getAllExercisesSync()
        val exerciseMap = allExercises.associateBy { it.id }
        
        // Визначаємо групи м'язів для оновлення
        val groupsToUpdate = if (exerciseId != null) {
            exerciseMap[exerciseId]?.muscleGroups ?: MuscleGroup.entries
        } else {
            MuscleGroup.entries
        }

        val muscleMap = mutableMapOf<MuscleGroup, Float>()
        
        // Поточні значення атрибутів
        MuscleGroup.entries.forEach { group ->
            muscleMap[group] = when(group) {
                MuscleGroup.CHEST -> player.chestAttr.toFloat()
                MuscleGroup.BACK -> player.backAttr.toFloat()
                MuscleGroup.SHOULDERS -> player.shouldersAttr.toFloat()
                MuscleGroup.QUADS -> player.quadsAttr.toFloat()
                MuscleGroup.HAMSTRINGS_GLUTES -> player.legsAttr.toFloat()
                MuscleGroup.ARMS -> player.armsAttr.toFloat()
                MuscleGroup.ABS -> player.absAttr.toFloat()
                MuscleGroup.LEGS -> player.legsGroupAttr.toFloat()
                MuscleGroup.CORE -> player.coreAttr.toFloat()
            }
        }

        // Перераховуємо лише необхідні групи
        groupsToUpdate.forEach { group ->
            val groupExercises = matrixEntries.filter { entry ->
                val entity = exerciseMap[entry.exerciseId]
                entity?.muscleGroups?.contains(group) == true
            }
            
            if (groupExercises.isEmpty()) {
                muscleMap[group] = 0f
            } else {
                val totalRankWeight = groupExercises.sumOf { it.currentRank.weight }
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
            armsAttr = muscleMap[MuscleGroup.ARMS]?.toInt() ?: 0,
            absAttr = muscleMap[MuscleGroup.ABS]?.toInt() ?: 0,
            legsGroupAttr = muscleMap[MuscleGroup.LEGS]?.toInt() ?: 0,
            coreAttr = muscleMap[MuscleGroup.CORE]?.toInt() ?: 0
        )

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
               old.armsAttr != new.armsAttr ||
               old.absAttr != new.absAttr ||
               old.legsGroupAttr != new.legsGroupAttr ||
               old.coreAttr != new.coreAttr
    }
}
