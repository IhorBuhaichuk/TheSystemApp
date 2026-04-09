package com.ihor.thesystem.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.data.local.room.database.DatabasePopulator
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        readinessRepo: DatabaseReadinessRepository,
        @ApplicationScope appScope: CoroutineScope
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
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16,
                AppDatabase.MIGRATION_16_17
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // База створена вперше
                }
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // База відкрита (кожен запуск)
                }
            })
            .build()

        // Заповнюємо базу в фоновому потоці та сигналізуємо про готовність
        appScope.launch(Dispatchers.IO) {
            try {
                DatabasePopulator.populate(database)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                readinessRepo.markAsReady()
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
    @Provides fun provideChatDao(db: AppDatabase)              = db.chatDao()
}
