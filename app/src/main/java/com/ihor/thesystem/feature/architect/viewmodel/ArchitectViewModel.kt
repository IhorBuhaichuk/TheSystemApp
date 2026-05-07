package com.ihor.thesystem.feature.architect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.AiDashboardData
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
        loadInitialContext()
        observeDashboardData()
    }

    fun refreshForCurrentData() {
        loadInitialContext()
        loadChatHistory(0L)
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
                        text = UiText.StringResource(R.string.architect_initial_message, listOf(context)),
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
                        text = UiText.StringResource(R.string.architect_no_data),
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
        
        viewModelScope.launch {
            // 1. Отримуємо актуальний контекст
            val context = getLastWorkoutContext() ?: return@launch
            
            // 2. Додаємо повідомлення користувача
            val userMsg = ChatMessage(
                role = ChatRole.USER, 
                text = UiText.StringResource(R.string.architect_btn_send_analysis)
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
                    analysisAlreadySent = true
                ) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to send workout context for architect analysis")
                _uiEvents.emit(UiEvent.ShowError(
                    UiText.StringResource(R.string.error_operation_failed)
                ))
                
                val errorMsg = ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = UiText.StringResource(
                        R.string.error_network_architect, 
                        listOf(e.message ?: "")
                    ),
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
                applyAiRecommendations(recs)
                
                val systemMsg = ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = UiText.StringResource(R.string.architect_directives_applied)
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
}

private fun AiDashboardData.toUiState(): AiDashboardUiState {
    return AiDashboardUiState(
        isLoading = false,
        shortConclusion = shortConclusion,
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
