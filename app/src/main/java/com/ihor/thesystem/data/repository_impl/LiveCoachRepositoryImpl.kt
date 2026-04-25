package com.ihor.thesystem.data.repository_impl

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException

class LiveCoachRepositoryImpl @Inject constructor(
    @Named("LiveCoachModel") private val generativeModel: GenerativeModel
) : LiveCoachRepository {

    override suspend fun sendMessage(history: List<Content>, newMessage: String): String = withContext(Dispatchers.IO) {
        try {
            performRetry(maxAttempts = 2, currentAttempt = 1, delayMillis = 1000L) {
                withTimeout(30_000L) {
                    val chat = generativeModel.startChat(history)
                    val response = chat.sendMessage(newMessage)
                    response.text ?: throw IllegalStateException("Empty AI response")
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException && e !is TimeoutCancellationException) throw e

            when {
                e is TimeoutCancellationException -> {
                    Timber.e(e, "LiveCoach request timed out")
                    TIMEOUT_ERROR_MESSAGE
                }

                e.message?.contains("429") == true -> {
                    Timber.e(e, "LiveCoach rate limit reached (429)")
                    RATE_LIMIT_ERROR_MESSAGE
                }

                else -> {
                    Timber.e(e, "LiveCoach error: ${e.message}")
                    DEFAULT_ERROR_MESSAGE
                }
            }
        }
    }

    /**
     * Рекурсивна функція для повторних спроб за зразком AiArchitectRepositoryImpl
     */
    private suspend fun <T> performRetry(
        maxAttempts: Int,
        currentAttempt: Int,
        delayMillis: Long,
        block: suspend () -> T
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            if (e is CancellationException && e !is TimeoutCancellationException) throw e

            val isRetryable = e is TimeoutCancellationException || e.message?.contains("429") == true

            if (!isRetryable || currentAttempt >= maxAttempts) {
                throw e
            }

            Timber.w(e, "Retry attempt $currentAttempt failed for LiveCoach. Retrying in ${delayMillis}ms...")
            delay(delayMillis)
            performRetry(
                maxAttempts = maxAttempts,
                currentAttempt = currentAttempt + 1,
                delayMillis = delayMillis,
                block = block
            )
        }
    }

    companion object {
        private const val TIMEOUT_ERROR_MESSAGE = "Час очікування вичерпано. Спробуйте пізніше."
        private const val RATE_LIMIT_ERROR_MESSAGE = "Забагато запитів. Будь ласка, зачекайте."
        private const val DEFAULT_ERROR_MESSAGE = "Помилка зв'язку з тренером."
    }
}
