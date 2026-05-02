package com.ihor.thesystem.domain.model

import kotlin.math.abs

data class MotivationLevelConfig(
    val scoringWeights: MotivationScoringWeights = MotivationScoringWeights(),
    val personalProgressScale: List<ScoreScalePoint> = defaultPersonalProgressScale,
    val planProgressScale: List<ScoreScalePoint> = defaultPlanProgressScale,
    val levelThresholds: List<MotivationLevelThreshold> = defaultLevelThresholds,
    val neutralScore: Double = 50.0
) {
    init {
        require(abs(scoringWeights.total - 1.0) <= WEIGHT_SUM_TOLERANCE) {
            "Motivation score weights must sum to 1.0"
        }
        require(personalProgressScale.size >= 2) {
            "Personal progress scale must contain at least two points"
        }
        require(planProgressScale.size >= 2) {
            "Plan progress scale must contain at least two points"
        }
        require(levelThresholds.isNotEmpty()) {
            "Motivation level thresholds must not be empty"
        }
        require(neutralScore in SCORE_MIN..SCORE_MAX) {
            "Neutral score must be in 0..100"
        }
    }
}

data class MotivationScoringWeights(
    val personalProgressWeight: Double = 0.35,
    val planProgressWeight: Double = 0.30,
    val consistencyWeight: Double = 0.20,
    val strengthBenchmarkWeight: Double = 0.15
) {
    val total: Double
        get() = personalProgressWeight +
            planProgressWeight +
            consistencyWeight +
            strengthBenchmarkWeight
}

data class ScoreScalePoint(
    val value: Double,
    val score: Double
)

data class MotivationLevelThreshold(
    val minScore: Int,
    val maxScore: Int,
    val level: MotivationLevel,
    val title: String,
    val description: String
)

private const val WEIGHT_SUM_TOLERANCE = 0.0001
private const val SCORE_MIN = 0.0
private const val SCORE_MAX = 100.0

val defaultPersonalProgressScale = listOf(
    ScoreScalePoint(value = 0.0, score = 30.0),
    ScoreScalePoint(value = 5.0, score = 45.0),
    ScoreScalePoint(value = 10.0, score = 60.0),
    ScoreScalePoint(value = 20.0, score = 80.0),
    ScoreScalePoint(value = 30.0, score = 100.0)
)

val defaultPlanProgressScale = listOf(
    ScoreScalePoint(value = 0.85, score = 30.0),
    ScoreScalePoint(value = 0.95, score = 60.0),
    ScoreScalePoint(value = 1.00, score = 85.0),
    ScoreScalePoint(value = 1.05, score = 95.0),
    ScoreScalePoint(value = 1.10, score = 100.0)
)

val defaultLevelThresholds = listOf(
    MotivationLevelThreshold(
        minScore = 0,
        maxScore = 39,
        level = MotivationLevel.BASIC,
        title = "Базовий",
        description = "Фундамент уже формується. Система фокусується на стабільності та безпечному прогресі."
    ),
    MotivationLevelThreshold(
        minScore = 40,
        maxScore = 64,
        level = MotivationLevel.STABLE,
        title = "Стабільний",
        description = "Є робочий ритм і помітна база. Наступний крок — утримувати план без різких стрибків."
    ),
    MotivationLevelThreshold(
        minScore = 65,
        maxScore = 84,
        level = MotivationLevel.ADVANCED,
        title = "Просунутий",
        description = "Прогрес добре тримається відносно плану. Варто берегти техніку і відновлення."
    ),
    MotivationLevelThreshold(
        minScore = 85,
        maxScore = 100,
        level = MotivationLevel.ATHLETIC,
        title = "Атлетичний",
        description = "Сила, стабільність і планова динаміка на високому рівні. Головне — не форсувати адаптацію."
    )
)
