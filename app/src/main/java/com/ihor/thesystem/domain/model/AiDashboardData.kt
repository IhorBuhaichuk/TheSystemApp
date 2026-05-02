package com.ihor.thesystem.domain.model

data class AiDashboardData(
    val shortConclusion: String = "",
    val lastRecommendation: AiRecommendationSummary? = null
)

data class AiRecommendationSummary(
    val exerciseName: String,
    val recommendedWeight: Double?,
    val recommendedSets: Int?,
    val recommendedReps: String?,
    val feedback: String?,
    val analyzedTimestamp: Long
)
