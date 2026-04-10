package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.DebuffRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.feature.status.viewmodel.*
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetStatusScreenDataUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val debuffRepo: DebuffRepository
) {
    operator fun invoke(): Flow<StatusUiData> =
        playerRepo.getPlayer().flatMapLatest { player ->
            if (player == null) return@flatMapLatest flowOf(StatusUiData())
            
            val dailyQuestsFlow = questRepo.getDailyQuestsForDate(System.currentTimeMillis())
            val promotionQuestsFlow = questRepo.getActivePromotionQuests()
            
            combine(
                dailyQuestsFlow,
                promotionQuestsFlow,
                debuffRepo.getActiveDebuffs(),
                playerRepo.getLatestWeight()
            ) { dailyQuests, promotionQuests, debuffs, weight ->
                val allQuests = dailyQuests + promotionQuests
                
                val daily = allQuests.find { it.type == DomainQuestType.DAILY }
                val main = allQuests.find { it.type == DomainQuestType.MAIN }
                val promotions = allQuests.filter { it.type == DomainQuestType.PROMOTION }

                StatusUiData(
                    playerName             = player.name,
                    playerClass            = player.playerClass,
                    currentMonth           = player.currentMonth,
                    totalMonths            = 12,
                    currentWeight          = weight ?: 80f,
                    height                 = player.height.takeIf { it > 0f } ?: 182f,
                    cycleDay               = player.currentCycleDay,
                    monthWorkoutsCompleted = 0,  // TODO: Phase 3 — з QuestLog
                    monthWorkoutsTotal     = 13,
                    activeDebuffs          = debuffs.map { it.toUiModel() }.toImmutableList(),
                    dailyQuest             = daily?.toUiModel(),
                    mainQuest              = main?.toUiModel(),
                    promotionQuests        = promotions.map { it.toUiModel() }.toImmutableList(),
                    globalRank             = player.globalRank,
                    strAttribute           = player.strAttribute,
                    endAttribute           = player.endAttribute,
                    disAttribute           = player.disAttribute
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
