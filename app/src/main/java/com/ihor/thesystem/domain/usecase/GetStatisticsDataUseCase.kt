package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetStatisticsDataUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val viewingDateRepo: ViewingDateRepository,
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<StatisticsData> {
        val configFlow = configRepo.getConfigFlow().filterNotNull()
        val selectedDateFlow = viewingDateRepo.selectedDate.filterNotNull()

        // Спочатку розраховуємо день циклу, щоб знати, який розклад тягнути
        val cycleDayFlow = combine(configFlow, selectedDateFlow) { config, date ->
            calculateCycleDay(
                targetDate = date,
                anchorEpochDay = config.cycleAnchorDateTimestamp,
                anchorCycleDay = config.cycleAnchorDay,
                cycleDaysPerMicrocycle = config.cycleDaysPerMicrocycle
            )
        }.distinctUntilChanged()

        return cycleDayFlow.flatMapLatest { cycleDay ->
            combine(
                playerRepo.getPlayer().filterNotNull(),
                matrixRepo.getAllEntries(),
                analyticsRepo.getAllWeightHistories(),
                scheduleRepo.getScheduleForDay(cycleDay),
                playerRepo.getWeightHistory()
            ) { player, matrix, allHistories, schedule, weightHistory ->
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

                val xpPerLevel = 1000
                val xpForCurrentLevel = player.level * xpPerLevel
                val xpProgress = (player.xpTotal - xpForCurrentLevel).coerceIn(0, xpPerLevel)

                StatisticsData(
                    playerName      = player.name,
                    playerClass     = player.playerClass,
                    level           = player.level,
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
                    avatarUri = player.avatarUri
                )
            }
        }.catch { e ->
            e.printStackTrace()
            emit(StatisticsData())
        }.flowOn(Dispatchers.Default)
    }
}
