package com.ihor.thesystem.domain.usecase

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlayerUseCaseCancellationGuardTest {

    @Test
    fun `player update use cases rethrow coroutine cancellation`() {
        val appRoot = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        val repoRoot = requireNotNull(appRoot.parentFile)
        val files = listOf(
            "domain/src/main/java/com/ihor/thesystem/domain/usecase/PlayerUseCases.kt",
            "domain/src/main/java/com/ihor/thesystem/domain/usecase/UpdatePlayerHeightUseCase.kt"
        ).map(repoRoot::resolve)

        files.forEach { file ->
            val source = file.readText()
            assertTrue(
                "${file.name} must catch CancellationException before generic Exception.",
                "catch (e: CancellationException)" in source && "throw e" in source
            )
        }
    }
}
