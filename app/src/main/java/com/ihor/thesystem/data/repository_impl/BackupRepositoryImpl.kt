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
            val tables = BackupImportPolicy.includedTables.map { tableName ->
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

    override suspend fun previewImport(payload: BackupPayload): BackupImportSummary =
        withContext(dispatchers.io) {
            validateTables(payload)
            val sqlite = database.openHelper.readableDatabase
            var validatedRows = 0

            payload.tables.forEach { table ->
                val allowedColumns = sqlite.columnNamesFor(table.name)
                table.rows.forEach { row ->
                    validateRow(table.name, row, allowedColumns)
                    row.values.forEach { (columnName, value) ->
                        BackupImportPolicy.bindableValue(table.name, columnName, value)
                    }
                    validatedRows += 1
                }
            }

            BackupImportSummary(
                tableCount = payload.tables.size,
                rowCount = validatedRows
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
        BackupImportPolicy.validateTables(payload)
    }

    private fun validateRow(
        tableName: String,
        row: BackupRow,
        allowedColumns: Set<String>
    ) {
        BackupImportPolicy.validateRow(tableName, row, allowedColumns)
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
        when (val bindable = BackupImportPolicy.bindableValue(tableName, columnName, value)) {
            BoundBackupValue.Null -> bindNull(index)
            is BoundBackupValue.LongValue -> bindLong(index, bindable.value)
            is BoundBackupValue.DoubleValue -> bindDouble(index, bindable.value)
            is BoundBackupValue.StringValue -> bindString(index, bindable.value)
            is BoundBackupValue.BlobValue -> bindBlob(index, bindable.value)
        }
    }

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
    }
}
