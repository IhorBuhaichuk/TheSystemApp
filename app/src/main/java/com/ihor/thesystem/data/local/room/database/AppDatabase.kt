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
        ProtocolTemplateEntity::class
    ],
    version = 12,
    exportSchema = false
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
        
        val MIGRATION_7_8 = object : Migration(7, 8) { override fun migrate(db: SupportSQLiteDatabase) {} }
        val MIGRATION_8_9 = object : Migration(8, 9) { override fun migrate(db: SupportSQLiteDatabase) {} }

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
                // Promotion type added to Enum, column already added in 10_11 or if missing we can check.
                // Based on prompt, we need to ensure isPromotionPending is there.
                // If version was 11, and it had isPromotionPending, then 12 is for PROMOTION type in QuestType.
                // QuestType is an Enum stored as String, so no SQL change needed for type.
            }
        }
    }
}
