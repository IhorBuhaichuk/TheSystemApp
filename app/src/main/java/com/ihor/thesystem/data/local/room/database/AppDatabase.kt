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
        // Додані таблиці для модуля AI Архітектор:
        WorkoutSessionEntity::class,
        ExerciseSetEntity::class,
        WorkoutDirectiveEntity::class
    ],
    version = 3,
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

    // Доданий DAO для аналітики тренувань та AI
    abstract fun workoutAnalyticsDao(): WorkoutAnalyticsDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Створення таблиці workout_sessions
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

                // Створення таблиці exercise_sets з прив'язкою до workout_sessions
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
                // Індекс для швидкого пошуку та видалення каскадом
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_sets_sessionId` ON `exercise_sets` (`sessionId`)")

                // Створення таблиці workout_directives
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_directives` (
                        `exerciseId` TEXT NOT NULL, 
                        `targetWeight` REAL NOT NULL, 
                        `targetSets` INTEGER NOT NULL, 
                        `targetReps` INTEGER NOT NULL, 
                        PRIMARY KEY(`exerciseId`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}