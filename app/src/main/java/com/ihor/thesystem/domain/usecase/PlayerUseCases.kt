package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.StringResourceException
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdatePlayerNameUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(player: Player, newName: String): Result<Unit> {
        if (newName.isBlank() || newName.length > 50) {
            return Result.failure(StringResourceException(UiText.StringResource(R.string.error_invalid_name)))
        }
        return try {
            repo.updatePlayer(player.copy(name = newName))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UpdatePlayerAvatarUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(player: Player, avatarUri: String?): Result<Unit> {
        return try {
            repo.updatePlayer(player.copy(avatarUri = avatarUri))
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
            return Result.failure(StringResourceException(UiText.StringResource(R.string.error_invalid_weight)))
        }
        return try {
            repo.logWeight(weight)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
