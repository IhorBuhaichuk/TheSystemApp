package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.Player
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.ValidationError
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerUseCasesTest {

    private val repository: PlayerRepository = mockk(relaxed = true)

    @Test
    fun `update name returns validation error without writing invalid player`() = runTest {
        val result = UpdatePlayerNameUseCase(repository)(player, "")

        assertEquals(Result.Error(ValidationError.INVALID_PLAYER_NAME), result)
        coVerify(exactly = 0) { repository.updatePlayer(any()) }
    }

    @Test
    fun `log weight propagates repository errors`() = runTest {
        coEvery { repository.logWeight(82f) } returns Result.Error(DataError.Local.SQLITE_EXCEPTION)

        val result = LogWeightUseCase(repository)(82f)

        assertEquals(Result.Error(DataError.Local.SQLITE_EXCEPTION), result)
    }

    private companion object {
        val player = Player(
            id = 1,
            name = "Hunter",
            level = 1,
            playerClass = PlayerRank.NOVICE,
            height = 180f,
            currentMonth = 1,
            currentWeek = 1,
            currentCycleDay = 1
        )
    }
}
