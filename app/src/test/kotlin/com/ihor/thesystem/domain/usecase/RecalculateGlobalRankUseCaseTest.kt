package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RecalculateGlobalRankUseCaseTest {

    private val matrixRepo: ProgressionMatrixRepository = mockk()
    private val playerRepo: PlayerRepository = mockk()
    private val useCase = RecalculateGlobalRankUseCase(matrixRepo, playerRepo)

    private fun createEntry(rank: Rank) = ProgressionMatrixEntry(
        id = 0, exerciseId = 0, exerciseName = "", startWeight = 0f, targetWeight = 0f,
        currentWeight = 0f, targetWeightNote = null, weeklyStep = 0f, progressPercent = 0f,
        currentRank = rank
    )

    private val defaultPlayer = Player(
        id = 1, name = "Test", level = 1, playerClass = PlayerRank.NOVICE,
        height = 180f, currentMonth = 1, currentWeek = 1, currentCycleDay = 1,
        globalRank = Rank.E
    )

    @Test
    fun `5 entries with weights 1-5 result in median 3`() = runTest {
        val entries = listOf(Rank.E, Rank.D, Rank.C, Rank.B, Rank.A).map { createEntry(it) }
        coEvery { matrixRepo.getAllEntries() } returns flowOf(entries)
        coEvery { playerRepo.getPlayer() } returns flowOf(defaultPlayer)
        coEvery { playerRepo.updatePlayer(any()) } returns Result.Success(Unit)

        useCase()

        coVerify { playerRepo.updatePlayer(match { it.globalRank == Rank.C }) }
    }

    @Test
    fun `all entries same rank weight does not update DB if unchanged`() = runTest {
        val entries = listOf(Rank.E, Rank.E, Rank.E).map { createEntry(it) }
        coEvery { matrixRepo.getAllEntries() } returns flowOf(entries)
        coEvery { playerRepo.getPlayer() } returns flowOf(defaultPlayer) // Default is E

        useCase()

        coVerify(exactly = 0) { playerRepo.updatePlayer(any()) }
    }

    @Test
    fun `empty matrix returns early and does not update DB`() = runTest {
        coEvery { matrixRepo.getAllEntries() } returns flowOf(emptyList())

        useCase()

        coVerify(exactly = 0) { playerRepo.updatePlayer(any()) }
    }
}
