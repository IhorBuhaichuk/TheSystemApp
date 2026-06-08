package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.QuestTask
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.util.AppLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleQuestTaskUseCaseTest {

    private val questRepository: QuestRepository = mockk(relaxed = true)
    private val matrixRepository: ProgressionMatrixRepository = mockk(relaxed = true)
    private val playerRepository: PlayerRepository = mockk(relaxed = true)
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase = mockk(relaxed = true)
    private var quest = promotionQuest(taskCompleted = false)

    @Test
    fun `completed boss fight promotes rank and clears pending flag`() = runTest {
        arrangeMutablePromotionQuest()

        useCase().invoke(
            taskId = TASK_ID,
            questId = QUEST_ID,
            currentCompletedState = false
        )

        coVerify(exactly = 1) { matrixRepository.setPromotionPending(EXERCISE_ID, false) }
        coVerify(exactly = 1) { matrixRepository.promoteRank(EXERCISE_ID) }
        coVerify(exactly = 1) { recalculateGlobalRank.invoke() }
    }

    private fun arrangeMutablePromotionQuest() {
        coEvery { questRepository.getQuestById(QUEST_ID) } answers { quest }
        coEvery { questRepository.toggleTaskCompletion(TASK_ID, QUEST_ID, true) } answers {
            quest = promotionQuest(taskCompleted = true)
        }
        coEvery { questRepository.updateQuestStatus(QUEST_ID, any()) } answers {
            quest = quest.copy(status = invocation.args[1] as DomainQuestStatus)
        }
        coEvery { questRepository.logQuestResult(any(), any(), any()) } returns Unit
        coEvery { recalculateGlobalRank.invoke() } returns Unit
    }

    private fun useCase(): ToggleQuestTaskUseCase =
        ToggleQuestTaskUseCase(
            repo = questRepository,
            matrixRepo = matrixRepository,
            recalculateGlobalRank = recalculateGlobalRank,
            completeQuest = CompleteQuestUseCase(
                transactionProvider = RecordingTransactionProvider(),
                questRepository = questRepository,
                playerRepository = playerRepository,
                logger = NoOpLogger()
            )
        )

    private fun promotionQuest(taskCompleted: Boolean): Quest =
        Quest(
            id = QUEST_ID,
            title = "Контрольний норматив: Pull-up",
            type = DomainQuestType.PROMOTION,
            date = 0L,
            status = DomainQuestStatus.ACTIVE,
            tasks = listOf(
                QuestTask(
                    id = TASK_ID,
                    questId = QUEST_ID,
                    name = "Умова: 10 чистих повторень",
                    isCompleted = taskCompleted,
                    exerciseId = EXERCISE_ID
                )
            ),
            targetExerciseId = EXERCISE_ID
        )

    private class RecordingTransactionProvider : TransactionProvider {
        override suspend fun <R> runInTransaction(block: suspend () -> R): R = block()
    }

    private class NoOpLogger : AppLogger {
        override fun d(message: String, tag: String?) = Unit
        override fun i(message: String, tag: String?) = Unit
        override fun w(message: String, tag: String?) = Unit
        override fun e(throwable: Throwable?, message: String, tag: String?) = Unit
    }

    private companion object {
        const val QUEST_ID = 12
        const val TASK_ID = 34
        const val EXERCISE_ID = 56
    }
}
