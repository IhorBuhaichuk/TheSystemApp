package com.ihor.thesystem.feature.status.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
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
    private val matrixRepo: ProgressionMatrixRepository,
    private val generateDailyQuestsUseCase: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting Day Finalization Worker")
            
            val player = playerRepo.getPlayer().firstOrNull() ?: return Result.failure()
            val config = configRepo.getConfigFlow().firstOrNull() ?: SystemConfig()
            val todayQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()
            
            // 1. Calculate XP and Streaks based on Main Quests
            val mainQuests = todayQuests.filter { it.type == DomainQuestType.MAIN }
            val (playerAfterQuests, _) = player.evaluateQuests(mainQuests).checkLevelUp()

            // 2. Advance time (Cycle Day / Week / Month)
            val finalPlayer = playerAfterQuests.advanceTime(config)

            // 3. Persist updated player state
            playerRepo.updatePlayer(finalPlayer)
            
            // 4. Cleanup and generate new day
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
