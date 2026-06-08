package com.ihor.thesystem.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val version: Int = SUPPORTED_VERSION,
    val exportedAtMillis: Long,
    val appDatabaseVersion: Int,
    val tables: List<BackupTable>
) {
    companion object {
        const val SUPPORTED_VERSION = 1
    }
}

@Serializable
data class BackupTable(
    val name: String,
    val rows: List<BackupRow>
)

@Serializable
data class BackupRow(
    val values: Map<String, BackupValue>
)

@Serializable
data class BackupValue(
    val type: BackupValueType,
    val value: String? = null
)

@Serializable
enum class BackupValueType {
    NULL,
    LONG,
    DOUBLE,
    STRING,
    BLOB
}

data class BackupStatus(
    val lastExportedAtMillis: Long? = null,
    val lastImportedAtMillis: Long? = null
)

data class BackupImportSummary(
    val tableCount: Int,
    val rowCount: Int
)

class BackupValidationException(message: String) : IllegalArgumentException(message)
