package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerProfileValidationPolicy
import com.ihor.thesystem.domain.repository.AvatarRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdatePlayerNameUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(player: Player, newName: String): Result<Unit, DomainError> {
        PlayerProfileValidationPolicy.validateName(newName)?.let { error ->
            return Result.Error(error)
        }
        return try {
            repo.updatePlayer(player.copy(name = newName))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}

class UpdatePlayerAvatarUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(player: Player, avatarUri: String?): Result<Unit, DomainError> {
        return try {
            repo.updatePlayer(player.copy(avatarUri = avatarUri))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}

class SaveAvatarUseCase @Inject constructor(
    private val avatarRepository: AvatarRepository
) {
    suspend operator fun invoke(sourceUri: String) = avatarRepository.saveAvatar(sourceUri)
}

class GetPlayerFlowUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    operator fun invoke(): Flow<Player?> = repo.getPlayer()
}

class LogWeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(weight: Float): Result<Unit, DomainError> {
        PlayerProfileValidationPolicy.validateWeight(weight)?.let { error ->
            return Result.Error(error)
        }
        return try {
            repo.logWeight(weight)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}

class UpdatePlayerAgeUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(age: Int): Result<Unit, DomainError> {
        PlayerProfileValidationPolicy.validateAge(age)?.let { error ->
            return Result.Error(error)
        }
        return try {
            repo.updateAge(age)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
