package com.ihor.thesystem.data.local.room.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migratesEveryExportedSchemaToLatestVersion() {
        val schemaVersions = exportedSchemaVersions()

        assertTrue("Room schema assets must be packaged for migration tests", schemaVersions.isNotEmpty())
        assertEquals(APP_DATABASE_VERSION, schemaVersions.last())

        for (startVersion in schemaVersions.dropLast(1)) {
            val databaseName = "migration-${startVersion}-to-$APP_DATABASE_VERSION"

            helper.createDatabase(databaseName, startVersion).close()

            helper.runMigrationsAndValidate(
                databaseName,
                APP_DATABASE_VERSION,
                true,
                *DatabaseMigrations.ALL_MIGRATIONS
            )
        }
    }

    private fun exportedSchemaVersions(): List<Int> {
        val schemaPath = AppDatabase::class.java.name

        return InstrumentationRegistry.getInstrumentation()
            .context
            .assets
            .list(schemaPath)
            .orEmpty()
            .mapNotNull { fileName -> fileName.removeSuffix(".json").toIntOrNull() }
            .sorted()
    }
}
