package com.ihor.thesystem.data.local.room.database

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DatabasePopulatorGuardTest {

    @Test
    fun `required exercise seed failures are not treated as successful population`() {
        val source = sourceFile("DatabasePopulator.kt").readText()

        assertTrue(
            "Required exercise seed failures must be promoted to DatabaseModule readiness failure.",
            "throw DatabasePopulationException" in source &&
                "required seed asset exercises_ua.json" in source
        )
        assertFalse(
            "DatabasePopulator must not return successfully from the required exercise seed failure path.",
            "return@withContext" in source
        )
    }

    @Test
    fun `base singleton rows are ensured before existing exercise seed is skipped`() {
        val source = sourceFile("DatabasePopulator.kt").readText()

        val baseRowsIndex = source.indexOf("ensureRequiredSingletonRows(db)")
        val exerciseSkipIndex = source.indexOf("existingExerciseCount > 0")

        assertTrue("DatabasePopulator must ensure player and system_config rows.", baseRowsIndex >= 0)
        assertTrue(
            "DatabasePopulator must not skip player/system_config initialization when exercises already exist.",
            exerciseSkipIndex > baseRowsIndex
        )
    }

    @Test
    fun `exercise population gate uses a count query instead of loading all exercises`() {
        val populatorSource = sourceFile("DatabasePopulator.kt").readText()
        val daoSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutDao.kt")
            .readText()

        assertFalse(
            "Population gate must not load the full exercises table just to check existence.",
            "getAllExercisesSync().isNotEmpty()" in populatorSource
        )
        assertTrue(
            "WorkoutDao must expose a cheap exercise count query for population gating.",
            "SELECT COUNT(*) FROM exercises" in daoSource
        )
    }

    @Test
    fun `database population uses injected dispatcher instead of hardcoded dispatchers io`() {
        val populatorSource = sourceFile("DatabasePopulator.kt").readText()
        val moduleSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/core/di/DatabaseModule.kt")
            .readText()

        assertFalse(
            "DatabasePopulator must not hardcode Dispatchers.IO.",
            "Dispatchers.IO" in populatorSource
        )
        assertTrue(
            "DatabaseModule must launch population with injected DispatcherProvider.",
            "dispatchers: DispatcherProvider" in moduleSource &&
                "appScope.launch(dispatchers.io)" in moduleSource &&
                "DatabasePopulator.populate(context, database, dispatchers.io)" in moduleSource
        )
    }

    private fun sourceFile(name: String): File =
        projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/database")
            .resolve(name)

    private fun projectRoot(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
}
