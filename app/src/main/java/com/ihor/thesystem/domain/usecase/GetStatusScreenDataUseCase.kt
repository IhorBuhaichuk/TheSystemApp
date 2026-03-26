package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.DebuffRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.feature.status.viewmodel.*
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
            combine(
                questRepo.getActiveDailyQuest(),
                questRepo.getActiveMainQuest(),
                debuffRepo.getActiveDebuffs(),
                playerRepo.getLatestWeight()
            ) { daily, main, debuffs, weight ->
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
                    activeDebuffs          = debuffs.map { it.toUiModel() },
                    dailyQuest             = daily?.toUiModel(),
                    mainQuest              = main?.toUiModel()
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
    },
    tasks       = tasks.map { TaskUiModel(it.id, it.name, it.isCompleted) },
    isCompleted = status == DomainQuestStatus.COMPLETED
)