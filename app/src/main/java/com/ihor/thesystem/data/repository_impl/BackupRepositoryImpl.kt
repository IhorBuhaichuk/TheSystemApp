package com.ihor.thesystem.data.repository_impl

import android.content.Context
import android.database.Cursor
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteStatement
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.data.local.room.database.APP_DATABASE_VERSION
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.domain.model.BackupImportSummary
import com.ihor.thesystem.domain.model.BackupPayload
import com.ihor.thesystem.domain.model.BackupRow
import com.ihor.thesystem.domain.model.BackupStatus
import com.ihor.thesystem.domain.model.BackupTable
import com.ihor.thesystem.domain.model.BackupValidationException
import com.ihor.thesystem.domain.model.BackupValue
import com.ihor.thesystem.domain.model.BackupValueType
import com.ihor.thesystem.domain.repository.BackupRepository
import com.ihor.thesystem.domain.util.AppClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext context: Context,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider
) : BackupRepository {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun exportBackup(): BackupPayload =
        withContext(dispatchers.io) {
            val sqlite = database.openHelper.readableDatabase
            val tables = BACKUP_TABLES.map { tableName ->
                BackupTable(
                    name = tableName,
                    rows = sqlite.readTableRows(tableName)
                )
            }
            val exportedAtMillis = clock.now()
            preferences.edit()
                .putLong(KEY_LAST_EXPORTED_AT, exportedAtMillis)
                .apply()

            BackupPayload(
                version = BackupPayload.SUPPORTED_VERSION,
                exportedAtMillis = exportedAtMillis,
                appDatabaseVersion = APP_DATABASE_VERSION,
                tables = tables
            )
        }

    override suspend fun importBackup(payload: BackupPayload): BackupImportSummary =
        withContext(dispatchers.io) {
            validateTables(payload)
            var importedRows = 0

            database.withTransaction {
                val sqlite = database.openHelper.writableDatabase
                payload.tables.forEach { table ->
                    val allowedColumns = sqlite.columnNamesFor(table.name)
                    table.rows.forEach { row ->
                        validateRow(table.name, row, allowedColumns)
                        sqlite.insertOrReplace(table.name, row)
                        importedRows += 1
                    }
                }
            }

            val importedAtMillis = clock.now()
            preferences.edit()
                .putLong(KEY_LAST_IMPORTED_AT, importedAtMillis)
                .apply()

            BackupImportSummary(
                tableCount = payload.tables.size,
                rowCount = importedRows
            )
        }

    override suspend fun getBackupStatus(): BackupStatus =
        withContext(dispatchers.io) {
            BackupStatus(
                lastExportedAtMillis = preferences.getLongOrNull(KEY_LAST_EXPORTED_AT),
                lastImportedAtMillis = preferences.getLongOrNull(KEY_LAST_IMPORTED_AT)
            )
        }

    private fun SupportSQLiteDatabase.readTableRows(tableName: String): List<BackupRow> {
        val query = query("SELECT * FROM `${tableName}`")
        return query.useCursor { cursor ->
            val columns = cursor.columnNames.toList()
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        BackupRow(
                            values = columns.associateWith { column ->
                                cursor.backupValue(cursor.getColumnIndexOrThrow(column))
                            }
                        )
                    )
                }
            }
        }
    }

    private fun Cursor.backupValue(index: Int): BackupValue =
        when (getType(index)) {
            Cursor.FIELD_TYPE_NULL -> BackupValue(BackupValueType.NULL)
            Cursor.FIELD_TYPE_INTEGER -> BackupValue(BackupValueType.LONG, getLong(index).toString())
            Cursor.FIELD_TYPE_FLOAT -> BackupValue(BackupValueType.DOUBLE, getDouble(index).toString())
            Cursor.FIELD_TYPE_BLOB -> BackupValue(
                type = BackupValueType.BLOB,
                value = Base64.getEncoder().encodeToString(getBlob(index))
            )
            else -> BackupValue(BackupValueType.STRING, getString(index))
        }

    private fun validateTables(payload: BackupPayload) {
        val supportedTables = BACKUP_TABLES.toSet()
        payload.tables.forEach { table ->
            if (table.name !in supportedTables) {
                throw BackupValidationException("Unsupported backup table: ${table.name}")
            }
        }
    }

    private fun validateRow(
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

    private fun SupportSQLiteDatabase.insertOrReplace(tableName: String, row: BackupRow) {
        val columns = row.values.keys.toList()
        val sql = buildString {
            append("INSERT OR REPLACE INTO `")
            append(tableName)
            append("` (")
            append(columns.joinToString(", ") { "`$it`" })
            append(") VALUES (")
            append(columns.joinToString(", ") { "?" })
            append(")")
        }
        val statement = compileStatement(sql)
        try {
            columns.forEachIndexed { index, column ->
                statement.bindBackupValue(index + 1, tableName, column, row.values.getValue(column))
            }
            statement.executeInsert()
        } finally {
            statement.close()
        }
    }

    private fun SupportSQLiteStatement.bindBackupValue(
        index: Int,
        tableName: String,
        columnName: String,
        value: BackupValue
    ) {
        when (value.type) {
            BackupValueType.NULL -> bindNull(index)
            BackupValueType.LONG -> bindLong(
                index,
                value.value?.toLongOrNull()
                    ?: throw malformedValue(tableName, columnName, value.type)
            )
            BackupValueType.DOUBLE -> bindDouble(
                index,
                value.value?.toDoubleOrNull()
                    ?: throw malformedValue(tableName, columnName, value.type)
            )
            BackupValueType.STRING -> bindString(
                index,
                value.value ?: throw malformedValue(tableName, columnName, value.type)
            )
            BackupValueType.BLOB -> bindBlob(
                index,
                runCatching {
                    Base64.getDecoder().decode(
                        value.value ?: throw malformedValue(tableName, columnName, value.type)
                    )
                }.getOrElse {
                    throw malformedValue(tableName, columnName, value.type)
                }
            )
        }
    }

    private fun malformedValue(
        tableName: String,
        columnName: String,
        type: BackupValueType
    ): BackupValidationException =
        BackupValidationException("Malformed $type value for $tableName.$columnName")

    private fun SupportSQLiteDatabase.columnNamesFor(tableName: String): Set<String> {
        val query = query("PRAGMA table_info(`${tableName}`)")
        return query.useCursor { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            buildSet {
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
    }

    private inline fun <T> Cursor.useCursor(block: (Cursor) -> T): T =
        use { block(it) }

    private fun android.content.SharedPreferences.getLongOrNull(key: String): Long? =
        if (contains(key)) getLong(key, 0L) else null

    private companion object {
        const val PREFERENCES_NAME = "backup_status"
        const val KEY_LAST_EXPORTED_AT = "last_exported_at"
        const val KEY_LAST_IMPORTED_AT = "last_imported_at"

        val BACKUP_TABLES = listOf(
            "exercises",
            "daily_task_template",
            "workout_templates",
            "workout_exercise_cross_ref",
            "schedule",
            "schedule_task_cross_ref",
            "player",
            "weight_log",
            "system_config",
            "progression_matrix",
            "quest",
            "quest_task",
            "quest_log",
            "workout_sessions",
            "exercise_sets",
            "workout_session_logs",
            "exercise_set_logs",
            "readiness_entries",
            "nutrition_entries",
            "equipment_profile",
            "calendar_cycle_config",
            "calendar_cycle_day",
            "todo"
        )
    }
}
