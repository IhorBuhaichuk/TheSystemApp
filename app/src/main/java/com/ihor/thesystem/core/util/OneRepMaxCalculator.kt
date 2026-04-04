package com.ihor.thesystem.core.util

import kotlin.math.roundToInt

object OneRepMaxCalculator {
    /**
     * Формула Еплі: 1RM = Weight * (1 + Reps / 30.0)
     */
    fun calculate(weight: Double, reps: Int): Double {
        if (reps <= 0) return weight
        return weight * (1.0 + reps / 30.0)
    }

    /**
     * Знаходить максимальний 1RM серед списку підходів
     */
    fun calculateMax(sets: List<Pair<Double, Int>>): Double {
        if (sets.isEmpty()) return 0.0
        return sets.maxOf { (weight, reps) -> calculate(weight, reps) }
    }
    
    fun format(value: Double): String {
        return "${value.roundToInt()} кг"
    }
}
