package com.ihor.thesystem.domain.model

/**
 * Загальна інформація про проведене тренування.
 */
data class WorkoutSession(
    val sessionId: Long = 0L,
    val questId: Long,
    val timestamp: Long,
    val totalTonnage: Double,
    val cycleDay: Int
)

/**
 * Дані про конкретний виконаний підхід (сет) у межах тренування.
 */
data class ExerciseSet(
    val setId: Long = 0L,
    val sessionId: Long,
    val exerciseId: String,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean
)

/**
 * Вказівка (директива) для наступного тренування щодо конкретної вправи.
 */
data class WorkoutDirective(
    val exerciseId: String,
    val targetWeight: Double,
    val targetSets: Int,
    val targetReps: Int
)

/**
 * Комплексний звіт від AI Архітектора після аналізу тренування.
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