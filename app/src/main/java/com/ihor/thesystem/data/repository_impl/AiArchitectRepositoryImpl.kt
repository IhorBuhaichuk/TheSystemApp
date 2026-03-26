package com.ihor.thesystem.data.repository_impl

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.data.remote.dto.GeminiWorkoutResponseDto
import com.ihor.thesystem.domain.model.AiArchitectReport
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AiArchitectRepositoryImpl @Inject constructor() : AiArchitectRepository {

    // Налаштовуємо парсер JSON так, щоб він не падав, якщо AI додасть зайві поля
    private val json = Json { ignoreUnknownKeys = true }

    // Ініціалізація моделі Gemini з ключем доступу та суворими системними інструкціями
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
        // Жорсткий ліміт очікування: 10 секунд
        return withTimeout(10_000L) {

            // Формуємо звіт про тренування для AI
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

            // Відправляємо запит
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: throw IllegalStateException("Порожня відповідь від AI")

            // Очищаємо текст від можливих маркерів форматування (```json ... ```)
            val cleanJson = responseText.replace("```json", "").replace("```", "").trim()

            // Перетворюємо текст (JSON) у програмний об'єкт (DTO)
            val dto = json.decodeFromString<GeminiWorkoutResponseDto>(cleanJson)

            // Перетворюємо DTO у чисту доменну модель
            dto.toDomain()
        }
    }

    // =========================================
    // MAPPER ФУНКЦІЇ
    // =========================================
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
            recoveryWindowHours = 24.0, // Буде перезаписано правильним значенням у FinalizeSessionUseCase
            isFallback = false
        )
    }
}