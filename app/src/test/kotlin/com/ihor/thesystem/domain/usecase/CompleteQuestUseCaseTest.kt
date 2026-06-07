package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.QuestTask
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.util.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteQuestUseCaseTest {

    private val questRepository: QuestRepository = mockk(relaxed = true)
    private val playerRepository: PlayerRepository = mockk()
    private val logger = NoOpLogger()
    private var quest = completedMainQuest()
    private var player = player()
    private var logCount = 0

    @Test
    fun `manual completed main quest grants XP once`() = runTest {
        arrangeMutableQuestAndPlayer()
        val useCase = useCase()

        val first = useCase(quest.id, mode = QuestCompletionMode.TaskUpdate)
        val second = useCase(quest.id, mode = QuestCompletionMode.TaskUpdate)

        assertTrue(first is Result.Success)
        assertTrue((first as Result.Success).data.rewardGranted)
        assertTrue(second is Result.Success)
        assertFalse((second as Result.Success).data.rewardGranted)
        assertEquals(DomainQuestStatus.COMPLETED, quest.status)
        assertEquals(100, player.xpTotal)
        assertEquals(1, player.currentStreak)
        assertEquals(1, logCount)
    }

    @Test
    fun `workout completed main quest grants XP once`() = runTest {
        quest = activeMainQuest(
            tasks = listOf(task(isCompleted = true, exerciseId = 10))
        )
        arrangeMutableQuestAndPlayer()
        val useCase = useCase()

        useCase(quest.id, mode = QuestCompletionMode.TaskUpdate)
        useCase(quest.id, mode = QuestCompletionMode.TaskUpdate)

        assertEquals(DomainQuestStatus.COMPLETED, quest.status)
        assertEquals(100, player.xpTotal)
        assertEquals(1, logCount)
    }

    @Test
    fun `system protocol main quest grants reduced XP`() = runTest {
        quest = activeMainQuest(
            title = "NO EXCUSE PROTOCOL",
            tasks = listOf(task(isCompleted = true, exerciseId = -10_001))
        )
        arrangeMutableQuestAndPlayer()
        val useCase = useCase()

        val result = useCase(quest.id, mode = QuestCompletionMode.TaskUpdate)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.rewardGranted)
        assertEquals(DomainQuestStatus.COMPLETED, quest.status)
        assertEquals(30, player.xpTotal)
        assertEquals(1, player.currentStreak)
    }

    @Test
    fun `incomplete quest at day finalization fails without reward`() = runTest {
        quest = activeMainQuest(
            tasks = listOf(task(isCompleted = false, exerciseId = 10))
        )
        player = player(currentStreak = 3)
        arrangeMutableQuestAndPlayer()
        val useCase = useCase()

        val result = useCase(quest.id, mode = QuestCompletionMode.DayFinalization)

        assertTrue(result is Result.Success)
        assertFalse((result as Result.Success).data.rewardGranted)
        assertEquals(DomainQuestStatus.FAILED, quest.status)
        assertEquals(0, player.xpTotal)
        assertEquals(0, player.currentStreak)
        assertEquals(1, logCount)
    }

    private fun arrangeMutableQuestAndPlayer() {
        coEvery { questRepository.getQuestById(quest.id) } answers { quest }
        coEvery { questRepository.updateQuestStatus(quest.id, any()) } answers {
            quest = quest.copy(status = invocation.args[1] as DomainQuestStatus)
        }
        coEvery { questRepository.logQuestResult(any(), any(), any()) } answers {
            logCount += 1
        }
        every { playerRepository.getPlayer() } answers { flowOf(player) }
        coEvery { playerRepository.getPlayerSnapshot() } answers { player }
        coEvery { playerRepository.updatePlayer(any()) } answers {
            player = invocation.args[0] as Player
            Result.Success(Unit)
        }
    }

    private fun useCase(): CompleteQuestUseCase =
        CompleteQuestUseCase(
            transactionProvider = RecordingTransactionProvider(),
            questRepository = questRepository,
            playerRepository = playerRepository,
            logger = logger
        )

    private fun completedMainQuest(): Quest =
        activeMainQuest(tasks = listOf(task(isCompleted = true, exerciseId = 10)))

    private fun activeMainQuest(
        tasks: List<QuestTask>,
        title: String = "Workout"
    ): Quest =
        Quest(
            id = 42,
            title = title,
            type = DomainQuestType.MAIN,
            date = 0L,
            status = DomainQuestStatus.ACTIVE,
            tasks = tasks,
            scheduleId = 1
        )

    private fun task(isCompleted: Boolean, exerciseId: Int? = null): QuestTask =
        QuestTask(
            id = 7,
            questId = 42,
            name = "Bench press",
            isCompleted = isCompleted,
            exerciseId = exerciseId
        )

    private fun player(currentStreak: Int = 0): Player =
        Player(
            id = 1,
            name = "Player",
            level = 1,
            playerClass = PlayerRank.NOVICE,
            height = 180f,
            currentMonth = 1,
            currentWeek = 1,
            currentCycleDay = 1,
            currentStreak = currentStreak,
            maxStreak = currentStreak
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
}
