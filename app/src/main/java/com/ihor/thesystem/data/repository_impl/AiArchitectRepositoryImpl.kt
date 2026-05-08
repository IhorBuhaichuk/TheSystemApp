package com.ihor.thesystem.data.repository_impl

import com.google.ai.client.generativeai.GenerativeModel
import com.ihor.thesystem.BuildConfig
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.data.remote.ai.AiArchitectResponseParser
import com.ihor.thesystem.data.remote.ai.AiErrorClassifier
import com.ihor.thesystem.data.remote.ai.AiFailureType
import com.ihor.thesystem.data.remote.ai.AiMalformedResponseException
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
    private val dispatchers: DispatcherProvider
) : AiArchitectRepository {

    private val parser = AiArchitectResponseParser()

    override suspend fun getChatResponse(prompt: String): ChatMessage = withContext(dispatchers.io) {
        if (!isApiKeyConfigured()) {
            Timber.e("Gemini API key is not configured.")
            return@withContext ChatMessage(
                role = ChatRole.AI,
                text = MessageText.Resource(MessageTextKey.ERROR_AI_GENERIC),
                isActionable = false
            )
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
                        return@withTimeout ChatMessage(
                            role = ChatRole.AI,
                            text = MessageText.Resource(MessageTextKey.ERROR_AI_PARSING),
                            isActionable = false
                        )
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

            Timber.e(error, "AI architect request failed after retries")
            ChatMessage(
                role = ChatRole.AI,
                text = error.toMessageText(),
                isActionable = false
            )
        }
    }

    private fun isApiKeyConfigured(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return apiKey.isNotBlank() && apiKey != "null"
    }

    private fun Throwable.toMessageText(): MessageText =
        when (AiErrorClassifier.classify(this)) {
            AiFailureType.RateLimit -> MessageText.Resource(MessageTextKey.ERROR_AI_RATE_LIMIT)
            AiFailureType.Overloaded -> MessageText.Resource(MessageTextKey.ERROR_AI_OVERLOADED)
            AiFailureType.Timeout,
            AiFailureType.MalformedResponse,
            AiFailureType.Unknown -> MessageText.Resource(MessageTextKey.ERROR_AI_GENERIC)
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
