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
    private val matrixRepo: ProgressionMatrixRepository,
    private val resolveTrainingCycleDay: ResolveTrainingCycleDayUseCase,
    private val decideTodayWorkout: DecideTodayWorkoutUseCase,
    private val recordTodayOrderDecision: RecordTodayOrderDecisionUseCase,
    private val clock: AppClock
) {
    private val progressionConfig = PlayerProgressionConfig()

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<StatusData> =
        combine(
            playerRepo.getPlayer(),
            configRepo.getConfigFlow(),
            questRepo.getActiveQuests(),
            matrixRepo.getAllEntries()
        ) { player, config, activeQuests, matrixEntries ->
            DataContainer(player, config ?: SystemConfig(), activeQuests, matrixEntries)
        }.flatMapLatest { container ->
            val player = container.player
            val config = container.config
            val activeQuests = container.activeQuests
            val matrixEntries = container.matrixEntries

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
            
            val zoneId = clock.zoneId()
            val now = clock.now()
            val currentDate = java.time.Instant.ofEpochMilli(now)
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
            
            val dailySnapshotFlow = combine(
                questRepo.getQuestsByDate(
                    dateMillis = now
                ),
                todoRepository.getTodosForDate(currentDate),
                playerRepo.getLatestWeight(),
                questRepo.getSuccessfulQuestCount(DomainQuestType.MAIN, monthStart, monthEnd)
            ) { dailyQuestsForDate, todos, weight, completedMainThisMonth ->
                StatusDailySnapshot(
                    dailyQuest = dailyQuestsForDate.find { it.type == DomainQuestType.DAILY },
                    mainQuest = dailyQuestsForDate.find { it.type == DomainQuestType.MAIN },
                    todos = todos,
                    weight = weight,
                    completedMainThisMonth = completedMainThisMonth
                )
            }

            val schedulesAndDecisionFlow = scheduleRepo
                .getSchedulesForDays((1..cycleDays).toList())
                .mapLatest { schedules ->
                    val todayDecision = decideTodayWorkout(currentDate)
                    recordTodayOrderDecision(todayDecision)
                    SchedulesAndDecision(
                        schedules = schedules,
                        todayDecision = todayDecision
                    )
                }

            combine(
                dailySnapshotFlow,
                schedulesAndDecisionFlow
            ) { snapshot, plan ->
                val promotions = activeQuests.filter { it.type == DomainQuestType.PROMOTION }
                val matrixEntriesByExercise = matrixEntries.associateBy { it.exerciseId }
                val activeBossFight = promotions.firstNotNullOfOrNull { quest ->
                    val exerciseId = quest.targetExerciseId
                    if (exerciseId == null) {
                        null
                    } else {
                        matrixEntriesByExercise[exerciseId]?.toBossFight(quest.status.toBossFightStatus())
                    }
                }

                val trainingDaysPerCycle = plan.schedules.count { it.workoutTemplateName != null }
                val monthWorkoutsTotal = trainingDaysPerCycle * config.microCyclesPerMonth
                
                val xpPerLevel = progressionConfig.xpPerLevel
                val derivedLevel = progressionConfig.levelForXp(player.xpTotal)
                val xpProgress = (player.xpTotal % xpPerLevel).coerceIn(0, xpPerLevel)
                StatusData(
                    playerName             = player.name,
                    playerClass            = player.playerClass,
                    level                  = derivedLevel.coerceAtLeast(player.level),
                    xpTotal                = xpProgress,
                    xpMax                  = xpPerLevel,
                    currentMonth           = player.currentMonth,
                    totalMonths            = 12,
                    currentWeight          = snapshot.weight,
                    height                 = player.height.takeIf { it > 0f },
                    cycleDay               = currentCycleDay,
                    monthWorkoutsCompleted = snapshot.completedMainThisMonth,
                    monthWorkoutsTotal     = monthWorkoutsTotal,
                    todos                  = snapshot.todos,
                    dailyQuest             = snapshot.dailyQuest,
                    mainQuest              = snapshot.mainQuest,
                    promotionQuests        = promotions,
                    activeBossFight        = activeBossFight,
                    globalRank             = player.globalRank,
                    characterAttributes    = muscleMap,
                    currentStreak          = player.currentStreak,
                    maxStreak              = player.maxStreak,
                    xpThisWeek             = player.xpThisWeek,
                    avatarUri              = player.avatarUri,
                    todayDecision          = plan.todayDecision
                )
            }
        }
}

private data class DataContainer(
    val player: Player?,
    val config: SystemConfig,
    val activeQuests: List<Quest>,
    val matrixEntries: List<ProgressionMatrixEntry>
)

private data class StatusDailySnapshot(
    val dailyQuest: Quest?,
    val mainQuest: Quest?,
    val todos: List<TodoItem>,
    val weight: Float?,
    val completedMainThisMonth: Int
)

private data class SchedulesAndDecision(
    val schedules: List<ScheduleDay>,
    val todayDecision: TodayTrainingDecision
)
