package com.ihor.thesystem.data.repository_impl

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.data.remote.ai.AiErrorClassifier
import com.ihor.thesystem.data.remote.ai.AiFailureType
import com.ihor.thesystem.domain.repository.LiveCoachRepository
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class LiveCoachRepositoryImpl @Inject constructor(
    @param:Named("LiveCoachModel") private val generativeModel: GenerativeModel,
    private val dispatchers: DispatcherProvider
) : LiveCoachRepository {

    override suspend fun sendMessage(history: List<Content>, newMessage: String): String =
        withContext(dispatchers.io) {
            if (!isApiKeyConfigured()) {
                Timber.e("Gemini API key is not configured for LiveCoach.")
                return@withContext CONFIGURATION_ERROR_MESSAGE
            }

            try {
                retry(maxAttempts = 2, initialDelay = 1_000L) {
                    withTimeout(30_000L) {
                        val chat = generativeModel.startChat(history)
                        val response = chat.sendMessage(newMessage)
                        response.text ?: throw IllegalStateException("Empty AI response")
                    }
                }
            } catch (error: Exception) {
                if (error is CancellationException && error !is TimeoutCancellationException) throw error

                val failureType = AiErrorClassifier.classify(error)
                Timber.e(error, "LiveCoach request failed: $failureType")
                failureType.toUserMessage()
            }
        }

    private fun isApiKeyConfigured(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return apiKey.isNotBlank() && apiKey != "null"
    }

    private suspend fun <T> retry(
        maxAttempts: Int,
        initialDelay: Long,
        block: suspend () -> T
    ): T {
        var delayMillis = initialDelay
        var lastError: Throwable? = null

        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (error: Exception) {
                if (error is CancellationException && error !is TimeoutCancellationException) throw error
                lastError = error

                val hasAttemptsLeft = attempt < maxAttempts - 1
                if (!hasAttemptsLeft || !AiErrorClassifier.isRetryable(error)) {
                    throw error
                }

                Timber.w(error, "LiveCoach retry ${attempt + 1} failed. Retrying in ${delayMillis}ms.")
                delay(delayMillis)
                delayMillis = (delayMillis * 2).coerceAtMost(2_000L)
            }
        }

        throw lastError ?: IllegalStateException("LiveCoach retry failed without an error.")
    }

    private fun AiFailureType.toUserMessage(): String =
        when (this) {
            AiFailureType.Timeout -> TIMEOUT_ERROR_MESSAGE
            AiFailureType.RateLimit -> RATE_LIMIT_ERROR_MESSAGE
            AiFailureType.Overloaded -> OVERLOADED_ERROR_MESSAGE
            AiFailureType.MalformedResponse,
            AiFailureType.Unknown -> DEFAULT_ERROR_MESSAGE
        }

    companion object {
        private const val CONFIGURATION_ERROR_MESSAGE =
            "AI-наставник не налаштований. Перевірте конфігурацію."
        private const val TIMEOUT_ERROR_MESSAGE =
            "Час очікування вичерпано. Спробуйте пізніше."
        private const val RATE_LIMIT_ERROR_MESSAGE =
            "Забагато запитів. Будь ласка, зачекайте."
        private const val OVERLOADED_ERROR_MESSAGE =
            "Сервери AI тимчасово перевантажені. Спробуйте пізніше."
        private const val DEFAULT_ERROR_MESSAGE =
            "Помилка зв'язку з тренером."
    }
}
