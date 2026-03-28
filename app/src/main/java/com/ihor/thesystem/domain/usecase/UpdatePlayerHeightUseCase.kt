package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.PlayerRepository
import javax.inject.Inject

class UpdatePlayerHeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(height: Float) {
        if (height > 0f) repo.updateHeight(height)
    }
}
