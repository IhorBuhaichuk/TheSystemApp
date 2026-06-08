package com.ihor.thesystem.domain.model

import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import kotlin.math.round
import kotlin.math.roundToInt

enum class BossFightTargetMetric {
    REPS,
    WEIGHT,
    TIME_SECONDS,
    DISTANCE_METERS
}

enum class BossFightStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    LOCKED
}

data class BossFight(
    val exerciseId: Int,
    val title: String,
    val rankFrom: Rank,
    val rankTo: Rank,
    val targetMetric: BossFightTargetMetric,
    val targetValue: Double,
    val rulesText: String,
    val status: BossFightStatus
)

fun ProgressionMatrixEntry.toBossFight(
    status: BossFightStatus = BossFightStatus.ACTIVE
): BossFight {
    val exerciseTitle = exerciseNameUk ?: exerciseName
    val rankTo = RankProgressionPolicy.nextRank(currentRank)
    val trackingMode = ExerciseTrackingModeResolver.resolve(
        trackingModeOverride = exerciseTrackingMode,
        name = exerciseName,
        nameUk = exerciseNameUk
    )
    val target = resolveBossFightTarget(trackingMode)

    return BossFight(
        exerciseId = exerciseId,
        title = "Контрольний норматив: $exerciseTitle",
        rankFrom = currentRank,
        rankTo = rankTo,
        targetMetric = target.metric,
        targetValue = target.value,
        rulesText = target.rulesText,
        status = status
    )
}

fun DomainQuestStatus.toBossFightStatus(): BossFightStatus =
    when (this) {
        DomainQuestStatus.ACTIVE -> BossFightStatus.ACTIVE
        DomainQuestStatus.COMPLETED -> BossFightStatus.COMPLETED
        DomainQuestStatus.FAILED -> BossFightStatus.FAILED
        DomainQuestStatus.LOCKED -> BossFightStatus.LOCKED
    }

private fun ProgressionMatrixEntry.resolveBossFightTarget(
    trackingMode: ExerciseTrackingMode
): BossFightTarget =
    when (trackingMode) {
        ExerciseTrackingMode.WEIGHT_REPS -> {
            val targetLoad = nextRecommendedWeight
                ?: this.targetWeight
                    .takeIf { it > 0f }
                    ?.let { target -> (round((target * WEIGHT_TEST_MULTIPLIER) / WEIGHT_ROUND_STEP) * WEIGHT_ROUND_STEP).toDouble() }
                ?: currentWeight.toDouble().coerceAtLeast(0.0)
            BossFightTarget(
                metric = BossFightTargetMetric.WEIGHT,
                value = targetLoad,
                rulesText = "Умова: ${targetLoad.formatBossMetric()} кг x 1 чисте повторення"
            )
        }
        ExerciseTrackingMode.BODYWEIGHT_REPS -> {
            val reps = nextRecommendedReps.toPositiveIntOrNull()
                ?: DEFAULT_BODYWEIGHT_REPS
            BossFightTarget(
                metric = BossFightTargetMetric.REPS,
                value = reps.toDouble(),
                rulesText = "Умова: $reps чистих повторень"
            )
        }
        ExerciseTrackingMode.TIME_SECONDS,
        ExerciseTrackingMode.TIME_MINUTES -> {
            val rawValue = nextRecommendedReps.toPositiveIntOrNull()
                ?: DEFAULT_TIME_SECONDS
            val seconds = trackingMode.toStoredTimeSeconds(rawValue)
            BossFightTarget(
                metric = BossFightTargetMetric.TIME_SECONDS,
                value = seconds.toDouble(),
                rulesText = "Умова: ${seconds.formatTimeTarget()} чистого утримання"
            )
        }
    }

private data class BossFightTarget(
    val metric: BossFightTargetMetric,
    val value: Double,
    val rulesText: String
)

private fun String?.toPositiveIntOrNull(): Int? =
    this
        ?.split("-", " ")
        ?.firstNotNullOfOrNull { token -> token.trim().toIntOrNull()?.takeIf { it > 0 } }

private fun Double.formatBossMetric(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        (this * 10.0).roundToInt().let { rounded -> "${rounded / 10}.${rounded % 10}" }
    }

private fun Int.formatTimeTarget(): String =
    if (this >= SECONDS_IN_MINUTE && this % SECONDS_IN_MINUTE == 0) {
        "${this / SECONDS_IN_MINUTE} хв"
    } else {
        "$this сек"
    }

private const val WEIGHT_TEST_MULTIPLIER = 1.025f
private const val WEIGHT_ROUND_STEP = 2.5f
private const val DEFAULT_BODYWEIGHT_REPS = 10
private const val DEFAULT_TIME_SECONDS = 60
private const val SECONDS_IN_MINUTE = 60
