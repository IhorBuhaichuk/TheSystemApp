package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.AnnualProgressComparison
import com.ihor.thesystem.domain.model.AnnualProgressStatus
import com.ihor.thesystem.domain.model.ExerciseProgressAnalysis
import com.ihor.thesystem.domain.model.ExerciseProgressStatus
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.MotivationLevelConfig
import com.ihor.thesystem.domain.model.NextWorkoutRecommendationAnalysis
import com.ihor.thesystem.domain.model.StrengthBenchmarkConfigSource
import com.ihor.thesystem.domain.model.WorkoutAnalysisData
import com.ihor.thesystem.domain.model.WorkoutExecutionAnalysis
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AnnualProgressionPlanNoteParser
import com.ihor.thesystem.domain.util.EstimatedOneRepMaxCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class GetWorkoutAnalysisUseCase @Inject constructor(
    private val analyticsRepository: WorkoutAnalyticsRepository,
    private val scheduleRepository: ScheduleRepository,
    private val progressionMatrixRepository: ProgressionMatrixRepository,
    private val playerRepository: PlayerRepository,
    private val strengthBenchmarkConfigSource: StrengthBenchmarkConfigSource,
    private val getSystemConfig: GetSystemConfigUseCase,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val calculateRecommendation: CalculateRecommendedSetUseCase,
    private val calculateMotivationLevel: CalculateMotivationLevelUseCase,
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase,
    private val clock: AppClock
) {
    suspend operator fun invoke(): WorkoutAnalysisData? {
        val allLogs = analyticsRepository.getAllLogs().firstOrNull().orEmpty()
        val mostRecent = allLogs.firstOrNull() ?: return null
        val sameDayLogs = analyticsRepository.getSessionsByDate(mostRecent.session.timestamp)
            .firstOrNull()
            .orEmpty()
            .ifEmpty { listOf(mostRecent) }
        val exerciseNames = analyticsRepository.getAllExercisesMap()
        val matrixEntries = progressionMatrixRepository.getAllEntries().first()
        val matrixByExercise = matrixEntries.associateBy { it.exerciseId }
        val latestSession = sameDayLogs.maxBy { it.session.timestamp }
        val latestDate = latestSession.session.timestamp.toLocalDate()
        val trainingPhaseContext = getTrainingPhaseContext(
            referenceTimestamp = latestSession.session.timestamp
        )
        val schedule = scheduleRepository.getScheduleForDay(latestSession.session.cycleDay).firstOrNull()
        val config = getSystemConfig().firstOrNull()

        val completedSets = sameDayLogs.flatMap { it.sets }.filter { it.isCompleted && it.weight > 0.0 && it.reps > 0 }
        if (completedSets.isEmpty()) return null

        val plannedExerciseIds = schedule?.exercises?.map { it.id }.orEmpty()
        val completedExerciseIds = completedSets.map { it.exerciseId }.toSet()
        val plannedSets = plannedExerciseIds.size * (config?.targetSets ?: DEFAULT_TARGET_SETS)

        val execution = WorkoutExecutionAnalysis(
            completedSets = completedSets.size,
            plannedSets = plannedSets,
            completedExercises = if (plannedExerciseIds.isEmpty()) {
                completedExerciseIds.size
            } else {
                completedExerciseIds.count { it in plannedExerciseIds }
            },
            skippedExercises = plannedExerciseIds.count { it !in completedExerciseIds }
        )

        val previousLogs = allLogs
            .filter { it.session.timestamp < sameDayLogs.minOf { log -> log.session.timestamp } }

        val progress = completedSets.groupBy { it.exerciseId }.map { (exerciseId, sets) ->
            val current = sets.maxEstimatedOneRepMax()
            val previous = previousLogs
                .flatMap { it.sets }
                .filter { it.exerciseId == exerciseId && it.isCompleted && it.weight > 0.0 && it.reps > 0 }
                .maxEstimatedOneRepMaxOrNull()
            ExerciseProgressAnalysis(
                exerciseId = exerciseId,
                exerciseName = exerciseNames[exerciseId] ?: "Вправа #$exerciseId",
                previousEstimatedOneRepMax = previous,
                currentEstimatedOneRepMax = current,
                difference = previous?.let { current - it },
                status = resolveExerciseProgressStatus(current = current, previous = previous)
            )
        }

        val annualProgress = completedSets.groupBy { it.exerciseId }.map { (exerciseId, sets) ->
            val currentBestSet = sets.maxBy { EstimatedOneRepMaxCalculator.calculate(it.weight, it.reps) }
            val plannedWeight = matrixByExercise[exerciseId]
                ?.plannedWeightForDate(latestDate)
                ?.takeIf { it > 0.0 }
            val difference = plannedWeight?.let { currentBestSet.weight - it }
            AnnualProgressComparison(
                exerciseId = exerciseId,
                exerciseName = exerciseNames[exerciseId] ?: "Вправа #$exerciseId",
                factWeight = currentBestSet.weight,
                plannedWeight = plannedWeight,
                difference = difference,
                status = resolveAnnualProgressStatus(fact = currentBestSet.weight, planned = plannedWeight)
            )
        }

        val recommendations = completedSets.map { it.exerciseId }.distinct().map { exerciseId ->
            val exerciseName = exerciseNames[exerciseId] ?: "Вправа #$exerciseId"
            val recommendation = calculateRecommendation(exerciseId, exerciseName)
            NextWorkoutRecommendationAnalysis(
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                recommendedWeight = recommendation.weight,
                recommendedReps = recommendation.reps.toString(),
                recommendedSets = recommendation.sets,
                reason = if (recommendation.isProgression) {
                    "Попередні робочі підходи виконані в цільовому діапазоні, тому система пропонує обережний крок вгору."
                } else {
                    "Система утримує навантаження і підтягує повторення до стабільного робочого діапазону."
                }
            )
        }

        val bodyWeight = playerRepository.getLatestWeight().firstOrNull()?.toDouble()
        val plannedWorkouts = countPlannedWorkoutsInLastWindow(latestDate)
        val completedWorkoutDates = allLogs
            .map { it.session.timestamp.toLocalDate() }
            .filter { date -> date in latestDate.minusDays(CONSISTENCY_WINDOW_DAYS - 1)..latestDate }
            .toSet()
            .size
        val motivationAnchor = progress.maxBy { it.currentEstimatedOneRepMax }
        val motivationSets = completedSets.filter { it.exerciseId == motivationAnchor.exerciseId }
        val motivationTopSet = motivationSets.maxBy { EstimatedOneRepMaxCalculator.calculate(it.weight, it.reps) }
        val motivationBaseline = allLogs
            .flatMap { it.sets }
            .filter { it.exerciseId == motivationAnchor.exerciseId && it.isCompleted && it.weight > 0.0 && it.reps > 0 }
            .minByOrNull { it.setId }
            ?.let { EstimatedOneRepMaxCalculator.calculate(it.weight, it.reps) }
        val motivationPlannedWeight = matrixByExercise[motivationAnchor.exerciseId]
            ?.plannedWeightForDate(latestDate)
        val motivationPlannedEstimated = motivationPlannedWeight?.let {
            EstimatedOneRepMaxCalculator.calculate(it, motivationTopSet.reps)
        }
        val motivationExerciseCategory = schedule
            ?.exercises
            ?.firstOrNull { it.id == motivationAnchor.exerciseId }
            ?.category

        val motivationLevel = calculateMotivationLevel(
            input = MotivationLevelInput(
                exerciseId = motivationAnchor.exerciseId,
                exerciseCategory = motivationExerciseCategory,
                currentWeightKg = motivationTopSet.weight,
                currentReps = motivationTopSet.reps,
                baselineEstimated1RM = motivationBaseline,
                plannedEstimated1RM = motivationPlannedEstimated,
                completedWorkouts = completedWorkoutDates,
                plannedWorkouts = plannedWorkouts,
                bodyWeightKg = bodyWeight
            ),
            config = MotivationLevelConfig(),
            strengthBenchmarks = strengthBenchmarkConfigSource.getBenchmarks()
        )

        return WorkoutAnalysisData(
            sessionTimestamp = latestSession.session.timestamp,
            workoutName = schedule?.workoutTemplateName ?: "День ${latestSession.session.cycleDay}",
            execution = execution,
            exerciseProgress = progress,
            annualProgress = annualProgress,
            recommendations = recommendations,
            motivationLevel = motivationLevel,
            aiFeedback = matrixEntries
                .mapNotNull { it.lastAiFeedback }
                .firstOrNull(),
            isInitialDataCollection = trainingPhaseContext.isInitialDataCollection,
            adaptationRemainingDays = trainingPhaseContext.remainingAdaptationDays
        )
    }

    private suspend fun countPlannedWorkoutsInLastWindow(referenceDate: LocalDate): Int {
        val config = getSystemConfig().firstOrNull() ?: return 0
        val fallbackCurrentCycleDay = playerRepository.getPlayer().firstOrNull()?.currentCycleDay

        val days = (0L until CONSISTENCY_WINDOW_DAYS).map { offset ->
            val date = referenceDate.minusDays(offset)
            resolveTrainingCycleDay(
                targetDate = date,
                config = config,
                fallbackCurrentCycleDay = fallbackCurrentCycleDay
            )
        }.distinct()

        val workoutDays = scheduleRepository.getSchedulesForDays(days).firstOrNull().orEmpty()
            .filter { it.isWorkoutDay && it.exercises.isNotEmpty() }
            .map { it.cycleDay }
            .toSet()

        return (0L until CONSISTENCY_WINDOW_DAYS).count { offset ->
            val date = referenceDate.minusDays(offset)
            val cycleDay = resolveTrainingCycleDay(
                targetDate = date,
                config = config,
                fallbackCurrentCycleDay = fallbackCurrentCycleDay
            )
            cycleDay in workoutDays
        }
    }

    private fun List<ExerciseSet>.maxEstimatedOneRepMax(): Double =
        maxOf { EstimatedOneRepMaxCalculator.calculate(it.weight, it.reps) }

    private fun List<ExerciseSet>.maxEstimatedOneRepMaxOrNull(): Double? =
        takeIf { it.isNotEmpty() }?.maxOf { EstimatedOneRepMaxCalculator.calculate(it.weight, it.reps) }

    private fun resolveExerciseProgressStatus(
        current: Double,
        previous: Double?
    ): ExerciseProgressStatus =
        when {
            previous == null -> ExerciseProgressStatus.Stable
            current > previous + PROGRESS_EPSILON -> ExerciseProgressStatus.Improved
            current < previous - PROGRESS_EPSILON -> ExerciseProgressStatus.Decreased
            else -> ExerciseProgressStatus.Stable
        }

    private fun resolveAnnualProgressStatus(
        fact: Double,
        planned: Double?
    ): AnnualProgressStatus {
        if (planned == null || planned <= 0.0) return AnnualProgressStatus.NoPlan
        val ratio = fact / planned
        return when {
            ratio >= ABOVE_PLAN_RATIO -> AnnualProgressStatus.AbovePlan
            ratio >= ON_PLAN_RATIO -> AnnualProgressStatus.OnPlan
            else -> AnnualProgressStatus.BelowPlan
        }
    }

    private fun ProgressionMatrixEntry.plannedWeightForDate(date: LocalDate): Double? {
        val annualTargets = targetWeightNote.parseAnnualTargets(date)
        if (annualTargets != null) return annualTargets
        return targetWeight.takeIf { it > 0f }?.toDouble()
    }

    private fun String?.parseAnnualTargets(date: LocalDate): Double? {
        val parsedPlan = AnnualProgressionPlanNoteParser.parse(this) ?: return null
        val monthIndex = java.time.temporal.ChronoUnit.MONTHS.between(
            parsedPlan.startDate.withDayOfMonth(1),
            date.withDayOfMonth(1)
        )
            .toInt()
            .coerceIn(0, 12)
        return parsedPlan.monthlyTargets.firstOrNull { it.monthIndex == monthIndex }?.weight
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(clock.zoneId()).toLocalDate()
}

private const val DEFAULT_TARGET_SETS = 3
private const val CONSISTENCY_WINDOW_DAYS = 28L
private const val PROGRESS_EPSILON = 0.25
private const val ON_PLAN_RATIO = 0.95
private const val ABOVE_PLAN_RATIO = 1.05
