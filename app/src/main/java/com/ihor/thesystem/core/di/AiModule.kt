package com.ihor.thesystem.core.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
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
import kotlin.time.Duration.Companion.seconds

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

        private val defaultSafetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.LOW_AND_ABOVE)
        )

        @Provides
        @Singleton
        @Named("GeminiApiKey")
        fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY

        @Provides
        @Singleton
        @Named("GeminiClientAiEnabled")
        fun provideGeminiClientAiEnabled(): Boolean = BuildConfig.GEMINI_CLIENT_AI_ENABLED

        @Provides
        @Singleton
        @Named("ArchitectModel")
        fun provideArchitectGenerativeModel(
            @Named("GeminiApiKey") geminiApiKey: String
        ): GenerativeModel {
            return GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = geminiApiKey,
                requestOptions = RequestOptions(timeout = 60.seconds),
                safetySettings = defaultSafetySettings,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                },
                systemInstruction = content {
                    text(
                        "Ти AI-аналітик тренувальної системи. " +
                            "Відповідай виключно валідним JSON-об'єктом без markdown. " +
                            "Не приймай фінальних рішень щодо плану: системний валідатор обмежить або відхилить цілі."
                    )
                }
            )
        }

        @Provides
        @Singleton
        @Named("LiveCoachModel")
        fun provideLiveCoachGenerativeModel(
            @Named("GeminiApiKey") geminiApiKey: String
        ): GenerativeModel {
            return GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = geminiApiKey,
                requestOptions = RequestOptions(timeout = 60.seconds),
                safetySettings = defaultSafetySettings,
                systemInstruction = content {
                    text(
                        "Ти живий AI-наставник з тренувань. " +
                            "Відповідай коротко, природно й українською. " +
                            "Не використовуй JSON або markdown. Не змінюй план і не призначай ваги як фінальне рішення."
                    )
                }
            )
        }
    }
}
