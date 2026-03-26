package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.AiArchitectReport
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutSession

interface AiArchitectRepository {
    /**
     * Відправляє дані про тренування до штучного інтелекту для глибокого аналізу.
     * Якщо виникає помилка (немає інтернету або таймаут понад 10 секунд),
     * функція викидає помилку, яку система перехопить і запустить резервний план (Fallback).
     */
    suspend fun analyzeSession(
        session: WorkoutSession,
        sets: List<ExerciseSet>
    ): AiArchitectReport
}