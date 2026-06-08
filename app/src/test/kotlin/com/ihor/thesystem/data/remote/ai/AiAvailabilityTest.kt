package com.ihor.thesystem.data.remote.ai

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Test

class AiAvailabilityTest {

    @Test
    fun `empty or disabled key is unconfigured`() {
        assertEquals(
            AiAvailabilityState.UNCONFIGURED,
            AiAvailabilityProvider(apiKey = "", clientAiEnabled = true).current()
        )
        assertEquals(
            AiAvailabilityState.UNCONFIGURED,
            AiAvailabilityProvider(apiKey = "dev-key", clientAiEnabled = false).current()
        )
    }

    @Test
    fun `configured key and enabled client produce configured state`() {
        assertEquals(
            AiAvailabilityState.CONFIGURED,
            AiAvailabilityProvider(apiKey = "dev-key", clientAiEnabled = true).current()
        )
    }

    @Test
    fun `classifier maps runtime failures to availability states`() = runTest {
        val timeout = try {
            withTimeout(1) { delay(1_000) }
            error("Timeout was expected.")
        } catch (error: TimeoutCancellationException) {
            error
        }

        assertEquals(
            AiAvailabilityState.RATE_LIMITED,
            AiErrorClassifier.classify(IllegalStateException("429 RESOURCE_EXHAUSTED quota")).toAvailabilityState()
        )
        assertEquals(
            AiAvailabilityState.OVERLOADED,
            AiErrorClassifier.classify(IllegalStateException("503 UNAVAILABLE overloaded")).toAvailabilityState()
        )
        assertEquals(
            AiAvailabilityState.OVERLOADED,
            AiErrorClassifier.classify(timeout).toAvailabilityState()
        )
        assertEquals(
            AiAvailabilityState.MALFORMED,
            AiErrorClassifier.classify(SerializationException("bad json")).toAvailabilityState()
        )
    }
}
