package com.ihor.thesystem.data.repository_impl

import com.google.ai.client.generativeai.GenerativeModel
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.data.remote.ai.AiAvailabilityProvider
import com.ihor.thesystem.data.remote.ai.AiAvailabilityState
import com.ihor.thesystem.data.remote.ai.AiArchitectResponseParser
import com.ihor.thesystem.data.remote.ai.AiErrorClassifier
import com.ihor.thesystem.data.remote.ai.AiFailureType
import com.ihor.thesystem.data.remote.ai.AiMalformedResponseException
import com.ihor.thesystem.data.remote.ai.toAvailabilityState
import com.ihor.thesystem.domain.model.ChatMessage
import com.ihor.thesystem.domain.model.ChatRole
import com.ihor.thesystem.domain.model.MessageText
import com.ihor.thesystem.domain.model.MessageTextKey
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class AiArchitectRepositoryImpl @Inject constructor(
    @param:Named("ArchitectModel") private val generativeModel: GenerativeModel,
    private val availabilityProvider: AiAvailabilityProvider,
    private val dispatchers: DispatcherProvider
) : AiArchitectRepository {

    private val parser = AiArchitectResponseParser()

    override suspend fun getChatResponse(prompt: String): ChatMessage = withContext(dispatchers.io) {
        val availability = availabilityProvider.current()
        if (availability != AiAvailabilityState.CONFIGURED) {
            Timber.w("AI architect unavailable: $availability")
            return@withContext availability.toChatMessage()
        }

        try {
            retry(times = 3, initialDelay = 1_000L) {
                withTimeout(30_000L) {
                    val response = generativeModel.generateContent(prompt)
                    val responseText = response.text ?: throw IllegalStateException("Empty AI response")

                    val parsedResponse = try {
                        parser.parse(responseText)
                    } catch (error: AiMalformedResponseException) {
                        Timber.e(error, "Malformed AI architect response")
                        return@withTimeout AiAvailabilityState.MALFORMED.toChatMessage()
                    }

                    ChatMessage(
                        role = ChatRole.AI,
                        text = if (parsedResponse.feedbackText.isBlank()) {
                            MessageText.Resource(MessageTextKey.AI_ANALYSIS_COMPLETE)
                        } else {
                            MessageText.DynamicString(parsedResponse.feedbackText)
                        },
                        recommendations = parsedResponse.recommendations,
                        isActionable = parsedResponse.recommendations.isNotEmpty(),
                        aiFeedback = parsedResponse.aiFeedback
                    )
                }
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error

            val failureType = AiErrorClassifier.classify(error)
            val failureState = failureType.toAvailabilityState()
            Timber.e(error, "AI architect request failed after retries: $failureState")
            failureState.toChatMessage(
                fallbackText = if (failureState == AiAvailabilityState.CONFIGURED) {
                    error.toMessageText()
                } else {
                    failureState.toMessageText()
                }
            )
        }
    }

    private fun Throwable.toMessageText(): MessageText =
        when (AiErrorClassifier.classify(this)) {
            AiFailureType.RateLimit -> MessageText.Resource(MessageTextKey.ERROR_AI_RATE_LIMIT)
            AiFailureType.Overloaded -> MessageText.Resource(MessageTextKey.ERROR_AI_OVERLOADED)
            AiFailureType.Timeout,
            AiFailureType.MalformedResponse,
            AiFailureType.Unknown -> MessageText.Resource(MessageTextKey.ERROR_AI_GENERIC)
        }

    private fun AiAvailabilityState.toChatMessage(
        fallbackText: MessageText = toMessageText()
    ): ChatMessage =
        ChatMessage(
            role = ChatRole.AI,
            text = fallbackText,
            isActionable = false
        )

    private fun AiAvailabilityState.toMessageText(): MessageText =
        when (this) {
            AiAvailabilityState.CONFIGURED -> MessageText.Resource(MessageTextKey.ERROR_AI_GENERIC)
            AiAvailabilityState.UNCONFIGURED -> MessageText.Resource(MessageTextKey.ERROR_AI_UNCONFIGURED)
            AiAvailabilityState.RATE_LIMITED -> MessageText.Resource(MessageTextKey.ERROR_AI_RATE_LIMIT)
            AiAvailabilityState.OVERLOADED -> MessageText.Resource(MessageTextKey.ERROR_AI_OVERLOADED)
            AiAvailabilityState.MALFORMED -> MessageText.Resource(MessageTextKey.ERROR_AI_PARSING)
        }

    private suspend fun <T> retry(
        times: Int,
        initialDelay: Long,
        maxDelay: Long = 2_000L,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        var lastError: Throwable? = null

        repeat(times) { attempt ->
            try {
                return block()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                lastError = error

                val hasAttemptsLeft = attempt < times - 1
                if (!hasAttemptsLeft || !AiErrorClassifier.isRetryable(error)) {
                    throw error
                }

                Timber.w(error, "AI architect retry ${attempt + 1} failed. Retrying in ${currentDelay}ms.")
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }

        throw lastError ?: IllegalStateException("AI architect retry failed without an error.")
    }
}
