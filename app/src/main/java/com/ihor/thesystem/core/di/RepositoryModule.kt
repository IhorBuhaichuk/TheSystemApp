package com.ihor.thesystem.core.di

import com.ihor.thesystem.data.repository_impl.*
import com.ihor.thesystem.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds @Singleton
    abstract fun bindQuestRepository(impl: QuestRepositoryImpl): QuestRepository

    @Binds @Singleton
    abstract fun bindSystemConfigRepository(impl: SystemConfigRepositoryImpl): SystemConfigRepository

    @Binds @Singleton
    abstract fun bindDebuffRepository(impl: DebuffRepositoryImpl): DebuffRepository

    @Binds @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds @Singleton
    abstract fun bindProgressionMatrixRepository(
        impl: ProgressionMatrixRepositoryImpl
    ): ProgressionMatrixRepository
}