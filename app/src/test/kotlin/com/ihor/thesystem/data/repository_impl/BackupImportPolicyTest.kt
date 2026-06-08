package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.model.BackupPayload
import com.ihor.thesystem.domain.model.BackupRow
import com.ihor.thesystem.domain.model.BackupTable
import com.ihor.thesystem.domain.model.BackupValidationException
import com.ihor.thesystem.domain.model.BackupValue
import com.ihor.thesystem.domain.model.BackupValueType
import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupImportPolicyTest {

    @Test
    fun `explicit backup includes user critical workout plan and achievement tables`() {
        val tables = BackupImportPolicy.includedTables.toSet()

        assertTrue("workout directives must survive explicit JSON backup", "workout_directives" in tables)
        assertTrue("exercise milestones must survive explicit JSON backup", "exercise_milestones" in tables)
        assertTrue("readiness history must survive explicit JSON backup", "readiness_entries" in tables)
        assertTrue("nutrition entries must survive explicit JSON backup", "nutrition_entries" in tables)
        assertTrue("equipment profile must survive explicit JSON backup", "equipment_profile" in tables)
    }

    @Test
    fun `import rejects unknown table`() {
        assertThrows(BackupValidationException::class.java) {
            BackupImportPolicy.validateTables(
                payload(
                    BackupTable(
                        name = "unknown_table",
                        rows = listOf(row("id", BackupValue(BackupValueType.LONG, "1")))
                    )
                )
            )
        }
    }

    @Test
    fun `import rejects unknown column`() {
        assertThrows(BackupValidationException::class.java) {
            BackupImportPolicy.validateRow(
                tableName = "player",
                row = row("unexpectedColumn", BackupValue(BackupValueType.STRING, "value")),
                allowedColumns = setOf("id", "name")
            )
        }
    }

    @Test
    fun `import rejects empty row`() {
        assertThrows(BackupValidationException::class.java) {
            BackupImportPolicy.validateRow(
                tableName = "player",
                row = BackupRow(values = emptyMap()),
                allowedColumns = setOf("id")
            )
        }
    }

    @Test
    fun `import rejects malformed long and double values before binding`() {
        assertThrows(BackupValidationException::class.java) {
            BackupImportPolicy.bindableValue(
                tableName = "player",
                columnName = "level",
                value = BackupValue(BackupValueType.LONG, "not-a-number")
            )
        }
        assertThrows(BackupValidationException::class.java) {
            BackupImportPolicy.bindableValue(
                tableName = "nutrition_entries",
                columnName = "bodyWeight",
                value = BackupValue(BackupValueType.DOUBLE, "12,5")
            )
        }
    }

    @Test
    fun `import rejects malformed blob values before binding`() {
        assertThrows(BackupValidationException::class.java) {
            BackupImportPolicy.bindableValue(
                tableName = "player",
                columnName = "avatar",
                value = BackupValue(BackupValueType.BLOB, "not base64")
            )
        }
    }

    @Test
    fun `import converts valid typed values for sqlite binding`() {
        assertEquals(
            BoundBackupValue.LongValue(42L),
            BackupImportPolicy.bindableValue("player", "level", BackupValue(BackupValueType.LONG, "42"))
        )
        assertEquals(
            BoundBackupValue.DoubleValue(82.5),
            BackupImportPolicy.bindableValue("nutrition_entries", "bodyWeight", BackupValue(BackupValueType.DOUBLE, "82.5"))
        )
        assertEquals(
            BoundBackupValue.StringValue("ok"),
            BackupImportPolicy.bindableValue("player", "name", BackupValue(BackupValueType.STRING, "ok"))
        )
        val blob = BackupImportPolicy.bindableValue(
            tableName = "player",
            columnName = "avatar",
            value = BackupValue(BackupValueType.BLOB, Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3)))
        ) as BoundBackupValue.BlobValue

        assertArrayEquals(byteArrayOf(1, 2, 3), blob.value)
    }

    private fun payload(vararg tables: BackupTable): BackupPayload =
        BackupPayload(
            version = BackupPayload.SUPPORTED_VERSION,
            exportedAtMillis = 1L,
            appDatabaseVersion = 50,
            tables = tables.toList()
        )

    private fun row(column: String, value: BackupValue): BackupRow =
        BackupRow(values = mapOf(column to value))
}
