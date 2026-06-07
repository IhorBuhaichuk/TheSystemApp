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
            "FinalizeSessionUseCase must delegate quest reward updates to CompleteQuestUseCase.",
            "completeQuest(activeMainQuest.id" in source
        )
        assertFalse(
            "FinalizeSessionUseCase must not grant quest XP directly.",
            "rewardWorkoutCompletion()" in source
        )
    }

    @Test
    fun `ai session directives are validated before matrix persistence`() {
        val source = sourceFile().readText()

        val validationIndex = source.indexOf("validateDirectives(report.nextWorkoutDirectives")
        val updateTargetIndex = source.indexOf("progressionMatrixRepository.updateTarget")

        assertTrue(
            "FinalizeSessionUseCase must validate AI directives before updating progression targets.",
            validationIndex >= 0 && updateTargetIndex > validationIndex
        )
        assertTrue(
            "Validated AI directive persistence must be transactional.",
            source.indexOf("transactionProvider.runInTransaction", validationIndex) in validationIndex until updateTargetIndex
        )
    }

    @Test
    fun `ai session directives only target externally loaded exercises`() {
        val source = sourceFile().readText()

        assertTrue(
            "FinalizeSessionUseCase must identify weighted exercises before accepting AI directives.",
            "val weightedExerciseIds = sets" in source &&
                ".filter { it.isCompleted && it.hasRealExternalLoad() }" in source
        )
        assertTrue(
            "AI recommendations must be filtered to weighted exercises before becoming WorkoutDirective.",
            ".filter { it.exerciseId in weightedExerciseIds }" in source
        )
        assertTrue(
            "Fallback directives must not create fake kg targets for bodyweight/time exercises.",
            "val fallbackDirectives = sets" in source &&
                ".filter { it.isCompleted && it.hasRealExternalLoad() }" in source
        )
        assertTrue(
            "The AI prompt must explicitly forbid kg targets for exercises without external load.",
            "НЕ додавай цю вправу до next_workout_targets і НЕ пропонуй кг" in source
        )
    }

    @Test
    fun `system protocol sessions return before ai directives and target updates`() {
        val source = sourceFile().readText()

        val systemProtocolIndex = source.indexOf("if (localData.systemTemplateType != null)")
        val aiRequestIndex = source.indexOf("sendArchitectAnalysis(exerciseContexts)")
        val updateTargetIndex = source.indexOf("progressionMatrixRepository.updateTarget")

        assertTrue(
            "System protocol sessions must short-circuit before AI analysis.",
            systemProtocolIndex >= 0 && aiRequestIndex > systemProtocolIndex
        )
        assertTrue(
            "System protocol sessions must not emit next workout directives.",
            "nextWorkoutDirectives = emptyList()" in source
        )
        assertTrue(
            "Target persistence must stay after the system protocol short-circuit.",
            updateTargetIndex > systemProtocolIndex
        )
    }

    private fun sourceFile(): File =
        requireNotNull(File(requireNotNull(System.getProperty("user.dir"))).absoluteFile.parentFile)
            .resolve("domain/src/main/java/com/ihor/thesystem/domain/usecase/FinalizeSessionUseCase.kt")
}
