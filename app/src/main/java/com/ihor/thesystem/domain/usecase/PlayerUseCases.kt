package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.repository.PlayerRepository
import javax.inject.Inject

class UpdatePlayerNameUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(currentPlayer: Player, newName: String) {
        if (newName.isBlank()) return
        repo.updatePlayer(currentPlayer.copy(name = newName.trim().uppercase()))
    }
}

class LogWeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(weight: Float) {
        if (weight > 0f) repo.logWeight(weight)
    }
}