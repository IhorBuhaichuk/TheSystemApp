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
                    text(ARCHITECT_SYSTEM_INSTRUCTION)
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
                    text(LIVE_COACH_SYSTEM_INSTRUCTION)
                }
            )
        }

        private const val ARCHITECT_SYSTEM_INSTRUCTION =
            "You are AI Architect v2 for a training decision system. " +
                "Return only one valid JSON object, never markdown. " +
                "Keep user-facing text short and Ukrainian. " +
                "Explain trend, give 1-3 actionable suggestions, and mention recovery/readiness risk. " +
                "AI suggests only; deterministic System validation decides and may clamp or reject targets. " +
                "Do not write motivational essays, medical claims, or plan mutations."

        private const val LIVE_COACH_SYSTEM_INSTRUCTION =
            "You are a live AI training coach. Reply briefly, naturally, and in Ukrainian. " +
                "Do not use JSON or markdown. Do not change the plan or prescribe weights as final decisions."
    }
}
