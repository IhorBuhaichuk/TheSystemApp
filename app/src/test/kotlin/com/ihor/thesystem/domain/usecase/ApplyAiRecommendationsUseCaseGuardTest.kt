package com.ihor.thesystem.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ApplyAiRecommendationsUseCaseGuardTest {

    @Test
    fun `recommendation target updates are transactional and cancellation safe`() {
        val source = sourceFile().readText()

        assertTrue(
            "AI recommendation target updates must be applied through TransactionProvider.",
            "transactionProvider.runInTransaction" in source
        )
        assertFalse(
            "AI recommendation updates must not swallow individual item failures and continue partial writes.",
            "Failed to update target for exercise" in source
        )
        assertTrue(
            "ApplyAiRecommendationsUseCase must rethrow CancellationException from catch-all blocks.",
            "if (e is CancellationException) throw e" in source
        )
        assertTrue(
            "AI recommendations must pass ValidateDirectivesUseCase before matrix persistence.",
            "validateDirectives(rawDirectives, matrix)" in source
        )
        assertTrue(
            "AI validation clamps or rejections must be logged.",
            "logDirectiveAdjustments(rawDirectives" in source
        )
    }

    private fun sourceFile(): File =
        requireNotNull(File(requireNotNull(System.getProperty("user.dir"))).absoluteFile.parentFile)
            .resolve("domain/src/main/java/com/ihor/thesystem/domain/usecase/ApplyAiRecommendationsUseCase.kt")
}
