package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ChatRepository
import javax.inject.Inject

class SendArchitectAnalysisUseCase @Inject constructor(
    private val aiArchitectRepository: AiArchitectRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(workoutContext: String): ChatMessage {
        val prompt = """
            Ти - AI-архітектор тренувальної системи: уважний, спокійний і людяний.
            Проаналізуй результати останнього тренування, наведені нижче в блоці <workout_data>.
            
            <workout_data>
            $workoutContext
            </workout_data>

            Роль AI:
            - AI НЕ є джерелом істини і НЕ приймає фінальне рішення по плану.
            - Фінальне рішення завжди приймає deterministic System validator.
            - Твоя задача: коротко пояснити, що видно з даних, і запропонувати обережну рекомендацію.
            - Якщо рекомендація буде занадто агресивною, System її відхилить або обмежить.
            - Відповідь має бути тільки JSON за схемою нижче.

            У блоці <workout_data> може бути службовий блок "Стан річної прогресії".
            Це правило має найвищий пріоритет:
            - Якщо тривають перші 14 днів збору базових даних, система ще НЕ має графіка M0-M12. Не оцінюй річний план, не критикуй його відсутність, не пиши про стагнацію, провал або відставання. Тільки похвала, підтримка і м'яке заохочення.
            - Якщо базові 14 днів завершено і M0-M12 є в даних, оцінюй прогрес відносно цих цілей.
            - Якщо базові 14 днів завершено, але M0-M12 ще немає, скажи природно, що вже можна сформувати річний графік на 12 місяців з цілями на кожен місяць. Не роби з цього помилку користувача.

            Завдання: 
            1) Коротко відзначити, що в тренуванні вийшло добре.
            2) Оцінити річну прогресію тільки тоді, коли це дозволяє службовий блок і є дані M0-M12.
            3) Дати природний підтримуючий висновок без кіберпанку, пафосу, агресії, сорому і штучних метафор.
            4) Запропонувати вагу та повторення на наступне тренування тільки для вправ із зовнішньою вагою.
            
            ПРОТОКОЛ ПРОГРЕСІЇ:
            - Якщо користувач виконав цільову кількість повторень у всіх сетах, збільш вагу на 2.5-5% для наступного тренування.
            - Якщо користувач не виконав цільову кількість повторень хоча б в одному сеті, обережно зменш або утримай цільову вагу для наступного тренування. У тексті поясни це нейтрально, без критики.
            - Якщо в блоці вправи написано "без зовнішньої ваги", НЕ додавай цю вправу до next_workout_targets і НЕ пропонуй кг. Можна згадати її тільки в feedback_text.
            
            Значення exercise_id ОБОВ'ЯЗКОВО бери з наданого блоку <workout_data> (не вигадуй свої ID та не пиши 0).
            feedback_text має бути 2-4 короткі природні речення без заголовків, нумерації та слів "кіберпанк", "кіборг", "протокол", "іржавіти".
            aiFeedback має бути одним коротким природним реченням до вагової вправи.
            
            ВАЖЛИВО: Відповідь поверни СУВОРО у форматі JSON об'єкта наступної структури:
            {
              "feedback_text": "короткий природний фідбек",
              "next_workout_targets": [
                {
                  "exercise_id": 1,
                  "nextWeight": 50.0,
                  "nextSets": 3,
                  "nextReps": "8-10",
                  "aiFeedback": "короткий коментар до вправи"
                }
              ]
            }
            КРИТИЧНО: У текстах feedback_text та aiFeedback не використовуй лапки всередині значень і не додавай символи переносу рядка.
        """.trimIndent()
        
        val response = aiArchitectRepository.getChatResponse(prompt)
        if (response.recommendations.isNotEmpty()) {
            chatRepository.saveChatMessage(
                sessionId = ARCHITECT_ANALYSIS_SESSION_ID,
                role = ChatRole.AI,
                text = response.persistableText()
            )
        }
        return response
    }

    private fun ChatMessage.persistableText(): String =
        when (val value = text) {
            is MessageText.DynamicString -> value.value
            is MessageText.Resource -> aiFeedback ?: ARCHITECT_ANALYSIS_FALLBACK_TEXT
        }
}

private const val ARCHITECT_ANALYSIS_SESSION_ID = 0L
private const val ARCHITECT_ANALYSIS_FALLBACK_TEXT = "AI analysis complete"
