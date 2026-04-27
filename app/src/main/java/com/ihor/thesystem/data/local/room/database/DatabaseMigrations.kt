package com.ihor.thesystem.data.local.room.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `workout_sessions` (`sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `questId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `totalTonnage` REAL NOT NULL, `cycleDay` INTEGER NOT NULL)""")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `exercise_sets` (`setId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `exerciseId` TEXT NOT NULL, `weight` REAL NOT NULL, `reps` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_sets_sessionId` ON `exercise_sets` (`sessionId`)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `workout_directives` (`exerciseId` INTEGER PRIMARY KEY NOT NULL, `targetWeight` REAL NOT NULL, `targetSets` INTEGER NOT NULL, `targetReps` INTEGER NOT NULL)""")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workout_templates ADD COLUMN timeLimitMinutes INTEGER NOT NULL DEFAULT 75")
            db.execSQL("ALTER TABLE system_config ADD COLUMN cycleAnchorDateTimestamp INTEGER NOT NULL DEFAULT 0")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `exercise_milestones` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `exerciseId` INTEGER NOT NULL, `milestoneWeight` REAL NOT NULL, `achievedAt` INTEGER NOT NULL, `note` TEXT)""")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `workout_session_logs` (`sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `questId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `totalTonnage` REAL NOT NULL, `cycleDay` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL DEFAULT 0)""")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `exercise_set_logs` (`setId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `weight` REAL NOT NULL, `reps` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL DEFAULT 1, FOREIGN KEY(`sessionId`) REFERENCES `workout_session_logs`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_set_logs_sessionId` ON `exercise_set_logs` (`sessionId`)")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `reference_matrix` (`exerciseId` TEXT PRIMARY KEY NOT NULL, `exerciseName` TEXT NOT NULL, `weightType` TEXT NOT NULL, `progressionStep` REAL NOT NULL, `milestones` TEXT NOT NULL, `repsMilestones` TEXT)""")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `protocol_template` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cycleDay` INTEGER NOT NULL, `taskName` TEXT NOT NULL, `category` TEXT NOT NULL, `contextRequirement` TEXT NOT NULL, `note` TEXT)""")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE progression_matrix_new (exerciseId INTEGER PRIMARY KEY NOT NULL, startWeight REAL NOT NULL, targetWeight REAL NOT NULL, currentWeight REAL NOT NULL, targetWeightNote TEXT)")
            db.execSQL("INSERT OR REPLACE INTO progression_matrix_new (exerciseId, startWeight, targetWeight, currentWeight, targetWeightNote) SELECT exerciseId, startWeight, targetWeight, currentWeight, targetWeightNote FROM progression_matrix")
            db.execSQL("DROP TABLE progression_matrix")
            db.execSQL("ALTER TABLE progression_matrix_new RENAME TO progression_matrix")
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE quest_task ADD COLUMN exerciseId INTEGER")
            db.execSQL("ALTER TABLE quest_task ADD COLUMN targetWeight REAL")
            db.execSQL("ALTER TABLE quest_task ADD COLUMN targetSets INTEGER")
            db.execSQL("ALTER TABLE quest_task ADD COLUMN targetReps INTEGER")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE system_config ADD COLUMN cycleAnchorDay INTEGER NOT NULL DEFAULT 1")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE progression_matrix ADD COLUMN currentRank TEXT NOT NULL DEFAULT 'E'")
            db.execSQL("ALTER TABLE progression_matrix ADD COLUMN completedCycles INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE player ADD COLUMN globalRank TEXT NOT NULL DEFAULT 'E'")
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE progression_matrix ADD COLUMN isPromotionPending INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Promotion type added to Enum, handled by TypeConverter
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Оновлення progression_matrix
            db.execSQL("ALTER TABLE progression_matrix ADD COLUMN nextRecommendedWeight REAL")
            db.execSQL("ALTER TABLE progression_matrix ADD COLUMN nextRecommendedSets INTEGER")
            db.execSQL("ALTER TABLE progression_matrix ADD COLUMN nextRecommendedReps TEXT")

            // 2. Міграція workout_directives для зміни типу targetReps з INTEGER на TEXT
            db.execSQL("CREATE TABLE IF NOT EXISTS `workout_directives_new` (`exerciseId` INTEGER NOT NULL, `targetWeight` REAL NOT NULL, `targetSets` INTEGER NOT NULL, `targetReps` TEXT NOT NULL, PRIMARY KEY(`exerciseId`))")
            db.execSQL("INSERT INTO `workout_directives_new` (`exerciseId`, `targetWeight`, `targetSets`, `targetReps`) SELECT `exerciseId`, `targetWeight`, `targetSets`, CAST(`targetReps` AS TEXT) FROM `workout_directives`")
            db.execSQL("DROP TABLE `workout_directives`")
            db.execSQL("ALTER TABLE `workout_directives_new` RENAME TO `workout_directives`")
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE exercise_set_logs ADD COLUMN userFeedback TEXT")
            db.execSQL("ALTER TABLE progression_matrix ADD COLUMN lastAiFeedback TEXT")
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `chat_message_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `role` TEXT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        }
    }

    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP INDEX IF EXISTS `index_exercise_sets_sessionId`")
            database.execSQL("DROP TABLE IF EXISTS `exercise_sets_old`")
            database.execSQL("ALTER TABLE `exercise_sets` RENAME TO `exercise_sets_old`")
            database.execSQL("""
                CREATE TABLE `exercise_sets` (
                    `setId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `sessionId` INTEGER NOT NULL, 
                    `exerciseId` INTEGER NOT NULL, 
                    `weight` REAL NOT NULL, 
                    `reps` INTEGER NOT NULL, 
                    `isCompleted` INTEGER NOT NULL, 
                    FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_sets_sessionId` ON `exercise_sets` (`sessionId`)")
            database.execSQL("""
                INSERT INTO `exercise_sets` (`setId`, `sessionId`, `exerciseId`, `weight`, `reps`, `isCompleted`) 
                SELECT `setId`, `sessionId`, CAST(`exerciseId` AS INTEGER), `weight`, `reps`, `isCompleted` 
                FROM `exercise_sets_old`
            """.trimIndent())
            database.execSQL("DROP TABLE `exercise_sets_old`")
        }
    }

    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `reference_matrix_old`")
            db.execSQL("ALTER TABLE `reference_matrix` RENAME TO `reference_matrix_old`")
            db.execSQL("""
                CREATE TABLE `reference_matrix` (
                    `exerciseId` INTEGER PRIMARY KEY NOT NULL,
                    `exerciseName` TEXT NOT NULL,
                    `weightType` TEXT NOT NULL,
                    `progressionStep` REAL NOT NULL,
                    `milestones` TEXT NOT NULL,
                    `repsMilestones` TEXT
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO `reference_matrix`
                SELECT CAST(`exerciseId` AS INTEGER), `exerciseName`,
                       `weightType`, `progressionStep`, `milestones`, `repsMilestones`
                FROM `reference_matrix_old`
            """.trimIndent())
            db.execSQL("DROP TABLE `reference_matrix_old`")
            db.execSQL("ALTER TABLE `exercise_sets` ADD COLUMN `userFeedback` TEXT")
        }
    }

    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE player ADD COLUMN strAttribute INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE player ADD COLUMN endAttribute INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE player ADD COLUMN disAttribute INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE player ADD COLUMN currentStreak INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE player ADD COLUMN maxStreak INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE player ADD COLUMN xpTotal INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE player ADD COLUMN xpThisWeek INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val cursor = db.query("PRAGMA table_info(player)")
            val columns = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
            cursor.close()

            if (!columns.contains("strAttribute")) db.execSQL("ALTER TABLE player ADD COLUMN strAttribute INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("endAttribute")) db.execSQL("ALTER TABLE player ADD COLUMN endAttribute INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("disAttribute")) db.execSQL("ALTER TABLE player ADD COLUMN disAttribute INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("currentStreak")) db.execSQL("ALTER TABLE player ADD COLUMN currentStreak INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("maxStreak")) db.execSQL("ALTER TABLE player ADD COLUMN maxStreak INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("xpTotal")) db.execSQL("ALTER TABLE player ADD COLUMN xpTotal INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("xpThisWeek")) db.execSQL("ALTER TABLE player ADD COLUMN xpThisWeek INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_19_20 = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE system_config ADD COLUMN cycleDaysPerMicrocycle INTEGER NOT NULL DEFAULT 4")
            db.execSQL("ALTER TABLE system_config ADD COLUMN microCyclesPerMonth INTEGER NOT NULL DEFAULT 4")
        }
    }

    val MIGRATION_20_21 = object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE quest ADD COLUMN targetExerciseId INTEGER")
        }
    }

    val MIGRATION_21_22 = object : Migration(21, 22) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val questCursor = db.query("PRAGMA table_info(quest)")
            val questColumns = mutableSetOf<String>()
            while (questCursor.moveToNext()) {
                questColumns.add(questCursor.getString(questCursor.getColumnIndexOrThrow("name")))
            }
            questCursor.close()
            if (!questColumns.contains("targetExerciseId")) {
                db.execSQL("ALTER TABLE quest ADD COLUMN targetExerciseId INTEGER")
            }
        }
    }

    val MIGRATION_22_23 = object : Migration(22, 23) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Міграція для додавання targetExerciseId, якщо він ще не існує
            val cursor = db.query("PRAGMA table_info(quest)")
            var exists = false
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "targetExerciseId") {
                    exists = true; break
                }
            }
            cursor.close()
            if (!exists) {
                db.execSQL("ALTER TABLE quest ADD COLUMN targetExerciseId INTEGER")
            }
        }
    }

    val MIGRATION_23_24 = object : Migration(23, 24) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Safely add missing columns for version 24 if they don't exist
            val cursor = db.query("PRAGMA table_info(player)")
            val columns = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
            cursor.close()

            if (!columns.contains("chestAttr")) db.execSQL("ALTER TABLE player ADD COLUMN chestAttr INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("backAttr")) db.execSQL("ALTER TABLE player ADD COLUMN backAttr INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("shouldersAttr")) db.execSQL("ALTER TABLE player ADD COLUMN shouldersAttr INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("quadsAttr")) db.execSQL("ALTER TABLE player ADD COLUMN quadsAttr INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("legsAttr")) db.execSQL("ALTER TABLE player ADD COLUMN legsAttr INTEGER NOT NULL DEFAULT 0")
            if (!columns.contains("armsAttr")) db.execSQL("ALTER TABLE player ADD COLUMN armsAttr INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_24_25 = object : Migration(24, 25) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Recreate table to drop columns
            db.execSQL("""
                CREATE TABLE `player_new` (
                    `id` INTEGER NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `level` INTEGER NOT NULL, 
                    `playerClass` TEXT NOT NULL, 
                    `height` REAL NOT NULL, 
                    `currentMonth` INTEGER NOT NULL, 
                    `currentWeek` INTEGER NOT NULL, 
                    `currentCycleDay` INTEGER NOT NULL, 
                    `consecutiveMainQuestFailures` INTEGER NOT NULL, 
                    `isPenaltyActive` INTEGER NOT NULL, 
                    `globalRank` TEXT NOT NULL, 
                    `currentStreak` INTEGER NOT NULL, 
                    `maxStreak` INTEGER NOT NULL, 
                    `xpTotal` INTEGER NOT NULL, 
                    `xpThisWeek` INTEGER NOT NULL, 
                    `chestAttr` INTEGER NOT NULL, 
                    `backAttr` INTEGER NOT NULL, 
                    `shouldersAttr` INTEGER NOT NULL, 
                    `quadsAttr` INTEGER NOT NULL, 
                    `legsAttr` INTEGER NOT NULL, 
                    `armsAttr` INTEGER NOT NULL, 
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())
            
            // Safely copy data. We assume columns exist in v24.
            db.execSQL("""
                INSERT INTO `player_new` (
                    `id`, `name`, `level`, `playerClass`, `height`, `currentMonth`, `currentWeek`, 
                    `currentCycleDay`, `consecutiveMainQuestFailures`, `isPenaltyActive`, `globalRank`, 
                    `currentStreak`, `maxStreak`, `xpTotal`, `xpThisWeek`, `chestAttr`, `backAttr`, 
                    `shouldersAttr`, `quadsAttr`, `legsAttr`, `armsAttr`
                )
                SELECT 
                    `id`, `name`, `level`, `playerClass`, `height`, `currentMonth`, `currentWeek`, 
                    `currentCycleDay`, `consecutiveMainQuestFailures`, `isPenaltyActive`, `globalRank`, 
                    `currentStreak`, `maxStreak`, `xpTotal`, `xpThisWeek`, `chestAttr`, `backAttr`, 
                    `shouldersAttr`, `quadsAttr`, `legsAttr`, `armsAttr` 
                FROM `player`
            """.trimIndent())
            
            db.execSQL("DROP TABLE `player`")
            db.execSQL("ALTER TABLE `player_new` RENAME TO `player`")
        }
    }

    val MIGRATION_25_26 = object : Migration(25, 26) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `debuff_config`")
        }
    }

    val MIGRATION_26_27 = object : Migration(26, 27) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Check if column exists first to avoid crash if it was partially added in a failed v26 attempt
            val cursor = db.query("PRAGMA table_info(progression_matrix)")
            var columnExists = false
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "lastAnalyzedTimestamp") {
                    columnExists = true
                    break
                }
            }
            cursor.close()

            if (!columnExists) {
                db.execSQL("ALTER TABLE progression_matrix ADD COLUMN lastAnalyzedTimestamp INTEGER NOT NULL DEFAULT 0")
            }
        }
    }


    val MIGRATION_27_28 = object : Migration(27, 28) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema changes for this version, just a version bump to force recreation for problematic devices
            // Or add any new schema changes here if needed for version 28
        }
    }

    val MIGRATION_28_29 = object : Migration(28, 29) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE player ADD COLUMN avatarUri TEXT")
        }
    }

    val MIGRATION_29_30 = object : Migration(29, 30) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema changes — version bump for exercise data seeding
        }
    }

    val MIGRATION_30_31 = object : Migration(30, 31) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema changes — version bump for exercise data seeding
        }
    }

    val MIGRATION_31_32 = object : Migration(31, 32) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema changes — version bump for exercise data seeding
        }
    }

    val MIGRATION_32_33 = object : Migration(32, 33) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema changes — version bump for exercise data seeding
        }
    }

    val MIGRATION_33_34 = object : Migration(33, 34) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Recreate player table to ensure exact schema match for version 34/35
            db.execSQL("""
                CREATE TABLE `player_new` (
                    `id` INTEGER NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `level` INTEGER NOT NULL, 
                    `playerClass` TEXT NOT NULL, 
                    `height` REAL NOT NULL, 
                    `currentMonth` INTEGER NOT NULL, 
                    `currentWeek` INTEGER NOT NULL, 
                    `currentCycleDay` INTEGER NOT NULL, 
                    `consecutiveMainQuestFailures` INTEGER NOT NULL, 
                    `isPenaltyActive` INTEGER NOT NULL, 
                    `globalRank` TEXT NOT NULL, 
                    `avatarUri` TEXT, 
                    `currentStreak` INTEGER NOT NULL, 
                    `maxStreak` INTEGER NOT NULL, 
                    `xpTotal` INTEGER NOT NULL, 
                    `xpThisWeek` INTEGER NOT NULL, 
                    `chestAttr` INTEGER NOT NULL, 
                    `backAttr` INTEGER NOT NULL, 
                    `shouldersAttr` INTEGER NOT NULL, 
                    `quadsAttr` INTEGER NOT NULL, 
                    `legsAttr` INTEGER NOT NULL, 
                    `armsAttr` INTEGER NOT NULL, 
                    `absAttr` INTEGER NOT NULL, 
                    `legsGroupAttr` INTEGER NOT NULL, 
                    `coreAttr` INTEGER NOT NULL, 
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO `player_new` (
                    `id`, `name`, `level`, `playerClass`, `height`, `currentMonth`, `currentWeek`, 
                    `currentCycleDay`, `consecutiveMainQuestFailures`, `isPenaltyActive`, `globalRank`, 
                    `avatarUri`, `currentStreak`, `maxStreak`, `xpTotal`, `xpThisWeek`, `chestAttr`, 
                    `backAttr`, `shouldersAttr`, `quadsAttr`, `legsAttr`, `armsAttr`, `absAttr`, 
                    `legsGroupAttr`, `coreAttr`
                )
                SELECT 
                    `id`, `name`, `level`, `playerClass`, `height`, `currentMonth`, `currentWeek`, 
                    `currentCycleDay`, `consecutiveMainQuestFailures`, `isPenaltyActive`, `globalRank`, 
                    `avatarUri`, `currentStreak`, `maxStreak`, `xpTotal`, `xpThisWeek`, `chestAttr`, 
                    `backAttr`, `shouldersAttr`, `quadsAttr`, `legsAttr`, `armsAttr`, 0, 0, 0 
                FROM `player`
            """.trimIndent())

            db.execSQL("DROP TABLE `player`")
            db.execSQL("ALTER TABLE `player_new` RENAME TO `player`")
        }
    }

    val MIGRATION_34_35 = object : Migration(34, 35) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema changes — version bump to match current AppDatabase version
        }
    }

    val MIGRATION_35_36 = object : Migration(35, 36) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE exercises ADD COLUMN level TEXT")
            db.execSQL("ALTER TABLE exercises ADD COLUMN mechanic TEXT")
            db.execSQL("ALTER TABLE exercises ADD COLUMN force TEXT")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
        MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12,
        MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
        MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22,
        MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27,
        MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32,
        MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36
    )
}
