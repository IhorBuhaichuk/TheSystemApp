package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdatePlayerNameUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(player: Player, newName: String) {
        repo.updatePlayer(player.copy(name = newName))
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
    suspend operator fun invoke(weight: Float) = repo.logWeight(weight)
}
