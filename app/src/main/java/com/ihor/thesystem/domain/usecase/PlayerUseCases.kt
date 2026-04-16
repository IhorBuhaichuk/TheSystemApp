package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.repository.PlayerRepository
import javax.inject.Inject

class UpdatePlayerNameUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(currentPlayer: Player, newName: String): Result<Unit, DataError.Local> {
        if (newName.isBlank()) return Result.Success(Unit)
        return repo.updatePlayer(currentPlayer.copy(name = newName.trim().uppercase()))
    }
}

class LogWeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(weight: Float): Result<Unit, DataError.Local> {
        if (weight <= 0f) return Result.Success(Unit)
        return repo.logWeight(weight)
    }
}
