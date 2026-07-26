package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.BodyWeightLog
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.model.ProgressProof
import com.ihor.thesystem.domain.model.ProgressProofType
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.OneRepMaxCalculator
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class BuildProgressProofsUseCase @Inject constructor(
    private val clock: AppClock
) {
    fun relevantPeriodStartMillis(): Long =
        comparisonPeriod().previousStartMillis

    operator fun invoke(
        workoutLogs: List<WorkoutLog>,
        matrixEntries: List<ProgressionMatrixEntry>,
        bodyWeightHistory: List<BodyWeightLog> = emptyList()
    ): List<ProgressProof> {
        val matrixByExercise = matrixEntries.associateBy { it.exerciseId }
        val period = comparisonPeriod()
        val exerciseProofs = workoutLogs
            .flatMap { log ->
                log.sets
                    .filter { it.isCompleted && it.reps > 0 }
                    .map { set -> TimedSet(set = set, timestamp = log.session.timestamp) }
            }
            .groupBy { it.set.exerciseId }
            .mapNotNull { (exerciseId, sets) ->
                buildExerciseProof(
                    exerciseId = exerciseId,
                    sets = sets,
                    matrixEntry = matrixByExercise[exerciseId],
                    period = period
                )
            }

        val consistencyProof = buildConsistencyProof(workoutLogs, period)
        val bodyWeightProof = buildBodyWeightProof(bodyWeightHistory)

        return (exerciseProofs + listOfNotNull(consistencyProof, bodyWeightProof))
            .sortedWith(compareByDescending<ProgressProof> { abs(it.percentageChange) }.thenBy { it.exerciseName })
            .take(MAX_PROOFS)
    }

    private fun buildExerciseProof(
        exerciseId: Int,
        sets: List<TimedSet>,
        matrixEntry: ProgressionMatrixEntry?,
        period: ComparisonPeriod
    ): ProgressProof? {
        if (sets.size < MIN_SETS_FOR_PROOF) return null

        val trackingMode = resolveTrackingMode(matrixEntry, sets)
        val sortedSets = sets.sortedBy { it.timestamp }
        val previousPeriodBest = sortedSets
            .filter { it.timestamp in period.previousStartMillis until period.currentStartMillis }
            .bestForMode(trackingMode)
        val currentPeriodBest = sortedSets
            .filter { it.timestamp >= period.currentStartMillis }
            .bestForMode(trackingMode)
        val previous = previousPeriodBest ?: sortedSets.first().scoreForMode(trackingMode)
        val current = currentPeriodBest ?: sortedSets.last().scoreForMode(trackingMode)

        if (previous == null || current == null || previous.metric <= 0.0) return null

        val percentageChange = (((current.metric - previous.metric) / previous.metric) * 100.0).toFloat()
        if (percentageChange < MIN_POSITIVE_PERCENT_CHANGE) return null

        return ProgressProof(
            exerciseName = matrixEntry?.exerciseNameUk ?: matrixEntry?.exerciseName ?: "Exercise #$exerciseId",
            previousLabel = previous.label,
            currentLabel = current.label,
            deltaText = percentageChange.formatPercentDelta(),
            percentageChange = percentageChange,
            proofType = when (trackingMode) {
                ExerciseTrackingMode.WEIGHT_REPS -> ProgressProofType.STRENGTH
                ExerciseTrackingMode.BODYWEIGHT_REPS,
                ExerciseTrackingMode.TIME_SECONDS,
                ExerciseTrackingMode.TIME_MINUTES -> ProgressProofType.REPS
            }
        )
    }

    private fun buildConsistencyProof(
        logs: List<WorkoutLog>,
        period: ComparisonPeriod
    ): ProgressProof? {
        val previousCount = logs.count { it.session.timestamp in period.previousStartMillis until period.currentStartMillis }
        val currentCount = logs.count { it.session.timestamp >= period.currentStartMillis }
        if (currentCount <= previousCount || currentCount == 0) return null

        val percentageChange = if (previousCount > 0) {
            (((currentCount - previousCount).toFloat() / previousCount.toFloat()) * 100f)
        } else {
            100f
        }

        return ProgressProof(
            exerciseName = "Ритм тренувань",
            previousLabel = "$previousCount трен.",
            currentLabel = "$currentCount трен.",
            deltaText = percentageChange.formatPercentDelta(),
            percentageChange = percentageChange,
            proofType = ProgressProofType.CONSISTENCY
        )
    }

    private fun buildBodyWeightProof(history: List<BodyWeightLog>): ProgressProof? {
        val sorted = history
            .filter { it.weight > 0f }
            .sortedBy { it.timestamp }
        if (sorted.size < 2) return null

        val previous = sorted.first()
        val current = sorted.last()
        if (previous.weight <= 0f || previous.weight == current.weight) return null

        val deltaKg = current.weight - previous.weight
        val percentageChange = ((deltaKg / previous.weight) * 100f)
        if (abs(percentageChange) < MIN_BODY_WEIGHT_PERCENT_CHANGE) return null

        return ProgressProof(
            exerciseName = "Вага тіла",
            previousLabel = "${previous.weight.formatNumber()} кг",
            currentLabel = "${current.weight.formatNumber()} кг",
            deltaText = "${if (deltaKg > 0f) "+" else ""}${deltaKg.formatNumber()} кг",
            percentageChange = percentageChange,
            proofType = ProgressProofType.BODY_WEIGHT
        )
    }

    private fun resolveTrackingMode(
        matrixEntry: ProgressionMatrixEntry?,
        sets: List<TimedSet>
    ): ExerciseTrackingMode =
        matrixEntry?.let { entry ->
            ExerciseTrackingModeResolver.resolve(
                trackingModeOverride = entry.exerciseTrackingMode,
                name = entry.exerciseName,
                nameUk = entry.exerciseNameUk
            )
        } ?: if (sets.all { it.set.weight <= TECHNICAL_BODYWEIGHT_LOAD }) {
            ExerciseTrackingMode.BODYWEIGHT_REPS
        } else {
            ExerciseTrackingMode.WEIGHT_REPS
        }

    private fun List<TimedSet>.bestForMode(mode: ExerciseTrackingMode): SetScore? =
        mapNotNull { it.scoreForMode(mode) }
            .maxByOrNull { it.metric }

    private fun TimedSet.scoreForMode(mode: ExerciseTrackingMode): SetScore? {
        val exerciseSet = this.set
        return when (mode) {
            ExerciseTrackingMode.WEIGHT_REPS -> {
                if (exerciseSet.weight <= TECHNICAL_BODYWEIGHT_LOAD || exerciseSet.reps <= 0) return null
                SetScore(
                    metric = OneRepMaxCalculator.calculate(exerciseSet.weight, exerciseSet.reps),
                    label = "${exerciseSet.weight.formatNumber()} кг x ${exerciseSet.reps}"
                )
            }
            ExerciseTrackingMode.BODYWEIGHT_REPS -> {
                SetScore(
                    metric = exerciseSet.reps.toDouble(),
                    label = "${exerciseSet.reps} повт."
                )
            }
            ExerciseTrackingMode.TIME_SECONDS,
            ExerciseTrackingMode.TIME_MINUTES -> {
                SetScore(
                    metric = exerciseSet.reps.toDouble(),
                    label = exerciseSet.reps.formatSeconds()
                )
            }
        }
    }

    private fun comparisonPeriod(): ComparisonPeriod {
        val today = Instant.ofEpochMilli(clock.now()).atZone(clock.zoneId()).toLocalDate()
        val currentStart = today.minusDays(PROGRESS_PROOF_PERIOD_DAYS).startOfDayMillis()
        val previousStart = today.minusDays(PROGRESS_PROOF_PERIOD_DAYS * 2).startOfDayMillis()
        return ComparisonPeriod(
            previousStartMillis = previousStart,
            currentStartMillis = currentStart
        )
    }

    private fun LocalDate.startOfDayMillis(): Long =
        atStartOfDay(clock.zoneId()).toInstant().toEpochMilli()

    private fun Float.formatPercentDelta(): String =
        "${if (this >= 0f) "+" else ""}${this.formatNumber()}%"

    private fun Double.formatNumber(): String =
        if (this % 1.0 == 0.0) {
            toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", this)
        }

    private fun Float.formatNumber(): String =
        if (this % 1f == 0f) {
            toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", this)
        }

    private fun Int.formatSeconds(): String =
        if (this >= SECONDS_IN_MINUTE && this % SECONDS_IN_MINUTE == 0) {
            "${this / SECONDS_IN_MINUTE} хв"
        } else {
            "$this сек"
        }

    private data class TimedSet(
        val set: ExerciseSet,
        val timestamp: Long
    )

    private data class SetScore(
        val metric: Double,
        val label: String
    )

    private data class ComparisonPeriod(
        val previousStartMillis: Long,
        val currentStartMillis: Long
    )

    private companion object {
        const val MAX_PROOFS = 4
        const val MIN_SETS_FOR_PROOF = 2
        const val MIN_POSITIVE_PERCENT_CHANGE = 0.5f
        const val MIN_BODY_WEIGHT_PERCENT_CHANGE = 0.5f
        const val TECHNICAL_BODYWEIGHT_LOAD = 1.0
        const val SECONDS_IN_MINUTE = 60
    }
}

private const val PROGRESS_PROOF_PERIOD_DAYS = 28L
