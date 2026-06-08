package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BackupImportSummary
import com.ihor.thesystem.domain.model.BackupPayload
import com.ihor.thesystem.domain.model.BackupStatus
import com.ihor.thesystem.domain.model.BackupValidationException
import com.ihor.thesystem.domain.repository.BackupRepository
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(): BackupPayload =
        repository.exportBackup()
}

class ImportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(payload: BackupPayload): BackupImportSummary {
        payload.validateForImport()
        return repository.importBackup(payload)
    }
}

class GetBackupStatusUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(): BackupStatus =
        repository.getBackupStatus()
}

fun BackupPayload.validateForImport() {
    if (version != BackupPayload.SUPPORTED_VERSION) {
        throw BackupValidationException("Unsupported backup version: $version")
    }
    if (tables.isEmpty()) {
        throw BackupValidationException("Backup payload does not contain tables.")
    }
    tables.forEach { table ->
        if (table.name.isBlank()) {
            throw BackupValidationException("Backup table name must not be blank.")
        }
        table.rows.forEach { row ->
            if (row.values.isEmpty()) {
                throw BackupValidationException("Backup row must contain values.")
            }
            if (row.values.keys.any { it.isBlank() }) {
                throw BackupValidationException("Backup column name must not be blank.")
            }
        }
    }
}
