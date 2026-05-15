package com.ihor.thesystem.feature.statistics.viewmodel

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AnnualProgressionDetailsViewModelGuardTest {

    @Test
    fun `manual annual editor only includes externally loaded exercises`() {
        val source = sourceFile().readText()

        assertTrue(
            "Manual annual editor must skip non-weight exercises before creating editable rows.",
            "if (!trackingMode.usesWeightInput) return@mapNotNull null" in source
        )
        assertTrue(
            "Manual annual editor empty state must explain that weighted exercises are required.",
            "У розкладі поки немає вагових вправ для річної прогресії." in source
        )
    }

    private fun sourceFile(): File =
        requireNotNull(File(requireNotNull(System.getProperty("user.dir"))).absoluteFile.parentFile)
            .resolve("app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/AnnualProgressionDetailsViewModel.kt")
}
