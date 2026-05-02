package com.ihor.thesystem.data.remote.ai

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException

internal enum class AiFailureType {
    Timeout,
    RateLimit,
    Overloaded,
    MalformedResponse,
    Unknown
}

internal object AiErrorClassifier {
    fun classify(error: Throwable): AiFailureType {
        val message = error.message.orEmpty()

        return when {
            error is TimeoutCancellationException -> AiFailureType.Timeout
            error is AiMalformedResponseException || error is SerializationException ->
                AiFailureType.MalformedResponse

            message.contains("429", ignoreCase = true) ||
                message.contains("quota", ignoreCase = true) ||
                message.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ->
                AiFailureType.RateLimit

            message.contains("503", ignoreCase = true) ||
                message.contains("UNAVAILABLE", ignoreCase = true) ||
                message.contains("overloaded", ignoreCase = true) ||
                message.contains("GrpcError", ignoreCase = true) ->
                AiFailureType.Overloaded

            else -> AiFailureType.Unknown
        }
    }

    fun isRetryable(error: Throwable): Boolean =
        when (classify(error)) {
            AiFailureType.Timeout,
            AiFailureType.RateLimit,
            AiFailureType.Overloaded -> true

            AiFailureType.MalformedResponse,
            AiFailureType.Unknown -> false
        }
}

internal class AiMalformedResponseException(
    message: String,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)
