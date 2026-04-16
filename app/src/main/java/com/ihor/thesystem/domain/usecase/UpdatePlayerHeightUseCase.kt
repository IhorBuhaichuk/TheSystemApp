package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.StringResourceException
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.repository.PlayerRepository
import javax.inject.Inject

class UpdatePlayerHeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(height: Float): Result<Unit> {
        if (height < 50f || height > 300f) {
            return Result.failure(StringResourceException(UiText.StringResource(R.string.error_invalid_height)))
        }
        return try {
            repo.updateHeight(height)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
