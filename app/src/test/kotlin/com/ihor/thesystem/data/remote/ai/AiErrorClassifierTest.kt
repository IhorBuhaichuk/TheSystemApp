package com.ihor.thesystem.data.remote.ai

import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiErrorClassifierTest {

    @Test
    fun `classifies quota errors as rate limit`() {
        val result = AiErrorClassifier.classify(IllegalStateException("RESOURCE_EXHAUSTED: 429 quota exceeded"))

        assertEquals(AiFailureType.RateLimit, result)
    }

    @Test
    fun `classifies unavailable errors as overloaded`() {
        val result = AiErrorClassifier.classify(IllegalStateException("GrpcError UNAVAILABLE 503"))

        assertEquals(AiFailureType.Overloaded, result)
    }

    @Test
    fun `classifies serialization errors as malformed response`() {
        val result = AiErrorClassifier.classify(SerializationException("bad json"))

        assertEquals(AiFailureType.MalformedResponse, result)
    }

    @Test
    fun `retries transient failures only`() {
        assertTrue(AiErrorClassifier.isRetryable(IllegalStateException("429")))
        assertTrue(AiErrorClassifier.isRetryable(IllegalStateException("503")))
        assertFalse(AiErrorClassifier.isRetryable(SerializationException("bad json")))
        assertFalse(AiErrorClassifier.isRetryable(IllegalStateException("bad request")))
    }
}
