package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.PlayerRepository
import javax.inject.Inject

class UpdatePlayerHeightUseCase @Inject constructor(
    private val repo: PlayerRepository
) {
    suspend operator fun invoke(height: Float): Result<Unit> {
        if (height < 50f || height > 300f) {
            return Result.failure(IllegalArgumentException("Некоректне значення: допустимий зріст від 50 до 300 см"))
        }
        return try {
            repo.updateHeight(height)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
