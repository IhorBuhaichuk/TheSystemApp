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
    fun provideCalculateCycleDayForDateUseCase() = CalculateCycleDayForDateUseCase()

    @Provides @Singleton
    fun provideCalculateRecommendedSetUseCase(
        analyticsRepo: WorkoutAnalyticsRepository,
        matrixRepo: ProgressionMatrixRepository
    ) = CalculateRecommendedSetUseCase(analyticsRepo, matrixRepo)

    @Provides @Singleton
    fun provideCalculateEffectiveWeightUseCase(
        configRepo: SystemConfigRepository,
        debuffRepo: DebuffRepository
    ) = CalculateEffectiveWeightUseCase(configRepo, debuffRepo)

    @Provides @Singleton
    fun provideGenerateDailyQuestsUseCase(
        questRepo:    QuestRepository,
        scheduleRepo: ScheduleRepository,
        playerRepo:   PlayerRepository,
        calculateRecommendation: CalculateRecommendedSetUseCase
    ) = GenerateDailyQuestsUseCase(questRepo, scheduleRepo, playerRepo, calculateRecommendation)

    @Provides @Singleton
    fun provideLevelUpUseCase(
        playerRepo: PlayerRepository
    ) = LevelUpUseCase(playerRepo)

    @Provides @Singleton
    fun provideCheckPenaltyZoneUseCase(
        playerRepo: PlayerRepository,
        questRepo:  QuestRepository
    ) = CheckPenaltyZoneUseCase(playerRepo, questRepo)

    @Provides @Singleton
    fun provideActivatePenaltyManuallyUseCase(
        playerRepo: PlayerRepository
    ) = ActivatePenaltyManuallyUseCase(playerRepo)

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

    @Provides @Singleton
    fun provideUpdatePlayerHeightUseCase(
        repo: PlayerRepository
    ) = UpdatePlayerHeightUseCase(repo)

    @Provides @Singleton
    fun provideSyncCycleAnchorUseCase(
        configRepo: SystemConfigRepository,
        playerRepo: PlayerRepository,
        questRepo: QuestRepository,
        generateQuests: GenerateDailyQuestsUseCase
    ) = SyncCycleAnchorUseCase(configRepo, playerRepo, questRepo, generateQuests)
    
    @Provides @Singleton
    fun provideFinalizeDayUseCase(
        playerRepo: PlayerRepository,
        questRepo: QuestRepository,
        generateDailyQuestsUseCase: GenerateDailyQuestsUseCase
    ) = FinalizeDayUseCase(playerRepo, questRepo, generateDailyQuestsUseCase)
}
