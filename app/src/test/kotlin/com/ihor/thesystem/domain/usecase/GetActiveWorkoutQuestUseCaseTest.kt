package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.repository.QuestRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetActiveWorkoutQuestUseCaseTest {

    private val questRepository: QuestRepository = mockk()
    private val useCase = GetActiveWorkoutQuestUseCase(questRepository)

    @Test
    fun `returns active main quest for matching schedule`() = runTest {
        val matchingQuest = quest(id = 10, type = DomainQuestType.MAIN, scheduleId = 4)
        every { questRepository.getActiveQuests() } returns flowOf(
            listOf(
                quest(id = 9, type = DomainQuestType.DAILY, scheduleId = 4),
                quest(id = 11, type = DomainQuestType.MAIN, scheduleId = 2),
                matchingQuest
            )
        )

        val result = useCase(scheduleId = 4).first()

        assertEquals(matchingQuest, result)
    }

    @Test
    fun `returns null when no active main quest matches schedule`() = runTest {
        every { questRepository.getActiveQuests() } returns flowOf(
            listOf(quest(id = 11, type = DomainQuestType.MAIN, scheduleId = 2))
        )

        val result = useCase(scheduleId = 4).first()

        assertNull(result)
    }

    @Test
    fun `returns null when schedule is missing`() = runTest {
        val result = useCase(scheduleId = null).first()

        assertNull(result)
    }

    private fun quest(
        id: Int,
        type: DomainQuestType,
        scheduleId: Int?
    ) = Quest(
        id = id,
        title = "Quest $id",
        type = type,
        date = 0L,
        status = DomainQuestStatus.ACTIVE,
        tasks = emptyList(),
        scheduleId = scheduleId
    )
}
