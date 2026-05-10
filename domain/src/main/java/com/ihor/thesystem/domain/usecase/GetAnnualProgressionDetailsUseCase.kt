package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.model.AnnualProgressionDetailStatus
import com.ihor.thesystem.domain.model.AnnualProgressionDetailsData
import com.ihor.thesystem.domain.model.AnnualProgressionExerciseDetails
import com.ihor.thesystem.domain.model.AnnualProgressionMonthlyProgress
import com.ihor.thesystem.domain.model.WeightHistoryWithId
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AnnualProgressionPlanNoteParser
import com.ihor.thesystem.domain.util.ParsedAnnualProgressionPlanNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class GetAnnualProgressionDetailsUseCase @Inject constructor(
    private val progressionMatrixRepository: ProgressionMatrixRepository,
    private val workoutAnalyticsRepository: WorkoutAnalyticsRepository,
    private val clock: AppClock
) {
    operator fun invoke(): Flow<AnnualProgressionDetailsData> =
        combine(
            progressionMatrixRepository.getAllEntries(),
            workoutAnalyticsRepository.getAllWeightHistories()
        ) { entries, histories ->
            val historiesByExercise = histories.groupBy { it.exerciseId }
            AnnualProgressionDetailsData(
                exercises = entries
                    .mapNotNull { entry ->
                        val parsedPlan = AnnualProgressionPlanNoteParser.parse(entry.targetWeightNote)
                            ?: return@mapNotNull null
                        entry.toDetails(
                            parsedPlan = parsedPlan,
                            histories = historiesByExercise[entry.exerciseId].orEmpty()
                        )
                    }
                    .sortedBy { it.exerciseName }
            )
        }

    private fun ProgressionMatrixEntry.toDetails(
        parsedPlan: ParsedAnnualProgressionPlanNote,
        histories: List<WeightHistoryWithId>
    ): AnnualProgressionExerciseDetails {
        val today = Instant.ofEpochMilli(clock.now()).atZone(clock.zoneId()).toLocalDate()
        val currentMonthIndex = monthsBetweenPlanStartAndDate(parsedPlan.startDate, today)
            .coerceIn(0, ANNUAL_MONTHS)
        val sortedHistory = histories.sortedBy { it.timestamp }

        val monthlyProgress = parsedPlan.monthlyTargets
            .filter { it.monthIndex in 0..ANNUAL_MONTHS }
            .map { target ->
                val actualWeight = actualWeightForMonth(
                    monthIndex = target.monthIndex,
                    startDate = parsedPlan.startDate,
                    today = today,
                    history = sortedHistory,
                    baselineWeight = startWeight.toDouble()
                )
                AnnualProgressionMonthlyProgress(
                    monthIndex = target.monthIndex,
                    planWeight = target.weight,
                    actualWeight = actualWeight,
                    status = resolveStatus(actualWeight = actualWeight, planWeight = target.weight)
                )
            }

        val currentStatus = monthlyProgress
            .filter { it.monthIndex <= currentMonthIndex }
            .lastOrNull { it.actualWeight != null }
            ?.status
            ?: AnnualProgressionDetailStatus.NoFact

        return AnnualProgressionExerciseDetails(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            startDate = parsedPlan.startDate,
            adaptationEndDate = parsedPlan.adaptationEndDate,
            inventoryStep = parsedPlan.inventoryStep,
            monthlyProgress = monthlyProgress,
            currentStatus = currentStatus,
            recommendation = lastAiFeedback?.takeIf { it.isNotBlank() }
        )
    }

    private fun actualWeightForMonth(
        monthIndex: Int,
        startDate: LocalDate,
        today: LocalDate,
        history: List<WeightHistoryWithId>,
        baselineWeight: Double
    ): Double? {
        val targetDate = startDate.plusMonths(monthIndex.toLong())
        if (monthIndex > 0 && targetDate > today) return null
        if (monthIndex == 0) {
            return history.latestWeightAtOrBefore(targetDate) ?: baselineWeight.takeIf { it > 0.0 }
        }
        val effectiveDate = minOf(targetDate, today)
        return history.latestWeightAtOrBefore(effectiveDate)
    }

    private fun List<WeightHistoryWithId>.latestWeightAtOrBefore(date: LocalDate): Double? =
        lastOrNull { entry ->
            Instant.ofEpochMilli(entry.timestamp).atZone(clock.zoneId()).toLocalDate() <= date
        }?.weight

    private fun resolveStatus(
        actualWeight: Double?,
        planWeight: Double
    ): AnnualProgressionDetailStatus {
        if (actualWeight == null || planWeight <= 0.0) return AnnualProgressionDetailStatus.NoFact
        val ratio = actualWeight / planWeight
        return when {
            ratio > ABOVE_PLAN_RATIO -> AnnualProgressionDetailStatus.AbovePlan
            ratio >= ON_PLAN_RATIO -> AnnualProgressionDetailStatus.OnPlan
            else -> AnnualProgressionDetailStatus.SlightlyBelow
        }
    }

    private fun monthsBetweenPlanStartAndDate(
        startDate: LocalDate,
        date: LocalDate
    ): Int {
        val yearDelta = date.year - startDate.year
        val monthDelta = date.monthValue - startDate.monthValue
        return yearDelta * 12 + monthDelta
    }
}

private const val ANNUAL_MONTHS = 12
private const val ON_PLAN_RATIO = 0.9
private const val ABOVE_PLAN_RATIO = 1.02
