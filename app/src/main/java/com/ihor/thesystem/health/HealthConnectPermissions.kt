package com.ihor.thesystem.health

import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import com.ihor.thesystem.domain.model.HealthPermissionRequest
import com.ihor.thesystem.domain.model.HealthSignalPermission

object HealthConnectPermissions {
    fun requestContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    fun permissionsFor(request: HealthPermissionRequest): Set<String> =
        permissionsFor(request.permissions)

    fun permissionsFor(required: Set<HealthSignalPermission>): Set<String> =
        required.map { it.toHealthConnectReadPermission() }.toSet()

    private fun HealthSignalPermission.toHealthConnectReadPermission(): String =
        when (this) {
            HealthSignalPermission.SLEEP ->
                HealthPermission.getReadPermission(SleepSessionRecord::class)
        }
}
