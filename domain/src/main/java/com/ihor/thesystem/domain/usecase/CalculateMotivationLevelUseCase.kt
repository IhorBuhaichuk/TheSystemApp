package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BenchmarkPoint
import com.ihor.thesystem.domain.model.ExerciseCategory
import com.ihor.thesystem.domain.model.MotivationComponentScores
import com.ihor.thesystem.domain.model.MotivationLevelConfig
import com.ihor.thesystem.domain.model.MotivationLevelResult
import com.ihor.thesystem.domain.model.ScoreScalePoint
import com.ihor.thesystem.domain.model.StrengthBenchmarkConfig
import com.ihor.thesystem.domain.util.EstimatedOneRepMaxCalculator
import com.ihor.thesystem.domain.util.LinearInterpolator
import javax.inject.Inject
import kotlin.math.roundToInt

data class MotivationLevelInput(
    val exerciseId: Int? = null,
    val exerciseCategory: ExerciseCategory? = null,
    val currentWeightKg: Double,
    val currentReps: Int,
    val baselineEstimated1RM: Double? = null,
    val plannedEstimated1RM: Double? = null,
    val completedWorkouts: Int,
    val plannedWorkouts: Int,
    val bodyWeightKg: Double? = null
)

class CalculateMotivationLevelUseCase @Inject constructor() {
    operator fun invoke(
        input: MotivationLevelInput,
        config: MotivationLevelConfig = MotivationLevelConfig(),
        strengthBenchmarks: List<StrengthBenchmarkConfig> = emptyList()
    ): MotivationLevelResult {
        val neutralScore = config.neutralScore.clampScore()
        val currentEstimated1RM = EstimatedOneRepMaxCalculator.calculate(
            weightKg = input.currentWeightKg,
            reps = input.currentReps
        )

        val personalProgressScore = calculatePersonalProgressScore(
            currentEstimated1RM = currentEstimated1RM,
            baselineEstimated1RM = input.baselineEstimated1RM,
            scale = config.personalProgressScale,
            neutralScore = neutralScore
        )
        val planProgressScore = calculatePlanProgressScore(
            currentEstimated1RM = currentEstimated1RM,
            plannedEstimated1RM = input.plannedEstimated1RM,
            scale = config.planProgressScale,
            neutralScore = neutralScore
        )
        val consistencyScore = calculateConsistencyScore(
            completedWorkouts = input.completedWorkouts,
            plannedWorkouts = input.plannedWorkouts,
            neutralScore = neutralScore
        )
        val strengthBenchmarkScore = calculateStrengthBenchmarkScore(
            currentEstimated1RM = currentEstimated1RM,
            bodyWeightKg = input.bodyWeightKg,
            exerciseId = input.exerciseId,
            exerciseCategory = input.exerciseCategory,
            benchmarks = strengthBenchmarks,
            neutralScore = neutralScore
        )

        val weights = config.scoringWeights
        val finalScore = (
            personalProgressScore * weights.personalProgressWeight +
                planProgressScore * weights.planProgressWeight +
                consistencyScore * weights.consistencyWeight +
                strengthBenchmarkScore * weights.strengthBenchmarkWeight
            )
            .roundToInt()
            .coerceIn(SCORE_MIN_INT, SCORE_MAX_INT)

        val levelThreshold = config.levelThresholds
            .firstOrNull { finalScore in it.minScore..it.maxScore }
            ?: config.levelThresholds.maxBy { it.maxScore }

        return MotivationLevelResult(
            finalScore = finalScore,
            level = levelThreshold.level,
            title = levelThreshold.title,
            description = levelThreshold.description,
            componentScores = MotivationComponentScores(
                personalProgressScore = personalProgressScore.roundScore(),
                planProgressScore = planProgressScore.roundScore(),
                consistencyScore = consistencyScore.roundScore(),
                strengthBenchmarkScore = strengthBenchmarkScore.roundScore()
            )
        )
    }

    private fun calculatePersonalProgressScore(
        currentEstimated1RM: Double,
        baselineEstimated1RM: Double?,
        scale: List<ScoreScalePoint>,
        neutralScore: Double
    ): Double {
        if (currentEstimated1RM <= 0.0 || baselineEstimated1RM == null || baselineEstimated1RM <= 0.0) {
            return neutralScore
        }

        val personalProgressPercent = ((currentEstimated1RM - baselineEstimated1RM) / baselineEstimated1RM) * 100.0
        return interpolateScale(personalProgressPercent, scale)
    }

    private fun calculatePlanProgressScore(
        currentEstimated1RM: Double,
        plannedEstimated1RM: Double?,
        scale: List<ScoreScalePoint>,
        neutralScore: Double
    ): Double {
        if (currentEstimated1RM <= 0.0 || plannedEstimated1RM == null || plannedEstimated1RM <= 0.0) {
            return neutralScore
        }

        val planRatio = currentEstimated1RM / plannedEstimated1RM
        return interpolateScale(planRatio, scale)
    }

    private fun calculateConsistencyScore(
        completedWorkouts: Int,
        plannedWorkouts: Int,
        neutralScore: Double
    ): Double {
        if (plannedWorkouts <= 0) return neutralScore

        val consistencyRate = completedWorkouts.toDouble() / plannedWorkouts.toDouble()
        return (consistencyRate * 100.0).clampScore()
    }

    private fun calculateStrengthBenchmarkScore(
        currentEstimated1RM: Double,
        bodyWeightKg: Double?,
        exerciseId: Int?,
        exerciseCategory: ExerciseCategory?,
        benchmarks: List<StrengthBenchmarkConfig>,
        neutralScore: Double
    ): Double {
        if (currentEstimated1RM <= 0.0 || bodyWeightKg == null || bodyWeightKg <= 0.0) {
            return neutralScore
        }

        val benchmark = benchmarks.firstOrNull { it.exerciseId != null && it.exerciseId == exerciseId }
            ?: benchmarks.firstOrNull {
                it.exerciseId == null &&
                    it.exerciseCategory != null &&
                    it.exerciseCategory == exerciseCategory
            }
            ?: return neutralScore

        val strengthRatio = currentEstimated1RM / bodyWeightKg
        return LinearInterpolator.interpolate(
            value = strengthRatio,
            points = benchmark.ratioPoints.toInterpolationPoints()
        ).clampScore()
    }

    private fun interpolateScale(value: Double, points: List<ScoreScalePoint>): Double =
        LinearInterpolator.interpolate(
            value = value,
            points = points.map { it.value to it.score }
        ).clampScore()

    private fun List<BenchmarkPoint>.toInterpolationPoints(): List<Pair<Double, Double>> =
        map { it.ratioToBodyWeight to it.score }

    private fun Double.clampScore(): Double =
        coerceIn(SCORE_MIN, SCORE_MAX)

    private fun Double.roundScore(): Int =
        roundToInt().coerceIn(SCORE_MIN_INT, SCORE_MAX_INT)
}

private const val SCORE_MIN = 0.0
private const val SCORE_MAX = 100.0
private const val SCORE_MIN_INT = 0
private const val SCORE_MAX_INT = 100
