package com.ihor.thesystem.core.di

import android.content.Context
import androidx.room.Room
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "the_system_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_2_3, 
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14
            )
            .build()

        // ОДИН ВИКЛИК: Заповнюємо базу в фоновому потоці
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                DatabasePopulator.populate(database)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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
    @Provides fun provideProtocolTemplateDao(db: AppDatabase)  = db.protocolTemplateDao()
}
