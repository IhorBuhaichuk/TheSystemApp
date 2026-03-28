package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject

class SyncCycleAnchorUseCase @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val generateQuests: GenerateDailyQuestsUseCase
) {
    suspend operator fun invoke(selectedDay: Int) {
        val config = configRepo.getConfig().firstOrNull() ?: return
        val player = playerRepo.getPlayer().firstOrNull() ?: return
        
        val today = LocalDate.now()
        
        // 1. Оновлюємо конфігурацію системи (Anchor)
        configRepo.updateConfig(
            config.copy(
                cycleAnchorDateTimestamp = today.toEpochDay(),
                cycleAnchorDay = selectedDay
            )
        )
        
        // 2. Оновлюємо поточний день гравця
        playerRepo.updatePlayer(
            player.copy(currentCycleDay = selectedDay)
        )
        
        // 3. Перегенеруємо квести на сьогодні
        questRepo.archiveActiveQuests()
        generateQuests()
    }
}
