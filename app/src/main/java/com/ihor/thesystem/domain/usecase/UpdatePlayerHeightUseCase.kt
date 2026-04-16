package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.repository.PlayerRepository
import javax.inject.Inject

class UpdatePlayerHeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(height: Float): Result<Unit, DataError.Local> {
        if (height <= 0f) return Result.Success(Unit)
        return repo.updateHeight(height)
    }
}
