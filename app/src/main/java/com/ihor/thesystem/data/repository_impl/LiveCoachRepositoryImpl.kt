package com.ihor.thesystem.data.repository_impl

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.data.remote.ai.AiAvailabilityProvider
import com.ihor.thesystem.data.remote.ai.AiAvailabilityState
import com.ihor.thesystem.data.remote.ai.AiErrorClassifier
import com.ihor.thesystem.data.remote.ai.AiFailureType
import com.ihor.thesystem.data.remote.ai.toAvailabilityState
import com.ihor.thesystem.domain.model.AiConversationMessage
import com.ihor.thesystem.domain.model.AiConversationRole
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
    private val availabilityProvider: AiAvailabilityProvider,
    private val dispatchers: DispatcherProvider
) : LiveCoachRepository {

    override suspend fun sendMessage(history: List<AiConversationMessage>, newMessage: String): String =
        withContext(dispatchers.io) {
            val availability = availabilityProvider.current()
            if (availability != AiAvailabilityState.CONFIGURED) {
                Timber.w("LiveCoach unavailable: $availability")
                return@withContext availability.toUserMessage()
            }

            try {
                retry(maxAttempts = 2, initialDelay = 1_000L) {
                    withTimeout(30_000L) {
                        val chat = generativeModel.startChat(history.toGeminiContent())
                        val response = chat.sendMessage(newMessage)
                        response.text ?: throw IllegalStateException("Empty AI response")
                    }
                }
            } catch (error: Exception) {
                if (error is CancellationException && error !is TimeoutCancellationException) throw error

                val failureType = AiErrorClassifier.classify(error)
                val failureState = failureType.toAvailabilityState()
                Timber.e(error, "LiveCoach request failed: $failureState")
                failureState.toUserMessage(fallback = failureType.toUserMessage())
            }
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
            AiFailureType.MalformedResponse -> MALFORMED_ERROR_MESSAGE
            AiFailureType.Unknown -> DEFAULT_ERROR_MESSAGE
        }

    private fun AiAvailabilityState.toUserMessage(
        fallback: String = DEFAULT_ERROR_MESSAGE
    ): String =
        when (this) {
            AiAvailabilityState.CONFIGURED -> fallback
            AiAvailabilityState.UNCONFIGURED -> CONFIGURATION_ERROR_MESSAGE
            AiAvailabilityState.RATE_LIMITED -> RATE_LIMIT_ERROR_MESSAGE
            AiAvailabilityState.OVERLOADED -> OVERLOADED_ERROR_MESSAGE
            AiAvailabilityState.MALFORMED -> MALFORMED_ERROR_MESSAGE
        }

    private fun List<AiConversationMessage>.toGeminiContent() =
        map { message ->
            content(role = message.role.toGeminiRole()) {
                text(message.text)
            }
        }

    private fun AiConversationRole.toGeminiRole(): String =
        when (this) {
            AiConversationRole.USER -> "user"
            AiConversationRole.MODEL -> "model"
        }

    companion object {
        private const val CONFIGURATION_ERROR_MESSAGE =
            "ШІ-наставник не налаштований. Тренування, записи й прогрес працюють без нього."
        private const val TIMEOUT_ERROR_MESSAGE =
            "ШІ не відповів вчасно. Тренування й прогрес продовжують працювати."
        private const val RATE_LIMIT_ERROR_MESSAGE =
            "Ліміт запитів до ШІ вичерпано. Тренування й прогрес продовжують працювати."
        private const val OVERLOADED_ERROR_MESSAGE =
            "ШІ тимчасово перевантажений. Тренування й прогрес продовжують працювати."
        private const val MALFORMED_ERROR_MESSAGE =
            "Відповідь ШІ не вдалося прочитати, тому її не застосовано."
        private const val DEFAULT_ERROR_MESSAGE =
            "ШІ-наставник зараз недоступний. Тренування й прогрес продовжують працювати."
    }
}
