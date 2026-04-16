package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.DatabaseStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseReadinessRepositoryImpl @Inject constructor() : DatabaseReadinessRepository {
    private val _isDbReady = MutableStateFlow(false)
    override val isDbReady: StateFlow<Boolean> = _isDbReady.asStateFlow()

    private val _status = MutableStateFlow<DatabaseStatus>(DatabaseStatus.Idle)
    override val status: StateFlow<DatabaseStatus> = _status.asStateFlow()

    override fun markAsReady() {
        _isDbReady.value = true
        _status.value = DatabaseStatus.Ready
    }

    override fun markAsFailed(reason: String) {
        _status.value = DatabaseStatus.Failed(reason)
    }
}
