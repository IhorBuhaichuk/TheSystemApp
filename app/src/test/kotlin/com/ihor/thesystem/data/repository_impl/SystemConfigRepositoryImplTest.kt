package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.SystemConfigDao
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SystemConfigRepositoryImplTest {

    private val dao: SystemConfigDao = mockk(relaxed = true)
    private val repository = SystemConfigRepositoryImpl(dao)

    @Test
    fun `setNeedsDailyInit updates only daily init flag`() = runTest {
        coEvery { dao.ensureConfigExists() } returns Unit
        coEvery { dao.updateNeedsDailyInit(true) } returns Unit

        repository.setNeedsDailyInit(true)

        coVerifyOrder {
            dao.ensureConfigExists()
            dao.updateNeedsDailyInit(true)
        }
        verify(exactly = 0) { dao.getConfigFlow() }
    }

    @Test
    fun `saveLastInitDate updates only last init date`() = runTest {
        coEvery { dao.ensureConfigExists() } returns Unit
        coEvery { dao.updateLastInitEpochDay(20_510L) } returns Unit

        repository.saveLastInitDate(20_510L)

        coVerifyOrder {
            dao.ensureConfigExists()
            dao.updateLastInitEpochDay(20_510L)
        }
        verify(exactly = 0) { dao.getConfigFlow() }
    }
}
