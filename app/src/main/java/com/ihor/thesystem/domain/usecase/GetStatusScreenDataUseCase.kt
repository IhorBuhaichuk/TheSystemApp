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
    private val clock: AppClock
) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(): Flow<StatusUiData> =
        combine(
            playerRepo.getPlayer(),
            configRepo.getConfigFlow(),
            matrixRepo.getAllEntries()
        ) { player: Player?, config: SystemConfig?, matrix: List<ProgressionMatrixEntry> ->
            Triple(player, config ?: SystemConfig(), matrix)
        }.flatMapLatest { (player, config, matrix) ->
            if (player == null) return@flatMapLatest flowOf(StatusUiData())

            // АВТОМАТИЧНИЙ РОЗРАХУНОК ПОТОЧНОГО ДНЯ ЦИКЛУ
            val currentCycleDay = if (config.cycleAnchorDateTimestamp > 0) {
                val daysPassed = ((clock.now() - config.cycleAnchorDateTimestamp) / (24 * 60 * 60 * 1000)).toInt()
                val calculatedDay = (config.cycleAnchorDay + daysPassed - 1) % config.cycleDaysPerMicrocycle + 1
                calculatedDay
            } else {
                player.currentCycleDay
            }
            
            val activeQuestsFlow = questRepo.getActiveQuests()
            val dailyQuestsForDateFlow = questRepo.getDailyQuestsForDate(clock.now())
            
            // RPG Muscle Attributes Calculation
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
            
            // Отримуємо розклад для всіх 4 днів циклу
            val scheduleFlows = (1..config.cycleDaysPerMicrocycle).map { scheduleRepo.getScheduleForDay(it) }
            
            combine(
                listOf(
                    activeQuestsFlow,
                    dailyQuestsForDateFlow,
                    playerRepo.getLatestWeight(),
                    questLogDao.getFullHistory()
                ) + scheduleFlows
            ) { args ->
                val activeQuests = args[0] as List<Quest>
                val dailyQuestsForDate = args[1] as List<Quest>
                val weight = args[2] as Float?
                val questHistory = args[3] as List<QuestLogEntity>
                val schedules = args.slice(4 until args.size).filterIsInstance<ScheduleDay>()

                // Основний квест та щоденні квести на сьогодні
                val daily = dailyQuestsForDate.find { it.type == DomainQuestType.DAILY }
                val main = dailyQuestsForDate.find { it.type == DomainQuestType.MAIN }
                
                // Промоушн квести шукаємо серед усіх активних
                val promotions = activeQuests.filter { it.type == DomainQuestType.PROMOTION }

                val completedMainThisMonth = questHistory.count {
                    it.questType == QuestType.MAIN && it.wasSuccessful
                }

                // Розрахунок загальної кількості тренувань у місяці (4 тижні)
                val trainingDaysPerCycle = schedules.count { it.workoutTemplateName != null }
                val monthWorkoutsTotal = trainingDaysPerCycle * config.microCyclesPerMonth

                StatusUiData(
                    playerName             = player.name,
                    playerClass            = player.playerClass,
                    level                  = player.level,
                    xpTotal                = player.xpTotal,
                    xpMax                  = player.level * 1000,
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
                    xpThisWeek             = player.xpThisWeek
                )
            }
        }
}

// ── Mappers ───────────────────────────────────────────────────────────────────
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
