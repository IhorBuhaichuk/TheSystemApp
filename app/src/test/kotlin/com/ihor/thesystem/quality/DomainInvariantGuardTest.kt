package com.ihor.thesystem.quality

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DomainInvariantGuardTest {

    @Test
    fun `critical RPG rules are delegated to domain policies`() {
        val appRoot = File(requireNotNull(System.getProperty("user.dir")))
        val repoRoot = requireNotNull(appRoot.parentFile)

        assertContains(
            file = repoRoot.resolve("domain/src/main/java/com/ihor/thesystem/domain/usecase/CycleUseCases.kt"),
            text = "QuestCompletionPolicy.resolveForDayFinalization"
        )
        assertContains(
            file = repoRoot.resolve("domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeDayUseCase.kt"),
            text = "QuestCompletionPolicy.resolveForDayFinalization"
        )
        assertContains(
            file = repoRoot.resolve("domain/src/main/java/com/ihor/thesystem/domain/usecase/RecalculateGlobalRankUseCase.kt"),
            text = "RankProgressionPolicy.resolveGlobalRank"
        )
        assertContains(
            file = appRoot.resolve("src/main/java/com/ihor/thesystem/data/repository_impl/ProgressionMatrixRepositoryImpl.kt"),
            text = "RankProgressionPolicy.nextRank"
        )

        val playerSource = repoRoot
            .resolve("domain/src/main/java/com/ihor/thesystem/domain/model/Player.kt")
            .readText()
        assertFalse(
            "Player progression must use PlayerProgressionConfig instead of embedded XP bonuses.",
            "xpTotal + 200" in playerSource || "xpThisWeek + 200" in playerSource
        )
    }

    private fun assertContains(file: File, text: String) {
        assertTrue(
            "${file.invariantSeparatorsPath} must contain $text",
            text in file.readText()
        )
    }
}
