package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DebuffConfig
import com.ihor.thesystem.domain.repository.DebuffRepository
import javax.inject.Inject

class UpdateDebuffUseCase @Inject constructor(
    private val repo: DebuffRepository
) {
    suspend operator fun invoke(debuff: DebuffConfig) = repo.updateDebuff(debuff)
}