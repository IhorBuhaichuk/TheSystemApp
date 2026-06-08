package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.HealthPermissionRequest
import com.ihor.thesystem.domain.model.HealthSignalPermission
import com.ihor.thesystem.domain.model.HealthSignals

interface HealthSignalsRepository {
    suspend fun isAvailable(): Boolean

    suspend fun hasPermissions(
        required: Set<HealthSignalPermission> = HealthSignalPermission.ReadinessDefaults
    ): Boolean

    fun requestPermissions(
        required: Set<HealthSignalPermission> = HealthSignalPermission.ReadinessDefaults
    ): HealthPermissionRequest

    suspend fun getTodaySignals(): HealthSignals

    suspend fun getRecentSignals(days: Int): List<HealthSignals>
}

object NoHealthSignalsRepository : HealthSignalsRepository {
    override suspend fun isAvailable(): Boolean = false

    override suspend fun hasPermissions(required: Set<HealthSignalPermission>): Boolean = false

    override fun requestPermissions(required: Set<HealthSignalPermission>): HealthPermissionRequest =
        HealthPermissionRequest(required)

    override suspend fun getTodaySignals(): HealthSignals = HealthSignals.Unavailable

    override suspend fun getRecentSignals(days: Int): List<HealthSignals> = emptyList()
}
