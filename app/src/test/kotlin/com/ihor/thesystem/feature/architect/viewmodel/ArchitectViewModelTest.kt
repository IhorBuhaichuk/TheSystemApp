package com.ihor.thesystem.feature.architect.viewmodel

import com.ihor.thesystem.data.remote.ai.AiAvailabilityProvider
import com.ihor.thesystem.data.remote.ai.AiAvailabilityState
import com.ihor.thesystem.domain.model.AiDashboardData
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.repository.ChatRepository
import com.ihor.thesystem.domain.usecase.ApplyAiRecommendationsUseCase
import com.ihor.thesystem.domain.usecase.GetAiDashboardDataUseCase
import com.ihor.thesystem.domain.usecase.GetLastWorkoutContextUseCase
import com.ihor.thesystem.domain.usecase.SendArchitectAnalysisUseCase
import com.ihor.thesystem.domain.usecase.SendLiveCoachMessageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchitectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getLastWorkoutContext: GetLastWorkoutContextUseCase = mockk()
    private val getAiDashboardData: GetAiDashboardDataUseCase = mockk()
    private val applyAiRecommendations: ApplyAiRecommendationsUseCase = mockk()
    private val sendArchitectAnalysis: SendArchitectAnalysisUseCase = mockk()
    private val sendLiveCoachMessage: SendLiveCoachMessageUseCase = mockk()
    private val chatRepository: ChatRepository = mockk()

    @Test
    fun `unconfigured ai is exposed and blocks live coach send`() {
        val viewModel = viewModel(apiKey = "", clientAiEnabled = true)
        mainDispatcherRule.advanceUntilIdle()

        assertEquals(AiAvailabilityState.UNCONFIGURED, viewModel.uiState.value.aiAvailability)

        viewModel.sendMessage(sessionId = 0L, text = "Підкажи по тренуванню")
        mainDispatcherRule.advanceUntilIdle()

        coVerify(exactly = 0) { sendLiveCoachMessage.invoke(any(), any()) }
    }

    @Test
    fun `configured ai allows live coach send`() {
        coEvery { sendLiveCoachMessage.invoke(0L, "Підкажи по тренуванню") } returns ChatMessage(
            role = ChatRole.AI,
            text = MessageText.DynamicString("Ок")
        )
        val viewModel = viewModel(apiKey = "dev-key", clientAiEnabled = true)
        mainDispatcherRule.advanceUntilIdle()

        assertEquals(AiAvailabilityState.CONFIGURED, viewModel.uiState.value.aiAvailability)

        viewModel.sendMessage(sessionId = 0L, text = "Підкажи по тренуванню")
        mainDispatcherRule.advanceUntilIdle()

        coVerify(exactly = 1) { sendLiveCoachMessage.invoke(0L, "Підкажи по тренуванню") }
    }

    private fun viewModel(
        apiKey: String,
        clientAiEnabled: Boolean
    ): ArchitectViewModel {
        coEvery { getLastWorkoutContext.invoke() } returns null
        coEvery { chatRepository.hasAiResponse(0L) } returns false
        every { chatRepository.getChatHistory(any()) } returns emptyFlow()
        every { getAiDashboardData.invoke() } returns flowOf(AiDashboardData())

        return ArchitectViewModel(
            getLastWorkoutContext = getLastWorkoutContext,
            getAiDashboardData = getAiDashboardData,
            applyAiRecommendations = applyAiRecommendations,
            sendArchitectAnalysis = sendArchitectAnalysis,
            sendLiveCoachMessage = sendLiveCoachMessage,
            aiAvailabilityProvider = AiAvailabilityProvider(
                apiKey = apiKey,
                clientAiEnabled = clientAiEnabled
            ),
            chatRepository = chatRepository
        )
    }
}
