package com.ihor.thesystem.core.di

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

    @Provides @Singleton
    fun provideGenerateDailyQuestsUseCase(
        playerRepo:   PlayerRepository,
        questRepo:    QuestRepository,
        scheduleRepo: ScheduleRepository
    ) = GenerateDailyQuestsUseCase(playerRepo, questRepo, scheduleRepo)

    @Provides @Singleton
    fun provideLevelUpUseCase(
        playerRepo: PlayerRepository
    ) = LevelUpUseCase(playerRepo)

    @Provides @Singleton
    fun provideCheckPenaltyZoneUseCase(
        playerRepo: PlayerRepository,
        questRepo:  QuestRepository,
        matrixRepo: ProgressionMatrixRepository
    ) = CheckPenaltyZoneUseCase(playerRepo, questRepo, matrixRepo)

    @Provides @Singleton
    fun provideActivatePenaltyManuallyUseCase(
        playerRepo: PlayerRepository,
        matrixRepo: ProgressionMatrixRepository
    ) = ActivatePenaltyManuallyUseCase(playerRepo, matrixRepo)

    @Provides @Singleton
    fun provideAdvanceCycleDayUseCase(
        playerRepo:     PlayerRepository,
        questRepo:      QuestRepository,
        generateQuests: GenerateDailyQuestsUseCase,
        levelUp:        LevelUpUseCase,
        checkPenalty:   CheckPenaltyZoneUseCase
    ) = AdvanceCycleDayUseCase(playerRepo, questRepo, generateQuests, levelUp, checkPenalty)

    @Provides @Singleton
    fun provideGetStatusScreenDataUseCase(
        playerRepo: PlayerRepository,
        questRepo:  QuestRepository,
        debuffRepo: DebuffRepository
    ) = GetStatusScreenDataUseCase(playerRepo, questRepo, debuffRepo)

    @Provides @Singleton
    fun provideGetProgressionMatrixUseCase(
        repo: ProgressionMatrixRepository
    ) = GetProgressionMatrixUseCase(repo)

    @Provides @Singleton
    fun provideUpdateExerciseWeightUseCase(
        repo: ProgressionMatrixRepository
    ) = UpdateExerciseWeightUseCase(repo)

    @Provides @Singleton
    fun provideGetSystemConfigUseCase(
        repo: SystemConfigRepository
    ) = GetSystemConfigUseCase(repo)

    @Provides @Singleton
    fun provideUpdateSystemConfigUseCase(
        repo: SystemConfigRepository
    ) = UpdateSystemConfigUseCase(repo)
}