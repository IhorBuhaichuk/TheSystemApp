package com.ihor.thesystem.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ihor.thesystem.data.local.room.TransactionProviderImpl
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.data.local.room.database.DatabaseMigrations
import com.ihor.thesystem.data.local.room.database.DatabasePopulator
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.repository.DatabaseReadinessRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
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
            @ApplicationScope appScope: CoroutineScope,
            dispatchers: DispatcherProvider
        ): AppDatabase {
            val database = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "the_system_db"
            )
                .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                .build()

            appScope.launch(dispatchers.io) {
                try {
                    DatabasePopulator.populate(context, database, dispatchers.io)
                    readinessRepo.markAsReady()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Timber.e(e, "Database population failed")
                    readinessRepo.markAsFailed(e.message ?: "Unknown error")
                }
            }

            return database
        }

        @Provides @Singleton fun providePlayerDao(db: AppDatabase)            = db.playerDao()
        @Provides @Singleton fun provideWeightLogDao(db: AppDatabase)         = db.weightLogDao()
        @Provides @Singleton fun provideSystemConfigDao(db: AppDatabase)      = db.systemConfigDao()
        @Provides @Singleton fun provideWorkoutDao(db: AppDatabase)           = db.workoutDao()
        @Provides @Singleton fun provideScheduleDao(db: AppDatabase)          = db.scheduleDao()
        @Provides @Singleton fun provideQuestDao(db: AppDatabase)             = db.questDao()
        @Provides @Singleton fun provideProgressionMatrixDao(db: AppDatabase) = db.progressionMatrixDao()
        @Provides @Singleton fun provideQuestLogDao(db: AppDatabase)          = db.questLogDao()
        @Provides @Singleton fun provideWorkoutAnalyticsDao(db: AppDatabase)  = db.workoutAnalyticsDao()
        @Provides @Singleton fun provideProtocolTemplateDao(db: AppDatabase)  = db.protocolTemplateDao()
        @Provides @Singleton fun provideChatDao(db: AppDatabase)              = db.chatDao()
        @Provides @Singleton fun provideCalendarCycleDao(db: AppDatabase)     = db.calendarCycleDao()
        @Provides @Singleton fun provideTodoDao(db: AppDatabase)              = db.todoDao()
    }
}
