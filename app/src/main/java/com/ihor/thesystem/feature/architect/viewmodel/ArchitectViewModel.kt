package com.ihor.thesystem.feature.architect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.usecase.ApplyAiRecommendationsUseCase
import com.ihor.thesystem.domain.usecase.GetLastWorkoutContextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchitectViewModel @Inject constructor(
    private val getLastWorkoutContext: GetLastWorkoutContextUseCase,
    private val applyAiRecommendations: ApplyAiRecommendationsUseCase,
    private val aiRepository: AiArchitectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchitectUiState())
    val uiState: StateFlow<ArchitectUiState> = _uiState.asStateFlow()

    init {
        loadInitialContext()
    }

    private fun loadInitialContext() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val context = getLastWorkoutContext()
            
            val welcomeMessage = if (context != null) {
                ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = "Вітаю. Ось результати вашого останнього тренування:\n\n$context\n\nБажаєте надіслати ці результати на аналіз Архітектору?",
                    isActionable = true
                )
            } else {
                ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = "Вітаю. Наразі в базі даних відсутні записи про ваші тренування. Виконайте хоча б один квест, щоб я міг проаналізувати прогрес."
                )
            }

            _uiState.update { 
                it.copy(
                    messages = listOf(welcomeMessage),
                    lastWorkoutContext = context,
                    isLoading = false
                ) 
            }
        }
    }

    /**
     * Відправляє контекст останнього тренування на аналіз до ШІ.
     */
    fun sendForAnalysis() {
        val context = _uiState.value.lastWorkoutContext ?: return
        
        viewModelScope.launch {
            // 1. Додаємо повідомлення користувача
            val userMsg = ChatMessage(role = ChatRole.USER, text = "Надіслати результати на аналіз")
            
            // 2. Вмикаємо завантаження та вимикаємо кнопку в попередньому системному повідомленні
            _uiState.update { state ->
                val updatedMessages = state.messages.map { it.copy(isActionable = false) }
                state.copy(
                    messages = updatedMessages + userMsg,
                    isLoading = true
                )
            }

            // 3. Формуємо промпт
            val prompt = """
                Ти — жорсткий AI-тренер Системи. 
                Проаналізуй результати останнього тренування: $context. 
                Завдання: 
                1) Дати оцінку тренуванню. 
                2) Оцінити прогрес відносно довгострокового річного плану. 
                3) Надати мотиваційний блок у кіберпанк-стилі. 
                4) Запропонувати вагу та повторення на наступне тренування (збільш вагу на 2.5-5% при успіху). 
                ВАЖЛИВО: Відповідь поверни СУВОРО у форматі JSON: {"feedback_text": "Твій текст з пунктами 1,2,3", "next_workout_targets": [{"exercise_id": ID, "weight": 50.0, "reps": 8}]}
            """.trimIndent()

            // 4. Запит до репозиторію
            val aiResponse = aiRepository.getChatResponse(prompt)
            
            _uiState.update { it.copy(
                messages = it.messages + aiResponse,
                isLoading = false
            ) }
        }
    }

    /**
     * Записує поради ШІ безпосередньо в матрицю прогресії.
     */
    fun applyRecommendations(recs: List<AiWorkoutRecommendation>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
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
        }
    }
}
