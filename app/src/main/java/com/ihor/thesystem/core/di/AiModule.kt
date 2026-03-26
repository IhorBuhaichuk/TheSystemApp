package com.ihor.thesystem.core.di

import com.ihor.thesystem.data.repository_impl.AiArchitectRepositoryImpl
import com.ihor.thesystem.data.repository_impl.WorkoutAnalyticsRepositoryImpl
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    abstract fun bindAiArchitectRepository(
        impl: AiArchitectRepositoryImpl
    ): AiArchitectRepository

    @Binds
    abstract fun bindWorkoutAnalyticsRepository(
        impl: WorkoutAnalyticsRepositoryImpl
    ): WorkoutAnalyticsRepository
}