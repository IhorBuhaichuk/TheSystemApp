package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AiArchitectInsight
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SendArchitectAnalysisUseCaseTest {

    private val aiArchitectRepository: AiArchitectRepository = mockk()
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val useCase = SendArchitectAnalysisUseCase(
        aiArchitectRepository = aiArchitectRepository,
        chatRepository = chatRepository
    )

    @Test
    fun `successful actionable architect analysis is persisted for session gate`() = runTest {
        val response = ChatMessage(
            role = ChatRole.AI,
            text = MessageText.DynamicString("analysis ready"),
            recommendations = listOf(
                AiWorkoutRecommendation(
                    exerciseId = 7,
                    weight = 80f,
                    sets = 3,
                    reps = "5"
                )
            ),
            isActionable = true
        )
        coEvery { aiArchitectRepository.getChatResponse(any()) } returns response

        val result = useCase("workout context")

        assertEquals(response, result)
        coVerify {
            chatRepository.saveChatMessage(
                sessionId = 0L,
                role = ChatRole.AI,
                text = "analysis ready"
            )
        }
    }

    @Test
    fun `non actionable empty architect response is not persisted as completed analysis`() = runTest {
        val response = ChatMessage(
            role = ChatRole.AI,
            text = MessageText.DynamicString("temporary error"),
            recommendations = emptyList(),
            isActionable = false
        )
        coEvery { aiArchitectRepository.getChatResponse(any()) } returns response

        val result = useCase("workout context")

        assertEquals(response, result)
        coVerify(exactly = 0) {
            chatRepository.saveChatMessage(any(), any(), any())
        }
    }

    @Test
    fun `architect insight without target mutation is persisted for session gate`() = runTest {
        val response = ChatMessage(
            role = ChatRole.AI,
            text = MessageText.DynamicString("weekly insight"),
            recommendations = emptyList(),
            isActionable = false,
            architectInsight = AiArchitectInsight(
                weeklyInsight = "Тиждень стабільний.",
                actionableSuggestions = listOf("Залиш план без змін."),
                recoveryRisk = "Ризик відновлення низький."
            )
        )
        coEvery { aiArchitectRepository.getChatResponse(any()) } returns response

        val result = useCase("workout context")

        assertEquals(response, result)
        coVerify {
            chatRepository.saveChatMessage(
                sessionId = 0L,
                role = ChatRole.AI,
                text = "weekly insight"
            )
        }
    }

    @Test
    fun `architect prompt forbids kg targets for exercises without external load`() = runTest {
        val prompt = slot<String>()
        coEvery { aiArchitectRepository.getChatResponse(capture(prompt)) } returns ChatMessage(
            role = ChatRole.AI,
            text = MessageText.DynamicString("analysis ready"),
            recommendations = emptyList()
        )

        useCase("Exercise: Push-up\n- Metric type: no external load")

        assertTrue("no external load" in prompt.captured)
        assertTrue("do not add it to next_workout_targets" in prompt.captured)
        assertTrue("do not propose kg" in prompt.captured)
    }

    @Test
    fun `architect prompt uses v2 concise safe contract`() = runTest {
        val prompt = slot<String>()
        coEvery { aiArchitectRepository.getChatResponse(capture(prompt)) } returns ChatMessage(
            role = ChatRole.AI,
            text = MessageText.DynamicString("analysis ready"),
            recommendations = emptyList()
        )

        useCase("workout context")

        assertTrue("AI explains trends and suggests" in prompt.captured)
        assertTrue("The deterministic System decides" in prompt.captured)
        assertTrue("ValidateDirectivesUseCase is the final gatekeeper" in prompt.captured)
        assertTrue("\"weekly_insight\"" in prompt.captured)
        assertTrue("\"actionable_suggestions\"" in prompt.captured)
        assertTrue("\"recovery_risk\"" in prompt.captured)
        assertTrue("Avoid motivational essays" in prompt.captured)
    }
}
