package com.ihor.thesystem.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.data.local.room.database.DatabasePopulator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        var db: AppDatabase? = null

        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(sqliteDb: SupportSQLiteDatabase) {
                super.onCreate(sqliteDb)
                db?.let { database ->
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        DatabasePopulator.populate(
                            playerDao            = database.playerDao(),
                            systemConfigDao      = database.systemConfigDao(),
                            workoutDao           = database.workoutDao(),
                            scheduleDao          = database.scheduleDao(),
                            progressionMatrixDao = database.progressionMatrixDao(),
                            debuffConfigDao      = database.debuffConfigDao()
                        )
                    }
                }
            }
        }

        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "the_system_db"
        )
            .addCallback(callback)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()

        db = database
        return database
    }

    @Provides fun providePlayerDao(db: AppDatabase)            = db.playerDao()
    @Provides fun provideWeightLogDao(db: AppDatabase)         = db.weightLogDao()
    @Provides fun provideSystemConfigDao(db: AppDatabase)      = db.systemConfigDao()
    @Provides fun provideWorkoutDao(db: AppDatabase)           = db.workoutDao()
    @Provides fun provideScheduleDao(db: AppDatabase)          = db.scheduleDao()
    @Provides fun provideQuestDao(db: AppDatabase)             = db.questDao()
    @Provides fun provideProgressionMatrixDao(db: AppDatabase) = db.progressionMatrixDao()
    @Provides fun provideDebuffConfigDao(db: AppDatabase)      = db.debuffConfigDao()
    @Provides fun provideQuestLogDao(db: AppDatabase)          = db.questLogDao()
    @Provides fun provideWorkoutAnalyticsDao(db: AppDatabase)  = db.workoutAnalyticsDao()
}