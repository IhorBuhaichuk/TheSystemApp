package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ReadinessInput
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.ReadinessScore
import javax.inject.Inject

class CalculateReadinessUseCase @Inject constructor() {

    operator fun invoke(input: ReadinessInput): ReadinessScore {
        var score = BASE_SCORE
        val reasons = mutableListOf("Baseline score: $BASE_SCORE")

        input.sleepHours?.let { sleep ->
            when {
                sleep >= GOOD_SLEEP_HOURS -> {
                    score += GOOD_SLEEP_BONUS
                    reasons += "Sleep >= 7h: +$GOOD_SLEEP_BONUS"
                }
                sleep < POOR_SLEEP_HOURS -> {
                    score += POOR_SLEEP_PENALTY
                    reasons += "Sleep < 5h: $POOR_SLEEP_PENALTY"
                }
                else -> reasons += "Sleep 5-6.9h: +0"
            }
        }

        input.stress?.let { stress ->
            val penalty = highRatingPenalty(stress)
            if (penalty != 0) {
                score += penalty
                reasons += "Stress $stress/5: $penalty"
            }
        }

        input.soreness?.let { soreness ->
            val penalty = highRatingPenalty(soreness)
            if (penalty != 0) {
                score += penalty
                reasons += "Soreness $soreness/5: $penalty"
            }
        }

        input.energy?.let { energy ->
            when (energy) {
                4, 5 -> {
                    score += HIGH_ENERGY_BONUS
                    reasons += "Energy $energy/5: +$HIGH_ENERGY_BONUS"
                }
                1, 2 -> {
                    score += LOW_ENERGY_PENALTY
                    reasons += "Energy $energy/5: $LOW_ENERGY_PENALTY"
                }
            }
        }

        input.motivation?.let { motivation ->
            if (motivation <= LOW_MOTIVATION_THRESHOLD) {
                score += LOW_MOTIVATION_PENALTY
                reasons += "Motivation $motivation/5: $LOW_MOTIVATION_PENALTY"
            }
        }

        val clampedScore = score.coerceIn(MIN_SCORE, MAX_SCORE)
        return ReadinessScore(
            score = clampedScore,
            level = resolveLevel(clampedScore),
            reasons = reasons
        )
    }

    private fun highRatingPenalty(value: Int): Int =
        when (value) {
            4 -> HIGH_RATING_PENALTY
            5 -> VERY_HIGH_RATING_PENALTY
            else -> 0
        }

    private fun resolveLevel(score: Int): ReadinessLevel =
        when (score) {
            in 85..100 -> ReadinessLevel.PROGRESS
            in 65..84 -> ReadinessLevel.STANDARD
            in 45..64 -> ReadinessLevel.REDUCED
            else -> ReadinessLevel.RECOVERY
        }

    private companion object {
        const val BASE_SCORE = 70
        const val MIN_SCORE = 0
        const val MAX_SCORE = 100
        const val GOOD_SLEEP_HOURS = 7f
        const val POOR_SLEEP_HOURS = 5f
        const val GOOD_SLEEP_BONUS = 10
        const val POOR_SLEEP_PENALTY = -15
        const val HIGH_RATING_PENALTY = -10
        const val VERY_HIGH_RATING_PENALTY = -20
        const val HIGH_ENERGY_BONUS = 10
        const val LOW_ENERGY_PENALTY = -10
        const val LOW_MOTIVATION_THRESHOLD = 2
        const val LOW_MOTIVATION_PENALTY = -5
    }
}
