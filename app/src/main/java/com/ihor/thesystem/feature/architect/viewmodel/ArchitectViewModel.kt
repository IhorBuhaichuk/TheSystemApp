package com.ihor.thesystem.feature.architect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.ChatRepository
import com.ihor.thesystem.domain.usecase.ApplyAiRecommendationsUseCase
import com.ihor.thesystem.domain.usecase.GetLastWorkoutContextUseCase
import com.ihor.thesystem.domain.usecase.SendArchitectAnalysisUseCase
import com.ihor.thesystem.domain.usecase.SendLiveCoachMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchitectViewModel @Inject constructor(
    private val getLastWorkoutContext: GetLastWorkoutContextUseCase,
    private val applyAiRecommendations: ApplyAiRecommendationsUseCase,
    private val sendArchitectAnalysis: SendArchitectAnalysisUseCase,
    private val sendLiveCoachMessage: SendLiveCoachMessageUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchitectUiState())
    val uiState: StateFlow<ArchitectUiState> = _uiState.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        loadInitialContext()
    }

    private fun loadInitialContext() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val context = getLastWorkoutContext()
            
            // Перевірка чи вже був відправлений аналіз (наявність відповіді від моделі)
            val history = chatRepository.getChatHistory(0L).first()
            val analysisAlreadySent = history.any { it.role == ChatRole.AI }

            if (context != null) {
                val initialMessages = listOf(
                    ChatMessage(
                        role = ChatRole.SYSTEM,
                        text = "Вітаю. Ось результати вашого останнього тренування:\n$context\n\nБажаєте надіслати ці результати на аналіз Архітектору?",
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
                        text = "СИСТЕМА: Відсутні дані для аналізу. Завершіть хоча б один тренувальний цикл, щоб Архітектор міг проаналізувати прогрес.",
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

    /**
     * Завантажує історію чату для конкретної сесії.
     */
    fun loadChatHistory(sessionId: Long) {
        viewModelScope.launch {
            chatRepository.getChatHistory(sessionId).collect { history ->
                _chatHistory.value = history
            }
        }
    }

    /**
     * Відправляє повідомлення живому тренеру.
     */
    fun sendMessage(sessionId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                sendLiveCoachMessage(sessionId, text)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Відправляє контекст останнього тренування на аналіз до ШІ.
     */
    fun sendForAnalysis() {
        viewModelScope.launch {
            // 1. Отримуємо актуальний контекст
            val context = getLastWorkoutContext() ?: return@launch
            
            // 2. Додаємо повідомлення користувача
            val userMsg = ChatMessage(role = ChatRole.USER, text = "Надіслати результати на аналіз")
            
            // 3. Вмикаємо завантаження та вимикаємо кнопку в попередньому системному повідомленні
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
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка операції"))
                
                val errorMsg = ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = "ПОМИЛКА МЕРЕЖІ: ${e.message ?: "Невідома помилка зв'язку з архітектором"}",
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
                    text = "Директиви отримано. Матрицю прогресії оновлено. Ваші цілі на наступне тренування скориговано."
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
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowError(e.localizedMessage ?: "Помилка застосування рекомендацій"))
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
