package com.ihor.thesystem.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ihor.thesystem.data.local.room.converters.Converters
import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*

@Database(
    entities = [
        PlayerEntity::class,
        WeightLogEntity::class,
        SystemConfigEntity::class,
        ExerciseEntity::class,
        DailyTaskTemplateEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutExerciseCrossRef::class,
        ScheduleEntity::class,
        ScheduleTaskCrossRef::class,
        QuestEntity::class,
        QuestTaskEntity::class,
        ProgressionMatrixEntity::class,
        DebuffConfigEntity::class,
        QuestLogEntity::class,
        WorkoutSessionEntity::class,
        ExerciseSetEntity::class,
        WorkoutDirectiveEntity::class,
        ExerciseMilestoneEntity::class,
        WorkoutSessionLogEntity::class,
        ExerciseSetLogEntity::class,
        ReferenceMatrixEntity::class,
        ProtocolTemplateEntity::class,
        ChatMessageEntity::class
    ],
    version = 22,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun systemConfigDao(): SystemConfigDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun questDao(): QuestDao
    abstract fun progressionMatrixDao(): ProgressionMatrixDao
    abstract fun debuffConfigDao(): DebuffConfigDao
    abstract fun questLogDao(): QuestLogDao
    abstract fun workoutAnalyticsDao(): WorkoutAnalyticsDao
    abstract fun protocolTemplateDao(): ProtocolTemplateDao
    abstract fun chatDao(): ChatDao

    companion object {
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
                db.execSQL("ALTER TABLE debuff_config ADD COLUMN cycleDay INTEGER")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ця міграція виправляє можливу невідповідність хешу ідентичності,
                // переконуючись, що всі поля існують.
                
                val questCursor = db.query("PRAGMA table_info(quest)")
                val questColumns = mutableSetOf<String>()
                while (questCursor.moveToNext()) {
                    questColumns.add(questCursor.getString(questCursor.getColumnIndexOrThrow("name")))
                }
                questCursor.close()
                if (!questColumns.contains("targetExerciseId")) {
                    db.execSQL("ALTER TABLE quest ADD COLUMN targetExerciseId INTEGER")
                }

                val debuffCursor = db.query("PRAGMA table_info(debuff_config)")
                val debuffColumns = mutableSetOf<String>()
                while (debuffCursor.moveToNext()) {
                    debuffColumns.add(debuffCursor.getString(debuffCursor.getColumnIndexOrThrow("name")))
                }
                debuffCursor.close()
                if (!debuffColumns.contains("cycleDay")) {
                    db.execSQL("ALTER TABLE debuff_config ADD COLUMN cycleDay INTEGER")
                }
            }
        }
    }
}
