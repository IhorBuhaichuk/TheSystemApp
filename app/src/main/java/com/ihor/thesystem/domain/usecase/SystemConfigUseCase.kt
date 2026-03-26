package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSystemConfigUseCase @Inject constructor(
    private val repo: SystemConfigRepository
) {
    operator fun invoke(): Flow<SystemConfig?> = repo.getConfig()
}

class UpdateSystemConfigUseCase @Inject constructor(
    private val repo: SystemConfigRepository
) {
    suspend operator fun invoke(config: SystemConfig) = repo.updateConfig(config)
}