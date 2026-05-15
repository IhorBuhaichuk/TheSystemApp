package com.ihor.thesystem.feature.statistics.ui.dialogs

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MatrixDialogsGuardTest {

    private val repoRoot: File =
        requireNotNull(File(requireNotNull(System.getProperty("user.dir"))).absoluteFile.parentFile)

    @Test
    fun `setup matrix dialog does not confirm kilogram goals for non weighted entries`() {
        val dialogSource = read("app/src/main/java/com/ihor/thesystem/feature/statistics/ui/dialogs/MatrixDialogs.kt")
        val hostSource = read("app/src/main/java/com/ihor/thesystem/feature/status/ui/WorkoutDialogHost.kt")

        assertTrue("Setup dialog must receive the matrix tracking capability", "usesExternalLoad: Boolean = true" in dialogSource)
        assertTrue("Setup dialog must hide kg inputs behind the weighted gate", "if (usesExternalLoad)" in dialogSource)
        assertTrue("Non weighted setup dialog should dismiss instead of updating matrix goals", "onDismiss()" in dialogSource)
        assertTrue(
            "Workout host must pass the entry tracking capability into setup dialog",
            "usesExternalLoad = dialogState.entry.usesExternalLoad" in hostSource
        )
    }

    @Test
    fun `edit weight dialog is gated by external load capability`() {
        val editSource = read("app/src/main/java/com/ihor/thesystem/feature/statistics/ui/dialogs/EditWeightDialog.kt")

        assertTrue("Edit dialog must derive weight behavior from the UI model", "val usesExternalLoad = entry.usesExternalLoad" in editSource)
        assertTrue("Weight-only controls must be conditional", "if (usesExternalLoad)" in editSource)
        assertTrue(
            "Non weighted edit dialog should close instead of requiring a kg value",
            "enabled = !usesExternalLoad || (!isError && input.isNotBlank())" in editSource
        )
    }

    @Test
    fun `view models ignore setup confirmation for non weighted matrix entries`() {
        val statisticsViewModel = read("app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt")
        val workoutViewModel = read("app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt")

        assertTrue("Statistics setup confirmation must guard non weighted entries", "setupState?.entry?.usesExternalLoad == false" in statisticsViewModel)
        assertTrue("Workout setup confirmation must guard non weighted entries", "setupState?.entry?.usesExternalLoad == false" in workoutViewModel)
    }

    private fun read(relativePath: String): String =
        repoRoot.resolve(relativePath).readText()
}
