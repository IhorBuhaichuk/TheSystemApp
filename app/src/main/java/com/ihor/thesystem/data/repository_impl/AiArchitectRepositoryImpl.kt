package com.ihor.thesystem.data.repository_impl

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.domain.model.AiArchitectReport
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AiArchitectRepositoryImpl @Inject constructor() : AiArchitectRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("Ти — AI Архітектор Системи v1.0. Тон: сухий, аналітичний. Відповідай СТРОГО у форматі JSON відповідно до схеми. Без пояснень поза JSON.")
        }
    )

    override suspend fun analyzeSession(
        session: WorkoutSession,
        sets: List<ExerciseSet>
    ): AiArchitectReport {
        val prompt = buildString {
            appendLine("Аналіз тренування:")
            appendLine("День циклу: ${session.cycleDay}")
            appendLine("Загальний тоннаж: ${session.totalTonnage} кг")
            appendLine("Виконані підходи:")
            sets.forEach { set ->
                val status = if (set.isCompleted) "Виконано" else "Провалено"
                appendLine("- Вправа: ${set.exerciseId}, Вага: ${set.weight}кг, Повторення: ${set.reps} ($status)")
            }
            appendLine("Очікувана схема JSON: { \"architectFeedback\": \"...\", \"currentStageStatus\": \"...\", \"completedExercises\": [\"id1\"], \"pendingExercises\": [\"id2\"], \"nextWorkoutDirective\": [{ \"exerciseId\": \"...\", \"targetWeight\": 0.0, \"targetSets\": 0, \"targetReps\": 0 }] }")
        }

        var lastException: Exception? = null
        val maxRetries = 3
        
        // --- Механізм Exponential Backoff ---
        for (attempt in 1..maxRetries) {
            try {
                return withTimeout(15_000L) { // Збільшено таймаут для стабільності
                    val response = generativeModel.generateContent(prompt)
                    val responseText = response.text ?: throw IllegalStateException("Порожня відповідь від AI")
                    val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
                    val dto = json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)
                    dto.toDomain()
                }
            } catch (e: Exception) {
                lastException = e
                Log.e("AiArchitect", "Спроба $attempt не вдалася: ${e.message}")
                
                if (attempt < maxRetries) {
                    val waitTime = when(attempt) {
                        1 -> 1000L
                        2 -> 2000L
                        else -> 4000L
                    }
                    delay(waitTime)
                }
            }
        }

        // Якщо всі спроби вичерпано
        throw lastException ?: RuntimeException("Не вдалося отримати аналіз від ШІ")
    }

    private fun GeminiWorkoutResponseDto.toDomain(): AiArchitectReport {
        return AiArchitectReport(
            architectFeedback = this.architectFeedback,
            currentStageStatus = this.currentStageStatus,
            completedExercises = this.completedExercises,
            pendingExercises = this.pendingExercises,
            nextWorkoutDirectives = this.nextWorkoutDirective.map {
                WorkoutDirective(
                    exerciseId = it.exerciseId,
                    targetWeight = it.targetWeight,
                    targetSets = it.targetSets,
                    targetReps = it.targetReps
                )
            },
            recoveryWindowHours = 24.0,
            isFallback = false
        )
    }
}
