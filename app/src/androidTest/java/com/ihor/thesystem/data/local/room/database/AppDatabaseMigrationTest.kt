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

    @Test
    fun migratesFrom48ToLatestPreservingExercisesAndAddingCoreMetadataDefaults() {
        val databaseName = "migration-48-to-$APP_DATABASE_VERSION-preserves-exercises"
        helper.createDatabase(databaseName, 48).apply {
            execSQL(
                """
                INSERT INTO `exercises` (
                    `id`, `externalId`, `name`, `nameUk`, `category`, `muscleGroups`,
                    `equipment`, `level`, `mechanic`, `force`, `instructions`, `gifUrl`, `trackingMode`
                ) VALUES (
                    7, 'legacy-pushup', 'Push-up', 'Віджимання', 'STRENGTH', '["CHEST"]',
                    'body only', 'beginner', 'compound', 'push', 'Keep core tight', NULL, 'REPS'
                )
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            APP_DATABASE_VERSION,
            true,
            *DatabaseMigrations.ALL_MIGRATIONS
        )

        try {
            migrated.query(
                """
                SELECT `name`, `nameUk`, `isCoreSystemExercise`, `movementPattern`,
                       `techniqueTips`, `commonMistakes`, `substitutionExternalIds`
                FROM `exercises`
                WHERE `externalId` = 'legacy-pushup'
                """.trimIndent()
            ).use { cursor ->
                assertTrue("Legacy exercise row must survive migration 48 -> latest", cursor.moveToFirst())
                assertEquals("Push-up", cursor.getString(cursor.getColumnIndexOrThrow("name")))
                assertEquals("Віджимання", cursor.getString(cursor.getColumnIndexOrThrow("nameUk")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("isCoreSystemExercise")))
                assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("movementPattern")))
                assertEquals("[]", cursor.getString(cursor.getColumnIndexOrThrow("techniqueTips")))
                assertEquals("[]", cursor.getString(cursor.getColumnIndexOrThrow("commonMistakes")))
                assertEquals("[]", cursor.getString(cursor.getColumnIndexOrThrow("substitutionExternalIds")))
            }
        } finally {
            migrated.close()
        }
    }

    @Test
    fun migratesFrom49To50CreatingUsableNutritionEntriesTable() {
        val databaseName = "migration-49-to-50-creates-nutrition"
        helper.createDatabase(databaseName, 49).close()

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            50,
            true,
            *DatabaseMigrations.ALL_MIGRATIONS
        )

        try {
            migrated.execSQL(
                """
                INSERT INTO `nutrition_entries` (
                    `dateEpochDay`, `proteinHit`, `waterHit`, `mealsQuality`, `bodyWeight`, `goalMode`, `note`
                ) VALUES (
                    20420, 1, 0, 'OK', NULL, 'MAINTAIN', 'created after migration'
                )
                """.trimIndent()
            )

            migrated.query("SELECT COUNT(*) FROM `nutrition_entries` WHERE `dateEpochDay` = 20420").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }
            migrated.query("PRAGMA index_list(`nutrition_entries`)").use { cursor ->
                var hasDateIndex = false
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    hasDateIndex = hasDateIndex ||
                        cursor.getString(nameIndex) == "index_nutrition_entries_dateEpochDay"
                }
                assertTrue("nutrition_entries must keep the date index after migration 49 -> 50", hasDateIndex)
            }
        } finally {
            migrated.close()
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
