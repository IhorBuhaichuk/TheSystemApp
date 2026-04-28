package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.data.local.room.dao.WeightLogDao
import com.ihor.thesystem.data.local.room.entity.ReferenceMatrixEntity
import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

class GetStatisticsDataUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val viewingDateRepo: ViewingDateRepository,
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val weightLogDao: WeightLogDao,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(): Flow<StatisticsUiData> {
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
                matrixRepo.getAllReferences(),
                analyticsRepo.getAllWeightHistories(),
                scheduleRepo.getScheduleForDay(cycleDay),
                weightLogDao.getAllLogs()
            ) { args ->
                val player = args[0] as Player
                val matrix = args[1] as List<ProgressionMatrixEntry>
                val references = args[2] as List<ReferenceMatrixEntity>
                val allHistories = args[3] as List<WeightHistoryWithId>
                val schedule = args[4] as ScheduleDay?
                val weightHistory = args[5] as List<WeightLogEntity>

                val activeExerciseIds = schedule?.exercises?.map { it.id } ?: emptyList()
                val historiesMap = allHistories.groupBy { it.exerciseId }

                val updatedEntries = matrix.map { entry ->
                    val isExerciseActive = activeExerciseIds.contains(entry.exerciseId)
                    val orderIndex = if (isExerciseActive) activeExerciseIds.indexOf(entry.exerciseId) else 999

                    val m0 = entry.startWeight
                    val m12 = entry.targetWeight
                    
                    val history = historiesMap[entry.exerciseId]?.map { 
                        WeightHistoryEntry(it.weight, it.timestamp) 
                    } ?: emptyList()

                    entry.toUiModel(isExerciseActive, orderIndex, history).copy(
                        startWeight = m0,
                        targetWeight = m12
                    )
                }.sortedWith(compareBy({ !it.isActive }, { it.orderIndex }, { it.exerciseName }))

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

                StatisticsUiData(
                    playerName      = player.name,
                    playerClass     = player.playerClass,
                    currentMonth    = player.currentMonth,
                    totalMonths     = 12,
                    currentWeek     = player.currentWeek,
                    currentCycleDay = cycleDay,
                    isPenaltyActive = player.isPenaltyActive,
                    globalRank      = player.globalRank,
                    currentWeight   = weightHistory.maxByOrNull { it.timestamp }?.weight ?: 0f,
                    currentHeight   = player.height,
                    matrixEntries   = updatedEntries.toImmutableList(),
                    weightHistory   = weightHistory.sortedBy { it.timestamp }.toImmutableList(),
                    characterAttributes = characterAttributes,
                    avatarUri = player.avatarUri
                )
            }
        }.catch { e ->
            e.printStackTrace()
            emit(StatisticsUiData())
        }.flowOn(Dispatchers.Default)
    }

    private fun ProgressionMatrixEntry.toUiModel(isActive: Boolean, orderIndex: Int, history: List<WeightHistoryEntry>) = MatrixEntryUiModel(
        exerciseId       = exerciseId,
        exerciseName     = exerciseName,
        startWeight      = startWeight,
        targetWeight     = targetWeight,
        currentWeight    = currentWeight,
        targetWeightNote = targetWeightNote,
        weeklyStep       = weeklyStep,
        progressPercent  = progressPercent,
        currentRank      = currentRank,
        completedCycles  = completedCycles,
        isActive         = isActive,
        orderIndex       = orderIndex,
        weightHistory    = history.toImmutableList(),
        nextRecommendedWeight = nextRecommendedWeight,
        nextRecommendedSets = nextRecommendedSets,
        nextRecommendedReps = nextRecommendedReps,
        lastAiFeedback = lastAiFeedback
    )
}
