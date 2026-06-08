package com.ihor.thesystem.data.local.room.database

import com.ihor.thesystem.data.repository_impl.BackupImportPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node

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

    @Test
    fun `latest room schema contains indices for high traffic queries`() {
        val latestSchema = latestSchema().readText()
        val expectedIndices = listOf(
            "index_chat_message_table_sessionId_timestamp",
            "index_chat_message_table_sessionId_role",
            "index_exercises_category",
            "index_exercise_set_logs_exerciseId",
            "index_exercise_set_logs_exerciseId_sessionId",
            "index_quest_status_date",
            "index_quest_type_status_date",
            "index_quest_task_questId",
            "index_quest_log_questType_wasSuccessful_completedAt",
            "index_reference_matrix_exerciseName",
            "index_schedule_cycleDay",
            "index_todo_dateEpochDay_parentTodoId_sortOrder",
            "index_weight_log_timestamp",
            "index_workout_exercise_cross_ref_workoutTemplateId_orderIndex",
            "index_workout_session_logs_timestamp"
        )

        val missing = expectedIndices.filterNot { it in latestSchema }

        assertTrue(
            "Latest Room schema must keep data-layer query indices: $missing",
            missing.isEmpty()
        )
    }

    @Test
    fun `explicit json backup covers every room entity table`() {
        val latestSchema = latestSchema().readText()
        val roomTables = Regex("\"tableName\"\\s*:\\s*\"([^\"]+)\"")
            .findAll(latestSchema)
            .map { it.groupValues[1] }
            .sorted()
            .toList()

        assertEquals(
            "Explicit JSON backup must include every Room entity table so user data survives restore.",
            roomTables,
            BackupImportPolicy.includedTables.sorted()
        )
    }

    @Test
    fun `silent android backup excludes room database files`() {
        val databaseFiles = setOf("the_system_db", "the_system_db-shm", "the_system_db-wal")

        assertEquals(
            "Cloud backup must not silently copy the Room database.",
            databaseFiles,
            databaseExclusions("data_extraction_rules.xml", "cloud-backup")
        )
        assertEquals(
            "Device transfer must stay aligned with cloud backup for the Room database.",
            databaseFiles,
            databaseExclusions("data_extraction_rules.xml", "device-transfer")
        )
        assertEquals(
            "Legacy full backup rules must exclude the Room database.",
            databaseFiles,
            databaseExclusions("full_backup_content.xml")
        )
    }

    @Test
    fun `schedule cycle day is unique in entity and migration`() {
        val scheduleEntity = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/entity/ScheduleEntity.kt")
            .readText()
        val migrations = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/database/DatabaseMigrations.kt")
            .readText()

        assertTrue(
            "ScheduleEntity must enforce one schedule per cycle day.",
            """Index(value = ["cycleDay"], unique = true)""" in scheduleEntity
        )
        assertTrue(
            "Database migration must create the unique schedule.cycleDay index without dropping schedule data.",
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_schedule_cycleDay` ON `schedule` (`cycleDay`)" in migrations
        )
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

    private fun latestSchema(): File {
        return projectRoot()
            .resolve("schemas")
            .resolve(AppDatabase::class.java.name)
            .resolve("$APP_DATABASE_VERSION.json")
    }

    private fun databaseExclusions(
        resourceName: String,
        sectionName: String? = null
    ): Set<String> {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(projectRoot().resolve("src/main/res/xml/$resourceName"))
        val root = if (sectionName == null) {
            document.documentElement
        } else {
            document.getElementsByTagName(sectionName).item(0) as Element
        }

        return buildSet {
            val nodes = root.getElementsByTagName("exclude")
            for (index in 0 until nodes.length) {
                val element = nodes.item(index)
                if (element.nodeType == Node.ELEMENT_NODE) {
                    element as Element
                    if (element.getAttribute("domain") == "database") {
                        add(element.getAttribute("path"))
                    }
                }
            }
        }
    }

    private fun projectRoot(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
}
