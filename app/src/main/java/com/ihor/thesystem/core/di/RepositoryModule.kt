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
    abstract fun bindAvatarRepository(impl: AvatarRepositoryImpl): AvatarRepository

    @Binds @Singleton
    abstract fun bindQuestRepository(impl: QuestRepositoryImpl): QuestRepository

    @Binds @Singleton
    abstract fun bindSystemConfigRepository(impl: SystemConfigRepositoryImpl): SystemConfigRepository

    @Binds @Singleton
    abstract fun bindCalendarCycleRepository(impl: CalendarCycleRepositoryImpl): CalendarCycleRepository

    @Binds @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository

    @Binds @Singleton
    abstract fun bindReadinessRepository(impl: ReadinessRepositoryImpl): ReadinessRepository

    @Binds @Singleton
    abstract fun bindEquipmentProfileRepository(impl: EquipmentProfileRepositoryImpl): EquipmentProfileRepository

    @Binds @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds @Singleton
    abstract fun bindHealthSignalsRepository(impl: HealthConnectSignalsRepositoryImpl): HealthSignalsRepository

    @Binds @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository

    @Binds @Singleton
    abstract fun bindNutritionRepository(impl: NutritionRepositoryImpl): NutritionRepository

    @Binds @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds @Singleton
    abstract fun bindProgressionMatrixRepository(
        impl: ProgressionMatrixRepositoryImpl
    ): ProgressionMatrixRepository

    @Binds @Singleton
    abstract fun bindDatabaseReadinessRepository(
        impl: DatabaseReadinessRepositoryImpl
    ): DatabaseReadinessRepository

    @Binds @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    // ViewingDateRepository не потребує @Binds, оскільки він є Singleton класом
}
