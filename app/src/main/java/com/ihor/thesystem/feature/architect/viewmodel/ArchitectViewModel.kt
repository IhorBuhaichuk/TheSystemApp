package com.ihor.thesystem.feature.architect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.data.remote.ai.AiAvailabilityProvider
import com.ihor.thesystem.data.remote.ai.AiAvailabilityState
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.AiDashboardData
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.model.MessageTextKey
import com.ihor.thesystem.domain.repository.ChatRepository
import com.ihor.thesystem.domain.usecase.ApplyAiRecommendationsUseCase
import com.ihor.thesystem.domain.usecase.GetAiDashboardDataUseCase
import com.ihor.thesystem.domain.usecase.GetLastWorkoutContextUseCase
import com.ihor.thesystem.domain.usecase.SendArchitectAnalysisUseCase
import com.ihor.thesystem.domain.usecase.SendLiveCoachMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArchitectViewModel @Inject constructor(
    private val getLastWorkoutContext: GetLastWorkoutContextUseCase,
    private val getAiDashboardData: GetAiDashboardDataUseCase,
    private val applyAiRecommendations: ApplyAiRecommendationsUseCase,
    private val sendArchitectAnalysis: SendArchitectAnalysisUseCase,
    private val sendLiveCoachMessage: SendLiveCoachMessageUseCase,
    private val aiAvailabilityProvider: AiAvailabilityProvider,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchitectUiState())
    val uiState: StateFlow<ArchitectUiState> = _uiState.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _dashboardState = MutableStateFlow(AiDashboardUiState())
    val dashboardState: StateFlow<AiDashboardUiState> = _dashboardState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        refreshAiAvailability()
        loadInitialContext()
        observeDashboardData()
    }

    fun refreshForCurrentData() {
        refreshAiAvailability()
        loadInitialContext()
        loadChatHistory(0L)
    }

    private fun refreshAiAvailability() {
        _uiState.update { it.copy(aiAvailability = aiAvailabilityProvider.current()) }
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            getAiDashboardData()
                .map { it.toUiState() }
                .catch { e ->
                    if (e is CancellationException) throw e
                    Timber.e(e, "Failed to observe AI dashboard data")
                    emit(AiDashboardUiState(isLoading = false))
                }
                .collect { state ->
                    _dashboardState.value = state
                }
        }
    }

    private fun loadInitialContext() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val context = getLastWorkoutContext()
            
            // Перевірка чи вже був відправлений аналіз (наявність відповіді від моделі)
            val analysisAlreadySent = chatRepository.hasAiResponse(0L)

            if (context != null) {
                val initialMessages = listOf(
                    ChatMessage(
                        role = ChatRole.SYSTEM,
                        text = MessageText.Resource(
                            key = MessageTextKey.ARCHITECT_INITIAL_MESSAGE,
                            args = listOf(context)
                        ),
                        isActionable = true
                    )
                )
                _uiState.update { 
                    it.copy(
                        messages = initialMessages, 
                        lastWorkoutContext = context,
                        isLoading = false,
                        analysisAlreadySent = analysisAlreadySent
                    ) 
                }
            } else {
                val initialMessages = listOf(
                    ChatMessage(
                        role = ChatRole.SYSTEM,
                        text = MessageText.Resource(MessageTextKey.ARCHITECT_NO_DATA),
                        isActionable = false
                    )
                )
                _uiState.update { 
                    it.copy(
                        messages = initialMessages,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    private var chatHistoryJob: Job? = null

    /**
     * Завантажує історію чату для конкретної сесії.
     */
    fun loadChatHistory(sessionId: Long) {
        chatHistoryJob?.cancel()
        chatHistoryJob = viewModelScope.launch {
            chatRepository.getChatHistory(sessionId).collect { history ->
                _chatHistory.value = history
            }
        }
    }

    /**
     * Відправляє повідомлення живому тренеру.
     * Викликається ВИКЛЮЧНО через подію від користувача (наприклад, натискання кнопки).
     */
    fun sendMessage(sessionId: Long, text: String) {
        if (text.isBlank() || _uiState.value.isLoading) return
        if (!isAiConfigured()) {
            emitAiUnavailable()
            return
        }
        
        viewModelScope.launch {
            // Встановлюємо стан завантаження, щоб заблокувати повторні натискання кнопки
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Прямий виклик UseCase. Він сам збереже повідомлення в БД.
                // Збір даних (collect) у loadChatHistory автоматично оновить UI при зміні БД.
                sendLiveCoachMessage(sessionId, text)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to send LiveCoach message")
                _uiEvents.emit(UiEvent.ShowError(
                    UiText.StringResource(R.string.error_operation_failed)
                ))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Відправляє контекст останнього тренування на аналіз до ШІ.
     */
    fun sendForAnalysis() {
        if (_uiState.value.isLoading) return
        if (!isAiConfigured()) {
            emitAiUnavailable()
            return
        }
        
        viewModelScope.launch {
            // 1. Отримуємо актуальний контекст
            val context = getLastWorkoutContext() ?: return@launch
            
            // 2. Додаємо повідомлення користувача
            val userMsg = ChatMessage(
                role = ChatRole.USER, 
                text = MessageText.Resource(MessageTextKey.ARCHITECT_SEND_ANALYSIS)
            )
            
            // 3. Вмикаємо завантаження
            _uiState.update { state ->
                val updatedMessages = state.messages.map { it.copy(isActionable = false) }
                state.copy(
                    messages = updatedMessages + userMsg,
                    isLoading = true
                )
            }

            // 4. Запит через UseCase аналізу
            try {
                val aiResponse = sendArchitectAnalysis(context)
                _uiState.update { it.copy(
                    messages = it.messages + aiResponse,
                    isLoading = false,
                    analysisAlreadySent = aiResponse.isCompletedArchitectAnalysis(),
                    latestInsight = aiResponse.architectInsight?.toUiModel() ?: it.latestInsight,
                    aiAvailability = aiResponse.availabilityFailure() ?: it.aiAvailability
                ) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to send workout context for architect analysis")
                _uiEvents.emit(UiEvent.ShowError(
                    UiText.StringResource(R.string.error_operation_failed)
                ))
                
                val errorMsg = ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = MessageText.Resource(MessageTextKey.ERROR_NETWORK_ARCHITECT),
                    isActionable = false
                )
                _uiState.update { it.copy(
                    messages = it.messages + errorMsg,
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Записує поради ШІ безпосередньо в матрицю прогресії.
     */
    fun applyRecommendations(recs: List<AiWorkoutRecommendation>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val applicationResult = applyAiRecommendations(recs)
                
                val systemMsg = ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = if (applicationResult.hasSystemCorrections) {
                        MessageText.DynamicString("Система скоригувала рекомендацію AI")
                    } else {
                        MessageText.Resource(MessageTextKey.ARCHITECT_DIRECTIVES_APPLIED)
                    }
                )
                
                _uiState.update { state ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.recommendations == recs) msg.copy(isActionable = false) else msg
                    }
                    state.copy(
                        messages = updatedMessages + systemMsg,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to apply AI recommendations")
                _uiEvents.emit(UiEvent.ShowError(
                    UiText.StringResource(R.string.error_apply_recommendations_failed)
                ))
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun isAiConfigured(): Boolean {
        refreshAiAvailability()
        return _uiState.value.aiAvailability == AiAvailabilityState.CONFIGURED
    }

    private fun emitAiUnavailable() {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowError(_uiState.value.aiAvailability.toUiText()))
        }
    }
}

private fun AiAvailabilityState.toUiText(): UiText =
    UiText.StringResource(
        when (this) {
            AiAvailabilityState.CONFIGURED -> R.string.error_ai_generic
            AiAvailabilityState.UNCONFIGURED -> R.string.error_ai_unconfigured
            AiAvailabilityState.RATE_LIMITED -> R.string.error_ai_rate_limit
            AiAvailabilityState.OVERLOADED -> R.string.error_ai_overloaded
            AiAvailabilityState.MALFORMED -> R.string.error_ai_parsing
        }
    )

private fun ChatMessage.availabilityFailure(): AiAvailabilityState? =
    when ((text as? MessageText.Resource)?.key) {
        MessageTextKey.ERROR_AI_UNCONFIGURED -> AiAvailabilityState.UNCONFIGURED
        MessageTextKey.ERROR_AI_RATE_LIMIT -> AiAvailabilityState.RATE_LIMITED
        MessageTextKey.ERROR_AI_OVERLOADED -> AiAvailabilityState.OVERLOADED
        MessageTextKey.ERROR_AI_PARSING -> AiAvailabilityState.MALFORMED
        else -> null
    }

private fun ChatMessage.isCompletedArchitectAnalysis(): Boolean =
    recommendations.isNotEmpty() || architectInsight?.hasSignal == true

private fun AiDashboardData.toUiState(): AiDashboardUiState {
    return AiDashboardUiState(
        isLoading = false,
        shortConclusion = shortConclusion,
        weeklyInsight = weeklyInsight,
        actionableSuggestions = actionableSuggestions,
        recoveryRisk = recoveryRisk,
        lastRecommendation = lastRecommendation?.let { recommendation ->
            AiRecommendationUiModel(
                exerciseName = recommendation.exerciseName,
                recommendedWeight = recommendation.recommendedWeight,
                recommendedSets = recommendation.recommendedSets,
                recommendedReps = recommendation.recommendedReps,
                feedback = recommendation.feedback
            )
        }
    )
}
