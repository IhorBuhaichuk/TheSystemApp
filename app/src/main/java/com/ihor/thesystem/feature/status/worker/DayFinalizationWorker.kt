package com.ihor.thesystem.feature.status.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.usecase.CalculateAttributesUseCase
import com.ihor.thesystem.domain.usecase.GenerateDailyQuestsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

@HiltWorker
class DayFinalizationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val configRepo: SystemConfigRepository,
    private val generateDailyQuestsUseCase: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting Day Finalization Worker")
            
            val player = playerRepo.getPlayer().firstOrNull() ?: return Result.failure()
            val config = configRepo.getConfigFlow().firstOrNull() ?: SystemConfig()
            val todayQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()
            val mainQuests = todayQuests.filter { it.type == DomainQuestType.MAIN }

            // 1. Domain logic
            val (updatedPlayer, _) = player
                .evaluateQuests(mainQuests)
                .advanceTime(config)
                .checkLevelUp()

            // 2. Persist
            playerRepo.updatePlayer(updatedPlayer)
            
            // 3. Side effects
            questRepo.archiveActiveQuests()
            generateDailyQuestsUseCase.invoke()
            calculateAttributes.invoke()

            Timber.d("Day Finalization completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during day finalization")
            Result.retry()
        }
    }
}
