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
        val config = configRepo.getConfigFlow().firstOrNull() ?: return
        
        // Гарантовано записуємо Epoch Day (кількість днів), а не мілісекунди
        val todayEpochDay = LocalDate.now().toEpochDay()
        
        // 1. Оновлюємо конфігурацію системи (Anchor)
        configRepo.updateConfig(
            config.copy(
                cycleAnchorDateTimestamp = todayEpochDay,
                cycleAnchorDay = selectedDay
            )
        )
        
        // 2. Оновлюємо поточний день гравця для миттєвого відображення в UI
        playerRepo.updateCurrentCycleDay(selectedDay)
        
        // 3. Перегенеруємо квести на сьогодні відповідно до нового дня циклу
        questRepo.archiveActiveQuests()
        generateQuests()
    }
}
