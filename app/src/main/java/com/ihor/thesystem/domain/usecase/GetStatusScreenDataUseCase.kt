package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.dao.QuestLogDao
import com.ihor.thesystem.data.local.room.entity.QuestLogEntity
import com.ihor.thesystem.data.local.room.entity.QuestType
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.feature.status.viewmodel.*
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetStatusScreenDataUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val debuffRepo: DebuffRepository,
    private val questLogDao: QuestLogDao,
    private val scheduleRepo: ScheduleRepository,
    private val clock: AppClock
) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(): Flow<StatusUiData> =
        playerRepo.getPlayer().flatMapLatest { player ->
            if (player == null) return@flatMapLatest flowOf(StatusUiData())
            
            val dailyQuestsFlow = questRepo.getDailyQuestsForDate(clock.now())
            val promotionQuestsFlow = questRepo.getActivePromotionQuests()
            
            // Отримуємо розклад для всіх 4 днів циклу
            val scheduleFlows = (1..4).map { scheduleRepo.getScheduleForDay(it) }
            
            combine(
                listOf(
                    dailyQuestsFlow,
                    promotionQuestsFlow,
                    debuffRepo.getActiveDebuffs(),
                    playerRepo.getLatestWeight(),
                    questLogDao.getFullHistory()
                ) + scheduleFlows
            ) { args ->
                val dailyQuests = args[0] as List<Quest>
                val promotionQuests = args[1] as List<Quest>
                val debuffs = args[2] as List<DebuffConfig>
                val weight = args[3] as Float?
                val questHistory = args[4] as List<QuestLogEntity>
                val schedules = args.slice(5..8) as List<ScheduleDay?>

                val allQuests = dailyQuests + promotionQuests
                
                val daily = allQuests.find { it.type == DomainQuestType.DAILY }
                val main = allQuests.find { it.type == DomainQuestType.MAIN }
                val promotions = allQuests.filter { it.type == DomainQuestType.PROMOTION }

                val completedMainThisMonth = questHistory.count {
                    it.questType == QuestType.MAIN && it.wasSuccessful
                }

                // Розрахунок загальної кількості тренувань у місяці (4 тижні)
                val trainingDaysPerCycle = schedules.count { it?.workoutTemplateName != null }
                val monthWorkoutsTotal = trainingDaysPerCycle * 4

                StatusUiData(
                    playerName             = player.name,
                    playerClass            = player.playerClass,
                    currentMonth           = player.currentMonth,
                    totalMonths            = 12,
                    currentWeight          = weight ?: 80f,
                    height                 = player.height.takeIf { it > 0f } ?: 182f,
                    cycleDay               = player.currentCycleDay,
                    monthWorkoutsCompleted = completedMainThisMonth,
                    monthWorkoutsTotal     = monthWorkoutsTotal,
                    activeDebuffs          = debuffs.map { it.toUiModel() }.toImmutableList(),
                    dailyQuest             = daily?.toUiModel(),
                    mainQuest              = main?.toUiModel(),
                    promotionQuests        = promotions.map { it.toUiModel() }.toImmutableList(),
                    globalRank             = player.globalRank,
                    strAttribute           = player.strAttribute,
                    endAttribute           = player.endAttribute,
                    disAttribute           = player.disAttribute,
                    currentStreak          = player.currentStreak,
                    maxStreak              = player.maxStreak,
                    xpTotal                = player.xpTotal,
                    xpThisWeek             = player.xpThisWeek
                )
            }
        }
}

// ── Mappers ───────────────────────────────────────────────────────────────────
private fun DebuffConfig.toUiModel() =
    DebuffUiModel(id, condition, text, penaltyPercent, isActive)

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
