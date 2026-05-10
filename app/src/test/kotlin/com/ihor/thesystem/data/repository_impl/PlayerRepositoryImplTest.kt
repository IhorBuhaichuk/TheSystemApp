package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.data.local.room.dao.PlayerDao
import com.ihor.thesystem.data.local.room.dao.WeightLogDao
import com.ihor.thesystem.data.local.room.entity.PlayerEntity
import com.ihor.thesystem.data.local.room.entity.WeightLogEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class PlayerRepositoryImplTest {

    private val playerDao: PlayerDao = mockk()
    private val weightLogDao: WeightLogDao = mockk()
    private val clock = object : AppClock {
        override fun now(): Long = FIXED_NOW
        override fun zoneId(): ZoneId = ZoneId.of("Europe/Kyiv")
    }
    private val repository = PlayerRepositoryImpl(playerDao, weightLogDao, clock)

    @Test
    fun `logWeight stores timestamp from AppClock`() = runTest {
        coEvery { weightLogDao.insert(any()) } returns Unit

        val result = repository.logWeight(82.5f)

        assertTrue(result is Result.Success)
        coVerify {
            weightLogDao.insert(
                match<WeightLogEntity> { log ->
                    log.weight == 82.5f && log.timestamp == FIXED_NOW
                }
            )
        }
    }

    @Test
    fun `getPlayerSnapshot reads the synchronous player row`() = runTest {
        coEvery { playerDao.getPlayerSync() } returns PlayerEntity(name = "Snapshot")

        val player = repository.getPlayerSnapshot()

        assertEquals("Snapshot", player?.name)
        coVerify { playerDao.getPlayerSync() }
    }

    private companion object {
        const val FIXED_NOW = 1_765_000_000_000L
    }
}
