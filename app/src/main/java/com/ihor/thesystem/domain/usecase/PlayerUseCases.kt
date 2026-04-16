package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdatePlayerNameUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(player: Player, newName: String): Result<Unit> {
        if (newName.isBlank() || newName.length > 50) {
            return Result.failure(IllegalArgumentException("Некоректне значення: ім'я має бути від 1 до 50 символів"))
        }
        return try {
            repo.updatePlayer(player.copy(name = newName))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetPlayerFlowUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    operator fun invoke(): Flow<Player?> = repo.getPlayer()
}

class LogWeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(weight: Float): Result<Unit> {
        if (weight < 20f || weight > 500f) {
            return Result.failure(IllegalArgumentException("Некоректне значення: допустима вага від 20 до 500 кг"))
        }
        return try {
            repo.logWeight(weight)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
