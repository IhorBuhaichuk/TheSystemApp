package com.ihor.thesystem.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ihor.thesystem.data.local.room.converters.Converters
import com.ihor.thesystem.data.local.room.dao.*
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.data.local.room.relations.SessionWithSets

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
        ReferenceMatrixEntity::class
    ],
    version = 5,
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

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_sessions` (
                        `sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `questId` INTEGER NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `totalTonnage` REAL NOT NULL, 
                        `cycleDay` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `exercise_sets` (
                        `setId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `sessionId` INTEGER NOT NULL, 
                        `exerciseId` TEXT NOT NULL, 
                        `weight` REAL NOT NULL, 
                        `reps` INTEGER NOT NULL, 
                        `isCompleted` INTEGER NOT NULL, 
                        FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_sets_sessionId` ON `exercise_sets` (`sessionId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_directives` (
                        `exerciseId` INTEGER PRIMARY KEY NOT NULL, 
                        `targetWeight` REAL NOT NULL, 
                        `targetSets` INTEGER NOT NULL, 
                        `targetReps` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_template ADD COLUMN timeLimitMinutes INTEGER NOT NULL DEFAULT 75")
                db.execSQL("ALTER TABLE system_config ADD COLUMN cycleAnchorDateTimestamp INTEGER NOT NULL DEFAULT 0")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `exercise_milestones` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `exerciseId` INTEGER NOT NULL, 
                        `milestoneWeight` REAL NOT NULL, 
                        `achievedAt` INTEGER NOT NULL, 
                        `note` TEXT
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workout_session_logs` (
                        `sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `questId` INTEGER NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `totalTonnage` REAL NOT NULL, 
                        `cycleDay` INTEGER NOT NULL, 
                        `durationMinutes` INTEGER NOT NULL DEFAULT 0
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `exercise_set_logs` (
                        `setId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `sessionId` INTEGER NOT NULL, 
                        `exerciseId` INTEGER NOT NULL, 
                        `weight` REAL NOT NULL, 
                        `reps` INTEGER NOT NULL, 
                        `isCompleted` INTEGER NOT NULL DEFAULT 1, 
                        FOREIGN KEY(`sessionId`) REFERENCES `workout_session_logs`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_set_logs_sessionId` ON `exercise_set_logs` (`sessionId`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reference_matrix` (
                        `exerciseId` TEXT PRIMARY KEY NOT NULL, 
                        `exerciseName` TEXT NOT NULL, 
                        `weightType` TEXT NOT NULL, 
                        `progressionStep` REAL NOT NULL, 
                        `milestones` TEXT NOT NULL, 
                        `repsMilestones` TEXT
                    )
                """)
            }
        }
    }
}
