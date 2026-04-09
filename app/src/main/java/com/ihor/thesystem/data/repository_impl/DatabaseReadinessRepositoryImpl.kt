package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseReadinessRepositoryImpl @Inject constructor() : DatabaseReadinessRepository {
    private val _isDbReady = MutableStateFlow(false)
    override val isDbReady: StateFlow<Boolean> = _isDbReady.asStateFlow()

    override fun markAsReady() {
        _isDbReady.value = true
    }
}
