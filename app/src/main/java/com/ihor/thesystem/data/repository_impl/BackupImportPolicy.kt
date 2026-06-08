package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.model.BackupPayload
import com.ihor.thesystem.domain.model.BackupRow
import com.ihor.thesystem.domain.model.BackupValidationException
import com.ihor.thesystem.domain.model.BackupValue
import com.ihor.thesystem.domain.model.BackupValueType
import java.util.Base64

internal object BackupImportPolicy {
    val includedTables = listOf(
        "player",
        "weight_log",
        "system_config",
        "exercises",
        "daily_task_template",
        "workout_templates",
        "workout_exercise_cross_ref",
        "schedule",
        "schedule_task_cross_ref",
        "quest",
        "quest_task",
        "progression_matrix",
        "quest_log",
        "workout_sessions",
        "exercise_sets",
        "workout_directives",
        "exercise_milestones",
        "workout_session_logs",
        "exercise_set_logs",
        "reference_matrix",
        "protocol_template",
        "chat_message_table",
        "calendar_cycle_config",
        "calendar_cycle_day",
        "todo",
        "readiness_entries",
        "equipment_profile",
        "nutrition_entries"
    )

    fun validateTables(payload: BackupPayload) {
        val supportedTables = includedTables.toSet()
        payload.tables.forEach { table ->
            if (table.name !in supportedTables) {
                throw BackupValidationException("Unsupported backup table: ${table.name}")
            }
        }
    }

    fun validateRow(
        tableName: String,
        row: BackupRow,
        allowedColumns: Set<String>
    ) {
        if (row.values.isEmpty()) {
            throw BackupValidationException("Backup row for $tableName must contain values.")
        }
        val unknownColumns = row.values.keys - allowedColumns
        if (unknownColumns.isNotEmpty()) {
            throw BackupValidationException(
                "Backup table $tableName contains unknown columns: ${unknownColumns.joinToString()}"
            )
        }
    }

    fun bindableValue(
        tableName: String,
        columnName: String,
        value: BackupValue
    ): BoundBackupValue =
        when (value.type) {
            BackupValueType.NULL -> BoundBackupValue.Null
            BackupValueType.LONG -> BoundBackupValue.LongValue(
                value.value?.toLongOrNull()
                    ?: throw malformedValue(tableName, columnName, value.type)
            )
            BackupValueType.DOUBLE -> BoundBackupValue.DoubleValue(
                value.value?.toDoubleOrNull()
                    ?: throw malformedValue(tableName, columnName, value.type)
            )
            BackupValueType.STRING -> BoundBackupValue.StringValue(
                value.value ?: throw malformedValue(tableName, columnName, value.type)
            )
            BackupValueType.BLOB -> BoundBackupValue.BlobValue(
                runCatching {
                    Base64.getDecoder().decode(
                        value.value ?: throw malformedValue(tableName, columnName, value.type)
                    )
                }.getOrElse {
                    throw malformedValue(tableName, columnName, value.type)
                }
            )
        }

    private fun malformedValue(
        tableName: String,
        columnName: String,
        type: BackupValueType
    ): BackupValidationException =
        BackupValidationException("Malformed $type value for $tableName.$columnName")
}

internal sealed class BoundBackupValue {
    data object Null : BoundBackupValue()
    data class LongValue(val value: Long) : BoundBackupValue()
    data class DoubleValue(val value: Double) : BoundBackupValue()
    data class StringValue(val value: String) : BoundBackupValue()
    data class BlobValue(val value: ByteArray) : BoundBackupValue()
}
