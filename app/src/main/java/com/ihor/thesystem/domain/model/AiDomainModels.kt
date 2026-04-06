package com.ihor.thesystem.domain.model

import java.util.UUID

enum class ChatRole { SYSTEM, USER, AI }

data class AiWorkoutRecommendation(
    val exerciseId: Long, 
    val weight: Float, 
    val sets: Int, 
    val reps: String
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val text: String,
    val recommendations: List<AiWorkoutRecommendation> = emptyList(),
    val isActionable: Boolean = false
)

/**
 * Базові моделі тренувань, що використовуються для аналізу Архітектором.
 */
data class WorkoutSession(
    val sessionId: Long = 0L,
    val questId: Long,
    val timestamp: Long,
    val totalTonnage: Double,
    val cycleDay: Int
)

data class ExerciseSet(
    val setId: Long = 0L,
    val sessionId: Long,
    val exerciseId: String,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean
)

data class WorkoutDirective(
    val exerciseId: String,
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
    val completedExercises: List<String>,
    val pendingExercises: List<String>,
    val nextWorkoutDirectives: List<WorkoutDirective>,
    val recoveryWindowHours: Double,
    val isFallback: Boolean
)
