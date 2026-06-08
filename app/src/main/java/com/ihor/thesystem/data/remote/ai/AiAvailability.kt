package com.ihor.thesystem.data.remote.ai

import javax.inject.Inject
import javax.inject.Named

enum class AiAvailabilityState {
    CONFIGURED,
    UNCONFIGURED,
    RATE_LIMITED,
    OVERLOADED,
    MALFORMED
}

class AiAvailabilityProvider @Inject constructor(
    @param:Named("GeminiApiKey") private val apiKey: String,
    @param:Named("GeminiClientAiEnabled") private val clientAiEnabled: Boolean
) {
    fun current(): AiAvailabilityState =
        if (clientAiEnabled && apiKey.isConfiguredGeminiApiKey()) {
            AiAvailabilityState.CONFIGURED
        } else {
            AiAvailabilityState.UNCONFIGURED
        }
}

internal fun String.isConfiguredGeminiApiKey(): Boolean =
    isNotBlank() && this != "null"

internal fun AiFailureType.toAvailabilityState(): AiAvailabilityState =
    when (this) {
        AiFailureType.RateLimit -> AiAvailabilityState.RATE_LIMITED
        AiFailureType.Overloaded,
        AiFailureType.Timeout -> AiAvailabilityState.OVERLOADED
        AiFailureType.MalformedResponse -> AiAvailabilityState.MALFORMED
        AiFailureType.Unknown -> AiAvailabilityState.CONFIGURED
    }
