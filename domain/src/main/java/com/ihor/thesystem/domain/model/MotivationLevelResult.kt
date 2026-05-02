package com.ihor.thesystem.domain.model

data class MotivationLevelResult(
    val finalScore: Int,
    val level: MotivationLevel,
    val title: String,
    val description: String,
    val componentScores: MotivationComponentScores
)

data class MotivationComponentScores(
    val personalProgressScore: Int,
    val planProgressScore: Int,
    val consistencyScore: Int,
    val strengthBenchmarkScore: Int
)
