package com.ihor.thesystem.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
        QuestLogEntity::class,
        WorkoutSessionEntity::class,
        ExerciseSetEntity::class,
        WorkoutDirectiveEntity::class,
        ExerciseMilestoneEntity::class,
        WorkoutSessionLogEntity::class,
        ExerciseSetLogEntity::class,
        ReferenceMatrixEntity::class,
        ProtocolTemplateEntity::class,
        ChatMessageEntity::class,
        CalendarCycleConfigEntity::class,
        CalendarCycleDayEntity::class
    ],
    version = 41,
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
    abstract fun questLogDao(): QuestLogDao
    abstract fun workoutAnalyticsDao(): WorkoutAnalyticsDao
    abstract fun protocolTemplateDao(): ProtocolTemplateDao
    abstract fun chatDao(): ChatDao
    abstract fun calendarCycleDao(): CalendarCycleDao
}
