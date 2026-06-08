package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BackupImportSummary
import com.ihor.thesystem.domain.model.BackupPayload
import com.ihor.thesystem.domain.model.BackupRow
import com.ihor.thesystem.domain.model.BackupStatus
import com.ihor.thesystem.domain.model.BackupTable
import com.ihor.thesystem.domain.model.BackupValidationException
import com.ihor.thesystem.domain.model.BackupValue
import com.ihor.thesystem.domain.model.BackupValueType
import com.ihor.thesystem.domain.repository.BackupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupUseCasesTest {

    @Test
    fun `export payload contains workout logs player and progression tables`() = runTest {
        val useCase = ExportBackupUseCase(FakeBackupRepository(exportPayload()))

        val payload = useCase()
        val tableNames = payload.tables.map { it.name }.toSet()

        assertTrue("workout session logs must be exported", "workout_session_logs" in tableNames)
        assertTrue("exercise set logs must be exported", "exercise_set_logs" in tableNames)
        assertTrue("player profile must be exported", "player" in tableNames)
        assertTrue("progression matrix must be exported", "progression_matrix" in tableNames)
    }

    @Test(expected = BackupValidationException::class)
    fun `import validates backup version`() = runTest {
        val useCase = ImportBackupUseCase(FakeBackupRepository(exportPayload()))

        useCase(exportPayload().copy(version = BackupPayload.SUPPORTED_VERSION + 1))
    }

    @Test(expected = BackupValidationException::class)
    fun `import rejects malformed payload`() = runTest {
        val useCase = ImportBackupUseCase(FakeBackupRepository(exportPayload()))

        useCase(
            BackupPayload(
                version = BackupPayload.SUPPORTED_VERSION,
                exportedAtMillis = 1L,
                appDatabaseVersion = 49,
                tables = listOf(BackupTable(name = "", rows = listOf(row("id", "1"))))
            )
        )
    }

    private fun exportPayload(): BackupPayload =
        BackupPayload(
            version = BackupPayload.SUPPORTED_VERSION,
            exportedAtMillis = 1L,
            appDatabaseVersion = 49,
            tables = listOf(
                BackupTable("workout_session_logs", listOf(row("sessionId", "1"))),
                BackupTable("exercise_set_logs", listOf(row("setId", "1"))),
                BackupTable("player", listOf(row("id", "1"))),
                BackupTable("progression_matrix", listOf(row("exerciseId", "10")))
            )
        )

    private fun row(column: String, value: String): BackupRow =
        BackupRow(
            values = mapOf(
                column to BackupValue(
                    type = BackupValueType.LONG,
                    value = value
                )
            )
        )

    private class FakeBackupRepository(
        private val payload: BackupPayload
    ) : BackupRepository {
        override suspend fun exportBackup(): BackupPayload = payload

        override suspend fun importBackup(payload: BackupPayload): BackupImportSummary =
            BackupImportSummary(
                tableCount = payload.tables.size,
                rowCount = payload.tables.sumOf { it.rows.size }
            )

        override suspend fun getBackupStatus(): BackupStatus = BackupStatus()
    }
}
