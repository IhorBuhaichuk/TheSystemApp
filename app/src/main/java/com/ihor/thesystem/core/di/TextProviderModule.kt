package com.ihor.thesystem.core.di

import com.ihor.thesystem.core.ui.AndroidWorkoutContextTextProvider
import com.ihor.thesystem.domain.usecase.WorkoutContextTextProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TextProviderModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutContextTextProvider(
        provider: AndroidWorkoutContextTextProvider
    ): WorkoutContextTextProvider
}
