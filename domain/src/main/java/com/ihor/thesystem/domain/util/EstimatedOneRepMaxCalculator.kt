package com.ihor.thesystem.domain.util

object EstimatedOneRepMaxCalculator {
    fun calculate(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0.0) return 0.0
        val boundedReps = reps.coerceIn(MIN_REPS, MAX_REPS)
        return weightKg * (1.0 + boundedReps / REP_DIVISOR)
    }
}

private const val MIN_REPS = 1
private const val MAX_REPS = 12
private const val REP_DIVISOR = 30.0
