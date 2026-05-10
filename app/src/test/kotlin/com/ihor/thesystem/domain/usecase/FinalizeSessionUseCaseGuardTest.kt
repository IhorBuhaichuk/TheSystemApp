package com.ihor.thesystem.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FinalizeSessionUseCaseGuardTest {

    @Test
    fun `quest completion is part of local session transaction`() {
        val source = sourceFile().readText()

        val transactionIndex = source.indexOf("transactionProvider.runInTransaction")
        val questCompletionIndex = source.indexOf("completeWorkoutQuestIfPossible(session.questId, sets)")
        val localDataIndex = source.indexOf("LocalSessionData(")

        assertTrue("FinalizeSessionUseCase must have a local transaction block.", transactionIndex >= 0)
        assertTrue(
            "Workout quest completion must happen inside the local session transaction.",
            questCompletionIndex > transactionIndex && questCompletionIndex < localDataIndex
        )
        assertEquals(
            "Workout quest completion must not be repeated outside the local transaction.",
            questCompletionIndex,
            source.lastIndexOf("completeWorkoutQuestIfPossible(session.questId, sets)")
        )
    }

    @Test
    fun `finalize session does not swallow coroutine cancellation`() {
        val source = sourceFile().readText()

        assertTrue(
            "FinalizeSessionUseCase must rethrow CancellationException from catch-all blocks.",
            "if (e is CancellationException) throw e" in source
        )
        assertFalse(
            "FinalizeSessionUseCase must not collect a Room Flow snapshot while completing a transaction.",
            "playerRepository.getPlayer().firstOrNull()" in source
        )
        assertTrue(
            "FinalizeSessionUseCase must use a synchronous player snapshot for transactional quest reward updates.",
            "playerRepository.getPlayerSnapshot()" in source
        )
    }

    private fun sourceFile(): File =
        requireNotNull(File(requireNotNull(System.getProperty("user.dir"))).absoluteFile.parentFile)
            .resolve("domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt")
}
