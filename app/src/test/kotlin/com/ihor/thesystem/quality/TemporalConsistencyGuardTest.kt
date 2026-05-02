package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TemporalConsistencyGuardTest {

    @Test
    fun `critical domain and data paths use AppClock for current time and timezone`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val relativePaths = listOf(
            "src/main/java/com/ihor/thesystem/data/repository_impl/ProgressionMatrixRepositoryImpl.kt",
            "src/main/java/com/ihor/thesystem/data/repository_impl/QuestRepositoryImpl.kt",
            "src/main/java/com/ihor/thesystem/domain/usecase/GetDailySummaryForDateUseCase.kt",
            "src/main/java/com/ihor/thesystem/domain/usecase/GetPlayerWeightContextUseCase.kt",
            "src/main/java/com/ihor/thesystem/domain/usecase/GetWorkoutAnalysisUseCase.kt",
            "src/main/java/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCase.kt",
            "src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt",
            "src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusViewModel.kt",
            "src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt"
        )
        val forbidden = listOf("System.currentTimeMillis()", "ZoneId.systemDefault()")

        val offenders = relativePaths
            .map { projectRoot.resolve(it) }
            .filter { it.exists() }
            .flatMap { file ->
                val path = file.relativeTo(projectRoot).invariantSeparatorsPath
                file.readLines().mapIndexedNotNull { index, line ->
                    forbidden.firstOrNull { it in line }?.let { usage ->
                        "$path:${index + 1} uses $usage"
                    }
                }
            }

        assertTrue(
            "Use injected AppClock for critical date grouping, logs, and ViewModel timestamps: $offenders",
            offenders.isEmpty()
        )
    }
}
