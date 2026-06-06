package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetStatusScreenDataUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val configRepo: SystemConfigRepository,
    private val todoRepository: TodoRepository,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val decideTodayWorkout: DecideTodayWorkoutUseCase,
    private val clock: AppClock
) {
    private val progressionConfig = PlayerProgressionConfig()

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<StatusData> =
        combine(
            playerRepo.getPlayer(),
            configRepo.getConfigFlow(),
            questRepo.getActiveQuests()
        ) { player, config, activeQuests ->
            DataContainer(player, config ?: SystemConfig(), activeQuests)
        }.flatMapLatest { container ->
            val player = container.player
            val config = container.config
            val activeQuests = container.activeQuests

            if (player == null) return@flatMapLatest flowOf(StatusData())

            val cycleDays = config.cycleDaysPerMicrocycle.coerceAtLeast(1)
            val todayDate = java.time.Instant.ofEpochMilli(clock.now())
                .atZone(clock.zoneId())
                .toLocalDate()
            val currentCycleDay = resolveTrainingCycleDay(
                targetDate = todayDate,
                config = config,
                fallbackCurrentCycleDay = player.currentCycleDay
            )
            
            val muscleMap = mapOf(
                MuscleGroup.CHEST             to player.chestAttr.toFloat(),
                MuscleGroup.BACK              to player.backAttr.toFloat(),
                MuscleGroup.SHOULDERS         to player.shouldersAttr.toFloat(),
                MuscleGroup.QUADS             to player.quadsAttr.toFloat(),
                MuscleGroup.HAMSTRINGS_GLUTES  to player.legsAttr.toFloat(),
                MuscleGroup.ARMS              to player.armsAttr.toFloat(),
                MuscleGroup.ABS               to player.absAttr.toFloat(),
                MuscleGroup.LEGS              to player.legsGroupAttr.toFloat(),
                MuscleGroup.CORE              to player.coreAttr.toFloat()
            )
            
            val scheduleFlows = (1..cycleDays).map { scheduleRepo.getScheduleForDay(it) }
            val schedulesFlow = if (scheduleFlows.isEmpty()) flowOf(emptyList<ScheduleDay>()) 
                               else combine(scheduleFlows) { it.filterIsInstance<ScheduleDay>() }
            val zoneId = clock.zoneId()
            val currentDate = java.time.Instant.ofEpochMilli(clock.now())
                .atZone(zoneId)
                .toLocalDate()
            val monthStart = currentDate.withDayOfMonth(1)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()
            val monthEnd = currentDate.plusMonths(1)
                .withDayOfMonth(1)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli() - 1
            
            combine(
                questRepo.getQuestsByDate(
                    dateMillis = clock.now()
                ),
                todoRepository.getTodosForDate(currentDate),
                playerRepo.getLatestWeight(),
                questRepo.getSuccessfulQuestCount(DomainQuestType.MAIN, monthStart, monthEnd),
                schedulesFlow
            ) { dailyQuestsForDate, todos, weight, completedMainThisMonth, schedules ->
                val daily = dailyQuestsForDate.find { it.type == DomainQuestType.DAILY }
                val main = dailyQuestsForDate.find { it.type == DomainQuestType.MAIN }
                val promotions = activeQuests.filter { it.type == DomainQuestType.PROMOTION }

                val trainingDaysPerCycle = schedules.count { it.workoutTemplateName != null }
                val monthWorkoutsTotal = trainingDaysPerCycle * config.microCyclesPerMonth
                
                val xpPerLevel = progressionConfig.xpPerLevel
                val derivedLevel = progressionConfig.levelForXp(player.xpTotal)
                val xpProgress = (player.xpTotal % xpPerLevel).coerceIn(0, xpPerLevel)
                val todayDecision = decideTodayWorkout(currentDate)

                StatusData(
                    playerName             = player.name,
                    playerClass            = player.playerClass,
                    level                  = derivedLevel.coerceAtLeast(player.level),
                    xpTotal                = xpProgress,
                    xpMax                  = xpPerLevel,
                    currentMonth           = player.currentMonth,
                    totalMonths            = 12,
                    currentWeight          = weight,
                    height                 = player.height.takeIf { it > 0f },
                    cycleDay               = currentCycleDay,
                    monthWorkoutsCompleted = completedMainThisMonth,
                    monthWorkoutsTotal     = monthWorkoutsTotal,
                    todos                  = todos,
                    dailyQuest             = daily,
                    mainQuest              = main,
                    promotionQuests        = promotions,
                    globalRank             = player.globalRank,
                    characterAttributes    = muscleMap,
                    currentStreak          = player.currentStreak,
                    maxStreak              = player.maxStreak,
                    xpThisWeek             = player.xpThisWeek,
                    avatarUri              = player.avatarUri,
                    todayDecision          = todayDecision
                )
            }
        }
}

private data class DataContainer(
    val player: Player?,
    val config: SystemConfig,
    val activeQuests: List<Quest>
)
