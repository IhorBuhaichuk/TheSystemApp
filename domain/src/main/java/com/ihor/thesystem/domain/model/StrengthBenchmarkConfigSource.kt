package com.ihor.thesystem.domain.model

import javax.inject.Inject

class StrengthBenchmarkConfigSource @Inject constructor() {
    fun getBenchmarks(): List<StrengthBenchmarkConfig> = listOf(
        StrengthBenchmarkConfig(
            exerciseCategory = ExerciseCategory.STRENGTH,
            ratioPoints = listOf(
                BenchmarkPoint(0.50, 30.0),
                BenchmarkPoint(0.75, 50.0),
                BenchmarkPoint(1.00, 70.0),
                BenchmarkPoint(1.25, 85.0),
                BenchmarkPoint(1.50, 100.0)
            )
        ),
        StrengthBenchmarkConfig(
            exerciseCategory = ExerciseCategory.HYPERTROPHY,
            ratioPoints = listOf(
                BenchmarkPoint(0.40, 30.0),
                BenchmarkPoint(0.60, 50.0),
                BenchmarkPoint(0.80, 70.0),
                BenchmarkPoint(1.00, 85.0),
                BenchmarkPoint(1.20, 100.0)
            )
        ),
        StrengthBenchmarkConfig(
            exerciseCategory = ExerciseCategory.ENDURANCE,
            ratioPoints = listOf(
                BenchmarkPoint(0.30, 30.0),
                BenchmarkPoint(0.45, 50.0),
                BenchmarkPoint(0.60, 70.0),
                BenchmarkPoint(0.80, 85.0),
                BenchmarkPoint(1.00, 100.0)
            )
        )
    )
}
