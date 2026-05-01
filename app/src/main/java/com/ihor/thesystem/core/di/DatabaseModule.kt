package com.ihor.thesystem.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ihor.thesystem.data.local.room.TransactionProviderImpl
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.data.local.room.database.DatabaseMigrations
import com.ihor.thesystem.data.local.room.database.DatabasePopulator
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import dagger.Binds
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
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindTransactionProvider(impl: TransactionProviderImpl): TransactionProvider

    companion object {
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
                .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                .build()

            appScope.launch(Dispatchers.IO) {
                try {
                    DatabasePopulator.populate(context, database)
                    readinessRepo.markAsReady()
                } catch (e: Exception) {
                    e.printStackTrace()
                    readinessRepo.markAsFailed(e.message ?: "Unknown error")
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
        @Provides fun provideQuestLogDao(db: AppDatabase)          = db.questLogDao()
        @Provides fun provideWorkoutAnalyticsDao(db: AppDatabase)  = db.workoutAnalyticsDao()
        @Provides fun provideProtocolTemplateDao(db: AppDatabase)  = db.protocolTemplateDao()
        @Provides fun provideChatDao(db: AppDatabase)              = db.chatDao()
        @Provides fun provideCalendarCycleDao(db: AppDatabase)     = db.calendarCycleDao()
        @Provides fun provideTodoDao(db: AppDatabase)              = db.todoDao()
    }
}
