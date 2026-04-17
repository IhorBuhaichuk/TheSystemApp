package com.ihor.thesystem.core.di

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.dao.QuestLogDao
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // Всі UseCase тепер використовують @Inject constructor() у доменному шарі.
    // Hilt автоматично знайде їх, якщо вони доступні в модулі :app через залежність implementation(project(":domain")).
    // Це дозволяє видалити явні @Provides методи для UseCase, які не мають складної ініціалізації.
}
