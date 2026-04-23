package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.dao.QuestLogDao
import com.ihor.thesystem.data.local.room.entity.QuestLogEntity
import com.ihor.thesystem.data.local.room.entity.QuestType
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.feature.status.viewmodel.*
import com.ihor.thesystem.domain.util.MuscleGroupMapper
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetStatusScreenDataUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val questLogDao: QuestLogDao,
    private val scheduleRepo: ScheduleRepository,
    private val configRepo: SystemConfigRepository,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase,
    private val clock: AppClock
) {
    operator fun invoke(): Flow<StatusUiData> =
        combine(
            playerRepo.getPlayer(),
            configRepo.getConfigFlow(),
            matrixRepo.getAllEntries()
        ) { player: Player?, config: SystemConfig?, matrix: List<ProgressionMatrixEntry> ->
            Triple(player, config ?: SystemConfig(), matrix)
        }.flatMapLatest { (player, config, matrix) ->
            if (player == null) return@flatMapLatest flowOf(StatusUiData())

            val cycleDays = config.cycleDaysPerMicrocycle.coerceAtLeast(1)
            val currentCycleDay = if (config.cycleAnchorDateTimestamp > 0) {
                val todayDate = java.time.Instant.ofEpochMilli(clock.now())
                    .atZone(java.time.ZoneId.systemDefault())
                    .minusHours(config.dayStartOffsetHours.toLong())
                    .toLocalDate()
                
                calculateCycleDay(
                    targetDate = todayDate,
                    anchorEpochDay = java.time.Instant.ofEpochMilli(config.cycleAnchorDateTimestamp)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay(),
                    anchorCycleDay = config.cycleAnchorDay,
                    cycleDaysPerMicrocycle = config.cycleDaysPerMicrocycle
                )
            } else {
                player.currentCycleDay
            }
            
            val muscleMap = MuscleGroup.entries.associateWith { group ->
                val groupExercises = matrix.filter { 
                    MuscleGroupMapper.getMuscleGroupsForExercise(it.exerciseName).contains(group)
                }
                if (groupExercises.isEmpty()) 0f else {
                    val totalRank = groupExercises.sumOf { it.currentRank.weight }
                    val maxPossible = groupExercises.size * 6.0
                    (totalRank / maxPossible * 100).toFloat().coerceIn(0f, 100f)
                }
            }
            
            val scheduleFlows = (1..cycleDays).map { scheduleRepo.getScheduleForDay(it) }
            val schedulesFlow = if (scheduleFlows.isEmpty()) flowOf(emptyList<ScheduleDay>()) 
                               else combine(scheduleFlows) { it.filterIsInstance<ScheduleDay>() }
            
            combine(
                questRepo.getActiveQuests(),
                questRepo.getDailyQuestsForDate(
                    java.time.Instant.ofEpochMilli(clock.now())
                        .atZone(java.time.ZoneId.systemDefault())
                        .minusHours(config.dayStartOffsetHours.toLong())
                        .toInstant()
                        .toEpochMilli()
                ),
                playerRepo.getLatestWeight(),
                questLogDao.getFullHistory(),
                schedulesFlow
            ) { activeQuests, dailyQuestsForDate, weight, questHistory, schedules ->
                val daily = dailyQuestsForDate.find { it.type == DomainQuestType.DAILY }
                val main = dailyQuestsForDate.find { it.type == DomainQuestType.MAIN }
                val promotions = activeQuests.filter { it.type == DomainQuestType.PROMOTION }

                val completedMainThisMonth = questHistory.count {
                    it.questType == QuestType.MAIN && it.wasSuccessful
                }

                val trainingDaysPerCycle = schedules.count { it.workoutTemplateName != null }
                val monthWorkoutsTotal = trainingDaysPerCycle * config.microCyclesPerMonth
                
                val currentLevelXp = player.level * 1000 // Базове XP для поточного рівня
                val xpMax = (player.level + 1) * 1000 // Ціль для наступного рівня
                val xpProgress = player.xpTotal.coerceIn(0, xpMax)

                StatusUiData(
                    playerName             = player.name,
                    playerClass            = player.playerClass,
                    level                  = player.level,
                    xpTotal                = xpProgress,
                    xpMax                  = xpMax,
                    currentMonth           = player.currentMonth,
                    totalMonths            = 12,
                    currentWeight          = weight ?: 80f,
                    height                 = player.height.takeIf { it > 0f } ?: 182f,
                    cycleDay               = currentCycleDay,
                    monthWorkoutsCompleted = completedMainThisMonth,
                    monthWorkoutsTotal     = monthWorkoutsTotal,
                    dailyQuest             = daily?.toUiModel(),
                    mainQuest              = main?.toUiModel(),
                    promotionQuests        = promotions.map { it.toUiModel() }.toImmutableList(),
                    globalRank             = player.globalRank,
                    characterAttributes    = muscleMap,
                    currentStreak          = player.currentStreak,
                    maxStreak              = player.maxStreak,
                    xpThisWeek             = player.xpThisWeek,
                    avatarUri              = player.avatarUri
                )
            }
        }
}

private fun Quest.toUiModel() = QuestUiModel(
    id          = id,
    title       = title,
    subtitle    = when (type) {
        DomainQuestType.DAILY ->
            "[ ПРОГРЕС: ${tasks.count { it.isCompleted }}/${tasks.size} ]"
        DomainQuestType.MAIN  ->
            if (status == DomainQuestStatus.COMPLETED) "[ ВИКОНАНО ✓ ]"
            else "[ НАГОРОДА: +1 ТИЖДЕНЬ ]"
        DomainQuestType.PROMOTION ->
            "[ НАГОРОДА: +500 EXP | ПІДТВЕРДЖЕННЯ РАНГУ ]"
    },
    tasks       = tasks.map { TaskUiModel(it.id, it.name, it.isCompleted) }.toImmutableList(),
    isCompleted = status == DomainQuestStatus.COMPLETED
)
