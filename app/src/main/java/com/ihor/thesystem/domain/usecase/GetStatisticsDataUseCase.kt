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
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(): Flow<StatisticsUiData> {
        return combine(
            listOf(
                playerRepo.getPlayer().filterNotNull(),
                matrixRepo.getAllEntries(),
                matrixRepo.getAllReferences(),
                analyticsRepo.getAllWeightHistories(),
                viewingDateRepo.selectedDate,
                configRepo.getConfigFlow().filterNotNull(),
                weightLogDao.getAllLogs()
            )
        ) { args: Array<Any?> ->
            val player = args[0] as Player
            val matrix = args[1] as List<ProgressionMatrixEntry>
            val references = args[2] as List<ReferenceMatrixEntity>
            val allHistories = args[3] as List<WeightHistoryWithId>
            val selectedDate = args[4] as LocalDate
            val config = args[5] as SystemConfig
            val weightHistory = args[6] as List<WeightLogEntity>

            val cycleDay = calculateCycleDay(
                targetDate = selectedDate,
                anchorEpochDay = config.cycleAnchorDateTimestamp,
                anchorCycleDay = config.cycleAnchorDay
            )

            val schedule = scheduleRepo.getScheduleForDay(cycleDay).firstOrNull()
            val activeExerciseIds = schedule?.exercises?.map { it.id } ?: emptyList()

            val historiesMap = allHistories.groupBy { it.exerciseId }

            val updatedEntries = matrix.map { entry ->
                val ref = references.find { it.exerciseName.equals(entry.exerciseName, ignoreCase = true) }
                val isExerciseActive = activeExerciseIds.contains(entry.exerciseId)
                val orderIndex = if (isExerciseActive) activeExerciseIds.indexOf(entry.exerciseId) else 999

                val m0 = ref?.milestones?.get("M0")?.toFloat() ?: entry.startWeight
                val m12 = ref?.milestones?.get("M12")?.toFloat() ?: entry.targetWeight
                
                val history = historiesMap[entry.exerciseId]?.map { 
                    WeightHistoryEntry(it.weight, it.timestamp) 
                } ?: emptyList()

                entry.toUiModel(isExerciseActive, orderIndex, history).copy(
                    startWeight = m0,
                    targetWeight = m12
                )
            }.sortedWith(compareBy({ !it.isActive }, { it.orderIndex }, { it.exerciseName }))

            StatisticsUiData(
                playerName      = player.name,
                playerClass     = player.playerClass,
                currentMonth    = player.currentMonth,
                totalMonths     = 12,
                currentWeek     = player.currentWeek,
                currentCycleDay = cycleDay,
                isPenaltyActive = player.isPenaltyActive,
                globalRank      = player.globalRank,
                matrixEntries   = updatedEntries.toImmutableList(),
                weightHistory   = weightHistory.sortedBy { it.timestamp }.toImmutableList()
            )
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
