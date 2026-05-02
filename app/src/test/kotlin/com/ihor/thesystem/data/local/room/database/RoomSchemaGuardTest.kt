package com.ihor.thesystem.data.local.room.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RoomSchemaGuardTest {

    @Test
    fun `database annotation uses the shared database version constant`() {
        val appDatabaseSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/database/AppDatabase.kt")
            .readText()

        assertTrue(
            "AppDatabase @Database version must use APP_DATABASE_VERSION",
            "version = APP_DATABASE_VERSION" in appDatabaseSource
        )
    }

    @Test
    fun `manual migrations cover every database version up to current`() {
        val actual = DatabaseMigrations.ALL_MIGRATIONS
            .map { it.startVersion to it.endVersion }
        val firstMigratedVersion = requireNotNull(actual.minOfOrNull { it.first }) {
            "DatabaseMigrations.ALL_MIGRATIONS must not be empty"
        }

        val expected = (firstMigratedVersion until APP_DATABASE_VERSION)
            .map { it to it + 1 }

        assertEquals("DatabaseMigrations.ALL_MIGRATIONS must be contiguous and ordered", expected, actual)
        assertEquals(
            "DatabaseMigrations.ALL_MIGRATIONS must not contain duplicate migration paths",
            actual.size,
            actual.toSet().size
        )
    }

    @Test
    fun `exported room schemas are contiguous through the current database version`() {
        val schemaVersions = schemaVersions()

        assertTrue("Room schema files must be exported", schemaVersions.isNotEmpty())
        assertEquals(
            "The latest exported Room schema must match APP_DATABASE_VERSION",
            APP_DATABASE_VERSION,
            schemaVersions.last()
        )

        val expected = (schemaVersions.first()..APP_DATABASE_VERSION).toList()
        assertEquals("Room schema files must be contiguous once export starts", expected, schemaVersions)
    }

    private fun schemaVersions(): List<Int> {
        val schemaDir = projectRoot()
            .resolve("schemas")
            .resolve(AppDatabase::class.java.name)

        return schemaDir
            .listFiles { file -> file.isFile && file.extension == "json" }
            .orEmpty()
            .mapNotNull { it.nameWithoutExtension.toIntOrNull() }
            .sorted()
    }

    private fun projectRoot(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
}
