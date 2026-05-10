package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.SystemConfigRepository
import javax.inject.Inject

class SaveLastInitDateUseCase @Inject constructor(
    private val repository: SystemConfigRepository
) {
    suspend operator fun invoke(epochDay: Long) {
        repository.saveLastInitDate(epochDay)
    }
}
