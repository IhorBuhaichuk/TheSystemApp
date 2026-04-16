package com.ihor.thesystem.domain.model

import com.ihor.thesystem.core.ui.UiText
import java.util.UUID

enum class ChatRole { SYSTEM, USER, AI }

data class AiWorkoutRecommendation(
    val exerciseId: Int, 
    val weight: Float, 
    val sets: Int, 
    val reps: String,
    val aiFeedback: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val text: String = "",
    val uiText: UiText? = null,
    val recommendations: List<AiWorkoutRecommendation> = emptyList(),
    val isActionable: Boolean = false,
    val aiFeedback: String? = null
)

/**
 * Базові моделі тренувань, що використовуються для аналізу Архітектором та в аналітиці.
 */
data class WorkoutSession(
    val sessionId: Long = 0L,
    val questId: Long,
    val timestamp: Long,
    val totalTonnage: Double,
    val cycleDay: Int,
    val durationMinutes: Int = 0
)

data class ExerciseSet(
    val setId: Long = 0L,
    val sessionId: Long,
    val exerciseId: Int,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean,
    val userFeedback: String? = null
)

data class WorkoutLog(
    val session: WorkoutSession,
    val sets: List<ExerciseSet>
)

data class WeightHistoryEntry(
    val weight: Double,
    val timestamp: Long
)

data class WeightHistoryWithId(
    val weight: Double,
    val timestamp: Long,
    val exerciseId: Int
)

data class WorkoutDirective(
    val exerciseId: Int,
    val targetWeight: Double,
    val targetSets: Int,
    val targetReps: String
)

/**
 * Комплексний звіт від AI Архітектора після аналізу тренування.
 * (Зберігаємо для сумісності під час рефакторингу)
 */
data class AiArchitectReport(
    val architectFeedback: String,
    val currentStageStatus: String,
    val completedExercises: List<Int>,
    val pendingExercises: List<Int>,
    val nextWorkoutDirectives: List<WorkoutDirective>,
    val recoveryWindowHours: Double,
    val isFallback: Boolean
)
