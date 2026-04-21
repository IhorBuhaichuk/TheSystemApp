package com.ihor.thesystem.core.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
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
        @Provides
        @Singleton
        @Named("ArchitectModel")
        fun provideArchitectGenerativeModel(): GenerativeModel {
            return GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = BuildConfig.GEMINI_API_KEY,
                requestOptions = RequestOptions(timeout = 60.seconds),
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                },
                systemInstruction = content {
                    text("Ти елітний ШІ-Аналітик фітнес-системи. Відповідай ВИКЛЮЧНО валідним JSON об'єктом наступної структури:\n{\n  \"feedback_text\": \"текст аналізу до 3 речень\",\n  \"next_workout_targets\": [\n    {\n      \"exercise_id\": 999,\n      \"nextWeight\": 0.0,\n      \"nextSets\": 0,\n      \"nextReps\": \"діапазон наприклад 8-10\",\n      \"aiFeedback\": \"коментар до вправи\"\n    }\n  ]\n}\n")
                }
            )
        }

        @Provides
        @Singleton
        @Named("LiveCoachModel")
        fun provideLiveCoachGenerativeModel(): GenerativeModel {
            return GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = BuildConfig.GEMINI_API_KEY,
                requestOptions = RequestOptions(timeout = 60.seconds),
                systemInstruction = content {
                    text("Ти 'ТРЕНЕР' - елітний живий ШІ-наставник. Спілкуйся природно, як людина. Відповідай коротко і по суті на питання гравця щодо поточного тренування, техніки чи болю. НЕ використовуй JSON та маркдаун.")
                }
            )
        }
    }
}
