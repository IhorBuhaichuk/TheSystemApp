package com.ihor.thesystem.core.di

import com.ihor.thesystem.data.local.room.dao.QuestLogDao
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
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
        matrixRepo:   ProgressionMatrixRepository,
        calculateRecommendation: CalculateRecommendedSetUseCase
    ) = GenerateDailyQuestsUseCase(questRepo, scheduleRepo, playerRepo, matrixRepo, calculateRecommendation)

    @Provides @Singleton
    fun provideActivatePenaltyManuallyUseCase(
        playerRepo: PlayerRepository
    ) = ActivatePenaltyManuallyUseCase(playerRepo)

    @Provides @Singleton
    fun provideAdvanceCycleDayUseCase(
        playerRepo:     PlayerRepository,
        questRepo:      QuestRepository
    ) = AdvanceCycleDayUseCase(playerRepo, questRepo)

    @Provides @Singleton
    fun provideGetStatusScreenDataUseCase(
        playerRepo: PlayerRepository,
        questRepo:  QuestRepository,
        debuffRepo: DebuffRepository,
        questLogDao: QuestLogDao,
        scheduleRepo: ScheduleRepository
    ) = GetStatusScreenDataUseCase(playerRepo, questRepo, debuffRepo, questLogDao, scheduleRepo)

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
        configRepo: SystemConfigRepository,
        generateDailyQuestsUseCase: GenerateDailyQuestsUseCase,
        calculateAttributes: CalculateAttributesUseCase
    ) = FinalizeDayUseCase(playerRepo, questRepo, configRepo, generateDailyQuestsUseCase, calculateAttributes)

    @Provides @Singleton
    fun provideGetLastWorkoutContextUseCase(
        analyticsRepo: WorkoutAnalyticsRepository
    ) = GetLastWorkoutContextUseCase(analyticsRepo)

    @Provides @Singleton
    fun provideApplyAiRecommendationsUseCase(
        matrixRepo: ProgressionMatrixRepository,
        playerRepo: PlayerRepository,
        analyticsRepo: WorkoutAnalyticsRepository,
        aiRepository: AiArchitectRepository
    ) = ApplyAiRecommendationsUseCase(matrixRepo, playerRepo, analyticsRepo, aiRepository)

    @Provides @Singleton
    fun provideRecalculateGlobalRankUseCase(
        matrixRepo: ProgressionMatrixRepository,
        playerRepo: PlayerRepository
    ) = RecalculateGlobalRankUseCase(matrixRepo, playerRepo)

    @Provides @Singleton
    fun provideSendArchitectAnalysisUseCase(
        aiArchitectRepository: AiArchitectRepository,
        chatRepository: ChatRepository
    ) = SendArchitectAnalysisUseCase(aiArchitectRepository, chatRepository)

    @Provides @Singleton
    fun provideSendLiveCoachMessageUseCase(
        liveCoachRepository: LiveCoachRepository,
        chatRepository: ChatRepository
    ) = SendLiveCoachMessageUseCase(liveCoachRepository, chatRepository)

    @Provides @Singleton
    fun provideSaveExerciseSetsUseCase(
        matrixRepo: ProgressionMatrixRepository,
        playerRepo: PlayerRepository,
        recalculateGlobalRank: RecalculateGlobalRankUseCase
    ) = SaveExerciseSetsUseCase(matrixRepo, playerRepo, recalculateGlobalRank)

    @Provides @Singleton
    fun provideCalculateAttributesUseCase(
        matrixRepo: ProgressionMatrixRepository,
        questLogDao: QuestLogDao,
        playerRepo: PlayerRepository
    ) = CalculateAttributesUseCase(matrixRepo, questLogDao, playerRepo)
}
