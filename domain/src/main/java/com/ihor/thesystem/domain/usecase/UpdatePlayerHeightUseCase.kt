package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.PlayerProfileValidationPolicy
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.util.Result
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class UpdatePlayerHeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(height: Float): Result<Unit, DomainError> {
        PlayerProfileValidationPolicy.validateHeight(height)?.let { error ->
            return Result.Error(error)
        }
        return try {
            repo.updateHeight(height)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
