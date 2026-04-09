package com.ihor.thesystem.core.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.data.repository_impl.AiArchitectRepositoryImpl
import com.ihor.thesystem.data.repository_impl.LiveCoachRepositoryImpl
import com.ihor.thesystem.data.repository_impl.WorkoutAnalyticsRepositoryImpl
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

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

    @Binds
    abstract fun bindLiveCoachRepository(
        impl: LiveCoachRepositoryImpl
    ): LiveCoachRepository

    companion object {
        @Provides
        @Singleton
        @Named("ArchitectModel")
        fun provideArchitectGenerativeModel(): GenerativeModel {
            return GenerativeModel(
                modelName = "gemini-2.0-flash-lite",
                apiKey = BuildConfig.GEMINI_API_KEY,
                systemInstruction = content {
                    text("Ти фітнес-аналітик. Відповідай СУВОРО масивом об'єктів JSON. Кожен об'єкт має містити параметри на наступне тренування та поле aiFeedback (текст до 3 речень). КРИТИЧНО: Стандарт JSON вимагає виключно подвійних лапок. Щоб не зламати парсер, всередині тексту aiFeedback КАТЕГОРИЧНО ЗАБОРОНЕНО використовувати будь-які лапки (ні подвійні, ні одинарні) та переноси рядків (\\n).")
                }
            )
        }

        @Provides
        @Singleton
        @Named("LiveCoachModel")
        fun provideLiveCoachGenerativeModel(): GenerativeModel {
            return GenerativeModel(
                modelName = "gemini-2.0-flash-lite",
                apiKey = BuildConfig.GEMINI_API_KEY,
                systemInstruction = content {
                    text("Ти 'ТРЕНЕР' - елітний живий ШІ-наставник. Спілкуйся природно, як людина. Відповідай коротко і по суті на питання гравця щодо поточного тренування, техніки чи болю. НЕ використовуй JSON та маркдаун.")
                }
            )
        }
    }
}
