package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class GetStatisticsDataUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val viewingDateRepo: ViewingDateRepository,
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val readinessRepo: ReadinessRepository,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val buildProgressProofs: BuildProgressProofsUseCase,
    private val getNutritionFloorStatus: GetNutritionFloorStatusUseCase,
    private val clock: AppClock,
    private val logger: AppLogger
) {
    private val progressionConfig = PlayerProgressionConfig()

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<StatisticsData> {
        val configFlow = configRepo.getConfigFlow().filterNotNull()
        val selectedDateFlow = viewingDateRepo.selectedDate.filterNotNull()

        // Спочатку розраховуємо день циклу, щоб знати, який розклад тягнути
        val cycleDayFlow = combine(
            configFlow,
            selectedDateFlow,
            playerRepo.getPlayer().filterNotNull()
        ) { config, date, player ->
            resolveTrainingCycleDay(
                targetDate = date,
                config = config,
                fallbackCurrentCycleDay = player.currentCycleDay
            )
        }.distinctUntilChanged()

        return cycleDayFlow.flatMapLatest { cycleDay ->
            val bodyWeightAndWorkoutLogs = combine(
                playerRepo.getWeightHistory(),
                analyticsRepo.getAllLogs()
            ) { weightHistory, workoutLogs ->
                weightHistory to workoutLogs
            }

            combine(
                playerRepo.getPlayer().filterNotNull(),
                matrixRepo.getAllEntries(),
                analyticsRepo.getAllWeightHistories(),
                scheduleRepo.getScheduleForDay(cycleDay),
                bodyWeightAndWorkoutLogs
            ) { player, matrix, allHistories, schedule, weightAndLogs ->
                val (weightHistory, workoutLogs) = weightAndLogs
                val activeExerciseIds = schedule?.exercises?.map { it.id }.orEmpty()
                val activeExerciseOrder = activeExerciseIds.withIndex()
                    .associate { (index, exerciseId) -> exerciseId to index }
                val historiesMap = allHistories.groupBy { it.exerciseId }

                val updatedEntries = matrix.map { entry ->
                    val orderIndex = activeExerciseOrder[entry.exerciseId] ?: 999
                    val isExerciseActive = orderIndex != 999
                    
                    val history = historiesMap[entry.exerciseId]?.map { 
                        WeightHistoryEntry(it.weight, it.timestamp) 
                    } ?: emptyList()

                    MatrixEntryData(
                        entry = entry,
                        isActive = isExerciseActive,
                        orderIndex = orderIndex,
                        weightHistory = history
                    )
                }.sortedWith(compareBy({ !it.isActive }, { it.orderIndex }, { it.entry.exerciseName }))

                // Use already calculated RPG muscle attributes from Player
                val characterAttributes = mapOf(
                    MuscleGroup.CHEST            to player.chestAttr.toFloat(),
                    MuscleGroup.BACK             to player.backAttr.toFloat(),
                    MuscleGroup.SHOULDERS        to player.shouldersAttr.toFloat(),
                    MuscleGroup.QUADS            to player.quadsAttr.toFloat(),
                    MuscleGroup.HAMSTRINGS_GLUTES to player.legsAttr.toFloat(),
                    MuscleGroup.ARMS             to player.armsAttr.toFloat(),
                    MuscleGroup.ABS              to player.absAttr.toFloat(),
                    MuscleGroup.LEGS             to player.legsGroupAttr.toFloat(),
                    MuscleGroup.CORE             to player.coreAttr.toFloat()
                )

                val xpPerLevel = progressionConfig.xpPerLevel
                val derivedLevel = progressionConfig.levelForXp(player.xpTotal)
                val xpProgress = (player.xpTotal % xpPerLevel).coerceIn(0, xpPerLevel)
                val weeklySummary = buildWeeklySummary(workoutLogs)
                val progressProofs = buildProgressProofs(
                    workoutLogs = workoutLogs,
                    matrixEntries = matrix,
                    bodyWeightHistory = weightHistory
                )
                val weeklySystemReport = buildWeeklySystemReport(
                    weeklySummary = weeklySummary,
                    matrixEntries = updatedEntries,
                    progressProofs = progressProofs,
                    readinessEntries = loadWeeklyReadinessEntries()
                )
                val nutritionFloorStatus = getNutritionFloorStatus()

                StatisticsData(
                    playerName      = player.name,
                    playerClass     = player.playerClass,
                    level           = derivedLevel.coerceAtLeast(player.level),
                    xpTotal         = xpProgress,
                    xpMax           = xpPerLevel,
                    currentMonth    = player.currentMonth,
                    totalMonths     = 12,
                    currentWeek     = player.currentWeek,
                    currentCycleDay = cycleDay,
                    isPenaltyActive = player.isPenaltyActive,
                    globalRank      = player.globalRank,
                    currentWeight   = weightHistory.maxByOrNull { it.timestamp }?.weight ?: 0f,
                    currentHeight   = player.height,
                    age             = player.age,
                    matrixEntries   = updatedEntries,
                    weightHistory   = weightHistory.sortedBy { it.timestamp },
                    characterAttributes = characterAttributes,
                    currentStreak   = player.currentStreak,
                    maxStreak       = player.maxStreak,
                    xpThisWeek      = player.xpThisWeek,
                    weeklySummary   = weeklySummary,
                    progressProofs  = progressProofs,
                    weeklySystemReport = weeklySystemReport,
                    nutritionFloorStatus = nutritionFloorStatus,
                    systemInsight   = buildSystemInsight(
                        matrixEntries = updatedEntries,
                        weeklySummary = weeklySummary,
                        currentStreak = player.currentStreak,
                        xpThisWeek = player.xpThisWeek
                    ),
                    avatarUri = player.avatarUri
                )
            }
        }.catch { e ->
            if (e is CancellationException) throw e
            logger.e(e, "Failed to build statistics data")
            emit(StatisticsData())
        }.flowOn(Dispatchers.Default)
    }

    private suspend fun loadWeeklyReadinessEntries(): List<ReadinessEntry> {
        val today = Instant.ofEpochMilli(clock.now()).atZone(clock.zoneId()).toLocalDate()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return readinessRepo.getEntriesBetween(
            startEpochDay = weekStart.toEpochDay(),
            endEpochDay = today.toEpochDay()
        )
    }

    private fun buildWeeklySummary(logs: List<WorkoutLog>): WeeklyTrainingSummary {
        val today = Instant.ofEpochMilli(clock.now()).atZone(clock.zoneId()).toLocalDate()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }
        val logsByDate = logs.groupBy { it.session.timestamp.toLocalDate() }

        val daySummaries = weekDays.map { date ->
            val dayLogs = logsByDate[date].orEmpty()
            WeeklyTrainingDaySummary(
                date = date,
                workoutCount = dayLogs.size,
                totalTonnage = dayLogs.sumOf { it.session.totalTonnage }
            )
        }

        return WeeklyTrainingSummary(
            days = daySummaries,
            workoutCount = daySummaries.sumOf { it.workoutCount },
            totalTonnage = daySummaries.sumOf { it.totalTonnage }
        )
    }

    private fun buildWeeklySystemReport(
        weeklySummary: WeeklyTrainingSummary,
        matrixEntries: List<MatrixEntryData>,
        progressProofs: List<ProgressProof>,
        readinessEntries: List<ReadinessEntry>
    ): WeeklySystemReport {
        val bestDay = weeklySummary.days
            .filter { it.workoutCount > 0 }
            .maxWithOrNull(compareBy<WeeklyTrainingDaySummary> { it.totalTonnage }.thenBy { it.workoutCount })
        val weakestEntry = matrixEntries
            .filter { it.entry.targetWeight > 0f }
            .minByOrNull { it.entry.progressPercent }
        val biggestProof = progressProofs.firstOrNull { it.proofType != ProgressProofType.BODY_WEIGHT }
            ?: progressProofs.firstOrNull()
        val lowestReadiness = readinessEntries.minByOrNull { it.score }

        val bestTrainingDay = bestDay?.let { day ->
            "${day.date.shortDayName()}: ${day.workoutCount} тренувань, ${day.totalTonnage.formatTonnage()}"
        } ?: "Немає зафіксованого тренувального дня."

        val weakestPattern = when {
            weeklySummary.workoutCount == 0 -> "Ритм тижня ще не зібрано."
            weakestEntry != null && weakestEntry.entry.progressPercent < 0.75f -> {
                val percent = (weakestEntry.entry.progressPercent.coerceIn(0f, 1f) * 100f).roundToInt()
                "${weakestEntry.entry.exerciseName}: нижче плану ($percent%)."
            }
            else -> "Помітного відставання від цілей немає."
        }

        val biggestProgress = biggestProof?.let { proof ->
            "${proof.exerciseName}: ${proof.previousLabel} -> ${proof.currentLabel} (${proof.deltaText})."
        } ?: "Поки недостатньо записаних тренувань, щоб показати прогрес."

        val recoveryIssue = when {
            lowestReadiness != null && lowestReadiness.score < 45 ->
                "Готовність знижувалася до ${lowestReadiness.score}%. Наступний тиждень варто почати легше."
            lowestReadiness != null && lowestReadiness.score < 65 ->
                "Готовність була нижчою за звичайну: мінімум ${lowestReadiness.score}%."
            weeklySummary.totalTonnage > HIGH_WEEKLY_TONNAGE ->
                "Загальне навантаження високе. Зверніть увагу на сон і відновлення."
            else -> "Ознак проблем із відновленням не виявлено."
        }

        val nextWeekDecision = when {
            weeklySummary.workoutCount == 0 ->
                "Повернути ритм: одне коротке тренування на старті тижня."
            lowestReadiness != null && lowestReadiness.score < 65 ->
                "Почати тиждень зі стандартного або зниженого навантаження."
            weakestEntry != null && weakestEntry.entry.progressPercent < 0.75f ->
                "Тримати фокус на ${weakestEntry.entry.exerciseName}, без різкого підвищення."
            biggestProof != null ->
                "Закріпити прогрес і не підвищувати навантаження різко."
            else ->
                "Підтримувати поточний план і записувати результати після кожного тренування."
        }

        return WeeklySystemReport(
            bestTrainingDay = bestTrainingDay,
            weakestPattern = weakestPattern,
            biggestProgress = biggestProgress,
            recoveryIssue = recoveryIssue,
            nextWeekDecision = nextWeekDecision
        )
    }

    private fun buildSystemInsight(
        matrixEntries: List<MatrixEntryData>,
        weeklySummary: WeeklyTrainingSummary,
        currentStreak: Int,
        xpThisWeek: Int
    ): SystemInsight {
        val improvedEntry = matrixEntries
            .filter { it.entry.usesExternalLoad() && it.entry.currentWeight > it.entry.startWeight }
            .maxByOrNull { it.entry.currentWeight - it.entry.startWeight }

        val weakestEntry = matrixEntries
            .filter { it.entry.usesExternalLoad() && it.entry.targetWeight > 0f }
            .minByOrNull { it.entry.progressPercent }

        val improved = improvedEntry?.let { entry ->
            val delta = entry.entry.currentWeight - entry.entry.startWeight
            "${entry.entry.exerciseName}: +${delta.formatWeight()} кг від старту."
        } ?: "Система накопичує базу прогресу."

        val weakPoint = when {
            weeklySummary.workoutCount == 0 -> "Цього тижня ще немає зафіксованих тренувань."
            weakestEntry != null && weakestEntry.entry.progressPercent < 0.75f -> {
                val percent = (weakestEntry.entry.progressPercent.coerceIn(0f, 1f) * 100f).roundToInt()
                "${weakestEntry.entry.exerciseName}: нижче плану ($percent%)."
            }
            else -> "Критичних просідань по плану не видно."
        }

        val recommendation = when {
            weeklySummary.workoutCount == 0 ->
                "Зафіксуй хоча б одне тренування, щоб система бачила ритм."
            weakestEntry != null && weakestEntry.entry.progressPercent < 0.9f ->
                "Тримай фокус на ${weakestEntry.entry.exerciseName} і не підвищуй план різко."
            currentStreak > 0 && xpThisWeek > 0 ->
                "Підтримуй поточний ритм і закрий тиждень без різких стрибків навантаження."
            else ->
                "Почни з короткого стабільного тижня і зафіксуй факт після кожного тренування."
        }

        return SystemInsight(
            improved = improved,
            weakPoint = weakPoint,
            recommendation = recommendation
        )
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(clock.zoneId()).toLocalDate()

    private fun LocalDate.shortDayName(): String =
        when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Пн"
            DayOfWeek.TUESDAY -> "Вт"
            DayOfWeek.WEDNESDAY -> "Ср"
            DayOfWeek.THURSDAY -> "Чт"
            DayOfWeek.FRIDAY -> "Пт"
            DayOfWeek.SATURDAY -> "Сб"
            DayOfWeek.SUNDAY -> "Нд"
        }

    private fun Float.formatWeight(): String =
        if (this % 1f == 0f) {
            this.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", this)
        }

    private fun Double.formatTonnage(): String =
        if (this >= 1000.0) {
            String.format(Locale.US, "%.1f т", this / 1000.0)
        } else {
            "${roundToInt()} кг"
        }

    private companion object {
        const val HIGH_WEEKLY_TONNAGE = 20_000.0
    }
}
