package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ChatRepository
import javax.inject.Inject

class SendArchitectAnalysisUseCase @Inject constructor(
    private val aiArchitectRepository: AiArchitectRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(workoutContext: String): ChatMessage {
        val prompt = """
            Ти — жорсткий AI-тренер Системи. 
            Проаналізуй результати останнього тренування, наведені нижче в блоці <workout_data>.
            
            <workout_data>
            $workoutContext
            </workout_data>

            Завдання: 
            1) Дати оцінку тренуванню. 
            2) Оцінити прогрес відносно довгострокового річного плану. 
            3) Надати мотиваційний блок у кіберпанк-стилі. 
            4) Запропонувати вагу та повторення на наступне тренування (збільш вагу на 2.5-5% при успіху). 
            
            Значення exercise_id ОБОВ'ЯЗКОВО бери з наданого блоку <workout_data> (не вигадуй свої ID та не пиши 0).
            
            ВАЖЛИВО: Відповідь поверни СУВОРО у форматі JSON об'єкта наступної структури:
            {
              "feedback_text": "Твій текст з пунктами 1,2,3",
              "next_workout_targets": [
                {
                  "exercise_id": 1,
                  "nextWeight": 50.0,
                  "nextSets": 3,
                  "nextReps": "8-10",
                  "aiFeedback": "Короткий коментар до вправи"
                }
              ]
            }
        """.trimIndent()
        
        return aiArchitectRepository.getChatResponse(prompt)
    }
}
