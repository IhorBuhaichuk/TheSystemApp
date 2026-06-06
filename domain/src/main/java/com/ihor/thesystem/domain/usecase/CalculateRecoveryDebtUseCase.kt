package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ReadinessScore
import com.ihor.thesystem.domain.model.RecoveryDebt
import com.ihor.thesystem.domain.model.RecoveryDebtInput
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import kotlin.math.roundToInt
import javax.inject.Inject

class CalculateRecoveryDebtUseCase @Inject constructor() {

    operator fun invoke(input: RecoveryDebtInput): RecoveryDebt {
        var value = 0
        val reasons = mutableListOf<String>()

        val completedWorkouts = input.recentWorkouts.filter { it.completed }
        val totalTonnage = completedWorkouts.sumOf { it.tonnage.coerceAtLeast(0.0) }
        val loadDebt = ((totalTonnage / TONNAGE_UNIT_KG) * LOAD_POINTS_PER_TONNE)
            .roundToInt()
            .coerceAtMost(MAX_LOAD_DEBT)
        if (loadDebt > 0) {
            value += loadDebt
            reasons += "Recent tonnage ${totalTonnage.roundToInt()} kg: +$loadDebt"
        }

        val densityDebt = (completedWorkouts.size * POINTS_PER_COMPLETED_WORKOUT)
            .coerceAtMost(MAX_DENSITY_DEBT)
        if (densityDebt > 0) {
            value += densityDebt
            reasons += "Completed recent workouts ${completedWorkouts.size}: +$densityDebt"
        }

        val missedWorkoutCount = missedWorkoutCount(input)
        val missedDebt = (missedWorkoutCount * POINTS_PER_MISSED_WORKOUT)
            .coerceAtMost(MAX_MISSED_DEBT)
        if (missedDebt > 0) {
            value += missedDebt
            reasons += "Missed planned workouts $missedWorkoutCount: +$missedDebt"
        }

        input.readiness?.let { readiness ->
            val readinessDebt = readiness.debtPoints()
            if (readinessDebt > 0) {
                value += readinessDebt
                reasons += "Readiness score ${readiness.score}: +$readinessDebt"
            }
        }

        val stressDebt = input.readinessInput?.stress.debtFromStressOrSoreness()
        if (stressDebt > 0) {
            value += stressDebt
            reasons += "Stress ${input.readinessInput?.stress}/5: +$stressDebt"
        }

        val sorenessDebt = input.readinessInput?.soreness.debtFromStressOrSoreness()
        if (sorenessDebt > 0) {
            value += sorenessDebt
            reasons += "Soreness ${input.readinessInput?.soreness}/5: +$sorenessDebt"
        }

        val clampedValue = value.coerceIn(MIN_DEBT, MAX_DEBT)
        return RecoveryDebt(
            value = clampedValue,
            level = resolveLevel(clampedValue),
            reasons = reasons
        )
    }

    private fun missedWorkoutCount(input: RecoveryDebtInput): Int {
        val referenceEpochDay = input.referenceEpochDay
            ?: input.plannedWorkoutEpochDays.maxOrNull()
            ?: input.recentWorkouts.maxOfOrNull { it.dateEpochDay }
            ?: return 0
        val completedDays = input.recentWorkouts
            .filter { it.completed }
            .map { it.dateEpochDay }
            .toSet()

        return input.plannedWorkoutEpochDays
            .distinct()
            .count { plannedDay -> plannedDay <= referenceEpochDay && plannedDay !in completedDays }
    }

    private fun ReadinessScore.debtPoints(): Int =
        when (score) {
            in 0..44 -> CRITICAL_READINESS_DEBT
            in 45..64 -> REDUCED_READINESS_DEBT
            in 65..84 -> STANDARD_READINESS_DEBT
            else -> 0
        }

    private fun Int?.debtFromStressOrSoreness(): Int =
        when (this) {
            4 -> HIGH_STRESS_OR_SORENESS_DEBT
            5 -> VERY_HIGH_STRESS_OR_SORENESS_DEBT
            else -> 0
        }

    private fun resolveLevel(value: Int): RecoveryDebtLevel =
        when (value) {
            in 0..24 -> RecoveryDebtLevel.LOW
            in 25..49 -> RecoveryDebtLevel.MODERATE
            in 50..74 -> RecoveryDebtLevel.HIGH
            else -> RecoveryDebtLevel.CRITICAL
        }

    private companion object {
        const val MIN_DEBT = 0
        const val MAX_DEBT = 100
        const val TONNAGE_UNIT_KG = 1_000.0
        const val LOAD_POINTS_PER_TONNE = 2
        const val MAX_LOAD_DEBT = 30
        const val POINTS_PER_COMPLETED_WORKOUT = 4
        const val MAX_DENSITY_DEBT = 16
        const val POINTS_PER_MISSED_WORKOUT = 12
        const val MAX_MISSED_DEBT = 36
        const val STANDARD_READINESS_DEBT = 5
        const val REDUCED_READINESS_DEBT = 15
        const val CRITICAL_READINESS_DEBT = 25
        const val HIGH_STRESS_OR_SORENESS_DEBT = 8
        const val VERY_HIGH_STRESS_OR_SORENESS_DEBT = 15
    }
}
