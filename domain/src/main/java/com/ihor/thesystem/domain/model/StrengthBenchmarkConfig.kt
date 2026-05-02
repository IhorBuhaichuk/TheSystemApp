package com.ihor.thesystem.domain.model

data class StrengthBenchmarkConfig(
    val exerciseId: Int? = null,
    val exerciseCategory: ExerciseCategory? = null,
    val ratioPoints: List<BenchmarkPoint>
) {
    init {
        require(exerciseId != null || exerciseCategory != null) {
            "Strength benchmark must target exerciseId or exerciseCategory"
        }
        require(ratioPoints.size >= 2) {
            "Strength benchmark ratio points must contain at least two points"
        }
    }
}

data class BenchmarkPoint(
    val ratioToBodyWeight: Double,
    val score: Double
)
