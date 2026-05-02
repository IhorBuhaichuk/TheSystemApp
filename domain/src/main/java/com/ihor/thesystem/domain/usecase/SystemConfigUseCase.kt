package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSystemConfigUseCase @Inject constructor(
    private val repository: SystemConfigRepository
) {
    operator fun invoke(): Flow<SystemConfig?> = repository.getConfigFlow()
}

class UpdateSystemConfigUseCase @Inject constructor(
    private val repository: SystemConfigRepository
) {
    suspend operator fun invoke(config: SystemConfig) = repository.updateConfig(config)
}

class SetNeedsDailyInitUseCase @Inject constructor(
    private val repository: SystemConfigRepository
) {
    suspend operator fun invoke(needed: Boolean) = repository.setNeedsDailyInit(needed)
}
