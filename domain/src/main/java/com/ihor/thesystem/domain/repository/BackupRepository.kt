package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.BackupImportSummary
import com.ihor.thesystem.domain.model.BackupPayload
import com.ihor.thesystem.domain.model.BackupStatus

interface BackupRepository {
    suspend fun exportBackup(): BackupPayload
    suspend fun importBackup(payload: BackupPayload): BackupImportSummary
    suspend fun getBackupStatus(): BackupStatus
}
