package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.repository.ChatRepository
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SendLiveCoachMessageUseCaseTest {

    private val liveCoachRepository: LiveCoachRepository = mockk()
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val useCase = SendLiveCoachMessageUseCase(
        liveCoachRepository = liveCoachRepository,
        chatRepository = chatRepository
    )

    @Test
    fun `live coach failure returns friendly local mode text without raw exception`() = runTest {
        coEvery { chatRepository.getRecentHistory(SESSION_ID, 20) } returns listOf(
            ChatMessage(
                role = ChatRole.USER,
                text = MessageText.DynamicString(USER_MESSAGE)
            )
        )
        coEvery { liveCoachRepository.sendMessage(any(), USER_MESSAGE) } throws
            IllegalStateException("RAW_SECRET_503")

        val result = useCase(sessionId = SESSION_ID, userMessage = USER_MESSAGE)

        val text = (result.text as MessageText.DynamicString).value
        assertTrue(text.contains("локально"))
        assertFalse(text.contains("RAW_SECRET_503"))
        coVerify(exactly = 1) {
            chatRepository.saveChatMessage(SESSION_ID, ChatRole.USER, USER_MESSAGE)
        }
        coVerify(exactly = 0) {
            chatRepository.saveChatMessage(SESSION_ID, ChatRole.AI, any())
        }
    }

    private companion object {
        const val SESSION_ID = 7L
        const val USER_MESSAGE = "Підкажи по тренуванню"
    }
}
